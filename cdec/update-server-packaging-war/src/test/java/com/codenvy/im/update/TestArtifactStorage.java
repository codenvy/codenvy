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
package com.codenvy.im.update;

import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.utils.Version;

import org.apache.commons.io.IOUtils;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

import static com.codenvy.im.artifacts.ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.BUILD_TIME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.FILE_NAME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.SUBSCRIPTION_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.VERSION_PROPERTY;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 */
public class TestArtifactStorage extends BaseTest {

    private ArtifactStorage artifactStorage;

    @BeforeTest
    public void prepare() throws Exception {
        artifactStorage = new ArtifactStorage(DOWNLOAD_DIRECTORY.toString());
    }

    @Test
    public void testGetArtifactDir() throws Exception {
        assertEquals(artifactStorage.getArtifactDir("installation-manager", "1.0.1").toString(),
                     "target/download/installation-manager/1.0.1");
        assertEquals(artifactStorage.getArtifactDir("installation-manager").toString(),
                     "target/download/installation-manager");
    }

    @Test
    public void testGetPropertiesFile() throws Exception {
        assertEquals(artifactStorage.getPropertiesFile("installation-manager", "1.0.1").toString(),
                     "target/download/installation-manager/1.0.1/" + ArtifactStorage.PROPERTIES_FILE);
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testLoadPropertiesThrowExceptionIfPropertyFileAbsent() throws Exception {
        Files.createDirectories(artifactStorage.getArtifactDir("installation-manager", "1.0.1"));
        assertFalse(Files.exists(artifactStorage.getPropertiesFile("installation-manager", "1.0.1")));

        artifactStorage.loadProperties("installation-manager", "1.0.1");
    }

    @Test
    public void testLoadStoreProperties() throws Exception {
        Path propertiesFile = Paths.get("target", "download", "installation-manager", "1.0.1", ArtifactStorage.PROPERTIES_FILE);
        assertFalse(Files.exists(propertiesFile));

        Properties properties = new Properties();
        properties.put(VERSION_PROPERTY, "1.0.1");
        properties.put(BUILD_TIME_PROPERTY, "20140930");
        properties.put(FILE_NAME_PROPERTY, "installation-manager-1.0.1.zip");
        properties.put(AUTHENTICATION_REQUIRED_PROPERTY, "true");
        properties.put(SUBSCRIPTION_PROPERTY, "OnPremises");
        artifactStorage.storeProperties("installation-manager", "1.0.1", properties);

        assertTrue(Files.exists(propertiesFile));

        properties = artifactStorage.loadProperties("installation-manager", "1.0.1");
        assertNotNull(properties);
        assertEquals(properties.size(), 5);
        assertEquals(properties.getProperty(VERSION_PROPERTY), "1.0.1");
        assertEquals(properties.getProperty(BUILD_TIME_PROPERTY), "20140930");
        assertEquals(properties.getProperty(FILE_NAME_PROPERTY), "installation-manager-1.0.1.zip");
        assertEquals(properties.getProperty(AUTHENTICATION_REQUIRED_PROPERTY), "true");
        assertEquals(properties.getProperty(SUBSCRIPTION_PROPERTY), "OnPremises");

        assertEquals(artifactStorage.getRequiredSubscription("installation-manager", "1.0.1"), "OnPremises");
        assertEquals(artifactStorage.getFileName("installation-manager", "1.0.1"), "installation-manager-1.0.1.zip");
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testIsAuthenticationRequiredThrowExceptionIfPropertiesAbsent() throws Exception {
        assertTrue(artifactStorage.isAuthenticationRequired("installation-manager", "1.0.1"));
    }

    @Test
    public void testIsAuthenticationRequired() throws Exception {
        Properties properties = new Properties();
        properties.put(AUTHENTICATION_REQUIRED_PROPERTY, "true");
        artifactStorage.storeProperties("installation-manager", "1.0.1", properties);

        assertTrue(artifactStorage.isAuthenticationRequired("installation-manager", "1.0.1"));
    }

    @Test
    public void testAuthenticationIsNotRequired() throws Exception {
        Properties properties = new Properties();
        properties.put(AUTHENTICATION_REQUIRED_PROPERTY, "false");
        artifactStorage.storeProperties("installation-manager", "1.0.1", properties);

        assertFalse(artifactStorage.isAuthenticationRequired("installation-manager", "1.0.1"));
    }

    @Test
    public void testAuthenticationIsNotRequiredIfArtifactPropertyAbsent() throws Exception {
        artifactStorage.storeProperties("installation-manager", "1.0.1", new Properties());

        assertFalse(artifactStorage.isAuthenticationRequired("installation-manager", "1.0.1"));
    }

    @Test
    public void testAuthenticationIsNotRequiredIfArtifactPropertyWrongFormat() throws Exception {
        Properties properties = new Properties();
        properties.put(AUTHENTICATION_REQUIRED_PROPERTY, "");
        artifactStorage.storeProperties("installation-manager", "1.0.1", properties);

        assertFalse(artifactStorage.isAuthenticationRequired("installation-manager", "1.0.1"));
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testGetFileNameThrowExceptionIfPropertiesAbsent() throws Exception {
        artifactStorage.getFileName("installation-manager", "1.0.1");
    }

    @Test
    public void testGetArtifact() throws Exception {
        Properties properties = new Properties();
        properties.put(FILE_NAME_PROPERTY, "file");
        artifactStorage.storeProperties("installation-manager", "1.0.1", properties);

        assertEquals(artifactStorage.getArtifact("installation-manager", "1.0.1").toString(),
                     "target/download/installation-manager/1.0.1/file");
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testGetArtifactThrowExceptionIfPropertiesAbsent() throws Exception {
        artifactStorage.getArtifact("installation-manager", "1.0.1");
    }

    @Test
    public void testDownload() throws Exception {
        Properties props = new Properties();
        props.put(FILE_NAME_PROPERTY, "installation-manager-1.0.1.jar");
        props.put(VERSION_PROPERTY, "1.0.1");
        artifactStorage.storeProperties("installation-manager", "1.0.1", props);

        Path artifact = Paths.get("target", "download", "installation-manager", "1.0.1", "installation-manager-1.0.1.jar");
        try (BufferedWriter writer = Files.newBufferedWriter(artifact, Charset.defaultCharset())) {
            writer.write("hello");
        }

        Path tmp = Paths.get("target", "download", "tmp");
        try (InputStream in = artifactStorage.download("installation-manager", "1.0.1")) {
            OutputStream out = new FileOutputStream(tmp.toFile());
            IOUtils.copyLarge(in, out);
            in.close();
            out.close();
        }

        try (BufferedReader reader = Files.newBufferedReader(artifact, Charset.defaultCharset())) {
            assertEquals(reader.readLine(), "hello");
        }
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testDownloadThrowExceptionIfPropertiesAbsent() throws Exception {
        artifactStorage.download("installation-manager", "1.0.1");
    }

    @Test
    public void testGetVersions() throws Exception {
        Files.createDirectories(artifactStorage.getArtifactDir("codenvy", "1.0.1"));
        Files.createDirectories(artifactStorage.getArtifactDir("codenvy", "1.0.2"));
        Files.createDirectories(artifactStorage.getArtifactDir("codenvy", "1.0.3"));

        Collection<Version> versions = artifactStorage.getVersions("codenvy", "1.0.2");

        assertEquals(versions.size(), 1);
        assertEquals(versions.iterator().next(), Version.valueOf("1.0.3"));
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class,
          expectedExceptionsMessageRegExp = "Artifact 'codenvy' not found")
    public void testGetLatestArtifactVersionFailOnAbsentAnyVersions() throws Exception {
        ArtifactStorage spyArtifactStorage = spy(new ArtifactStorage(DOWNLOAD_DIRECTORY.toString()));
        doThrow(new IOException("error")).when(spyArtifactStorage).getProperty(CDECArtifact.NAME, Version.valueOf("1.0.0"), ArtifactProperties.LABEL_PROPERTY);
        spyArtifactStorage.getLatestVersion(CDECArtifact.NAME, "any value");
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = "java.io.IOException: error")
    public void testGetLatestArtifactVersionFailOnIOException() throws Exception {
        ArtifactStorage spyArtifactStorage = spy(new ArtifactStorage(DOWNLOAD_DIRECTORY.toString()));

        spyArtifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.0", "tmp", new Properties());
        doThrow(new IOException("error")).when(spyArtifactStorage).getProperty(CDECArtifact.NAME, Version.valueOf("1.0.0"), ArtifactProperties.LABEL_PROPERTY);

        spyArtifactStorage.getLatestVersion(CDECArtifact.NAME, "any value");
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class,
          expectedExceptionsMessageRegExp = "There is no version of artifact codenvy with label 'any value'")
    public void testGetLatestArtifactVersionFailOnAbsentStableVersion() throws Exception {
        ArtifactStorage spyArtifactStorage = spy(new ArtifactStorage(DOWNLOAD_DIRECTORY.toString()));
        spyArtifactStorage.upload(new ByteArrayInputStream("content".getBytes()), CDECArtifact.NAME, "1.0.0", "tmp", new Properties());
        spyArtifactStorage.getLatestVersion(CDECArtifact.NAME, "any value");
    }
}
