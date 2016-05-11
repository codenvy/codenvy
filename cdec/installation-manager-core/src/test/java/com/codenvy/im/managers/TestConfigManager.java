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
import com.codenvy.im.artifacts.ArtifactFactory;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.commands.SimpleCommand;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Dmytro Nochevnov
 */
public class TestConfigManager extends BaseTest {

    private ConfigManager spyConfigManager;
    private HttpTransport transport;
    private Artifact spyCdec;

    @BeforeMethod
    public void setUp() throws Exception {
        transport = mock(HttpTransport.class);
        spyConfigManager = spy(new ConfigManager(UPDATE_API_ENDPOINT, PUPPET_BASE_DIR, transport));

        spyCdec = spy(ArtifactFactory.createArtifact(CDECArtifact.NAME));
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(spyCdec).getInstalledVersion();
    }

    @Test
    public void testConfigProperties() throws Exception {
        Path conf = Paths.get("target", "conf.properties");
        FileUtils.write(conf.toFile(), "user=1\npwd=2\n");

        Map<String, String> m = spyConfigManager.loadConfigProperties(conf);
        assertEquals(m.size(), 2);
        assertEquals(m.get("user"), "1");
        assertEquals(m.get("pwd"), "2");


        m = spyConfigManager.loadConfigProperties(conf.toAbsolutePath().toString());
        assertEquals(m.size(), 2);
        assertEquals(m.get("user"), "1");
        assertEquals(m.get("pwd"), "2");
    }

    @Test(expectedExceptions = FileNotFoundException.class, expectedExceptionsMessageRegExp = "Configuration file 'non-existed' not found")
    public void testLoadNonExistedConfigFile() throws IOException {
        spyConfigManager.loadConfigProperties("non-existed");
    }

    @Test(expectedExceptions = ConfigException.class, expectedExceptionsMessageRegExp = "Can't load properties: error")
    public void testLoadConfigFileWhichCantBeLoad() throws IOException {
        Path confFile = Paths.get("target", "conf.properties");
        FileUtils.write(confFile.toFile(), "user=1\npwd=2\n");

        doThrow(new IOException("error")).when(spyConfigManager).doLoadCodenvyProperties(any(Path.class));
        spyConfigManager.loadConfigProperties(confFile);
    }

    @Test
    public void testLoadDefaultSingleServerCdecConfig() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "a=1\n" +
                                             "b=2\n" +
                                             "c=\n" +
                                             "d=3\\n4\n");
        doReturn(properties).when(transport).download(endsWith("codenvy-single-server-properties/3.1.0"), any(Path.class));

        Map<String, String> m = spyConfigManager.loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.SINGLE_SERVER);
        assertEquals(m.size(), 4);
        assertEquals(m.get("a"), "1");
        assertEquals(m.get("b"), "2");
        assertEquals(m.get("c"), "");
        assertEquals(m.get("d"), "3\n4");
    }

    @Test
    public void testLoadDefaultMultiServerCdecConfig() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "a=1\n" +
                                             "b=\\$2\n" +
                                             "custom_ldap=false\n" +
                                             "\n" +
                                             "# ldap dn configurations\n" +
                                             "# those properties will be used only with default codenvy ldap i.e. $custom_ldap=\"false\"\n" +
                                             "user_ldap_dn=dc=codenvy-enterprise,dc=com\n" +
                                             "billing_resources_refill_cron=0 15 0 * * ?\n" +
                                             "admin_ldap_dn=dc=codenvycorp,dc=com\n");
        doReturn(properties).when(transport).download(endsWith("codenvy-multi-server-properties/3.1.0"), any(Path.class));

        Map<String, String> m = spyConfigManager.loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.MULTI_SERVER);
        assertEquals(m.size(), 6);
        assertEquals(m.get("a"), "1");
        assertEquals(m.get("b"), "\\$2");
        assertEquals(m.get("billing_resources_refill_cron"), "0 15 0 * * ?");
        assertEquals(m.get("custom_ldap"), "false");
        assertEquals(m.get("user_ldap_dn"), "dc=codenvy-enterprise,dc=com");
        assertEquals(m.get("admin_ldap_dn"), "dc=codenvycorp,dc=com");
    }

    @Test(expectedExceptions = IOException.class,
            expectedExceptionsMessageRegExp = "Can't download installation properties. error")
    public void testLoadDefaultCdecConfigTransportError() throws Exception {
        doThrow(new IOException("error")).when(transport).download(endsWith("codenvy-multi-server-properties/3.1.0"), any(Path.class));

        spyConfigManager.loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.MULTI_SERVER);
    }

    @Test(expectedExceptions = ConfigException.class, expectedExceptionsMessageRegExp = "Can't load properties: error")
    public void testLoadDefaultCdecConfigLoadError() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "a=1\n" +
                                             "b=2\n");
        doReturn(properties).when(transport).download(endsWith("codenvy-multi-server-properties/3.1.0"), any(Path.class));

        doThrow(new IOException("error")).when(spyConfigManager).doLoadCodenvyProperties(any(Path.class));

        spyConfigManager.loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.MULTI_SERVER);
    }

    @Test
    public void testMerge() throws Exception {
        Version curVersion = Version.valueOf("1.0.0");
        Map<String, String> curProps = ImmutableMap.of("a", "1", "b", "1");
        Map<String, String> newProps = ImmutableMap.of("a", "2", "b", "1", "c", "3");

        doReturn(InstallType.SINGLE_SERVER).when(spyConfigManager).detectInstallationType();
        doReturn(ImmutableMap.of("a", "1", "b", "1")).when(spyConfigManager).loadCodenvyDefaultProperties(curVersion, InstallType.SINGLE_SERVER);

        Map<String, String> m = spyConfigManager.merge(curProps, newProps);

        assertEquals(m.size(), 3);
        assertEquals(m.get("a"), "1");
        assertEquals(m.get("b"), "1");
        assertEquals(m.get("c"), "3");
    }

    @Test
    public void testLoadInstalledCodenvyProperties() throws Exception {
        Path properties = Paths.get("target/test.properties");
        FileUtils.write(properties.toFile(), "#\n" +
                                             "# Please finalize configurations by entering required values below:\n" +
                                             "#\n" +
                                             "# replace test.com placeholder with dns name of your single server installation.\n" +
                                             "# Note: DNS name that you configure must be later used as DNS name of the server where a single " +
                                             "server Codenvy Enterprise will be\n" +
                                             "# installed\n" +
                                             "node \"test.com\" inherits \"base_config\" {\n" +
                                             "  # enter dns name of your single server installation, same as above.\n" +
                                             "  $aio_host_url = \"test.com\"\n" +
                                             "  # $aio_host_url = \"codenvy.com\"\n" +
                                             "\n" +
                                             "  ###############################\n" +
                                             "  # Codenvy Builder configurations\n" +
                                             "  #\n" +
                                             "  $admin_ldap_password = \"$system_ldap_password\"\n" +
                                             "  # custom ldap\n" +
                                             "  # false by default which means that default ldap (installed on codenvy servers) will be used.\n" +
                                             "  # In in order to connect codenvy to any third-party ldap please set this to true and change any \n" +
                                             "  $custom_ldap = \"false\"\n" +
                                             "  \n" +
                                             "  # ldap dn configurations\n" +
                                             "  # those properties will be used only with default codenvy ldap i.e. $custom_ldap = \"false\"\n" +
                                             "  $user_ldap_dn = \"dc=codenvy-enterprise,dc=com\"\n" +
                                             "  $admin_ldap_dn = \"dc=codenvycorp,dc=com\"\n" +
                                             "  # (Mandatory) builder_max_execution_time -  max execution time in seconds for build process.\n" +
                                             "  # If process doesn't end before this time it may be terminated forcibly.\n" +
                                             "  $builder_max_execution_time = \"600\"\n" +
                                             "  $empty = \"\"\n" +
                                             "  #\n" +
                                             "  $node_ssh_user_private_key = \"-----BEGIN RSA PRIVATE KEY-----\n" +
                                             "aaasdf3adsfasfasfasfdsafsafasdfasdfasdfasfdasdfasdfasdfasdfasdff\n" +
                                             "\"\n" +
                                             "  $builder_base_directory=\"\\${catalina.base}/temp/builder\"\n" +
                                             "  #\n" +
                                             "  #\n");

        doReturn(ImmutableList.of(properties).iterator()).when(spyConfigManager)
                                                         .getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        Map<String, String> m = spyConfigManager.loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);
        assertEquals(m.size(), 9);
        assertEquals(m.get("aio_host_url"), "test.com");
        assertEquals(m.get("builder_max_execution_time"), "600");
        assertEquals(m.get("empty"), "");
        assertEquals(m.get("admin_ldap_password"), "$system_ldap_password");
        assertEquals(m.get("custom_ldap"), "false");
        assertEquals(m.get("user_ldap_dn"), "dc=codenvy-enterprise,dc=com");
        assertEquals(m.get("admin_ldap_dn"), "dc=codenvycorp,dc=com");
        assertEquals(m.get("builder_base_directory"), "\\${catalina.base}/temp/builder");
        assertEquals(m.get("node_ssh_user_private_key"),
                     "-----BEGIN RSA PRIVATE KEY-----\naaasdf3adsfasfasfasfdsafsafasdfasdfasdfasfdasdfasdfasdfasdfasdff\n");
    }

    @Test
    public void testLoadCodenvyPropertiesFromBinaries() throws Exception {
        createDirectories(Paths.get(TEST_DIR, "codenvy", "manifests", "nodes", "single_server"));

        Path parent = Paths.get(TEST_DIR, "codenvy");
        Path binaries = Paths.get(TEST_DIR, "binaries.zip");

        Path singleServerProps = parent.resolve(Config.SINGLE_SERVER_PP);
        Path singleServerBaseProps = parent.resolve(Config.SINGLE_SERVER_BASE_CONFIG_PP);

        createFile(singleServerProps);
        createFile(singleServerBaseProps);
        FileUtils.write(singleServerProps.toFile(), "$prop1 = \"1\"\n");
        FileUtils.write(singleServerBaseProps.toFile(), "$prop2 = \"2\"\n");

        SimpleCommand command = createCommand(format("cd %s; ", parent.toString())
                                              + format("zip -r %s .", binaries.toAbsolutePath().toString()));
        command.execute();

        Map<String, String> m = spyConfigManager.loadConfigProperties(binaries, InstallType.SINGLE_SERVER);

        assertEquals(m.size(), 2);
        assertEquals(m.get("prop1"), "1");
        assertEquals(m.get("prop2"), "2");
    }

    @Test(expectedExceptions = ConfigException.class)
    public void testLoadInstalledCodenvyPropertiesErrorIfFileAbsent() throws Exception {
        Path properties = Paths.get("target/unexisted");
        doReturn(ImmutableList.of(properties).iterator()).when(spyConfigManager)
                                                         .getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        spyConfigManager.loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);
        doReturn(ImmutableList.of(properties).iterator()).when(spyConfigManager)
                                                         .getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        spyConfigManager.loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);
    }

    @Test
    public void testGetSingleServerPropertiesFiles() throws Exception {
        prepareSingleNodeEnv(spyConfigManager, transport);

        Iterator<Path> singleServerCssPropertiesFiles = spyConfigManager.getCodenvyPropertiesFiles(InstallType.SINGLE_SERVER);
        assertTrue(singleServerCssPropertiesFiles.next().toAbsolutePath().toString()
                                                 .endsWith("target/puppet/manifests/nodes/single_server/base_config.pp"));
        assertTrue(singleServerCssPropertiesFiles.next().toAbsolutePath().toString()
                                                 .endsWith("target/puppet/manifests/nodes/single_server/single_server.pp"));
    }

    @Test
    public void testGetMultiServerPropertiesFiles() throws Exception {
        prepareMultiNodeEnv(spyConfigManager, transport);

        Iterator<Path> multiServerCssPropertiesFiles = spyConfigManager.getCodenvyPropertiesFiles(InstallType.MULTI_SERVER);
        assertTrue(multiServerCssPropertiesFiles.next().toAbsolutePath().toString()
                                                .endsWith("target/puppet/manifests/nodes/multi_server/base_configurations.pp"));
        assertTrue(multiServerCssPropertiesFiles.next().toAbsolutePath().toString()
                                                .endsWith("target/puppet/manifests/nodes/multi_server/custom_configurations.pp"));
    }

    @Test
    public void testGetPuppetNodesConfigReplacement() {
        List<NodeConfig> nodes = ImmutableList.of(
                new NodeConfig(NodeConfig.NodeType.API, "api.dev.com"),
                new NodeConfig(NodeConfig.NodeType.DATA, "data.dev.com"),
                new NodeConfig(NodeConfig.NodeType.BUILDER, "builder2.dev.com"),
                new NodeConfig(NodeConfig.NodeType.RUNNER, "runner23.runner89.com")
                                                 );

        Map<String, String> expected = ImmutableMap.of("builder\\\\d+\\\\.example.com", "builder\\\\d+\\\\.dev.com",
                                                       "runner\\\\d+\\\\.example.com", "runner\\\\d+\\\\.runner89.com",
                                                       "data.example.com", "data.dev.com",
                                                       "api.example.com", "api.dev.com");
        Map<String, String> actual = ConfigManager.getPuppetNodesConfigReplacement(nodes);

        assertEquals(actual, expected);
    }

    @Test
    public void testLoadInstalledCodenvyConfig() throws IOException {
        Map<String, String> properties = ImmutableMap.of("a", "1", "b", "2");
        doReturn(properties).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.MULTI_SERVER);

        Config result = spyConfigManager.loadInstalledCodenvyConfig(InstallType.MULTI_SERVER);
        assertEquals(result.getProperties().toString(), properties.toString());
    }

    @Test(expectedExceptions = UnknownInstallationTypeException.class)
    public void testDetectInstallationTypeErrorIfConfAbsent() throws Exception {
        spyConfigManager.detectInstallationType();
    }

    @Test
    public void testDetectInstallationMultiTypeFromPuppetConf() throws Exception {
        createMultiNodeConf();
        assertEquals(spyConfigManager.detectInstallationType(), InstallType.MULTI_SERVER);
    }

    @Test
    public void testDetectInstallationMultiTypeFromCodenvyConfig() throws Exception {
        doReturn(ImmutableMap.of(Config.CODENVY_INSTALL_TYPE, "multi_server")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.MULTI_SERVER);
        doReturn(ImmutableMap.of(Config.CODENVY_INSTALL_TYPE, "")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);

        assertEquals(spyConfigManager.detectInstallationType(), InstallType.MULTI_SERVER);
    }

    @Test
    public void testDetectInstallationSingleTypeFromPuppetConf() throws Exception {
        createSingleNodeConf();
        assertEquals(spyConfigManager.detectInstallationType(), InstallType.SINGLE_SERVER);
    }

    @Test
    public void testDetectInstallationSingleTypeFromCodenvyConfig() throws Exception {
        doReturn(ImmutableMap.of(Config.CODENVY_INSTALL_TYPE, "")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.MULTI_SERVER);
        doReturn(ImmutableMap.of(Config.CODENVY_INSTALL_TYPE, "single_server")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);

        assertEquals(spyConfigManager.detectInstallationType(), InstallType.SINGLE_SERVER);
    }

    @Test(expectedExceptions = IOException.class)
    public void testFetchMasterHostNameErrorIfFileAbsent() throws Exception {
        spyConfigManager.fetchMasterHostName();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFetchMasterHostNameErrorIfFileEmpty() throws Exception {
        doReturn(new Config(new HashMap<String, String>())).when(spyConfigManager).loadInstalledCodenvyConfig();

        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "");
        spyConfigManager.fetchMasterHostName();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFetchMasterHostNameErrorIfPropertyAbsent() throws Exception {
        doReturn(new Config(new HashMap<String, String>())).when(spyConfigManager).loadInstalledCodenvyConfig();

        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "[main]");
        spyConfigManager.fetchMasterHostName();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFetchMasterHostNameErrorIfValueEmpty() throws Exception {
        doReturn(new Config(new HashMap<String, String>())).when(spyConfigManager).loadInstalledCodenvyConfig();

        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "[main]\n" +
                                                            "   certname = ");
        spyConfigManager.fetchMasterHostName();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testFetchMasterHostNameErrorIfBadFormat() throws Exception {
        doReturn(new Config(new HashMap<String, String>())).when(spyConfigManager).loadInstalledCodenvyConfig();

        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "[main]\n" +
                                                            "    certname  bla.bla.com\n");
        spyConfigManager.fetchMasterHostName();
    }

    @Test
    public void testFetchMasterHostName() throws Exception {
        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "[main]\n" +
                                                            "certname=master.dev.com\n" +
                                                            "    hostprivkey= $privatekeydir/$certname.pem { mode = 640 }\n" +
                                                            "[agent]\n" +
                                                            "certname=la-la.com");
        assertEquals(spyConfigManager.fetchMasterHostName(), "master.dev.com");
    }

    @Test
    public void testFetchMasterHostNameUseCase2() throws Exception {
        FileUtils.write(BaseTest.PUPPET_CONF_FILE.toFile(), "[agent]\n" +
                                                            "certname=la-la.com\n" +
                                                            "[main]\n" +
                                                            "certname= master.dev.com\n" +
                                                            "    hostprivkey= $privatekeydir/$certname.pem { mode = 640 }\n");
        assertEquals(spyConfigManager.fetchMasterHostName(), "master.dev.com");
    }

    @Test
    public void testPrepareInstallPropertiesIMArtifact() throws Exception {
        Map<String, String> properties = spyConfigManager.prepareInstallProperties(null,
                                                                                null,
                                                                                null,
                                                                                ArtifactFactory.createArtifact(InstallManagerArtifact.NAME),
                                                                                null,
                                                                                true);
        assertTrue(properties.isEmpty());
    }

    @Test
    public void testPrepareInstallPropertiesLoadPropertiesFromConfigInstallUseCase() throws Exception {
        Map<String, String> properties = new HashMap<>(ImmutableMap.of("a", "b",
                                                                       Config.HTTP_PROXY_FOR_CODENVY, "",
                                                                       Config.HTTPS_PROXY_FOR_CODENVY, ""));

        doReturn(properties).when(spyConfigManager).loadConfigProperties("file");

        final String testHttpProxy = "http://proxy.net:8080";
        final String testHttpsProxy = "https://proxy.net:8080";
        final ImmutableMap<String, Object> environment = ImmutableMap.of(Config.HTTP_PROXY, testHttpProxy,
                                                                Config.HTTPS_PROXY, testHttpsProxy);
        doReturn(environment).when(spyConfigManager).getEnvironment();

        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties("file",
                                                                                      null,
                                                                                      InstallType.SINGLE_SERVER,
                                                                                      ArtifactFactory.createArtifact(CDECArtifact.NAME),
                                                                                      Version.valueOf("3.1.0"),
                                                                                      true);
        assertEquals(actualProperties.size(), 7);
        assertEquals(actualProperties.get("a"), "b");

        assertTrue(actualProperties.containsKey(Config.PRIVATE_KEY));
        assertTrue(actualProperties.containsKey(Config.PUBLIC_KEY));

        assertEquals(actualProperties.get(Config.HTTP_PROXY), testHttpProxy);
        assertEquals(actualProperties.get(Config.HTTPS_PROXY), testHttpsProxy);
    }

    @Test
    public void testPrepareInstallPropertiesLoadDefaultPropertiesInstallUseCase() throws Exception {
        Map<String, String> expectedProperties = new HashMap<>(ImmutableMap.of("a", "b"));

        doReturn(expectedProperties).when(spyConfigManager).loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.SINGLE_SERVER);

        final String testHttpProxy = "http://proxy.net:8080";
        final String testHttpsProxy = "https://proxy.net:8080";
        final ImmutableMap<String, Object> environment = ImmutableMap.of(Config.HTTP_PROXY, testHttpProxy,
                                                                         Config.HTTPS_PROXY, testHttpsProxy);
        doReturn(environment).when(spyConfigManager).getEnvironment();

        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties(null,
                                                                                      null,
                                                                                      InstallType.SINGLE_SERVER,
                                                                                      ArtifactFactory.createArtifact(CDECArtifact.NAME),
                                                                                      Version.valueOf("3.1.0"),
                                                                                      true);
        assertEquals(actualProperties.size(), 5);
        assertEquals(actualProperties.get("a"), "b");

        assertTrue(actualProperties.containsKey(Config.PRIVATE_KEY));
        assertTrue(actualProperties.containsKey(Config.PUBLIC_KEY));

        assertEquals(actualProperties.get(Config.HTTP_PROXY), testHttpProxy);
        assertEquals(actualProperties.get(Config.HTTPS_PROXY), testHttpsProxy);
    }

    @Test
    public void testPrepareInstallPropertiesIncludingHttpProxyForCodenvy() throws Exception {
        final String testHttpProxyForCodenvy = "http://proxy.for.codenvy.net:8080";
        final String testHttpsProxyForCodenvy = "https://proxy.for.codenvy.net:8080";

        Map<String, String> defaultProperties = new HashMap<>(ImmutableMap.of("a", "b",
                                                                               Config.HTTP_PROXY_FOR_CODENVY, testHttpProxyForCodenvy,
                                                                               Config.HTTPS_PROXY_FOR_CODENVY, testHttpsProxyForCodenvy));

        doReturn(defaultProperties).when(spyConfigManager).loadCodenvyDefaultProperties(Version.valueOf("3.1.0"), InstallType.SINGLE_SERVER);

        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties(null,
                                                                                         null,
                                                                                         InstallType.SINGLE_SERVER,
                                                                                         ArtifactFactory.createArtifact(CDECArtifact.NAME),
                                                                                         Version.valueOf("3.1.0"),
                                                                                         true);

        assertEquals(actualProperties.get("a"), "b");

        assertTrue(actualProperties.containsKey(Config.PRIVATE_KEY));
        assertTrue(actualProperties.containsKey(Config.PUBLIC_KEY));

        assertEquals(actualProperties.get(Config.HTTP_PROXY), testHttpProxyForCodenvy);
        assertEquals(actualProperties.get(Config.HTTPS_PROXY), testHttpsProxyForCodenvy);

        assertEquals(actualProperties.get(Config.HTTP_PROXY_FOR_CODENVY), testHttpProxyForCodenvy);
        assertEquals(actualProperties.get(Config.HTTPS_PROXY_FOR_CODENVY), testHttpsProxyForCodenvy);
    }

    @Test
    public void testPrepareInstallPropertiesLoadPropertiesFromConfigUpdateUseCase() throws Exception {
        Map<String, String> properties = new HashMap<>(ImmutableMap.of("a", "1"));

        Artifact artifact = mock(Artifact.class);
        doReturn(Optional.of(Version.valueOf("1.0.0"))).when(artifact).getInstalledVersion();
        doReturn(CDECArtifact.NAME).when(artifact).getName();
        doReturn(properties).when(spyConfigManager).loadConfigProperties("file");
        doReturn(ImmutableMap.of("b", "2")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);
        doReturn(new HashMap<String, String>() {{
            put("a", "1");
            put("b", "2");
            put(Config.VERSION, "1.0.0");
        }}).when(spyConfigManager).merge(anyMap(), anyMap());

        String version2Install = "3.1.0";
        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties("file",
                                                                                         null,
                                                                                         InstallType.SINGLE_SERVER,
                                                                                         artifact,
                                                                                         Version.valueOf(version2Install),
                                                                                         false);

        assertEquals(actualProperties.size(), 3);
        assertEquals(actualProperties.get("a"), "1");
        assertEquals(actualProperties.get("b"), "2");
        assertEquals(actualProperties.get(Config.VERSION), version2Install);
    }

    @Test
    public void testPrepareInstallPropertiesLoadDefaultPropertiesUpdateUseCase() throws Exception {
        Map<String, String> expectedProperties = new HashMap<>(ImmutableMap.of("a", "1"));

        doReturn(new HashMap<String, String>() {{
            put("a", "1");
            put("b", "2");
            put(Config.VERSION, "1.0.0");
        }}).when(spyConfigManager).merge(anyMap(), anyMap());
        final String version2Install = "3.1.0";
        doReturn(expectedProperties).when(spyConfigManager).loadCodenvyDefaultProperties(Version.valueOf(version2Install), InstallType.SINGLE_SERVER);
        doReturn(ImmutableMap.of("b", "2")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.SINGLE_SERVER);

        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties(null,
                                                                                      null,
                                                                                      InstallType.SINGLE_SERVER,
                                                                                      spyCdec,
                                                                                      Version.valueOf(version2Install),
                                                                                      false);

        assertEquals(actualProperties.size(), 3);
        assertEquals(actualProperties.get("a"), "1");
        assertEquals(actualProperties.get("b"), "2");
        assertEquals(actualProperties.get(Config.VERSION), version2Install);
    }

    @Test
    public void testPrepareInstallPropertiesLoadDefaultPropertiesUpdateMultiServerUseCase() throws Exception {
        Map<String, String> expectedProperties = new HashMap<>(ImmutableMap.of("a", "1"));

        doReturn(new HashMap<String, String>() {{
            put("a", "1");
            put("b", "2");
            put(Config.VERSION, "1.0.0");
        }}).when(spyConfigManager).merge(anyMap(), anyMap());

        final String version2Install = "3.1.0";
        doReturn(expectedProperties).when(spyConfigManager).loadCodenvyDefaultProperties(Version.valueOf(version2Install), InstallType.MULTI_SERVER);
        doReturn(ImmutableMap.of("b", "2")).when(spyConfigManager).loadInstalledCodenvyProperties(InstallType.MULTI_SERVER);
        doReturn("master").when(spyConfigManager).fetchMasterHostName();


        Map<String, String> actualProperties = spyConfigManager.prepareInstallProperties(null,
                                                                                      null,
                                                                                      InstallType.MULTI_SERVER,
                                                                                      spyCdec,
                                                                                      Version.valueOf(version2Install),
                                                                                      false);

        assertEquals(actualProperties.size(), 4);
        assertEquals(actualProperties.get("a"), "1");
        assertEquals(actualProperties.get("b"), "2");
        assertEquals(actualProperties.get(Config.PUPPET_MASTER_HOST_NAME), "master");
        assertEquals(actualProperties.get(Config.VERSION), version2Install);
    }

    @Test
    public void testGetApiEndpointSingleServer() throws Exception {
        doReturn(InstallType.SINGLE_SERVER).when(spyConfigManager).detectInstallationType();

        assertEquals(spyConfigManager.getApiEndpoint(), "http://localhost/api");
    }

    @Test
    public void testGetApiEndpointMultiServer() throws Exception {
        doReturn(InstallType.MULTI_SERVER).when(spyConfigManager).detectInstallationType();
        doReturn(new Config(ImmutableMap.of("host_protocol", "http", "host_url", "codenvy.onprem")))
                .when(spyConfigManager).loadInstalledCodenvyConfig(InstallType.MULTI_SERVER);

        assertEquals(spyConfigManager.getApiEndpoint(), "http://codenvy.onprem/api");
    }
}

