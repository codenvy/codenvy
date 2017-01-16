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

import com.google.common.collect.ImmutableMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class StorageManagerTest {

    private StorageManager storageManager;
    private Path           storageDir;
    private Path           configProperties;

    @BeforeMethod
    public void setUp() throws Exception {
        storageDir = Paths.get("target").resolve("storage");
        deleteDirectory(storageDir.toFile());

        storageManager = new StorageManager(storageDir.toString());
        configProperties = storageDir.resolve(StorageManager.STORAGE_FILE_NAME);
    }

    @Test
    public void testStoreLoadProperties() throws Exception {
        storageManager.storeProperties(ImmutableMap.of("x1", "y1", "x2", "y2"));

        Map<String, String> m = storageManager.loadProperties();
        assertEquals(m.size(), 2);
        assertEquals(m.get("x1"), "y1");
        assertEquals(m.get("x2"), "y2");

        storageManager.storeProperties(ImmutableMap.of("x1", "y2", "x3", "y3"));

        m = storageManager.loadProperties();
        assertEquals(m.size(), 3);
        assertEquals(m.get("x1"), "y2");
        assertEquals(m.get("x2"), "y2");
        assertEquals(m.get("x3"), "y3");
    }

    @Test
    public void testStoreEmptyMap() throws Exception {
        storageManager.storeProperties(Collections.<String, String>emptyMap());
    }

    @Test(expectedExceptions = StorageNotFoundException.class)
    public void testLoadEmptyMap() throws Exception {
        Map<String, String> m = storageManager.loadProperties();
    }

    @Test
    public void testLoadProperty() throws IOException {
        storageManager.storeProperties(ImmutableMap.of("x1", "y1", "x2", "y2"));

        String value = storageManager.loadProperty("x1");
        assertEquals(value, "y1");
    }

    @Test(expectedExceptions = PropertyNotFoundException.class,
          expectedExceptionsMessageRegExp = "Property 'x1' not found")
    public void testLoadNonExistedProperty() throws IOException {
        createStorage("x=y");

        String value = storageManager.loadProperty("x1");
        assertEquals(value, "y1");
    }

    @Test
    public void testUpdateProperty() throws IOException {
        storageManager.storeProperties(ImmutableMap.of("x1", "y1", "x2", "y2"));

        storageManager.storeProperty("x1", "y11");
        String value = storageManager.loadProperty("x1");
        assertEquals(value, "y11");
    }

    @Test(expectedExceptions = PropertyNotFoundException.class,
          expectedExceptionsMessageRegExp = "Property 'x1' not found")
    public void testUpdateNonExistedProperty() throws IOException {
        storageManager.storeProperty("x1", "y1");
    }

    @Test
    public void testDeleteProperty() throws IOException {
        String key = "x1";
        storageManager.storeProperties(ImmutableMap.of(key, "y1"));

        storageManager.deleteProperty(key);

        Map<String, String> value = storageManager.loadProperties();
        assertFalse(value.containsKey(key));
    }

    @Test(expectedExceptions = PropertyNotFoundException.class,
          expectedExceptionsMessageRegExp = "Property 'x1' not found")
    public void testDeleteNonExistedProperty() throws IOException {
        createStorage("x=y");
        storageManager.deleteProperty("x1");
    }

    private void createStorage(String content) throws IOException {
        createDirectory(storageDir);
        createFile(configProperties);
        write(configProperties, content.getBytes());
    }
}
