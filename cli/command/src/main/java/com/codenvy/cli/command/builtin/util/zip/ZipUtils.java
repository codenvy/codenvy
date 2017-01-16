/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.cli.command.builtin.util.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Florent Benoit
 */
public class ZipUtils {

    public static InputStream getZipProjectStream(final File file) {


        final AtomicBoolean writeDoneSignal = new AtomicBoolean(false);
        final CountDownLatch writeStartLock = new CountDownLatch(1);

        final PipedInputStream pipedInputStream = new PipedInputStream() {
            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                try {
                    // Wait until writer get created and connected
                    writeStartLock.await();

                    // Hack to avoid premature closing of this PipedInputStream by a reader whose basing closing action on assumption that
                    // if read() send -1 the stream is to be closed (for instance InputStreamProvider of Jersey). The stream can be only
                    // closed when the thread where PipedOutputStream write is ended. Otherwise, if PipedOutputStream write ’slower’ than
                    // PipedInputStream is read, there could be underflow and read could send -1 whereas there's still some data to be
                    // written.
                    int result = super.read(b, off, len);
                    if (result == -1 && !writeDoneSignal.get()) {
                        return 0;
                    }
                    return result;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        if (file.isDirectory()) {
            executor.execute(new ZipRunnable(pipedInputStream, writeStartLock, file, writeDoneSignal));
        } else {
            executor.execute(new ZipFileRunnable(pipedInputStream, writeStartLock, file, writeDoneSignal));
        }
        executor.shutdown();
        return pipedInputStream;
    }

    private static class ZipRunnable implements Runnable {
        private final PipedInputStream pipedInputStream;
        private final CountDownLatch writeStartLock;
        private final File           directoryToSend;
        private final AtomicBoolean  writeDoneSignal;

        public ZipRunnable(PipedInputStream pipedInputStream, CountDownLatch writeStartLock, File directoryToSend,
                           AtomicBoolean writeDoneSignal) {
            this.pipedInputStream = pipedInputStream;
            this.writeStartLock = writeStartLock;
            this.directoryToSend = directoryToSend;
            this.writeDoneSignal = writeDoneSignal;
        }

        @Override
        public void run() {
            try {
                final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
                // Writer is on, unlock the reader
                writeStartLock.countDown();
                final ZipOutputStream outputStream = new ZipOutputStream(pipedOutputStream);


                List<File> listFiles = new ArrayList<>();
                getAllFiles(directoryToSend, listFiles);
                for (File file : listFiles) {
                    if (!file.isDirectory()) { // we only zip files, not directories

                        String entryPath = file.getPath().substring(directoryToSend.getPath().length() + 1);
                        entryPath = entryPath.replace(File.separatorChar, '/');
                        ZipEntry zipEntry = new ZipEntry(entryPath);
                        outputStream.putNextEntry(zipEntry);

                        try (FileInputStream fis = new FileInputStream(file)) {
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = fis.read(bytes)) >= 0) {
                                outputStream.write(bytes, 0, length);
                            }

                            outputStream.closeEntry();
                        }
                    }
                }

                // Flag for writing end, see hack above for PipedInputStream#read(…).
                writeDoneSignal.set(true);
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    private static class ZipFileRunnable implements Runnable {
        private final PipedInputStream pipedInputStream;
        private final CountDownLatch writeStartLock;
        private final File           file;
        private final AtomicBoolean  writeDoneSignal;

        public ZipFileRunnable(PipedInputStream pipedInputStream, CountDownLatch writeStartLock, File file,
                           AtomicBoolean writeDoneSignal) {
            this.pipedInputStream = pipedInputStream;
            this.writeStartLock = writeStartLock;
            this.file = file;
            this.writeDoneSignal = writeDoneSignal;
        }

        @Override
        public void run() {
            try {
                final PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream);
                // Writer is on, unlock the reader
                writeStartLock.countDown();
                final ZipOutputStream outputStream = new ZipOutputStream(pipedOutputStream);

                try(ZipFile zipFile = new ZipFile(file)) {
                    Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
                    while (zipEntries.hasMoreElements()) {
                        ZipEntry zipEntry = zipEntries.nextElement();
                        outputStream.putNextEntry(zipEntry);

                        try (InputStream is = zipFile.getInputStream(zipEntry)) {
                            byte[] bytes = new byte[1024];
                            int length;
                            while ((length = is.read(bytes)) >= 0) {
                                outputStream.write(bytes, 0, length);
                            }

                            outputStream.closeEntry();
                        }
                    }
                }


                // Flag for writing end, see hack above for PipedInputStream#read(…).
                writeDoneSignal.set(true);
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }



    public static void getAllFiles(File dir, List<File> fileList) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            }
        }
    }
}
