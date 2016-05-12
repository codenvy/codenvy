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

import com.codenvy.im.BaseTest;
import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.event.Event;
import com.codenvy.im.license.CodenvyLicense;
import com.codenvy.im.license.CodenvyLicenseManager;
import com.codenvy.im.license.InvalidLicenseException;
import com.codenvy.im.license.LicenseNotFoundException;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.BackupManager;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.managers.InstallManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.LdapManager;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.StorageManager;
import com.codenvy.im.response.ArtifactInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.saas.SaasAuthServiceProxy;
import com.codenvy.im.saas.SaasRepositoryServiceProxy;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.codenvy.im.utils.Commons.toJson;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestInstallationManagerFacade extends BaseTest {
    private InstallationManagerFacade installationManagerFacade;
    private Artifact cdecArtifact;

    @Mock
    private HttpTransport              transport;
    @Mock
    private SaasAuthServiceProxy       saasAuthServiceProxy;
    @Mock
    private SaasRepositoryServiceProxy saasRepositoryServiceProxy;
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
    @Mock
    private CodenvyLicenseManager      codenvyLicenseManager;
    @Mock
    private CodenvyLicense             codenvyLicense;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        cdecArtifact = ArtifactFactory.createArtifact(CDECArtifact.NAME);
        installationManagerFacade = spy(new InstallationManagerFacade("target/download",
                                                                      "update/endpoint",
                                                                      "saas/endpoint",
                                                                      transport,
                                                                      saasAuthServiceProxy,
                                                                      saasRepositoryServiceProxy,
                                                                      ldapManager,
                                                                      nodeManager,
                                                                      backupManager,
                                                                      storageManager,
                                                                      installManager,
                                                                      downloadManager,
                                                                      codenvyLicenseManager));
    }

    @Test
    public void testStartDownload() throws Exception {
        Version version = Version.valueOf("1.0.1");

        installationManagerFacade.startDownload(cdecArtifact, version);

        verify(downloadManager).startDownload(cdecArtifact, version);
    }

    @Test
    public void testStopDownload() throws Exception {
        installationManagerFacade.stopDownload();

        verify(downloadManager).stopDownload();
    }

    @Test
    public void testGetDownloadStatus() throws Exception {
        installationManagerFacade.getDownloadProgress();

        verify(downloadManager).getDownloadProgress();
    }

    @Test
    public void testInstall() throws Exception {
        InstallOptions installOptions = new InstallOptions();
        final Version version = Version.valueOf("2.10.5");
        final Path pathToBinaries = Paths.get("file");

        doReturn(new TreeMap<Version, Path>() {{
            put(version, pathToBinaries);
        }}).when(downloadManager).getDownloadedVersions(cdecArtifact);

        installationManagerFacade.install(cdecArtifact, version, installOptions);

        verify(installManager).performInstallStep(cdecArtifact, version, pathToBinaries, installOptions, true);
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testInstallError() throws Exception {
        InstallOptions installOptions = new InstallOptions();
        Version version = Version.valueOf("1.0.1");

        doThrow(FileNotFoundException.class).when(downloadManager).getDownloadedVersions(cdecArtifact);

        installationManagerFacade.install(cdecArtifact, version, installOptions);
    }

    @Test
    public void testUpdate() throws Exception {
        InstallOptions installOptions = new InstallOptions();
        final Version version = Version.valueOf("2.10.5");
        final Path pathToBinaries = Paths.get("file");

        doReturn(new TreeMap<Version, Path>() {{
            put(version, pathToBinaries);
        }}).when(downloadManager).getDownloadedVersions(cdecArtifact);

        installationManagerFacade.update(cdecArtifact, version, installOptions);

        verify(installManager).performUpdateStep(cdecArtifact, version, pathToBinaries, installOptions, true);
    }

    @Test(expectedExceptions = FileNotFoundException.class)
    public void testUpdateError() throws Exception {
        InstallOptions installOptions = new InstallOptions();
        Version version = Version.valueOf("1.0.1");

        doThrow(FileNotFoundException.class).when(downloadManager).getDownloadedVersions(cdecArtifact);

        installationManagerFacade.update(cdecArtifact, version, installOptions);
    }

    @Test
    public void testAddNode() throws IOException {
        doReturn(new NodeConfig(NodeConfig.NodeType.BUILDER, "builder.node.com")).when(nodeManager).add("builder.node.com");
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("3.0.0"))).when(installManager).getInstalledArtifacts();
        assertEquals(toJson(installationManagerFacade.addNode("builder.node.com")), "{\n" +
                                                                                    "  \"type\" : \"BUILDER\",\n" +
                                                                                    "  \"host\" : \"builder.node.com\"\n" +
                                                                                    "}");
    }


    @Test(expectedExceptions = IOException.class)
    public void testAddNodeException() throws IOException {
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("3.0.0"))).when(installManager).getInstalledArtifacts();
        doThrow(new IOException("error")).when(nodeManager).add("builder.node.com");

        installationManagerFacade.addNode("builder.node.com");
    }

//    @Test(expectedExceptions = IllegalStateException.class,
//          expectedExceptionsMessageRegExp = "Codenvy License is invalid or has unappropriated format.")
    public void testAddNodeShouldFailedWhenLicenseInvalid() throws IOException {
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("4.0.0"))).when(installManager).getInstalledArtifacts();
        doThrow(new InvalidLicenseException("error")).when(codenvyLicenseManager).load();

        installationManagerFacade.addNode("builder.node.com");

        verify(nodeManager, never()).add(anyString());
    }

//    @Test(expectedExceptions = IllegalStateException.class,
//          expectedExceptionsMessageRegExp = "Your Codenvy subscription only allows a single server.")
    public void testAddNodeShouldFailedWhenLicenseNotFound() throws IOException {
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("4.0.0"))).when(installManager).getInstalledArtifacts();
        doThrow(new LicenseNotFoundException("error")).when(codenvyLicenseManager).load();

        installationManagerFacade.addNode("builder.node.com");

        verify(nodeManager, never()).add(anyString());
    }

//    @Test(expectedExceptions = IllegalStateException.class)
    public void testAddNodeShouldFailedWhenEvaluationKeyExpired() throws IOException {
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("4.0.0"))).when(installManager).getInstalledArtifacts();
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(Boolean.TRUE).when(codenvyLicense).isExpired();
        doReturn(CodenvyLicense.LicenseType.EVALUATION_PRODUCT_KEY).when(codenvyLicense).getLicenseType();

        installationManagerFacade.addNode("builder.node.com");

        verify(nodeManager, never()).add(anyString());
    }

    @Test
    public void testAddNodeShouldPassWhenProductKeyExpired() throws IOException {
        doReturn(new NodeConfig(NodeConfig.NodeType.BUILDER, "builder.node.com")).when(nodeManager).add("builder.node.com");
        doReturn(ImmutableMap.of(cdecArtifact, Version.valueOf("4.0.0"))).when(installManager).getInstalledArtifacts();
        doReturn(codenvyLicense).when(codenvyLicenseManager).load();
        doReturn(CodenvyLicense.LicenseType.PRODUCT_KEY).when(codenvyLicense).getLicenseType();
        doReturn(Boolean.TRUE).when(codenvyLicense).isExpired();

        installationManagerFacade.addNode("builder.node.com");

        verify(nodeManager).add(anyString());
    }

    @Test
    public void testRemoveNode() throws IOException {
        final String TEST_NODE_DNS = "builder.node.com";
        final NodeConfig TEST_NODE = new NodeConfig(NodeConfig.NodeType.BUILDER, TEST_NODE_DNS);
        doReturn(TEST_NODE).when(nodeManager).remove(TEST_NODE_DNS);

        assertEquals(toJson(installationManagerFacade.removeNode(TEST_NODE_DNS)), "{\n" +
                                                                                  "  \"type\" : \"BUILDER\",\n" +
                                                                                  "  \"host\" : \"builder.node.com\"\n" +
                                                                                  "}");
    }

    @Test(expectedExceptions = IOException.class)
    public void testRemoveNodeException() throws IOException {
        final String TEST_NODE_DNS = "builder.node.com";
        doThrow(new IOException("error")).when(nodeManager).remove(TEST_NODE_DNS);

        installationManagerFacade.removeNode(TEST_NODE_DNS);
    }

    @Test
    public void testBackup() throws IOException {
        Path testBackupDirectory = Paths.get("test/backup/directory");
        Path testBackupFile = testBackupDirectory.resolve("backup.tar.gz");
        BackupConfig testBackupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                          .setBackupDirectory(testBackupDirectory.toString());

        doReturn(testBackupConfig.setBackupFile(testBackupFile.toString()).setArtifactVersion("1.0.0"))
                .when(backupManager).backup(testBackupConfig);

        assertEquals(toJson(installationManagerFacade.backup(testBackupConfig)), "{\n" +
                                                                                 "  \"file\" : \"test/backup/directory/backup.tar.gz\",\n" +
                                                                                 "  \"artifact\" : \"codenvy\",\n" +
                                                                                 "  \"version\" : \"1.0.0\"\n" +
                                                                                 "}");
    }


    @Test(expectedExceptions = IOException.class)
    public void testBackupException() throws IOException {
        String testBackupDirectory = "test/backup/directory";
        BackupConfig testBackupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                          .setBackupDirectory(testBackupDirectory);

        doThrow(new IOException("error")).when(backupManager).backup(testBackupConfig);

        installationManagerFacade.backup(testBackupConfig);
    }

    @Test
    public void testRestore() throws IOException {
        String testBackupFile = "test/backup/directory/backup.tar.gz";
        BackupConfig testBackupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                          .setBackupFile(testBackupFile);

        assertEquals(toJson(installationManagerFacade.restore(testBackupConfig)), "{\n" +
                                                                                  "  \"file\" : \"test/backup/directory/backup.tar.gz\",\n" +
                                                                                  "  \"artifact\" : \"codenvy\"\n" +
                                                                                  "}");
    }


    @Test(expectedExceptions = IOException.class)
    public void testRestoreException() throws IOException {
        String testBackupFile = "test/backup/directory/backup.tar.gz";
        BackupConfig testBackupConfig = new BackupConfig().setArtifactName(CDECArtifact.NAME)
                                                          .setBackupFile(testBackupFile);

        doThrow(new IOException("error")).when(backupManager).restore(testBackupConfig);

        installationManagerFacade.restore(testBackupConfig);
    }

    @Test
    public void testLoginToSaas() throws Exception {
        Credentials credentials = DtoFactory.newDto(Credentials.class);

        installationManagerFacade.loginToCodenvySaaS(credentials);

        verify(saasAuthServiceProxy).login(credentials);
    }

    @Test
    public void testLoginFromSaas() throws Exception {
        installationManagerFacade.logoutFromCodenvySaaS("token");

        verify(saasAuthServiceProxy).logout("token");
    }

    @Test
    public void testLoginToSaasWhenTokenNull() throws Exception {
        final String TEST_USER_NAME = "user";
        final String TEST_USER_PASSWORD = "password";
        final String TEST_CREDENTIALS_JSON = "{\n"
                                             + "  \"username\": \"" + TEST_USER_NAME + "\",\n"
                                             + "  \"password\": \"" + TEST_USER_PASSWORD + "\"\n"
                                             + "}";

        Credentials testSaasUsernameAndPassword = Commons.createDtoFromJson(TEST_CREDENTIALS_JSON, Credentials.class);

        Object body = new JsonStringMapImpl<>(ImmutableMap.of("username", TEST_USER_NAME,
                                                              "password", TEST_USER_PASSWORD));

        doReturn(null).when(transport).doPost("api/endpoint/auth/login", body);

        Token token = installationManagerFacade.loginToCodenvySaaS(testSaasUsernameAndPassword);
        assertNull(token);
    }

    @Test
    public void testChangeAdminPassword() throws Exception {
        byte[] curPwd = "curPassword".getBytes("UTF-8");
        byte[] newPwd = "newPassword".getBytes("UTF-8");

        installationManagerFacade.changeAdminPassword(curPwd, newPwd);
        verify(ldapManager).changeAdminPassword(curPwd, newPwd);
    }

    @Test
    public void testStoreProperties() throws Exception {
        Map<String, String> properties = ImmutableMap.of("x", "y");

        installationManagerFacade.storeStorageProperties(properties);

        verify(storageManager).storeProperties(properties);
    }

    @Test
    public void testLoadProperties() throws Exception {
        installationManagerFacade.loadStorageProperties();
        verify(storageManager).loadProperties();
    }

    @Test
    public void testLoadProperty() throws Exception {
        String key = "x";

        installationManagerFacade.loadStorageProperty(key);
        verify(storageManager).loadProperty(key);
    }

    @Test
    public void testStoreProperty() throws Exception {
        String key = "x";
        String value = "y";

        installationManagerFacade.storeStorageProperty(key, value);
        verify(storageManager).storeProperty(key, value);
    }

    @Test
    public void testDeleteProperty() throws Exception {
        String key = "x";

        installationManagerFacade.deleteStorageProperty(key);
        verify(storageManager).deleteProperty(key);
    }

    @Test
    public void testGetConfig() throws Exception {
        doReturn("update/endpoint").when(installationManagerFacade).extractServerUrl("update/endpoint");
        doReturn("saas/endpoint").when(installationManagerFacade).extractServerUrl("saas/endpoint");
        Map<String, String> m = installationManagerFacade.getInstallationManagerProperties();
        assertEquals(m.size(), 3);
        assertTrue(m.containsValue("target/download"));
        assertTrue(m.containsValue("update/endpoint"));
        assertTrue(m.containsValue("saas/endpoint"));
    }

    @Test
    public void updateArtifactConfig() throws IOException {
        Map<String, String> properties = ImmutableMap.of("a", "b");
        doNothing().when(installationManagerFacade).doUpdateArtifactConfig(cdecArtifact, properties);

        installationManagerFacade.updateArtifactConfig(cdecArtifact, properties);

        verify(installationManagerFacade).doUpdateArtifactConfig(cdecArtifact, properties);
    }

    @Test(expectedExceptions = IOException.class)
    public void updateArtifactConfigWhenError() throws Exception {
        Map<String, String> properties = ImmutableMap.of("a", "b");
        prepareSingleNodeEnv(configManager);
        doThrow(IOException.class).when(installationManagerFacade).doUpdateArtifactConfig(cdecArtifact, properties);

        installationManagerFacade.updateArtifactConfig(cdecArtifact, properties);
    }

    @Test
    public void testGetUpdates() throws Exception {
        final Version version100 = Version.valueOf("1.0.0");
        when(downloadManager.getUpdates()).thenReturn(new LinkedHashMap<Artifact, Version>() {
            {
                put(cdecArtifact, version100);
            }
        });

        when(downloadManager.getDownloadedVersions(cdecArtifact)).thenReturn(new TreeMap<Version, Path>() {{
            put(version100, null);
        }});

        Collection<UpdateArtifactInfo> updates = installationManagerFacade.getUpdates();
        assertEquals(updates.size(), 1);

        UpdateArtifactInfo info = updates.iterator().next();
        assertEquals(info.getStatus(), UpdateArtifactInfo.Status.DOWNLOADED);
        assertEquals(info.getArtifact(), cdecArtifact.getName());
        assertEquals(info.getVersion(), version100.toString());
    }

    @Test
    public void testAllGetUpdates() throws Exception {
        doReturn(new ArrayList<Map.Entry<Artifact, Version>>() {{
            add(new AbstractMap.SimpleEntry<>(cdecArtifact, Version.valueOf("1.0.1")));
            add(new AbstractMap.SimpleEntry<>(cdecArtifact, Version.valueOf("1.0.2")));
        }}).when(downloadManager).getAllUpdates(cdecArtifact);

        doReturn(new TreeMap<Version, Path>() {{
            put(Version.valueOf("1.0.1"), Paths.get("file1"));
        }}).when(downloadManager).getDownloadedVersions(cdecArtifact);

        Collection<UpdateArtifactInfo> updates = installationManagerFacade.getAllUpdates(cdecArtifact);
        assertEquals(updates.size(), 2);

        Iterator<UpdateArtifactInfo> iter = updates.iterator();

        UpdateArtifactInfo info = iter.next();
        assertEquals(info.getVersion(), "1.0.1");
        assertEquals(info.getArtifact(), cdecArtifact.getName());
        assertEquals(info.getStatus(), UpdateArtifactInfo.Status.DOWNLOADED);

        info = iter.next();
        assertEquals(info.getVersion(), "1.0.2");
        assertEquals(info.getArtifact(), cdecArtifact.getName());
        assertEquals(info.getStatus(), UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
    }

    @Test
    public void testGetArtifacts() throws Exception {
        doReturn(new HashMap<Artifact, Version>() {{
            put(cdecArtifact, Version.valueOf("1.0.1"));
        }}).when(installManager).getInstalledArtifacts();

        doReturn(new LinkedHashMap<Artifact, SortedMap<Version, Path>>() {{
            put(cdecArtifact, new TreeMap<Version, Path>() {{
                put(Version.valueOf("1.0.0"), Paths.get("target/file1"));
                put(Version.valueOf("1.0.1"), Paths.get("target/file2"));
                put(Version.valueOf("1.0.2"), Paths.get("target/file3"));
            }});
        }}).when(downloadManager).getDownloadedArtifacts();

        doReturn(true).when(installManager).isInstallable(cdecArtifact, Version.valueOf("1.0.2"));

        Collection<ArtifactInfo> artifacts = installationManagerFacade.getArtifacts();

        assertEquals(artifacts.size(), 3);

        Iterator<ArtifactInfo> iterator = artifacts.iterator();
        ArtifactInfo info = iterator.next();
        assertEquals(info.getArtifact(), "codenvy");
        assertEquals(info.getVersion(), "1.0.2");
        assertEquals(info.getStatus(), ArtifactInfo.Status.READY_TO_INSTALL);

        info = iterator.next();
        assertEquals(info.getArtifact(), "codenvy");
        assertEquals(info.getVersion(), "1.0.1");
        assertEquals(info.getStatus(), ArtifactInfo.Status.INSTALLED);

        info = iterator.next();
        assertEquals(info.getArtifact(), "codenvy");
        assertEquals(info.getVersion(), "1.0.0");
        assertEquals(info.getStatus(), ArtifactInfo.Status.DOWNLOADED);
    }

    @Test
    public void testWaitForInstallStepCompleted() throws Exception {
        installationManagerFacade.waitForInstallStepCompleted("id");

        verify(installManager).waitForStepCompleted("id");
    }

    @Test
    public void testGetInstallStepInfo() throws Exception {
        installationManagerFacade.getUpdateStepInfo("id");

        verify(installManager).getUpdateStepInfo("id");
    }

    @Test
    public void testReinstallCodenvy() throws IOException {
        installationManagerFacade.reinstall(cdecArtifact);
        verify(installManager).performReinstall(cdecArtifact);
    }

    @Test
    public void testLogSaasAnalyticsEvent() throws Exception {
        Map<String, String> params = ImmutableMap.of("a", "b");
        Event event = new Event(Event.Type.CDEC_FIRST_LOGIN, params);

        String token = "token";

        installationManagerFacade.logSaasAnalyticsEvent(event, token);
        verify(saasRepositoryServiceProxy).logAnalyticsEvent(event, token);
    }

    @Test
    public void testGetCodenvyNodes() throws Exception {
        final ImmutableMap<String, ImmutableList<String>> testNodes = ImmutableMap.of(Config.SWARM_NODES, ImmutableList.of("node1.test.com"));
        doReturn(testNodes).when(nodeManager).getNodes();

        Map result = installationManagerFacade.getNodes();
        assertEquals(result, testNodes);
    }

    @Test
    public void testGetNumberOfUsers() throws Exception {
        final long testUsers = 3L;
        doReturn(testUsers).when(ldapManager).getNumberOfUsers();
        long result = installationManagerFacade.getNumberOfUsers();
        assertEquals(result, testUsers);
    }
}
