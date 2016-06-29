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
package com.codenvy.im.managers;

import com.codenvy.im.BaseTest;
import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.utils.AuthenticationException;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FileUtils;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import static com.codenvy.im.artifacts.ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.SUBSCRIPTION_PROPERTY;
import static java.lang.Thread.sleep;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class DownloadManagerTest extends BaseTest {

    public static final String EMPTY_FILE_MD5 = "d41d8cd98f00b204e9800998ecf8427e";
    public static final String ANY_FILE_MD5   = "asldfj345l4lkfjgs945jlkfjgs8945fgsd";

    @Mock
    private ConfigManager configManager;
    @Mock
    private HttpTransport transport;
    @Mock
    private NodeManager nodeManager;

    private CDECArtifact           cdecArtifact;
    private InstallManagerArtifact installManagerArtifact;
    private DownloadManager        downloadManager;
    private Path                   pathCDEC;
    private Path                   pathIM;


    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        cdecArtifact = spy(new CDECArtifact(UPDATE_API_ENDPOINT, DOWNLOAD_DIR, transport, configManager, nodeManager));
        installManagerArtifact = spy(new InstallManagerArtifact(CODENVY_IM_BASE_DIR, UPDATE_API_ENDPOINT, DOWNLOAD_DIR, SAAS_API_ENDPOINT, transport, configManager));
        downloadManager = spy(new DownloadManager(UPDATE_API_ENDPOINT,
                                                  DOWNLOAD_DIR,
                                                  transport,
                                                  ImmutableSet.<Artifact>of(cdecArtifact, installManagerArtifact)));
        downloadManager.downloadProgress = null;

        pathCDEC = Paths.get("./target/cdec.zip");
        pathIM = Paths.get("./target/im.zip");
    }

    @Test
    public void testCheckEnoughDiskSpace() throws Exception {
        downloadManager.checkEnoughDiskSpace(100);
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Not enough disk space.*")
    public void testCheckEnoughDiskSpaceThrowIOException() throws Exception {
        downloadManager.checkEnoughDiskSpace(Long.MAX_VALUE);
    }

    @Test(expectedExceptions = IOException.class)
    public void testInitializationIfDownloadDirectoryNotExist() throws IOException {
        new DownloadManager(UPDATE_API_ENDPOINT, "/home/bla-bla", transport, Collections.<Artifact>emptySet());
    }

    @Test(expectedExceptions = IOException.class)
    public void testInitializationIfWrongPermission() throws Exception {
        new DownloadManager(UPDATE_API_ENDPOINT, "/root", transport, Collections.<Artifact>emptySet());
    }


    @Test
    public void testGetPathToBinaries() throws Exception {
        Version version = Version.valueOf("1.0.1");
        doReturn(ImmutableMap.of(ArtifactProperties.FILE_NAME_PROPERTY, "binaries")).when(cdecArtifact).getProperties(version);

        Path path = downloadManager.getPathToBinaries(cdecArtifact, version);
        assertEquals(path, Paths.get(DOWNLOAD_DIR + "/codenvy/1.0.1/binaries"));
    }

    @Test
    public void testGetBinariesSize() throws Exception {
        Version version = Version.valueOf("1.0.1");
        doReturn(ImmutableMap.of(ArtifactProperties.SIZE_PROPERTY, "100")).when(cdecArtifact).getProperties(version);

        Long binariesSize = downloadManager.getBinariesSize(cdecArtifact, version);

        assertEquals(binariesSize.intValue(), 100);
    }

    @Test
    public void testGetDownloadedArtifacts() throws Exception {
        doReturn(new TreeMap<Version, Path>(new Version.ReverseOrderComparator()) {{
            put(Version.valueOf("1.0.0"), Paths.get("file1"));
            put(Version.valueOf("1.0.1"), Paths.get("file2"));
        }}).when(downloadManager).getDownloadedVersions(cdecArtifact);

        doReturn(new TreeMap<Version, Path>() {{
            put(Version.valueOf("2.0.0"), Paths.get("file3"));
        }}).when(downloadManager).getDownloadedVersions(installManagerArtifact);

        Map<Artifact, SortedMap<Version, Path>> artifacts = downloadManager.getDownloadedArtifacts();
        assertEquals(artifacts.size(), 2);

        // check order
        assertEquals(artifacts.toString(), "{codenvy={" +
                                           "1.0.1=file2, " +
                                           "1.0.0=file1" +
                                           "}, installation-manager-cli={" +
                                           "2.0.0=file3" +
                                           "}}");
    }

    @Test
    public void testGetDownloadedArtifactsReturnsEmptyMap() throws Exception {
        doReturn(new TreeMap<Version, Path>()).when(downloadManager).getDownloadedVersions(cdecArtifact);
        doReturn(new TreeMap<Version, Path>()).when(downloadManager).getDownloadedVersions(installManagerArtifact);

        Map<Artifact, SortedMap<Version, Path>> m = downloadManager.getDownloadedArtifacts();
        assertTrue(m.isEmpty());
    }

    @Test
    public void testGetUpdates() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");
        final Version version200 = Version.valueOf("2.0.0");

        doReturn(version100).when(cdecArtifact)
                            .getLatestInstallableVersion();

        doReturn(version200).when(installManagerArtifact)
                            .getLatestInstallableVersion();

        Map<Artifact, Version> updates = downloadManager.getUpdates();
        assertEquals(updates.size(), 2);
        assertEquals(updates.toString(), "{codenvy=1.0.0, " + InstallManagerArtifact.NAME + "=2.0.0}");
    }

    @Test
    public void testGetUpdatesToDownload() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");
        final Version version200 = Version.valueOf("2.0.0");

        doReturn(new TreeMap<Artifact, Version>() {{
            put(cdecArtifact, version100);
            put(installManagerArtifact, version200);
        }}).when(downloadManager).getUpdates();

        Map<Artifact, Version> artifactsToDownload = downloadManager.getLatestUpdatesToDownload(null, null);
        assertEquals(artifactsToDownload.size(), 2);
        assertEquals(artifactsToDownload.toString(), "{" + InstallManagerArtifact.NAME + "=2.0.0, codenvy=1.0.0}");
    }

    @Test
    public void testGetUpdatesToDownloadForSpecificArtifact() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");

        doReturn(version100).when(cdecArtifact).getLatestInstallableVersion();

        Map<Artifact, Version> artifactsToDownload = downloadManager.getLatestUpdatesToDownload(cdecArtifact, null);
        assertEquals(artifactsToDownload.size(), 1);
        assertEquals(artifactsToDownload.toString(), "{codenvy=1.0.0}");
    }

    @Test
    public void testAlreadyDownloadedUpdatesToDownload() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");
        final Version version200 = Version.valueOf("2.0.0");

        doReturn(new TreeMap<Artifact, Version>() {{
            put(cdecArtifact, version200);
            put(installManagerArtifact, version100);
        }}).when(downloadManager).getUpdates();

        doReturn(new TreeMap<Version, Path>() {{
            put(version200, null);
        }}).when(downloadManager).getDownloadedVersions(cdecArtifact);

        doReturn(new TreeMap<Version, Path>() {{
            put(version100, null);
        }}).when(downloadManager).getDownloadedVersions(installManagerArtifact);

        Map<Artifact, Version> artifactsToDownload = downloadManager.getLatestUpdatesToDownload(null, null);
        assertEquals(artifactsToDownload.size(), 0);
    }


    @Test
    public void testDownloadVersion() throws Exception {
        Version version100 = Version.valueOf("1.0.0");
        when(transport.doGet(endsWith("repository/properties/" + cdecArtifact.getName() + "/" + version100.toString())))
                .thenReturn(String.format("{\"%s\": \"true\", \"%s\":\"OnPremises\"}", AUTHENTICATION_REQUIRED_PROPERTY, SUBSCRIPTION_PROPERTY));

        downloadManager.download(cdecArtifact, version100);
    }

    @Test
    public void testGetDownloadedVersions() throws IOException {
        doReturn(ImmutableMap.of("file", "file1", "md5", EMPTY_FILE_MD5)).when(cdecArtifact).getProperties(Version.valueOf("1.0.1"));
        doReturn(ImmutableMap.of("file", "file2", "md5", EMPTY_FILE_MD5)).when(cdecArtifact).getProperties(Version.valueOf("1.0.2"));

        Path file1 = Paths.get(DOWNLOAD_DIR, cdecArtifact.getName(), "1.0.1", "file1");
        Path propertiesFile1 = Paths.get(DOWNLOAD_DIR, cdecArtifact.getName(), "1.0.1", Artifact.ARTIFACT_PROPERTIES_FILE_NAME);

        Path file2 = Paths.get(DOWNLOAD_DIR, cdecArtifact.getName(), "1.0.2", "file2");
        Path propertiesFile2 = Paths.get(DOWNLOAD_DIR, cdecArtifact.getName(), "1.0.2", Artifact.ARTIFACT_PROPERTIES_FILE_NAME);

        Files.createDirectories(file1.getParent());
        Files.createDirectories(file2.getParent());

        Files.createFile(file1);
        Files.createFile(propertiesFile1);

        Files.createFile(file2);
        Files.createFile(propertiesFile2);

        SortedMap<Version, Path> versions = downloadManager.getDownloadedVersions(cdecArtifact);
        assertEquals(versions.size(), 2);
        assertEquals(versions.toString(), "{1.0.2=target/updates/codenvy/1.0.2/file2, " +
                                          "1.0.1=target/updates/codenvy/1.0.1/file1" +
                                          "}");
    }

    @Test(expectedExceptions = ArtifactNotFoundException.class)
    public void testGetDownloadedVersionsWhenPropertiesAbsent() throws Exception {
        Path file1 = Paths.get(DOWNLOAD_DIR, cdecArtifact.getName(), "1.0.1", "file1");
        Files.createDirectories(file1.getParent());
        Files.createFile(file1);

        doThrow(new ArtifactNotFoundException(cdecArtifact)).when(cdecArtifact).getProperties(any(Version.class));

        downloadManager.getDownloadedVersions(cdecArtifact);
    }

    @Test
    public void testDownloadAll() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        final Version imVersion = Version.valueOf("1.0.0");
        final int cdecSize = 100;
        final int imSize = 50;

        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
                put(installManagerArtifact, imVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(null, null);

        doAnswer(invocationOnMock -> {
            FileUtils.writeByteArrayToFile(pathCDEC.toFile(), new byte[cdecSize]);
            return pathCDEC;
        }).when(downloadManager).download(cdecArtifact, cdecVersion);

        doAnswer(invocationOnMock -> {
            FileUtils.writeByteArrayToFile(pathIM.toFile(), new byte[imSize]);
            return pathIM;
        }).when(downloadManager).download(installManagerArtifact, imVersion);

        doReturn(pathCDEC).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doReturn(pathIM).when(downloadManager).getPathToBinaries(installManagerArtifact, imVersion);

        doReturn((long)cdecSize).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);
        doReturn((long)imSize).when(downloadManager).getBinariesSize(installManagerArtifact, imVersion);

        doNothing().when(downloadManager).validateMd5Sum(cdecArtifact, cdecVersion, pathCDEC);
        doNothing().when(downloadManager).validateMd5Sum(installManagerArtifact, imVersion, pathIM);
        doNothing().when(downloadManager).saveArtifactProperties(cdecArtifact, cdecVersion, pathCDEC);
        doNothing().when(downloadManager).saveArtifactProperties(installManagerArtifact, imVersion, pathIM);

        downloadManager.startDownload(null, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(info.getPercents(), 100);

        List<DownloadArtifactInfo> artifacts = info.getArtifacts();
        assertEquals(artifacts.size(), 2);

        assertEquals(artifacts.get(0).getArtifact(), cdecArtifact.getName());
        assertEquals(artifacts.get(0).getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(artifacts.get(0).getFile(), pathCDEC.toString());
        assertEquals(artifacts.get(0).getVersion(), cdecVersion.toString());

        assertEquals(artifacts.get(1).getArtifact(), installManagerArtifact.getName());
        assertEquals(artifacts.get(1).getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(artifacts.get(1).getFile(), pathIM.toString());
        assertEquals(artifacts.get(1).getVersion(), imVersion.toString());
    }

    @Test
    public void testDownloadSpecificArtifact() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        final int cdecSize = 100;

        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, null);

        doAnswer(invocationOnMock -> {
            FileUtils.writeByteArrayToFile(pathCDEC.toFile(), new byte[cdecSize]);
            return pathCDEC;
        }).when(downloadManager).download(cdecArtifact, cdecVersion);

        doReturn(pathCDEC).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doReturn((long)cdecSize).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);

        doNothing().when(downloadManager).validateMd5Sum(cdecArtifact, cdecVersion, pathCDEC);
        doNothing().when(downloadManager).saveArtifactProperties(cdecArtifact, cdecVersion, pathCDEC);

        downloadManager.startDownload(cdecArtifact, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(info.getPercents(), 100);

        List<DownloadArtifactInfo> artifacts = info.getArtifacts();
        assertEquals(artifacts.size(), 1);
        assertEquals(artifacts.get(0).getArtifact(), cdecArtifact.getName());
        assertEquals(artifacts.get(0).getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(artifacts.get(0).getFile(), pathCDEC.toString());
        assertEquals(artifacts.get(0).getVersion(), cdecVersion.toString());
    }

    @Test
    public void testDownloadSpecificVersionArtifact() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        final int cdecSize = 100;

        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, cdecVersion);

        doAnswer(invocationOnMock -> {
            FileUtils.writeByteArrayToFile(pathCDEC.toFile(), new byte[cdecSize]);
            return pathCDEC;
        }).when(downloadManager).download(cdecArtifact, cdecVersion);

        doReturn(pathCDEC).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doReturn((long)cdecSize).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);

        doNothing().when(downloadManager).validateMd5Sum(cdecArtifact, cdecVersion, pathCDEC);
        doNothing().when(downloadManager).saveArtifactProperties(cdecArtifact, cdecVersion, pathCDEC);

        downloadManager.startDownload(cdecArtifact, cdecVersion);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(info.getPercents(), 100);

        List<DownloadArtifactInfo> artifacts = info.getArtifacts();
        assertEquals(artifacts.size(), 1);
        assertEquals(artifacts.get(0).getArtifact(), cdecArtifact.getName());
        assertEquals(artifacts.get(0).getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(artifacts.get(0).getFile(), pathCDEC.toString());
        assertEquals(artifacts.get(0).getVersion(), cdecVersion.toString());
    }

    @Test
    public void testDownloadNothing() throws Exception {
        doReturn(Collections.emptyMap()).when(downloadManager).getLatestUpdatesToDownload(null, null);

        downloadManager.startDownload(null, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(info.getPercents(), 0);
        assertEquals(info.getArtifacts().size(), 0);
    }

    @Test
    public void testDownloadFailedIfArtifactNotFound() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, cdecVersion);

        doThrow(ArtifactNotFoundException.class).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);
        doThrow(ArtifactNotFoundException.class).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);

        downloadManager.startDownload(cdecArtifact, cdecVersion);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.FAILED);
    }

    @Test(expectedExceptions = DownloadAlreadyStartedException.class)
    public void testStartDownloadTwice() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        final int cdecSize = 100;

        doReturn(pathCDEC).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doReturn((long)cdecSize).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);

        doAnswer(invocationOnMock -> {
            downloadManager.createDownloadDescriptor(ImmutableMap.<Artifact, Version>of(cdecArtifact, cdecVersion));

            Object[] arguments = invocationOnMock.getArguments();
            CountDownLatch countDownLatch = (CountDownLatch)arguments[2];
            countDownLatch.countDown();
            return null;
        }).when(downloadManager).download(any(Artifact.class), any(Version.class), any(CountDownLatch.class));

        downloadManager.startDownload(cdecArtifact, null);
        downloadManager.startDownload(cdecArtifact, null);
    }

    @Test(expectedExceptions = DownloadNotStartedException.class)
    public void testGetDownloadStatusWhenDownloadNotStarted() throws Exception {
        downloadManager.getDownloadProgress();
    }

    @Test
    public void testDownloadErrorIfAuthenticationFailedOnGetUpdates() throws Exception {
        doThrow(AuthenticationException.class).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, null);

        downloadManager.startDownload(cdecArtifact, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.FAILED);
    }

    @Test
    public void testDownloadErrorIfAuthenticationFailedOnDownload() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");

        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, null);

        doThrow(new AuthenticationException()).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doThrow(new AuthenticationException()).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);
        doThrow(new AuthenticationException()).when(downloadManager).download(cdecArtifact, cdecVersion);

        downloadManager.startDownload(cdecArtifact, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.FAILED);
    }

    @Test
    public void testGetAllUpdatesAfterInstalledVersion() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        doReturn("[\"2.0.1\", \"2.0.2\"]").when(transport).doGet(endsWith("updates/codenvy?fromVersion=2.0.0"));
        doReturn(Optional.of(cdecVersion)).when(cdecArtifact).getInstalledVersion();

        Collection<Map.Entry<Artifact, Version>> updates = downloadManager.getAllUpdates(cdecArtifact, true);

        assertEquals(updates.size(), 2);
    }

    @Test
    public void testGetAllUpdatesNoStartVersion() throws Exception {
        doReturn("[\"2.0.1\"]").when(transport).doGet(endsWith("updates/codenvy"));
        doReturn(Optional.empty()).when(cdecArtifact).getInstalledVersion();

        Collection<Map.Entry<Artifact, Version>> updates = downloadManager.getAllUpdates(cdecArtifact, true);
        assertEquals(updates.size(), 1);
    }

    @Test
    public void testGetAllUpdates() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        doReturn("[\"2.0.0\", \"2.0.1\", \"2.0.2\"]").when(transport).doGet(endsWith("updates/codenvy"));
        doReturn(Optional.of(cdecVersion)).when(cdecArtifact).getInstalledVersion();

        Collection<Map.Entry<Artifact, Version>> updates = downloadManager.getAllUpdates(cdecArtifact, false);

        assertEquals(updates.size(), 3);
    }

    @Test
    public void testDeleteDownloadedArtifact() throws Exception {
        Artifact artifact = cdecArtifact;
        Version version = Version.valueOf("1.0.1");

        Path binary = Paths.get(DOWNLOAD_DIR, artifact.getName(), version.toString(), "file1");
        Files.createDirectories(binary.getParent());
        Files.createFile(binary);

        doReturn(binary).when(downloadManager).getPathToBinaries(artifact, version);

        downloadManager.deleteArtifact(artifact, version);
        assertFalse(Files.exists(Paths.get(DOWNLOAD_DIR, artifact.getName(), version.toString())));
    }

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Artifact 'codenvy' version '1.0.1' is being downloaded and cannot be deleted.")
    public void testDeleteDownloadingArtifactShouldThrowException() throws Exception {
        Artifact artifact = cdecArtifact;
        Version version = Version.valueOf("1.0.1");

        Path binary = Paths.get(DOWNLOAD_DIR, artifact.getName(), version.toString(), "file1");
        doReturn(binary).when(downloadManager).getPathToBinaries(artifact, version);

        DownloadArtifactInfo mockDownloadArtifactInfo = mock(DownloadArtifactInfo.class);
        doReturn(artifact.getName()).when(mockDownloadArtifactInfo).getArtifact();
        doReturn(version.toString()).when(mockDownloadArtifactInfo).getVersion();

        downloadManager.downloadProgress = new DownloadProgress(Collections.<Path, Long>emptyMap(), ImmutableMap.of(artifact, version));

        downloadManager.deleteArtifact(artifact, version);
    }

    @Test
    public void testSaveArtifactProperties() throws IOException {
        Artifact artifact = cdecArtifact;
        Version version = Version.valueOf("1.0.1");
        String binaryFileName = "file1";

        Path binary = Paths.get(DOWNLOAD_DIR, artifact.getName(), version.toString(), binaryFileName);
        doReturn(binary).when(downloadManager).getPathToBinaries(artifact, version);

        Files.createDirectories(binary.getParent());
        Files.createFile(binary);

        String propertiesFileContent = String.format("{\"%s\": \"%s\", \"%s\":\"%s\"}",
                                                     ArtifactProperties.FILE_NAME_PROPERTY, binaryFileName,
                                                     ArtifactProperties.MD5_PROPERTY, EMPTY_FILE_MD5);
        when(transport.doGet(endsWith("repository/properties/" + cdecArtifact.getName() + "/" + version.toString())))
            .thenReturn(propertiesFileContent);

        downloadManager.saveArtifactProperties(cdecArtifact, version, binary);

        Path propertiesFile = Paths.get(DOWNLOAD_DIR, artifact.getName(), version.toString(), Artifact.ARTIFACT_PROPERTIES_FILE_NAME);
        assertTrue(Files.exists(propertiesFile));
        assertTrue(FileUtils.readFileToString(propertiesFile.toFile()).endsWith("file=file1\n"
                                                                                + "md5=d41d8cd98f00b204e9800998ecf8427e\n"));
    }

    @Test
    public void testDownloadErrorIfFileCorrupted() throws Exception {
        final Version cdecVersion = Version.valueOf("2.0.0");
        final int cdecSize = 100;

        doReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, cdecVersion);
            }
        }).when(downloadManager).getLatestUpdatesToDownload(cdecArtifact, null);

        doAnswer(invocationOnMock -> {
            FileUtils.writeByteArrayToFile(pathCDEC.toFile(), new byte[cdecSize]);
            return pathCDEC;
        }).when(downloadManager).download(cdecArtifact, cdecVersion);

        doReturn(pathCDEC).when(downloadManager).getPathToBinaries(cdecArtifact, cdecVersion);
        doReturn((long)cdecSize).when(downloadManager).getBinariesSize(cdecArtifact, cdecVersion);

        String propertiesFileContent = String.format("{\"%s\":\"%s\"}", ArtifactProperties.MD5_PROPERTY, ANY_FILE_MD5);
        when(transport.doGet(endsWith("repository/properties/" + cdecArtifact.getName() + "/" + cdecVersion.toString())))
                .thenReturn(propertiesFileContent);

        downloadManager.startDownload(cdecArtifact, null);

        DownloadProgressResponse info;
        do {
            sleep(100); // due to async request, wait a bit to get proper download status
            info = downloadManager.getDownloadProgress();
        } while (info.getStatus() == DownloadArtifactInfo.Status.DOWNLOADING);

        assertEquals(info.getStatus(), DownloadArtifactInfo.Status.FAILED);
        assertEquals(info.getPercents(), 100);
        assertTrue(info.getMessage().contains("File corrupted"));
    }
}
