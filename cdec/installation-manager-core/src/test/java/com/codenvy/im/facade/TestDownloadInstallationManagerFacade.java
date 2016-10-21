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
package com.codenvy.im.facade;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.managers.BackupManager;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.managers.InstallManager;
import com.codenvy.im.managers.LdapManager;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.StorageManager;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.saas.SaasAuthServiceProxy;
import com.codenvy.im.saas.SaasRepositoryServiceProxy;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestDownloadInstallationManagerFacade {
    private InstallationManagerFacade installationManagerService;

    @Mock
    private SaasAuthServiceProxy       saasAuthServiceProxy;
    @Mock
    private SaasRepositoryServiceProxy saasRepositoryServiceProxy;
    @Mock
    private HttpTransport              transport;
    @Mock
    private LdapManager                ldapManager;
    @Mock
    private NodeManager                nodeManager;
    @Mock
    private BackupManager              backupManager;
    @Mock
    private StorageManager             storageManager;
    @Mock
    private InstallManager             installManager;
    @Mock
    private DownloadManager            downloadManager;
    @Mock
    private ConfigManager              configManager;

    private Artifact installManagerArtifact;
    private Artifact cdecArtifact;
    private Path     pathCDEC;
    private Path     pathIM;

    @BeforeMethod
    public void init() throws Exception {
        initMocks(this);
        installationManagerService = new InstallationManagerFacade(
                transport,
                configManager,
                saasAuthServiceProxy,
                saasRepositoryServiceProxy,
                ldapManager,
                nodeManager,
                backupManager,
                storageManager,
                installManager,
                downloadManager);
        this.pathCDEC = Paths.get("./target/cdec.zip");
        this.pathIM = Paths.get("./target/im.zip");

        installManagerArtifact = createArtifact(InstallManagerArtifact.NAME);
        cdecArtifact = createArtifact(CDECArtifact.NAME);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        Files.deleteIfExists(pathCDEC);
        Files.deleteIfExists(pathIM);
    }


    @Test
    public void testGetDownloads() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");
        final Version version101 = Version.valueOf("1.0.1");
        final Version version200 = Version.valueOf("2.0.0");

        doReturn(false).when(installManager).isInstallable(cdecArtifact, version100);
        doReturn(true).when(installManager).isInstallable(cdecArtifact, version101);
        doReturn(true).when(installManager).isInstallable(installManagerArtifact, version200);

        doReturn(new LinkedHashMap<Artifact, SortedMap<Version, Path>>() {{
            put(cdecArtifact, new TreeMap<Version, Path>() {{
                put(version100, Paths.get("target/file1"));
                put(Version.valueOf("1.0.1"), Paths.get("target/file2"));
            }});
            put(installManagerArtifact, new TreeMap<Version, Path>() {{
                put(Version.valueOf("2.0.0"), Paths.get("target/file3"));
            }});
        }}).when(downloadManager).getDownloadedArtifacts();

        Collection<DownloadArtifactInfo> downloads = installationManagerService.getDownloads(null, null);
        Iterator<DownloadArtifactInfo> iter = downloads.iterator();

        assertEquals(downloads.size(), 3);
        DownloadArtifactInfo result = iter.next();
        assertEquals(result.getArtifact(), "codenvy");
        assertEquals(result.getVersion(), "1.0.0");
        assertEquals(result.getFile(), "target/file1");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);

        result = iter.next();
        assertEquals(result.getArtifact(), "codenvy");
        assertEquals(result.getVersion(), "1.0.1");
        assertEquals(result.getFile(), "target/file2");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.READY_TO_INSTALL);

        result = iter.next();
        assertEquals(result.getArtifact(), InstallManagerArtifact.NAME);
        assertEquals(result.getVersion(), "2.0.0");
        assertEquals(result.getFile(), "target/file3");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.READY_TO_INSTALL);
    }

    @Test
    public void testGetDownloadsSpecificArtifact() throws Exception {
        final Version version200 = Version.valueOf("2.0.0");
        final Version version201 = Version.valueOf("2.0.1");

        doReturn(new LinkedHashMap<Artifact, SortedMap<Version, Path>>() {{
            put(installManagerArtifact, new TreeMap<Version, Path>() {{
                put(version200, Paths.get("target/file1"));
                put(version201, Paths.get("target/file2"));
            }});
        }}).when(downloadManager).getDownloadedArtifacts();

        doReturn(false).when(installManager).isInstallable(installManagerArtifact, version200);
        doReturn(true).when(installManager).isInstallable(installManagerArtifact, version201);

        Collection<DownloadArtifactInfo> downloads = installationManagerService.getDownloads(createArtifact(InstallManagerArtifact.NAME), null);

        Iterator<DownloadArtifactInfo> iter = downloads.iterator();

        assertEquals(downloads.size(), 2);
        DownloadArtifactInfo result = iter.next();
        assertEquals(result.getArtifact(), InstallManagerArtifact.NAME);
        assertEquals(result.getVersion(), "2.0.0");
        assertEquals(result.getFile(), "target/file1");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);

        result = iter.next();
        assertEquals(result.getArtifact(), InstallManagerArtifact.NAME);
        assertEquals(result.getVersion(), "2.0.1");
        assertEquals(result.getFile(), "target/file2");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.READY_TO_INSTALL);
    }

    @Test
    public void testGetDownloadsOlderVersionInstallManagerArtifact() throws Exception {
        final Version version200 = Version.valueOf("2.0.0");
        doReturn(new LinkedHashMap<Artifact, SortedMap<Version, Path>>() {{
            put(installManagerArtifact, new TreeMap<Version, Path>() {{
                put(version200, Paths.get("target/file1"));
            }});
        }}).when(downloadManager).getDownloadedArtifacts();

        doReturn(true).when(installManager).isInstallable(installManagerArtifact, version200);

        Collection<DownloadArtifactInfo> downloads = installationManagerService.getDownloads(createArtifact(InstallManagerArtifact.NAME),
                                                                                         Version.valueOf("2.0.0"));

        assertEquals(downloads.size(), 1);
        DownloadArtifactInfo result = downloads.iterator().next();
        assertEquals(result.getArtifact(), InstallManagerArtifact.NAME);
        assertEquals(result.getVersion(), "2.0.0");
        assertEquals(result.getFile(), "target/file1");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.READY_TO_INSTALL);
    }

    @Test
    public void testGetDownloadsSpecificArtifactShouldReturnEmptyList() throws Exception {
        doReturn(Collections.emptyMap()).when(downloadManager).getDownloadedArtifacts();

        Collection<DownloadArtifactInfo> downloads = installationManagerService.getDownloads(createArtifact(InstallManagerArtifact.NAME), null);
        assertTrue(downloads.isEmpty());
    }
}
