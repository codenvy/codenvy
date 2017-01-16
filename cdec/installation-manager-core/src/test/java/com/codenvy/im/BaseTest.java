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
package com.codenvy.im;

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.OSUtils;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * @author Anatoliy Bazko
 */
public class BaseTest {
    public static final String BASE_DIR            = format("%s/codenvy", System.getProperty("user.home"));
    public static final String PUPPET_BASE_DIR     = "target/puppet";
    public static final String TEST_DIR            = "target";
    public static final String DOWNLOAD_DIR        = "target/updates";
    public static final String CODENVY_IM_BASE_DIR = "target";
    public static final String UPDATE_API_ENDPOINT = "http://update.endpoint";
    public static final String ASSEMBLY_PROPERTIES = "target/assembly.properties";
    public static final String API_ENDPOINT        = "http://api.endpoint";
    public static final String SAAS_API_ENDPOINT   = "http://saas.api.endpoint";
    public static final Path   PUPPET_CONF_FILE    = Paths.get("target", "puppet", Config.PUPPET_CONF_FILE_NAME).toAbsolutePath();
    public static final String TEST_VERSION_STR    = "3.3.0";
    public static final String INITIAL_OS_VERSION  = OSUtils.VERSION;
    public static final String HOSTNAME            = "hostname";
    public static final String UNSUPPORTED_VERSION = "1.0.0";
    public static final String SYSTEM_USER_NAME    = System.getProperty("user.name");

    @BeforeMethod
    public void clear() throws Exception {
        if (exists(PUPPET_CONF_FILE)) {
            delete(PUPPET_CONF_FILE);
        }

        Path props = Paths.get(ASSEMBLY_PROPERTIES);
        if (exists(props)) {
            delete(props);
        }

        FileUtils.deleteDirectory(Paths.get(DOWNLOAD_DIR).toFile());
        FileUtils.deleteDirectory(Paths.get(PUPPET_BASE_DIR).toFile());
    }

    @BeforeClass
    public void setupClass() {
        OSUtils.VERSION = "7";
    }

    protected void createSingleNodeConf() throws Exception {
        FileUtils.writeStringToFile(Paths.get(PUPPET_BASE_DIR, Config.SINGLE_SERVER_PP).toFile(), "");
        FileUtils.writeStringToFile(Paths.get(PUPPET_BASE_DIR, Config.SINGLE_SERVER_BASE_CONFIG_PP).toFile(), "");
        FileUtils.writeStringToFile(PUPPET_CONF_FILE.toFile(), "[master]\n" +
                                                               "    certname = hostname\n" +
                                                               "[main]\n" +
                                                               "  dns_alt_names = puppet\n" +
                                                               "[agent]\n" +
                                                               "  show_diff = true\n" +
                                                               "  pluginsync = true\n" +
                                                               "  report = false\n" +
                                                               "  default_schedules = false\n" +
                                                               "  certname = %s\n" +
                                                               "  runinterval = 300\n" +
                                                               "  configtimeout = 600\n");
    }

    protected void createMultiNodeConf() throws Exception {
        FileUtils.writeStringToFile(Paths.get(PUPPET_BASE_DIR, Config.MULTI_SERVER_BASE_CONFIG_PP).toFile(), "");
        FileUtils.writeStringToFile(Paths.get(PUPPET_BASE_DIR, Config.MULTI_SERVER_CUSTOM_CONFIG_PP).toFile(), "");
        FileUtils.writeStringToFile(PUPPET_CONF_FILE.toFile(), "[main]\n"
                                                               + " server = master.codenvy.onprem\n"
                                                               + " runinterval = 420\n"
                                                               + " configtimeout = 600\n"
                                                               + "\n"
                                                               + "   # The Puppet log directory.\n"
                                                               + "   # The default value is '$vardir/log'.\n"
                                                               + "   logdir = /var/log/puppet\n"
                                                               + "\n"
                                                               + "   # Where Puppet PID files are kept.\n"
                                                               + "   # The default value is '$vardir/run'.\n"
                                                               + "   rundir = /var/run/puppet\n"
                                                               + "\n"
                                                               + "   # Where SSL certificates are kept.\n"
                                                               + "   # The default value is '$confdir/ssl'.\n"
                                                               + "   ssldir = $vardir/ssl\n"
                                                               + "\n"
                                                               + "[master]\n"
                                                               + " certname = master.codenvy.onprem\n"
                                                               + "\n"
                                                               + "[agent]\n"
                                                               + " show_diff = true\n"
                                                               + " pluginsync = true\n"
                                                               + " report = false\n"
                                                               + " default_schedules = false\n"
                                                               + " certname = master.codenvy.onprem\n"
                                                               + "\n"
                                                               + "   # The file in which puppetd stores a list of the classes\n"
                                                               + "   # associated with the retrieved configuratiion.  Can be loaded in\n"
                                                               + "   # the separate ``puppet`` executable using the ``--loadclasses``\n"
                                                               + "   # option.\n"
                                                               + "   # The default value is '$confdir/classes.txt'.\n"
                                                               + "   classfile = $vardir/classes.txt\n"
                                                               + "\n"
                                                               + "   # Where puppetd caches the local configuration.  An\n"
                                                               + "   # extension indicating the cache format is added automatically.\n"
                                                               + "   # The default value is '$confdir/localconfig'.\n"
                                                               + "   localconfig = $vardir/localconfig\n");
    }

    private void createAssemblyProperty() throws IOException {
        File assemblyPropertiesFile = Paths.get(ASSEMBLY_PROPERTIES).toFile();
        FileUtils.deleteQuietly(assemblyPropertiesFile);
        FileUtils.writeStringToFile(assemblyPropertiesFile, "assembly.version=" + TEST_VERSION_STR);
    }

    protected void prepareSingleNodeEnv(ConfigManager configManager) throws Exception {
        Map<String, String> properties = getTestSingleNodeProperties();

        createSingleNodeConf();
        createAssemblyProperty();
        doReturn(InstallType.SINGLE_SERVER).when(configManager).detectInstallationType();
        doReturn(API_ENDPOINT).when(configManager).getApiEndpoint();
        doReturn(new Config(properties)).when(configManager).loadInstalledCodenvyConfig();
    }

    protected Map<String, String> getTestSingleNodeProperties() {
        return ImmutableMap.of("host_url", HOSTNAME,
                               Config.VERSION, TEST_VERSION_STR);
    }

    protected void prepareMultiNodeEnv(ConfigManager configManager, HttpTransport transport) throws Exception {
        prepareMultiNodeEnv(configManager);
        when(transport.doOption("http://hostname/api/", null)).thenReturn("{\"ideVersion\":\"" + TEST_VERSION_STR + "\"}");
    }

    protected void prepareMultiNodeEnv(ConfigManager configManager) throws Exception {
        Map<String, String> properties = getTestMultiNodeProperties();
        createMultiNodeConf();
        createAssemblyProperty();
        doReturn(InstallType.MULTI_SERVER).when(configManager).detectInstallationType();
        doReturn(new Config(properties)).when(configManager).loadInstalledCodenvyConfig();
    }

    protected Map<String, String> getTestMultiNodeProperties() {
        return new HashMap<String, String>() {{
            put("api_host_name", "api.example.com");
            put("data_host_name", "data.example.com");
            put("analytics_host_name", "analytics.example.com");
            put("host_url", "hostname");
            put(Config.PUPPET_MASTER_HOST_NAME, "master.example.com");
            put(Config.VERSION, TEST_VERSION_STR);
        }};
    }

    @AfterMethod
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(Paths.get(DOWNLOAD_DIR).toFile());
    }

    @AfterClass
    public void tearDownClass() {
        OSUtils.VERSION = INITIAL_OS_VERSION;
    }
}
