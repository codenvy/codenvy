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

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.commands.SimpleCommand;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.SshKey;
import com.codenvy.im.utils.Version;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalINIConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.StringUtils;
import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static com.codenvy.im.utils.Commons.combinePaths;
import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newBufferedReader;

/** @author Dmytro Nochevnov */
@Singleton
public class ConfigManager {
    public static final String PUPPET_MASTER_DEFAULT_HOSTNAME = "puppet-master.example.com";

    public static final Pattern PUPPET_PROP_TEMPLATE  = Pattern.compile(" *\\$([^\\s]+) *= *\"([^\"]*)\"");
    public static final Pattern CODENVY_PROP_TEMPLATE = Pattern.compile("^([^\\s=#]+)=(.*)");

    public static final String PATH_TO_MANIFEST_PATCH_VARIABLE = "PATH_TO_MANIFEST";

    private final HttpTransport transport;
    private final String        updateEndpoint;
    private final String        puppetBaseDir;
    private final Path          puppetConfFile;

    @Inject
    public ConfigManager(@Named("installation-manager.update_server_endpoint") String updateEndpoint,
                         @Named("puppet.base_dir") String puppetBaseDir,
                         HttpTransport transport) {
        this.transport = transport;
        this.updateEndpoint = updateEndpoint;
        this.puppetBaseDir = puppetBaseDir;
        this.puppetConfFile = Paths.get(puppetBaseDir, Config.PUPPET_CONF_FILE_NAME).toAbsolutePath();
    }

    /** Loads properties from the given file. */
    public Map<String, String> loadConfigProperties(Path confFile) throws IOException {
        if (!exists(confFile)) {
            throw new FileNotFoundException(format("Configuration file '%s' not found", confFile.toString()));
        }

        try {
            return doLoadCodenvyProperties(confFile);
        } catch (IOException e) {
            throw new ConfigException(format("Can't load properties: %s", e.getMessage()), e);
        }
    }

    /** Loads properties from the given file. */
    public Map<String, String> loadConfigProperties(String confFile) throws IOException {
        return loadConfigProperties(Paths.get(confFile));
    }

    /** Loads default properties. */
    public Map<String, String> loadCodenvyDefaultProperties(Version version, InstallType installType) throws IOException {
        Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));

        String requestUrl = combinePaths(updateEndpoint, "/repository/public/download/codenvy-" +
                                                         (installType == InstallType.MULTI_SERVER ? "multi" : "single")
                                                         + "-server-properties/" + version);
        Path properties;
        try {
            properties = transport.download(requestUrl, tmpDir);
        } catch (IOException e) {
            throw new IOException("Can't download installation properties. " + e.getMessage(), e);
        }

        try {
            return doLoadCodenvyProperties(properties);
        } catch (IOException e) {
            throw new ConfigException(format("Can't load properties: %s", e.getMessage()), e);
        }
    }

    /**
     * Merges two bunches of the properties from current and new configurations.
     */
    public Map<String, String> merge(Map<String, String> curProps,
                                     Map<String, String> newProps) throws IOException {

        Map<String, String> props = new HashMap<>(newProps);

        for (Map.Entry<String, String> e : curProps.entrySet()) {
            String name = e.getKey();
            String curValue = e.getValue();

            // don't change existed variable
            if (props.containsKey(name)) {
                props.put(name, curValue);
            }
        }

        return props;
    }

    /**
     * Loads properties of the installed cdec artifact.
     * <p/>
     * The properties file has the follow format:
     * $property="value"
     * ...
     * <p/>
     * Finally method removes leading '$' for key name and quota characters for its value.
     */
    public Map<String, String> loadInstalledCodenvyProperties(InstallType installType) throws IOException {
        Map<String, String> properties = new HashMap<>();

        Iterator<Path> propertiesFiles = getCodenvyPropertiesFiles(installType);
        while (propertiesFiles.hasNext()) {
            Path file = propertiesFiles.next();

            try {
                properties.putAll(doLoadInstalledCodenvyProperties(file));
            } catch (IOException e) {
                throw new ConfigException(format("Can't load Codenvy properties: %s", e.getMessage()), e);
            }
        }

        return properties;
    }

    /**
     * Returns the list of puppet properties files.
     */
    public Iterator<Path> getCodenvyPropertiesFiles(InstallType installType) {
        return getCodenvyPropertiesFiles(puppetBaseDir, installType);
    }

    /**
     * Returns the list of puppet properties files.
     */
    public Iterator<Path> getCodenvyPropertiesFiles(String baseDir, InstallType installType) {
        List<Path> propertiesFiles = new LinkedList<>();

        List<String> properties;
        switch (installType) {
            case MULTI_SERVER:
                properties = Config.MULTI_SERVER_PROPERTIES;
                break;
            case SINGLE_SERVER:
            default:
                properties = Config.SINGLE_SERVER_PROPERTIES;
        }

        for (String relPathPropertyFile : properties) {
            Path absPathCandidate = Paths.get(baseDir, relPathPropertyFile);
            if (exists(absPathCandidate)) {
                propertiesFiles.add(absPathCandidate);
            }
        }

        return propertiesFiles.iterator();
    }

    /**
     * @return list of replacements for multi-node master puppet config file Config.MULTI_SERVER_NODES_PP based on the node configs.
     */
    public static Map<String, String> getPuppetNodesConfigReplacement(List<NodeConfig> nodeConfigs) {
        Map<String, String> replacements = new HashMap<>(nodeConfigs.size());

        for (NodeConfig node : nodeConfigs) {
            NodeConfig.NodeType type = node.getType();
            switch (type) {
                case DATA:
                case API:
                case SITE:
                case DATASOURCE:
                case ANALYTICS: {
                    String replacingToken = format("%s.example.com", type.toString().toLowerCase());
                    String replacement = node.getHost();
                    replacements.put(replacingToken, replacement);
                    break;
                }

                case BUILDER: {
                    String replacingToken = "builder\\\\d+\\\\.example.com";
                    String replacement = format("builder\\\\d+\\\\%s", getBaseNodeDomain(node));
                    replacements.put(replacingToken, replacement);
                    break;
                }

                case RUNNER: {
                    String replacingToken = "runner\\\\d+\\\\.example.com";
                    String replacement = format("runner\\\\d+\\\\%s", getBaseNodeDomain(node));
                    replacements.put(replacingToken, replacement);
                    break;
                }

                default:
                    break;
            }
        }

        return replacements;
    }

    public static String getBaseNodeDomain(NodeConfig node) {
        String regex = format("^%s\\d+", node.getType().toString().toLowerCase());
        return node.getHost().toLowerCase().replaceAll(regex, "");
    }

    /**
     * Loads appropriate Codenvy config for given installation type.
     */
    public Config loadInstalledCodenvyConfig(InstallType installType) throws IOException {
        Map<String, String> properties = loadInstalledCodenvyProperties(installType);
        return new Config(properties);
    }


    /**
     * Loads appropriate Codenvy config depending on installation type.
     */
    public Config loadInstalledCodenvyConfig() throws UnknownInstallationTypeException, IOException {
        return loadInstalledCodenvyConfig(detectInstallationType());
    }

    /**
     * Detects which Codenvy installation type is used. Uses first non-empty value of variable <b>codenvy_install_type</b>
     * in puppet manifest files. Return it's value if it matches set of InstallType enum values: [single_server, multi_server, ...].
     * 
     * Workaround in case of absence of <b>codenvy_install_type</b>: analyzing puppet.conf to figure out in which sections
     * 'certname' property exists.  If method can't detect installation type then an exception will be thrown. The result totally
     * depends on implementations of {@link com.codenvy.im.artifacts.helper.CDECSingleServerHelper} and
     * {@link com.codenvy.im.artifacts.helper.CDECMultiServerHelper}.
     * <p/>
     * SINGLE-node type configuration sample:
     * [master]
     * certname = host_name
     * ...
     * [main]
     * ...
     * [agent]
     * certname = host_name
     * <p/>
     * <p/>
     * MULTI-node type configuration sample:
     * [main]
     * server = host_name
     * ...
     * [agent]
     * certname = host_name
     * ...
     *
     * @throws UnknownInstallationTypeException
     *         if can't detect installation type
     * @throws IOException 
     */
    @NotNull
    public InstallType detectInstallationType() throws UnknownInstallationTypeException, IOException {
        for (InstallType installType : InstallType.values()) {
            String codenvyInstallType = loadInstalledCodenvyProperties(installType).get(Config.CODENVY_INSTALL_TYPE);
            if (codenvyInstallType != null && codenvyInstallType.equals(installType.toString().toLowerCase())) {
                return installType;
            }
        }
        
        try {
            HierarchicalINIConfiguration iniFile = new HierarchicalINIConfiguration();
            iniFile.load(puppetConfFile.toFile());

            if (isSingleTypeConfig(iniFile)) {
                return InstallType.SINGLE_SERVER;
            } else if (isMultiTypeConfig(iniFile)) {
                return InstallType.MULTI_SERVER;
            }
            throw new UnknownInstallationTypeException();
        } catch (ConfigurationException e) {
            throw new UnknownInstallationTypeException(e);
        }
    }

    /**
     * Reads puppet master host name from the puppet configuration file.
     * It is supposed that we have deal with multi-server configuration type.
     * <p/>
     * MULTI-node type configuration sample:
     * [master]
     * ...
     * [main]
     * certname = some_host_name
     * ...
     *
     * @throws java.io.IOException
     *         if any I/O errors occur
     * @throws java.lang.IllegalStateException
     *         if host name is not set
     */
    @NotNull
    public String fetchMasterHostName() throws IOException {
        try {
            HierarchicalINIConfiguration iniFile = new HierarchicalINIConfiguration();
            iniFile.load(puppetConfFile.toFile());

            SubnodeConfiguration section = iniFile.getSection("main");
            if (section.getString("certname", "").isEmpty()) {
                // try to obtain host name from codenvy config
                Map<String, String> codenvyProperties = loadInstalledCodenvyConfig().getProperties();
                if (codenvyProperties.containsKey(Config.PUPPET_MASTER_HOST_NAME)) {
                    return codenvyProperties.get(Config.PUPPET_MASTER_HOST_NAME);
                }

                throw new IllegalStateException("There is no puppet master host name in the configuration");
            }
            return section.getString("certname");
        } catch (ConfigurationException e) {
            throw new IOException(e);
        }
    }


    /**
     * @return apiEndpoint
     */
    public String getApiEndpoint() throws IOException {
        InstallType installType = detectInstallationType();
        if (installType == InstallType.SINGLE_SERVER) {
            // in single-node installation it's not required to modify '/etc/hosts' on the server where Codenvy is being installed
            return "http://localhost/api";
        } else {
            Config config = loadInstalledCodenvyConfig(installType);
            return format("%s://%s/api", config.getValue("host_protocol"), config.getValue("host_url"));
        }
    }

    /**
     * Prepares installation properties depending on artifact and installation type.
     *
     * @param configFile
     *         file to read properties from, if absent the default properties will be loaded from the update server
     * @param binaries
     *         binaries to read codenvy properties from
     * @param installType
     *         installation type
     * @param artifact
     *         artifact to load properties for
     * @param version2Install
     *         version of the artifact
     * @return installation properties
     * @throws IOException
     *         if any I/O error occurred
     */
    public Map<String, String> prepareInstallProperties(@Nullable String configFile,
                                                        @Nullable Path binaries,
                                                        InstallType installType,
                                                        Artifact artifact,
                                                        Version version2Install,
                                                        boolean isInstall) throws IOException {
        switch (artifact.getName()) {
            case InstallManagerArtifact.NAME:
                return Collections.emptyMap();

            case CDECArtifact.NAME:
                Map<String, String> properties;

                if (isInstall) {
                    if (binaries != null) {
                        properties = loadConfigProperties(binaries, installType);
                    } else if (configFile != null) {
                        properties = loadConfigProperties(configFile);
                    } else {
                        properties = loadCodenvyDefaultProperties(version2Install, installType);
                    }

                    if (installType == InstallType.MULTI_SERVER) {
                        setupSSHAccessProperties(properties);
                    }
                    setupSshKeyParts(properties);
                    setupProxyProperties(properties);

                } else { // update
                    Map<String, String> newProperties;
                    if (binaries != null) {
                        newProperties = loadConfigProperties(binaries, installType);
                    } else {
                        Optional<Version> installVersion = artifact.getInstalledVersion();
                        if (!installVersion.isPresent()) {
                            throw new IOException("It is impossible to obtain installed version.");
                        }

                        newProperties = configFile != null ? loadConfigProperties(configFile)
                            : loadCodenvyDefaultProperties(version2Install, installType);
                    }

                    properties = merge(loadInstalledCodenvyProperties(installType),
                                       newProperties);

                    properties.put(Config.VERSION, version2Install.toString());

                    if (installType == InstallType.MULTI_SERVER) {
                        properties.put(Config.PUPPET_MASTER_HOST_NAME, fetchMasterHostName());  // set puppet master host name
                    }
                }

                return properties;
            default:
                throw new ArtifactNotFoundException(artifact);
        }
    }

    private void setupProxyProperties(Map<String, String> codenvyProperties) {
        String httpProxyForCodenvy = codenvyProperties.get(Config.HTTP_PROXY_FOR_CODENVY);
        String httpsProxyForCodenvy = codenvyProperties.get(Config.HTTPS_PROXY_FOR_CODENVY);

        Map<String, String> systemProperties = obtainProxyProperties();

        if (! StringUtils.isEmpty(httpProxyForCodenvy)) {
            codenvyProperties.put(Config.HTTP_PROXY, codenvyProperties.get(Config.HTTP_PROXY_FOR_CODENVY));
        } else {
            codenvyProperties.put(Config.HTTP_PROXY, systemProperties.get(Config.HTTP_PROXY));
        }

        if (! StringUtils.isEmpty(httpsProxyForCodenvy)) {
            codenvyProperties.put(Config.HTTPS_PROXY, codenvyProperties.get(Config.HTTPS_PROXY_FOR_CODENVY));
        } else {
            codenvyProperties.put(Config.HTTPS_PROXY, systemProperties.get(Config.HTTPS_PROXY));
        }
    }

    public Path getPuppetConfigFile(String configFilename) {
        return Paths.get(puppetBaseDir).resolve(configFilename);
    }

    /**
     * Loads properties from the binaries archive.
     */
    Map<String, String> loadConfigProperties(Path binaries, InstallType installType) throws IOException {
        SimpleCommand command = createCommand(format("rm -rf /tmp/codenvy; " +
                                                     "mkdir /tmp/codenvy/; " +
                                                     "unzip -o %s -d /tmp/codenvy", binaries.toString()));
        command.execute();

        ConfigManager configManager = new ConfigManager(updateEndpoint, "/tmp/codenvy", transport);
        return configManager.loadInstalledCodenvyProperties(installType);
    }

    /**
     * @return system environment
     */
    public Map<String, String> getEnvironment() {
        return System.getenv();
    }

    protected Map<String, String> doLoadCodenvyProperties(Path file) throws IOException {
        Map<String, String> m = new HashMap<>();

        try (BufferedReader in = newBufferedReader(file, Charset.forName("UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                Matcher matcher = CODENVY_PROP_TEMPLATE.matcher(line);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2).replace("\\n", "\n");
                    m.put(key, value);
                }
            }
        }

        return m;
    }

    private Map<String, String> doLoadInstalledCodenvyProperties(Path file) throws IOException {
        Map<String, String> m = new HashMap<>();

        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = Files.newReader(file.toFile(), Charset.forName("UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("#")) {
                    data.append(line);
                    data.append('\n');
                }
            }
        }

        Matcher matcher = PUPPET_PROP_TEMPLATE.matcher(data.toString());
        while (matcher.find()) {
            m.put(matcher.group(1), matcher.group(2));
        }

        return m;
    }

    /**
     * Sets properties needed for SSH access to other nodes.
     */
    private void setupSSHAccessProperties(Map<String, String> properties) throws IOException {
        String userName = System.getProperty("user.name");
        Path pathToIdRsa = Paths.get(System.getProperty("user.home")).resolve(".ssh").resolve("id_rsa");
        String sshKey = readSSHKey(pathToIdRsa);

        properties.put(Config.NODE_SSH_USER_NAME, userName);  // set name of user to access the nodes
        properties.put(Config.NODE_SSH_USER_PRIVATE_KEY, sshKey);  // set private key of ssh user
    }

    /**
     * Generates and sets private and public parts of the ssh key.
     */
    private void setupSshKeyParts(Map<String, String> properties) throws IOException {
        SshKey sshKey = new SshKey();
        properties.put(Config.PUBLIC_KEY, sshKey.getPublicPart());
        properties.put(Config.PRIVATE_KEY, sshKey.getPrivatePart().replace("\n", "\\n"));
    }

    private String readSSHKey(Path pathToIdRsa) throws IOException {
        if (!exists(pathToIdRsa)) {
            throw new RuntimeException("SSH private key not found: " + pathToIdRsa.toString());
        }
        return Files.toString(pathToIdRsa.toFile(), Charsets.UTF_8);
    }

    /**
     * Returns proxy settings which are needed to configure Codenvy to work behind the proxy basing on the system environment.
     */
    public Map<String, String> obtainProxyProperties() {
        Map<String, String> proxyProperties = new HashMap<>();
        Map<String, String> environment = getEnvironment();

        if (environment.containsKey(Config.HTTP_PROXY)) {
            proxyProperties.put(Config.HTTP_PROXY, environment.get(Config.HTTP_PROXY));
        }

        if (environment.containsKey(Config.HTTPS_PROXY)) {
            proxyProperties.put(Config.HTTPS_PROXY, environment.get(Config.HTTPS_PROXY));
        }

        return proxyProperties;
    }

    /**
     * @return true if only there is "main" section without "server" property in the puppet.conf file
     */
    private boolean isSingleTypeConfig(HierarchicalINIConfiguration iniFile) {
        Set<String> sections = iniFile.getSections();
        return sections.contains("main")
               && iniFile.getSection("main").getString("server", "").isEmpty();
    }

    /**
     * @return true if only there is "main" section with "server" property in the puppet.conf file
     */
    private boolean isMultiTypeConfig(HierarchicalINIConfiguration iniFile) {
        Set<String> sections = iniFile.getSections();
        return sections.contains("main")
               && !iniFile.getSection("main").getString("server", "").isEmpty();
    }
}
