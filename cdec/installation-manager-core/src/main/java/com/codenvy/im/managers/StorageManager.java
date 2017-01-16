/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class StorageManager {

    public static final String STORAGE_FILE_NAME = "config.properties";
    private final Path storageFile;

    @Inject
    public StorageManager(@Named("installation-manager.storage_dir") String storageDir) throws IOException {
        this.storageFile = Paths.get(storageDir).resolve(STORAGE_FILE_NAME);
    }

    /**
     * Stores installation related properties.
     *
     * @param newProperties
     *         properties to store
     * @throws java.io.IOException
     *         if any I/O error occurred
     */
    public void storeProperties(@Nullable Map<String, String> newProperties) throws IOException {
        if (newProperties != null && !newProperties.isEmpty()) {
            Path storageFile = getStorageFile().orElse(createStorage());
            Properties properties = loadProperties(storageFile);

            for (Map.Entry<String, String> entry : newProperties.entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }

            try (OutputStream out = new BufferedOutputStream(newOutputStream(storageFile))) {
                properties.store(out, null);
            }
        }
    }

    /**
     * Loads installation related properties.
     *
     * @throws IOException
     *         if any I/O error occurred
     */
    public Map<String, String> loadProperties() throws IOException {
        Path storageFile = getStorageFile().orElseThrow(() -> new StorageNotFoundException());
        Properties properties = loadProperties(storageFile);

        return new HashMap<>((Map)properties);
    }

    /** Loads property with certain key. */
    @Nullable
    public String loadProperty(@NotNull String key) throws IOException {
        Path storageFile = getStorageFile().orElseThrow(() -> new StorageNotFoundException());
        Properties properties = loadProperties(storageFile);

        if (!properties.containsKey(key)) {
            throw PropertyNotFoundException.from(key);
        }

        return properties.getProperty(key);
    }

    /** Stores property with certain key. */
    public void storeProperty(@NotNull String key, @NotNull String value) throws IOException {
        Path storageFile = getStorageFile().orElse(createStorage());
        Properties properties = loadProperties(storageFile);

        if (!properties.containsKey(key)) {
            throw PropertyNotFoundException.from(key);
        }

        properties.setProperty(key, value);

        try (OutputStream out = new BufferedOutputStream(newOutputStream(storageFile))) {
            properties.store(out, null);
        }
    }

    /** Deletes property with certain key. */
    public void deleteProperty(@NotNull String key) throws IOException {
        Path storageFile = getStorageFile().orElseThrow(FileNotFoundException::new);
        Properties properties = loadProperties(storageFile);

        if (!properties.containsKey(key)) {
            throw PropertyNotFoundException.from(key);
        }

        properties.remove(key);

        try (OutputStream out = new BufferedOutputStream(newOutputStream(storageFile))) {
            properties.store(out, null);
        }
    }

    private Properties loadProperties(Path propertiesFile) throws IOException {
        Properties properties = new Properties();

        if (exists(propertiesFile)) {
            try (InputStream in = new BufferedInputStream(newInputStream(propertiesFile))) {
                properties.load(in);
            }
        }
        return properties;
    }

    private Optional<Path> getStorageFile() throws IOException {
        if (!exists(storageFile.getParent())) {
            return Optional.empty();
        }

        return Optional.of(storageFile);
    }

    private Path createStorage() throws IOException {
        createDirectories(storageFile.getParent());
        return storageFile;
    }
}
