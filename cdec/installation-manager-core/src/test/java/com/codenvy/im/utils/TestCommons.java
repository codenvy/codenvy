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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Thread.currentThread;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestCommons {

    @Test
    public void testExtractVersion() throws Exception {
        assertEquals(Commons.extractVersion(Paths.get("repository", "artifact", "version", "file")), "version");
    }

    @Test
    public void testExtractArtifactName() throws Exception {
        assertEquals(Commons.extractArtifactName(Paths.get("repository", "artifact", "version", "file")), "artifact");
    }

    @Test
    public void testCombinePath() throws Exception {
        assertEquals(Commons.combinePaths("api", "update"), "api/update");
        assertEquals(Commons.combinePaths("api", "/update"), "api/update");
        assertEquals(Commons.combinePaths("api/", "update"), "api/update");
        assertEquals(Commons.combinePaths("api/", "/update"), "api/update");
    }

    @Test
    public void testAddQueryParameter() throws Exception {
        assertEquals(Commons.addQueryParam("api", "a", "b"), "api?a=b");
        assertEquals(Commons.addQueryParam("api?a=b", "c", "d"), "api?a=b&c=d");
    }

    @Test
    public void testMapFromJson() throws Exception {
        Map<String, String> m = Commons.fromJson("{\"a\":\"b\",\"c\":\"d\"}", Map.class);
        assertEquals(m.size(), 2);
        assertEquals(m.get("a"), "b");
        assertEquals(m.get("c"), "d");
    }

    @Test
    public void testAsMap() throws Exception {
        Map m = Commons.asMap("{\n"
                              + "  \"name\" : \"name\",\n"
                              + "  \"id\" : \"id\"\n"
                              + "}");
        assertEquals(m.size(), 2);
        assertEquals(m.get("name"), "name");
        assertEquals(m.get("id"), "id");
    }

    @Test
    public void testCalculateMD5Sum() throws Exception {
        Path testFile = Paths.get("target", "testFile");
        Files.write(testFile, "SomeText".getBytes());

        String expectedMD5Sum = "5b6865e9622804fd70324543d06869c4"; // calculated by md5sum linux command
        assertEquals(Commons.calculateMD5Sum(testFile), expectedMD5Sum);
    }

    @Test
    public void testCopyInterruptable() throws Exception {
        String context = new String(Arrays.copyOf("test content".getBytes("UTF-8"), 10000), "UTF-8");

        Path inFile = Paths.get("target", "inFile");
        Path outFile = Paths.get("target", "outFile");
        FileOutputStream out = new FileOutputStream(outFile.toFile());

        FileUtils.writeStringToFile(inFile.toFile(), context);

        Commons.copyInterruptable(new FileInputStream(inFile.toFile()), out);
        String output = FileUtils.readFileToString(outFile.toFile());

        assertEquals(output, context);

    }

    @Test(expectedExceptions = CopyStreamInterruptedException.class, expectedExceptionsMessageRegExp = "The copying was interrupted.")
    public void testFailCopyInterruptable() throws Exception {
        final Path inFile = Paths.get("target", "inFile");
        final Path outFile = Paths.get("target", "outFile");

        IOUtils.write(Arrays.copyOf("test content".getBytes(), 10000), new FileOutputStream(inFile.toFile()));

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Exception> exception = new AtomicReference<>();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream out = new FileOutputStream(outFile.toFile()); FileInputStream in = new FileInputStream(inFile.toFile())) {
                    currentThread().interrupt();
                    Commons.copyInterruptable(in, out);
                } catch (Exception ex) {
                    exception.set(ex);
                }

                latch.countDown();
            }
        });
        t.start();
        latch.await();

        assertNotNull(exception.get());
        throw exception.get();
    }

    @Test
    public void testMapToSortedAndAlignedJson() throws Exception {
        Map<String, String> testProperties = new LinkedHashMap<String, String>(){{
            put("short_prop", "1");
            put("longer_prop", "20");
            put("longest_prop", "300");
            put("null_prop", null);
        }};

        String json = Commons.toJsonWithSortedAndAlignedProperties(testProperties);
        assertEquals(json, "{\n"
                           + "  \"longer_prop\"  : \"20\",\n"
                           + "  \"longest_prop\" : \"300\",\n"
                           + "  \"short_prop\"   : \"1\"\n"
                           + "}");
    }

    @Test
    public void testEmptyMapToSortedAndAlignedJson() throws Exception {
        Map<String, String> testProperties = new LinkedHashMap<>();

        String json = Commons.toJsonWithSortedAndAlignedProperties(testProperties);
        assertEquals(json, "{ }");
    }

    @Test
    public void testOneEntryMapToSortedAndAlignedJson() throws Exception {
        Map<String, String> testProperties = new LinkedHashMap<String, String>(){{
            put("prop", "value");
        }};

        String json = Commons.toJsonWithSortedAndAlignedProperties(testProperties);
        assertEquals(json, "{\n"
                           + "  \"prop\" : \"value\"\n"
                           + "}");
    }

    @Test
    public void testGetVersionsList() throws Exception {
        Path baseDir = Paths.get("target", "versions");

        Files.createDirectories(baseDir.resolve("1.0.2"));
        Files.createDirectories(baseDir.resolve("1.0.1"));
        Files.createDirectories(baseDir.resolve("1.0.4"));
        Files.createDirectories(baseDir.resolve("1.0.3"));

        TreeSet<Version> versions = Commons.getVersionsList(baseDir);
        Iterator<Version> iterator = versions.iterator();

        assertEquals(iterator.next(), Version.valueOf("1.0.1"));
        assertEquals(iterator.next(), Version.valueOf("1.0.2"));
        assertEquals(iterator.next(), Version.valueOf("1.0.3"));
        assertEquals(iterator.next(), Version.valueOf("1.0.4"));

        assertEquals(versions.first(), Version.valueOf("1.0.1"));
        assertEquals(versions.last(), Version.valueOf("1.0.4"));

        NavigableSet<Version> s = versions.subSet(Version.valueOf("1.0.1"), false, Version.valueOf("1.0.4"), true);

        iterator = s.iterator();
        assertEquals(iterator.next(), Version.valueOf("1.0.2"));
        assertEquals(iterator.next(), Version.valueOf("1.0.3"));
        assertEquals(iterator.next(), Version.valueOf("1.0.4"));
    }

    @Test
    public void testGetVersionsListReturnsEmptyList() throws Exception {
        Path baseDir = Paths.get("target", "versions");

        TreeSet<Version> versions = Commons.getVersionsList(baseDir.resolve("fake"));
        assertTrue(versions.isEmpty());
    }

    @Test
    public void testCreateArtifactOrNull() throws Exception {
        assertNull(Commons.createArtifactOrNull(null));
        assertNotNull(Commons.createArtifactOrNull("codenvy"));
    }

    @Test
    public void testCreateVersionOrNull() throws Exception {
        assertNull(Commons.createVersionOrNull(null));
        assertNotNull(Commons.createVersionOrNull("1.0.1"));
    }

    @Test(dataProvider = "TestExtractServerUrlData")
    public void testExtractServerUrl(String testUrl, String extractedUrl) {
        assertEquals(Commons.extractServerUrl(testUrl), extractedUrl);
    }

    @DataProvider(name = "TestExtractServerUrlData")
    public Object[][] getTestExtractServerUrlData() {
        return new Object[][] {
            {"http://test.com/path", "http://test.com"},
            {"https://test-path.com:8080/path", "https://test-path.com:8080"},
            {"http://test.com", "http://test.com"}
        };
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testExtractServerUrlFromIncorrectUrl() {
        Commons.extractServerUrl("123");
    }

}
