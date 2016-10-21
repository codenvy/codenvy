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
import com.codenvy.im.artifacts.VersionLabel;
import com.codenvy.im.managers.BackupManager;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.managers.InstallManager;
import com.codenvy.im.managers.LdapManager;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.StorageManager;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.saas.SaasAuthServiceProxy;
import com.codenvy.im.saas.SaasRepositoryServiceProxy;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertEquals;

public class IMArtifactLabeledFacadeTest extends BaseTest {

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

    private IMArtifactLabeledFacade         facade;
    private ImmutableMap<Artifact, Version> versions;
    private Artifact                        artifact;
    private Version                         version;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);

        artifact = ArtifactFactory.createArtifact(CDECArtifact.NAME);
        version = Version.valueOf("1.0.1");

        versions = ImmutableMap.of(artifact, version);

        facade = spy(new IMArtifactLabeledFacade(SAAS_API_ENDPOINT,
                                                 transport,
                                                 configManager,
                                                 saasAuthServiceProxy,
                                                 saasRepositoryServiceProxy,
                                                 ldapManager,
                                                 nodeManager,
                                                 backupManager,
                                                 storageManager,
                                                 installManager,
                                                 downloadManager));
    }

    @Test
    public void testGetInstalledVersions() throws Exception {
        doReturn(versions).when(installManager).getInstalledArtifacts();
        doReturn(VersionLabel.STABLE).when(facade).fetchVersionLabel("codenvy", "1.0.1");

        Collection<InstallArtifactInfo> installedVersions = facade.getInstalledVersions();

        assertEquals(installedVersions.size(), 1);

        InstallArtifactInfo installArtifactInfo = installedVersions.iterator().next();
        assertEquals(installArtifactInfo.getArtifact(), "codenvy");
        assertEquals(installArtifactInfo.getVersion(), "1.0.1");
        assertEquals(installArtifactInfo.getStatus(), InstallArtifactInfo.Status.SUCCESS);
        assertEquals(installArtifactInfo.getLabel(), VersionLabel.STABLE);
    }

    @Test
    public void testGetUpdates() throws Exception {
        doReturn(versions).when(downloadManager).getUpdates();
        doReturn(new TreeMap<Version, Path>() {{
            put(version, Paths.get("path"));
        }}).when(downloadManager).getDownloadedVersions(artifact);
        doReturn(VersionLabel.STABLE).when(facade).fetchVersionLabel("codenvy", "1.0.1");

        Collection<UpdateArtifactInfo> updates = facade.getUpdates();

        assertEquals(updates.size(), 1);

        UpdateArtifactInfo result = updates.iterator().next();
        assertEquals(result.getArtifact(), "codenvy");
        assertEquals(result.getVersion(), "1.0.1");
        assertEquals(result.getStatus(), UpdateArtifactInfo.Status.DOWNLOADED);
        assertEquals(result.getLabel(), VersionLabel.STABLE);
    }


    @Test
    public void testGetAllUpdates() throws Exception {
        doReturn(new ArrayList<Map.Entry<Artifact, Version>>() {{
            add(new AbstractMap.SimpleEntry<>(artifact, version));
        }}).when(downloadManager).getAllUpdates(artifact, true);
        doReturn(new TreeMap<Version, Path>() {{
            put(version, Paths.get("path"));
        }}).when(downloadManager).getDownloadedVersions(artifact);
        doReturn(VersionLabel.UNSTABLE).when(facade).fetchVersionLabel("codenvy", "1.0.1");

        Collection<UpdateArtifactInfo> updates = facade.getAllUpdatesAfterInstalledVersion(artifact);

        assertEquals(updates.size(), 1);

        UpdateArtifactInfo result = updates.iterator().next();
        assertEquals(result.getArtifact(), "codenvy");
        assertEquals(result.getVersion(), "1.0.1");
        assertEquals(result.getStatus(), UpdateArtifactInfo.Status.DOWNLOADED);
        assertEquals(result.getLabel(), VersionLabel.UNSTABLE);
    }

    @Test
    public void testGetDownloads() throws Exception {
        doReturn(new HashMap<Artifact, SortedMap<Version, Path>>() {{
            put(artifact, new TreeMap<Version, Path>() {{
                put(version, Paths.get("path"));
            }});
        }}).when(downloadManager).getDownloadedArtifacts();
        doReturn(VersionLabel.STABLE).when(facade).fetchVersionLabel("codenvy", "1.0.1");

        Collection<DownloadArtifactInfo> downloads = facade.getDownloads(artifact, version);

        assertEquals(downloads.size(), 1);

        DownloadArtifactInfo result = downloads.iterator().next();
        assertEquals(result.getArtifact(), "codenvy");
        assertEquals(result.getVersion(), "1.0.1");
        assertEquals(result.getStatus(), DownloadArtifactInfo.Status.DOWNLOADED);
        assertEquals(result.getLabel(), VersionLabel.STABLE);
        assertEquals(result.getFile(), "path");
    }
}
