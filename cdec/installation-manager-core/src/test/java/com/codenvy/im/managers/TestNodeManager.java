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
import com.codenvy.im.artifacts.UnsupportedArtifactVersionException;
import com.codenvy.im.commands.Command;
import com.codenvy.im.managers.helper.NodeManagerHelper;
import com.codenvy.im.utils.HttpTransport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestNodeManager extends BaseTest {

    @Mock
    private ConfigManager           mockConfigManager;
    @Mock
    private Command                 mockCommand;
    @Mock
    private NodeManagerHelper       mockHelper;
    @Mock
    private HttpTransport           transport;
    @Mock
    private Codenvy4xLicenseManager codenvy4xLicenseManager;

    private static final String              TEST_NODE_DNS  = "localhost";
    private static final NodeConfig.NodeType TEST_NODE_TYPE = NodeConfig.NodeType.RUNNER;
    private static final NodeConfig          TEST_NODE      = new NodeConfig(TEST_NODE_TYPE, TEST_NODE_DNS);

    private static final String ADDITIONAL_RUNNERS_PROPERTY_NAME = "additional_runners";

    private NodeManager spyManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        spyManager = spy(new NodeManager(mockConfigManager, transport, codenvy4xLicenseManager));

        doReturn(mockHelper).when(spyManager).getHelper();

        doReturn(ImmutableList.of(Paths.get("/etc/puppet/" + Config.MULTI_SERVER_CUSTOM_CONFIG_PP),
                                  Paths.get("/etc/puppet/" + Config.MULTI_SERVER_BASE_CONFIG_PP)).iterator())
            .when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);
        doReturn(ImmutableList.of(Paths.get("/etc/puppet/" + Config.SINGLE_SERVER_BASE_CONFIG_PP),
                                  Paths.get("/etc/puppet/" + Config.SINGLE_SERVER_PP)).iterator())
            .when(mockConfigManager).getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);

        initConfigs();
    }

    private void initConfigs() throws IOException {
        doReturn(ADDITIONAL_RUNNERS_PROPERTY_NAME).when(mockHelper).getPropertyNameBy(TEST_NODE_TYPE);
    }

    @Test
    public void testAddNode() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);

        doNothing().when(mockHelper).validate(TEST_NODE, HOSTNAME);
        doReturn(TEST_NODE).when(mockHelper).recognizeNodeConfigFromDns(TEST_NODE_DNS);
        doReturn(mockCommand).when(mockHelper)
                             .getAddNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);
        doNothing().when(mockHelper).validateLicense();
        doReturn(HOSTNAME).when(mockConfigManager).fetchMasterHostName();

        assertEquals(spyManager.add(TEST_NODE_DNS), TEST_NODE);
        verify(mockHelper).validate(TEST_NODE, HOSTNAME);
        verify(mockCommand).execute();
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "This type of node isn't supported")
    public void testAddNodeWhichIsNotSupported() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);

        doReturn(TEST_NODE).when(mockHelper).recognizeNodeConfigFromDns(TEST_NODE_DNS);
        doReturn(null).when(mockHelper).getPropertyNameBy(TEST_NODE.getType());
        doNothing().when(mockHelper).validateLicense();

        spyManager.add(TEST_NODE_DNS);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "error")
    public void testAddNodeWhenWrongInstallTypeException() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);
        doThrow(new IllegalStateException("error")).when(mockHelper).checkInstallType();
        spyManager.add(TEST_NODE_DNS);
    }

    @Test
    public void testRemoveNode() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);

        doReturn(TEST_NODE_TYPE).when(mockHelper).recognizeNodeTypeFromConfigBy(TEST_NODE_DNS);
        doReturn(mockCommand).when(mockHelper)
                             .getRemoveNodeCommand(TEST_NODE, ADDITIONAL_RUNNERS_PROPERTY_NAME);
        doReturn(TEST_NODE).when(mockHelper).recognizeNodeConfigFromDns(TEST_NODE_DNS);

        assertEquals(spyManager.remove(TEST_NODE_DNS), TEST_NODE);
        verify(mockCommand).execute();
    }

    @Test
    public void testUpdatePuppetConfig() throws Exception {
        final String oldHostName = "hostname";
        final String newHostName = "new.hostname";

        doReturn(new Config(ImmutableMap.of(Config.VERSION, "4.0.0")))
            .when(mockConfigManager).loadInstalledCodenvyConfig();

        doReturn(mockCommand).when(mockHelper).getUpdatePuppetConfigCommand(oldHostName, newHostName);

        spyManager.updatePuppetConfig(oldHostName, newHostName);
        verify(mockCommand).execute();
    }

    @Test(expectedExceptions = NodeException.class,
        expectedExceptionsMessageRegExp = "Node 'localhost' is not found in Codenvy configuration")
    public void testRemoveNonExistsNodeError() throws Exception {
        prepareMultiNodeEnv(mockConfigManager);
        doReturn(null).when(mockHelper)
                      .recognizeNodeTypeFromConfigBy(TEST_NODE_DNS);

        spyManager.remove(TEST_NODE_DNS);
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "error")
    public void testRemoveNodeWhenWrongInstallTypeException() throws Exception {
        prepareSingleNodeEnv(mockConfigManager);
        doThrow(new IllegalStateException("error")).when(mockHelper).checkInstallType();
        spyManager.remove(TEST_NODE_DNS);
    }

    @Test(expectedExceptions = UnsupportedArtifactVersionException.class,
        expectedExceptionsMessageRegExp = "Version '1.0.0' of artifact 'codenvy' is not supported")
    public void shouldThrowUnsupportedArtifactVersionExceptionWhenAdd() throws Exception {
        NodeManager manager = new NodeManager(mockConfigManager, transport, codenvy4xLicenseManager);
        doReturn(new Config(ImmutableMap.of(Config.VERSION, UNSUPPORTED_VERSION)))
            .when(mockConfigManager).loadInstalledCodenvyConfig();

        manager.add(TEST_NODE_DNS);
    }

    @Test(expectedExceptions = UnsupportedArtifactVersionException.class,
        expectedExceptionsMessageRegExp = "Version '1.0.0' of artifact 'codenvy' is not supported")
    public void shouldThrowUnsupportedArtifactVersionExceptionWhenRemove() throws Exception {
        NodeManager manager = new NodeManager(mockConfigManager, transport, codenvy4xLicenseManager);
        doReturn(new Config(ImmutableMap.of(Config.VERSION, UNSUPPORTED_VERSION)))
            .when(mockConfigManager).loadInstalledCodenvyConfig();

        manager.remove(TEST_NODE_DNS);
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "System License error")
    public void testAddNodeShouldFailedWhenLicenseInvalid() throws Exception {
        doThrow(new IllegalStateException("System License error")).when(mockHelper).validateLicense();
        spyManager.add(TEST_NODE_DNS);
        verify(mockHelper, never()).getAddNodeCommand(any(NodeConfig.class), anyString());
    }
}
