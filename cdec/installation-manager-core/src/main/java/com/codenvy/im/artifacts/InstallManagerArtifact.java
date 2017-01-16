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
package com.codenvy.im.artifacts;

import com.codenvy.im.agent.Agent;
import com.codenvy.im.agent.LocalAgent;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.MacroCommand;
import com.codenvy.im.commands.SimpleCommand;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.InjectorBootstrap;
import com.codenvy.im.utils.Version;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.codenvy.im.utils.Commons.extractServerUrl;
import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class InstallManagerArtifact extends AbstractArtifact {
    public static final String NAME = "installation-manager-cli";

    private static final String CODENVY_CLI_DIR_NAME   = "cli";
    private static final String UPDATE_CLI_SCRIPT_NAME = "update-im-cli";
    private static final String RELATIVE_PATH_TO_JAVA  = "jre";

    private String saasServerEndpoint;
    private Path imBaseDir;

    @Inject
    public InstallManagerArtifact(@Named("installation-manager.base_dir") String imBaseDir,
                                  @Named("installation-manager.update_server_endpoint") String updateEndpoint,
                                  @Named("installation-manager.download_dir") String downloadDir,
                                  @Named("saas.api.endpoint") String saasApiEndpoint,
                                  HttpTransport transport,
                                  ConfigManager configManager) {
        super(NAME, updateEndpoint, downloadDir, transport, configManager);
        this.saasServerEndpoint = saasApiEndpoint;
        this.imBaseDir = Paths.get(imBaseDir);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Version> getInstalledVersion() throws IOException {
        try (InputStream in = Artifact.class.getClassLoader().getResourceAsStream("codenvy/BuildInfo.properties")) {
            Properties props = new Properties();
            props.load(in);

            if (props.containsKey("version")) {
                return Optional.of(Version.valueOf((String)props.get("version")));
            } else {
                throw new IOException(format("Can't get the version of '%s' artifact", NAME));
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int getPriority() {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getInstallInfo(InstallType installType) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getUpdateInfo(InstallType installType) throws IOException {
        return new ArrayList<String>() {{
            add("Initialize updating installation manager");
        }};
    }

    /** {@inheritDoc} */
    @Override
    public Command getInstallCommand(Version versionToInstall, final Path pathToBinaries, InstallOptions installOptions) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Command getUpdateCommand(Version versionToUpdate, Path pathToBinaries, InstallOptions installOptions) throws IOException {
        int step = installOptions.getStep();

        final Agent syncAgent = new LocalAgent();
        switch (step) {
            case 0:
                final Path cliClientDir = imBaseDir.resolve(CODENVY_CLI_DIR_NAME);

                final Path updateCliScript = imBaseDir.resolve(UPDATE_CLI_SCRIPT_NAME);

                final Path newPlacementOfUpdateBinaries = imBaseDir.resolve(pathToBinaries.getFileName());

                final Path absolutePathToJava = imBaseDir.resolve(RELATIVE_PATH_TO_JAVA);


                final String contentOfUpdateCliScript = format("#!/bin/bash\n"
                                                               + "\n"
                                                               + "moveCliDir=false\n"
                                                               + "if [[ -d %5$s/codenvy-cli ]]; then\n"
                                                               + "    moveCliDir=true\n"
                                                               + "\n"
                                                               + "    if [[ \\$PWD =~ %5$s/codenvy-cli ]]; then\n"
                                                               + "        echo \"Current directory '\\$PWD' is being removed...\"\n"
                                                               + "        cd %5$s\n"
                                                               + "    fi\n"
                                                               + "\n"
                                                               + "    rm -rf %5$s/codenvy-cli\n"
                                                               + "\n"
                                                               + "    if [[ $? == 0 ]]; then\n"
                                                               + "        mkdir %5$s/cli\n"
                                                               + "        sed -i \"s|codenvy-cli/bin|cli/bin|\" ~/.bashrc &> /dev/null\n"   // fix path to IM CLI scripts
                                                               + "        source ~/.bashrc\n"                                               // aply changes in path
                                                               + "    fi\n"
                                                               + "else\n"
                                                               + "    rm -rf %1$s/bin/* \n"                                  // remove content of "bin" dir of cli client
                                                               + "    find %1$s/* ! -name 'bin' -type d -exec rm -rf {} + \n"   // remove all content of cli client except "bin" dir
                                                               + "fi\n"
                                                               + "\n"
                                                               + "tar -xzf %2$s -C %1$s/ \n"                                    // unpack update into the cli client dir
                                                               + "chmod +x %1$s/bin/* \n"                                       // set permissions to execute CLI client scripts
                                                               + "sed -i \"2iJAVA_HOME=%3$s\" %1$s/bin/codenvy \n"              // setup java home path
                                                               + "rm -f %4$s \n"                                                // remove update script
                                                               + "rm -f %2$s \n"                                                // remove binaries of update
                                                               + "%1$s/bin/codenvy \\$@\n"                                      // run script from updated directory
                                                               + "\n"
                                                               + "if [[ \\$moveCliDir == true ]]; then\n"
                                                               + "    echo \"Please, execute 'source ~/.bashrc' command to apply an update of installation manager CLI client.\"\n"
                                                               + "fi"
                                                               + "",
                                                               cliClientDir.toAbsolutePath(),
                                                               newPlacementOfUpdateBinaries,
                                                               absolutePathToJava,
                                                               updateCliScript.toAbsolutePath(),
                                                               imBaseDir.toAbsolutePath());

                return new MacroCommand(ImmutableList.<Command>of(
                    new SimpleCommand(format("cat >> %s << EOF\n%s\nEOF", updateCliScript.toAbsolutePath(), contentOfUpdateCliScript),
                                      syncAgent, "Create script to update cli client"),

                    new SimpleCommand(format("chmod 775 %s", updateCliScript.toAbsolutePath()),
                                      syncAgent, "Set permissions to execute update script"),

                    new SimpleCommand(format("cp %s %s", pathToBinaries.toAbsolutePath(), newPlacementOfUpdateBinaries),
                                      syncAgent, "Copy update binaries to the user home directory")),
                                        "Update installation manager CLI client");

            default:
                throw new IllegalArgumentException(format("Step number %d is out of range", step));
        }
    }

    @Override
    public Command getReinstallCommand() throws IOException {
        throw new UnsupportedOperationException("Re-install of installation manager CLI client isn't supported");
    }

    @Override
    public boolean isAlive() {
        return true;
    }

    /** @return path where artifact located */
    protected Path getInstalledPath() throws URISyntaxException {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        return Paths.get(location.toURI()).getParent();
    }

    /** {@inheritDoc} */
    @Override
    public Command getBackupCommand(BackupConfig backupConfig) throws IOException {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Command getRestoreCommand(BackupConfig backupConfig) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * @param properties
     */
    @Override
    public void updateConfig(Map<String, String> properties) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    /**
     * @return configuration of installed Installation Manager
     */
    public Map<String, String> getConfig() throws IOException {
        return new LinkedHashMap<String, String>() {{
            put("download_directory", downloadDir.toString());
            put("update_server_url", extractServerUrl(updateServerEndpoint));
            put("saas_server_url", extractServerUrl(saasServerEndpoint));
            put("base_directory", imBaseDir.toString());
            put("backup_directory", InjectorBootstrap.getProperty("installation-manager.backup_dir"));
            put("report_directory", InjectorBootstrap.getProperty("installation-manager.report_dir"));
        }};
    }
}
