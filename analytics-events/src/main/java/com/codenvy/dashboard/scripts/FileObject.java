/*
 *    Copyright (C) 2013 eXo Platform SAS.
 *
 *    This is free software; you can redistribute it and/or modify it
 *    under the terms of the GNU Lesser General Public License as
 *    published by the Free Software Foundation; either version 2.1 of
 *    the License, or (at your option) any later version.
 *
 *    This software is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this software; if not, write to the Free
 *    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *    02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.dashboard.scripts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class FileObject {

    /** The file name where {@link #value} is stored. */
    public static final String FILE_NAME = "value";

    /** {@link ScriptType}. It is used also as a part of path to destination {@link #FILE_NAME}. */
    private final ScriptType scriptType;

    /**
     * The sequence of values are used as unique identifier of result relatively to {@link #type}. They are used also as a part of path to
     * destination {@link #FILE_NAME}.
     */
    private final LinkedHashMap<String, String> keys;

    /** The parent directory of the storage. */
    private final String baseDir;

    /** Contains the actual value. */
    private final Object value;

    /** {@link ValueTranslator} instance. */
    private final ValueTranslator translator;

    /** {@link FileObject} constructor. */
    FileObject(String baseDir, ScriptType scriptType, Map<String, String> executionParams, Object value)
            throws IOException {
        this.scriptType = scriptType;
        this.baseDir = baseDir;
        this.keys = makeKeys(executionParams);
        this.translator = scriptType.getResultType().getValueTranslator();
        this.value = translator.translate(value);
    }

    /** {@link FileObject} constructor. Loads value from the file. */
    FileObject(String baseDir, ScriptType scriptType, Map<String, String> executionParams) throws IOException {
        this.scriptType = scriptType;
        this.baseDir = baseDir;
        this.keys = makeKeys(executionParams);
        this.translator = scriptType.getResultType().getValueTranslator();
        this.value = load();
    }

    /** @return {@link #keys}. */
    public final Map<String, String> getKeys() {
        return keys;
    }

    /** @return {@link #value}. */
    public final Object getValue() throws IOException {
        return value;
    }

    /** Stores value into the file. */
    public final synchronized void store() throws IOException {
        if (scriptType.isStoreSupport()) {
            File file = getFile();

            validateDestination(file);
            doStore(file);
        }
    }

    /** Checks if it is possible to write to destination file. The directory should exist and the file does not. */
    private void validateDestination(File file) throws IOException {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can not create directory tree " + dir.getAbsolutePath());
            }
        }

        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File " + file.getAbsolutePath() + " already exists and can not be removed");
            }
        }
    }

    /** Returns the file to store in or load value from. */
    private File getFile() throws IOException {
        File dir;

        if (baseDir.startsWith("file://")) {
            URI uri;
            try {
                uri = new URI(baseDir);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }

            dir = new File(uri);
        } else {
            dir = new File(baseDir);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(scriptType.toString().toLowerCase());
        builder.append(File.separatorChar);

        for (Entry<String, String> entry : keys.entrySet()) {
            builder.append(toRelativePath(entry));
            builder.append(File.separatorChar);
        }
        builder.append(FILE_NAME);

        return new File(dir, builder.toString());
    }

    /** Translates key into relative path of the destination file. */
    private String toRelativePath(Entry<String, String> entry) {
        if (entry.getKey().equals(ScriptParameters.FROM_DATE.getName())) {
            return translateDateToRelativePath(entry.getValue());
        }

        return entry.getValue().toLowerCase().replace('-', File.separatorChar);
    }

    /** Translate date from format YYYYMMDD into format like YYYY/MM/DD and {@link File#separatorChar} is used as delimiter. */
    private String translateDateToRelativePath(String date) {
        StringBuilder builder = new StringBuilder();

        builder.append(date.substring(0, 4));
        builder.append(File.separatorChar);

        builder.append(date.substring(4, 6));
        builder.append(File.separatorChar);

        builder.append(date.substring(6, 8));

        return builder.toString();
    }

    private void doStore(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        try {
            translator.doWrite(writer, value);
        } finally {
            writer.close();
        }
    }

    /** Loads value from the file. */
    private Object load() throws IOException {
        File file = getFile();
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist " + file.getAbsolutePath());
        }

        return doLoad(file);
    }

    private Object doLoad(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        try {
            return translator.doRead(reader);
        } finally {
            reader.close();
        }
    }

    /** Prepare keys for {@link FileObject#keys}. */
    private LinkedHashMap<String, String> makeKeys(Map<String, String> executionParams) throws IOException {
        LinkedHashMap<String, String> keys = new LinkedHashMap<String, String>();

        List<ScriptParameters> manParams = scriptType.getMandatoryParams();
        List<ScriptParameters> addParams = scriptType.getAdditionalParams();

        for (int i = 0; i < manParams.size(); i++) {
            String paramKey = manParams.get(i).getName();
            String paramValue = executionParams.get(paramKey);

            if (paramValue == null) {
                throw new IOException("There is no mandatory parameter " + paramKey + " in execution context");
            }

            keys.put(paramKey, paramValue);
        }

        for (int i = 0; i < addParams.size(); i++) {
            String paramKey = addParams.get(i).getName();
            String paramValue = executionParams.get(paramKey);

            if (paramValue == null) {
                paramValue = addParams.get(i).getDefaultValue();
            }

            keys.put(paramKey, paramValue);
        }

        return keys;
    }
}
