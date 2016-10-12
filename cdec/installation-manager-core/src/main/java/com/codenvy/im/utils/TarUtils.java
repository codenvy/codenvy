/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.im.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.nio.file.Files.setLastModifiedTime;
import static org.apache.commons.io.IOUtils.copy;

/** @author Dmytro Nochevnov */
public class TarUtils {

    public static void uncompress(Path pack, Path dirToUnpack) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(newInputStream(pack))))) {

            if (!Files.exists(dirToUnpack)) {
                createDirectories(dirToUnpack);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = in.getNextTarEntry()) != null) {
                Path destPath = dirToUnpack.resolve(tarEntry.getName());

                if (tarEntry.isDirectory()) {
                    if (!Files.exists(destPath)) {
                        createDirectories(destPath);
                    }
                } else {
                    try (BufferedOutputStream out = new BufferedOutputStream(newOutputStream(destPath))) {
                        copy(in, out);
                        setLastModifiedTime(destPath, FileTime.fromMillis(tarEntry.getModTime().getTime()));
                    }
                }
            }
        }
    }

    public static void unpackFile(Path pack, Path dirToUnpack, Path fileToUnpack) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(new BufferedInputStream(newInputStream(pack)))) {

            if (!Files.exists(dirToUnpack)) {
                createDirectories(dirToUnpack);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = in.getNextTarEntry()) != null) {
                Path destPath = dirToUnpack.resolve(tarEntry.getName());

                if (tarEntry.isFile() && tarEntry.getName().equals(fileToUnpack.toFile().getName())) {
                    try (BufferedOutputStream out = new BufferedOutputStream(newOutputStream(destPath))) {
                        copy(in, out);
                        setLastModifiedTime(destPath, FileTime.fromMillis(tarEntry.getModTime().getTime()));
                    }

                    return;
                }
            }
        }
    }

    public static void unpackAllFiles(Path pack, Path dirToUnpack) throws IOException {
        try (TarArchiveInputStream in = new TarArchiveInputStream(new BufferedInputStream(newInputStream(pack)))) {

            if (!Files.exists(dirToUnpack)) {
                createDirectories(dirToUnpack);
            }

            TarArchiveEntry tarEntry;
            while ((tarEntry = in.getNextTarEntry()) != null) {
                Path destPath = dirToUnpack.resolve(tarEntry.getName());

                if (tarEntry.isDirectory()) {
                    if (!Files.exists(destPath)) {
                        createDirectories(destPath);
                    }
                } else {
                    try (BufferedOutputStream out = new BufferedOutputStream(newOutputStream(destPath))) {
                        copy(in, out);
                        setLastModifiedTime(destPath, FileTime.fromMillis(tarEntry.getModTime().getTime()));
                    }
                }
            }
        }
    }

    /** Pack file without compression */
    public static void packFile(Path fileToPack, Path packageFile) throws IOException {
        pack(fileToPack, packageFile, false);
    }

    /** Pack file with compression */
    public static void compressFile(Path fileToPack, Path packageFile) throws IOException {
        pack(fileToPack, packageFile, true);
    }

    private static void pack(Path fileToPack, Path packageFile, boolean needCompress) throws IOException {
        if (!Files.exists(fileToPack)) {
            throw new IllegalArgumentException("Packing file doesn't exist");
        }

        if (Files.isDirectory(fileToPack)) {
            throw new IllegalArgumentException("Packing item is a directory");
        }

        OutputStream os = new BufferedOutputStream(newOutputStream(packageFile));
        if (needCompress) {
            os = new GzipCompressorOutputStream(os);
        }

        try (TarArchiveOutputStream out = new TarArchiveOutputStream(os)) {
            out.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
            TarArchiveEntry tarEntry = new TarArchiveEntry(fileToPack.toFile(),
                                                           fileToPack.getFileName().toString());
            out.putArchiveEntry(tarEntry);
            IOUtils.copy(new FileInputStream(fileToPack.toFile()), out);
            out.closeArchiveEntry();
        }
    }

}
