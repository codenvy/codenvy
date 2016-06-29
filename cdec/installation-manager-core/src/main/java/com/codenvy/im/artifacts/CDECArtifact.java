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
package com.codenvy.im.artifacts;

import com.codenvy.im.artifacts.helper.CDECArtifactHelper;
import com.codenvy.im.artifacts.helper.CDECMultiServerHelper;
import com.codenvy.im.artifacts.helper.CDECSingleServerHelper;
import com.codenvy.im.commands.Command;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.PropertiesNotFoundException;
import com.codenvy.im.managers.UnknownInstallationTypeException;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.IllegalVersionException;
import com.codenvy.im.utils.OSUtils;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.eclipse.che.api.core.rest.shared.dto.ApiInfo;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.codenvy.im.commands.SimpleCommand.createCommandWithoutLogging;
import static com.codenvy.im.managers.NodeConfig.extractConfigFrom;
import static com.codenvy.im.utils.Commons.createDtoFromJson;
import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class CDECArtifact extends AbstractArtifact {
    public static final String                         NAME    = "codenvy";
    private final Map<InstallType, CDECArtifactHelper> HELPERS = ImmutableMap.of(
            InstallType.SINGLE_SERVER, new CDECSingleServerHelper(this, configManager),
            InstallType.MULTI_SERVER, new CDECMultiServerHelper(this, configManager));

    private final NodeManager nodeManager;

    @Inject
    public CDECArtifact(@Named("installation-manager.update_server_endpoint") String updateEndpoint,
                        @Named("installation-manager.download_dir") String downloadDir,
                        HttpTransport transport,
                        ConfigManager configManager,
                        NodeManager nodeManager) {
        super(NAME, updateEndpoint, downloadDir, transport, configManager);
        this.nodeManager = nodeManager;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Version> getInstalledVersion() throws IOException {
        try {
            Optional<Version> version;
            try {
                version = fetchAssemblyVersion();
                if (version.isPresent()) {
                    return version;
                }
            } catch (IOException e) {
                // ignore IOException here
            }

            try {
                version = getVersionFromApiService();
                if (version.isPresent()) {
                    return version;
                }
            } catch (IOException e) {
                // ignore IOException here
            }

            try {
                return fetchVersionFromPuppetConfig();
            } catch (IOException e) {
                return Optional.empty();
            }
        } catch(UnknownInstallationTypeException | IllegalVersionException e) {
            return Optional.empty();
        }
    }

    /**
     * @throws IOException if API server is down
     */
    protected Optional<Version> getVersionFromApiService() throws IOException {
        String response = transport.doOption(configManager.getApiEndpoint() + "/", null);
        ApiInfo apiInfo = createDtoFromJson(response, ApiInfo.class);
        if (apiInfo != null) {
            String ideVersion = apiInfo.getIdeVersion();
            if (ideVersion != null
                && !ideVersion.contains("codenvy.cloud-ide.version")) {
                return Optional.of(Version.valueOf(ideVersion));
            }
        }

        return Optional.empty();
    }

    protected Optional<Version> fetchVersionFromPuppetConfig() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        String value = config.getValue(Config.VERSION);
        return value == null ? Optional.<Version>empty() : Optional.of(Version.valueOf(value));
    }

    protected Optional<Version> fetchAssemblyVersion() throws IOException {
        Path pathToAssemblyProperties = getPathToAssemblyProperties(Version.VERSION_4_4_0);
        Command command = getReadAssemblyPropertiesCommand(pathToAssemblyProperties);
        String result = command.execute().trim();
        if (StringUtils.isEmpty(result)) {
            pathToAssemblyProperties = getPathToAssemblyProperties(Version.VERSION_3);
            command = getReadAssemblyPropertiesCommand(pathToAssemblyProperties);
            result = command.execute().trim();
        }

        return StringUtils.isEmpty(result) ? Optional.<Version>empty() : Optional.of(Version.valueOf(result));
    }

    /**
     * Get path to Codenvy assembly.properties file
     * @param codenvyVersion
     */
    Path getPathToAssemblyProperties(Version codenvyVersion) {
        return getHelper().getPathToCodenvyRoot(codenvyVersion).resolve("conf/assembly.properties");
    }

    protected Command getReadAssemblyPropertiesCommand(Path pathToAssemblyProperties) throws IOException {
        String cmd = format("if sudo test -f %1$s; then " +
                            "   sudo cat %1$s " +
                            "       | grep assembly.version " +
                            "       | sed 's/assembly.version\\s*=\\s*\\(.*\\)/\\1/';" +
                            "fi", pathToAssemblyProperties);
        if (!pathToAssemblyProperties.startsWith("/")) { // make it works for tests
            cmd = cmd.replaceAll("sudo", "");
        }

        InstallType installType = configManager.detectInstallationType();
        if (installType.equals(InstallType.SINGLE_SERVER)) {
            return createCommandWithoutLogging(cmd);
        } else {
            Config config = configManager.loadInstalledCodenvyConfig();
            NodeConfig apiNode = extractConfigFrom(config, NodeConfig.NodeType.API);
            return createCommandWithoutLogging(cmd, apiNode);
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return 10;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getUpdateInfo(InstallType installType) throws IOException {
        if (installType != configManager.detectInstallationType()) {
            throw new IllegalArgumentException("Only update to the Codenvy of the same installation type is supported");
        }

        return ImmutableList.of("Unzip Codenvy binaries to /tmp/codenvy",
                                "Configure Codenvy",
                                "Patch resources before update",
                                "Move Codenvy binaries to /etc/puppet",
                                "Update Codenvy",
                                "Patch resources after update");
    }

    /** {@inheritDoc} */
    @Override
    public Command getUpdateCommand(Version versionToUpdate,
                                    Path pathToBinaries,
                                    InstallOptions installOptions) throws IOException, IllegalArgumentException {
        if (!OSUtils.getVersion().equals("7")) {
            throw new IllegalStateException("Codenvy On-Prem can be updated on CentOS 7 only");
        }

        if (installOptions.getInstallType() != configManager.detectInstallationType()) {
            throw new IllegalArgumentException("Only update to the Codenvy of the same installation type is supported");
        }

        return getHelper(installOptions.getInstallType()).getUpdateCommand(versionToUpdate, pathToBinaries, installOptions);
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getInstallInfo(InstallType installType) throws IOException {
        return getHelper(installType).getInstallInfo();
    }

    /** {@inheritDoc} */
    @Override
    public Command getInstallCommand(final Version versionToInstall,
                                     final Path pathToBinaries,
                                     final InstallOptions installOptions) throws IOException {

        if (!OSUtils.getVersion().equals("7")) {
            throw new IllegalStateException("Codenvy On-Prem can be installed on CentOS 7 or RHEL 7 only");
        }

        return getHelper(installOptions.getInstallType())
                .getInstallCommand(versionToInstall, pathToBinaries, installOptions);
    }

    /** {@inheritDoc} */
    @Override
    public Command getBackupCommand(BackupConfig backupConfig) throws IOException {
        CDECArtifactHelper helper = getHelper(configManager.detectInstallationType());
        return helper.getBackupCommand(backupConfig);
    }

    /** {@inheritDoc} */
    @Override
    public Command getRestoreCommand(BackupConfig backupConfig) throws IOException {
        CDECArtifactHelper helper = getHelper(configManager.detectInstallationType());
        return helper.getRestoreCommand(backupConfig);
    }

    /** {@inheritDoc} */
    @Override
    public void updateConfig(Map<String, String> propertiesToUpdate) throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        Map<String, String> actualProperties = config.getProperties();

        // check if there are nonexistent property among propertiesToUpdate
        List<String> nonexistentProperties = new ArrayList<>();
        for (String property : propertiesToUpdate.keySet()) {
            if (! actualProperties.containsKey(property)) {
                nonexistentProperties.add(property);
            }
        }

        if (!nonexistentProperties.isEmpty()) {
            throw new PropertiesNotFoundException(nonexistentProperties);
        }

        InstallType installType = configManager.detectInstallationType();
        CDECArtifactHelper helper = getHelper(installType);

        String currentHostName = config.getHostUrl();
        String newHostName = propertiesToUpdate.get(Config.HOST_URL);
        if (newHostName != null && !newHostName.equals(currentHostName)) {
            Command commands = helper.getUpdatePuppetConfigCommand(config, currentHostName, newHostName);
            commands.execute();

            nodeManager.updatePuppetConfig(currentHostName, newHostName);
        }

        Command commands = helper.getUpdateConfigCommand(config, propertiesToUpdate);
        commands.execute();
    }

    /**
     * @return Codenvy configuration properties where all private keys ("password", "secret" etc.) were been hidden by "*" value
     * @throws IOException
     */
    @Override
    public Map<String, String> getConfig() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        Map<String, String> properties = configManager.maskPrivateProperties(config.getProperties());

        return properties;
    }

    /** {@inheritDoc} */
    @Override
    public Command getReinstallCommand() throws IOException {
        Optional<Version> installedVersion = getInstalledVersion();
        if (!installedVersion.isPresent()) {
            throw new RuntimeException("It is impossible to define installed version.");
        }

        InstallType installType = configManager.detectInstallationType();
        Config config = configManager.loadInstalledCodenvyConfig(installType);

        return getHelper(installType).getReinstallCommand(config, installedVersion.get());
    }

    @Override
    public boolean isAlive() {
        return isApiServiceAlive();
    }

    protected CDECArtifactHelper getHelper(InstallType type) {
        return HELPERS.get(type);
    }

    protected CDECArtifactHelper getHelper() {
        return HELPERS.get(InstallType.SINGLE_SERVER);
    }

    protected boolean isApiServiceAlive() {
        try {
            transport.doOption(configManager.getApiEndpoint() + "/", null);
            return true;
        } catch (IOException e) {
            return false;  // API server is down
        }
    }
}
