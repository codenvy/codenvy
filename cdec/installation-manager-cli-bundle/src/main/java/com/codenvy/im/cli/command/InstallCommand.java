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
package com.codenvy.im.cli.command;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.response.InstallResponse;
import com.codenvy.im.response.ResponseCode;
import com.codenvy.im.utils.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.codenvy.im.commands.PatchCDECCommand.UPDATE_INFO;
import static com.codenvy.im.event.EventFactory.createImArtifactInstallStartedWithTime;
import static com.codenvy.im.event.EventFactory.createImArtifactInstallSuccessWithTime;
import static com.codenvy.im.event.EventFactory.createImArtifactInstallUnsuccessWithTime;
import static com.codenvy.im.utils.Commons.toJson;
import static com.codenvy.im.utils.InjectorBootstrap.INJECTOR;
import static com.codenvy.im.utils.InjectorBootstrap.INSTALLATION_MANAGER_BASE_DIR;
import static com.codenvy.im.utils.InjectorBootstrap.getProperty;
import static java.lang.Math.max;
import static java.lang.String.format;

/**
 * @author Alexander Reshetnyak
 * @author Anatoliy Bazko
 */
@Command(scope = "codenvy", name = "install", description = "Install, update artifact or print the list of already installed ones")
public class InstallCommand extends AbstractIMCommand {

    private static final Logger LOG = Logger.getLogger(InstallCommand.class.getName());

    private final ConfigManager configManager;
    private       InstallType   installType;

    @Argument(index = 0, name = "artifact", description = "The name of the specific artifact to install.", required = false, multiValued = false)
    protected String artifactName;

    @Argument(index = 1, name = "version", description = "The specific version of the artifact to install", required = false, multiValued = false)
    private String versionNumber;

    @Option(name = "--list", aliases = "-l", description = "To show installed list of artifacts", required = false)
    private boolean list;

    @Option(name = "--multi", aliases = "-m", description = "To install artifact on multiply nodes (by default on single node)", required = false)
    private boolean multi;

    @Option(name = "--config", aliases = "-c", description = "Path to the configuration file", required = false)
    private String configFilePath;

    @Option(name = "--binaries", aliases = "-b", description = "Path to binaries to install", required = false)
    private String binaries;

    /** Could be */
    @Option(name = "--step", aliases = "-s", description = "Particular installation step to perform", required = false)
    private Integer installStep;

    @Option(name = "--forceInstall", aliases = "-fi", description = "Force installation in case of splitting process by steps", required = false)
    private boolean forceInstall;

    @Option(name = "--reinstall", aliases = "-r", description = "Re-install Codenvy (binaries only)", required = false)
    private boolean reinstall;

    public InstallCommand() {
        this.configManager = INJECTOR.getInstance(ConfigManager.class);
    }

    @Deprecated
    /**
     * For testing purpose only
     */
    InstallCommand(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    protected void doExecuteCommand() throws Exception {
        if (list) {
            doExecuteListInstalledArtifacts();		
        } else if (reinstall) {
            doExecuteReinstall();
        } else {
            doExecuteInstall();
        }
    }

    private void doExecuteReinstall() throws JsonParseException, JsonProcessingException {
        if (artifactName == null) {
            artifactName = CDECArtifact.NAME;
        }

        getConsole().showProgressor();

        InstallArtifactInfo installArtifactInfo = new InstallArtifactInfo();
        installArtifactInfo.setArtifact(artifactName);

        InstallResponse installResponse = new InstallResponse();
        installResponse.setArtifacts(ImmutableList.of(installArtifactInfo));

        try {
            getFacade().reinstall(createArtifact(artifactName));
            installArtifactInfo.setStatus(InstallArtifactInfo.Status.SUCCESS);
            installResponse.setStatus(ResponseCode.OK);
            getConsole().println(toJson(installResponse));

        } catch (Exception e) {
            installArtifactInfo.setStatus(InstallArtifactInfo.Status.FAILURE);
            installResponse.setStatus(ResponseCode.ERROR);
            installResponse.setMessage(e.getMessage());
            getConsole().printResponseExitInError(installResponse);
        } finally {
            getConsole().hideProgressor();
        }
    }

    private Void doExecuteInstall() throws IOException, JsonParseException, InterruptedException {
        if (binaries != null) {
            if (versionNumber == null) {
                throw new IllegalStateException("Parameter 'version' is missed");
            } else if (artifactName == null) {
                throw new IllegalStateException("Parameter 'artifact' is missed");
            }
        }

        if (artifactName == null) {
            artifactName = CDECArtifact.NAME;
        }

        final Artifact artifact = createArtifact(artifactName);
        final Version version = versionNumber != null ? Version.valueOf(versionNumber) : getFacade().getLatestInstallableVersion(artifact);
        if (version == null) {
            throw new IllegalStateException("There is no new version to install");
        }
        versionNumber = version.toString();

        final InstallOptions installOptions = new InstallOptions();
        final boolean isInstall = isInstall(artifact);

        final int firstStep = getFirstInstallStep();

        if ((firstStep == 0) && isInstall) {
            logEventToSaasCodenvy(createImArtifactInstallStartedWithTime(artifactName, versionNumber));
        }

        if (isInstall) {
            if (multi) {
                installType = InstallType.MULTI_SERVER;
            } else {
                installType = InstallType.SINGLE_SERVER;
            }
        } else {
            installType = getConfigManager().detectInstallationType();
        }

        installOptions.setInstallType(installType);
        setInstallProperties(installOptions, isInstall);

        List<String> infos;
        if (isInstall) {
            try {
                infos = getFacade().getInstallInfo(artifact, installType);
            } catch(Exception e) {
                logEventToSaasCodenvy(createImArtifactInstallUnsuccessWithTime(artifactName, versionNumber, e.getMessage()));
                throw e;
            }
        } else {
            infos = getFacade().getUpdateInfo(artifact, installType);
            removeUpdateInfoFile();
        }

        final int finalStep = infos.size() - 1;
        final int lastStep = getLastInstallationStep(finalStep);

        int maxInfoLen = 0;
        for (String i : infos) {
            maxInfoLen = max(maxInfoLen, i.length());
        }

        InstallArtifactInfo installArtifactInfo = InstallArtifactInfo.createInstance(artifactName, versionNumber, InstallArtifactInfo.Status.SUCCESS);

        InstallResponse installResponse = new InstallResponse();
        installResponse.setStatus(ResponseCode.OK);
        installResponse.setArtifacts(ImmutableList.of(installArtifactInfo));

        for (int step = firstStep; step <= lastStep; step++) {
            String info = infos.get(step);
            getConsole().print(info);
            getConsole().printWithoutCodenvyPrompt(new String(new char[maxInfoLen - info.length()]).replace("\0", " "));

            getConsole().showProgressor();

            try {
                installOptions.setStep(step);

                try {
                    String stepId;
                    if (isInstall) {
                        stepId = binaries != null ? getFacade().install(artifact, version, Paths.get(binaries), installOptions)
                                                  : getFacade().install(artifact, version, installOptions);
                    } else {
                        stepId = binaries != null ? getFacade().update(artifact, version, Paths.get(binaries), installOptions)
                                                  : getFacade().update(artifact, version, installOptions);
                    }
                    getFacade().waitForInstallStepCompleted(stepId);
                    InstallArtifactStepInfo updateStepInfo = getFacade().getUpdateStepInfo(stepId);
                    if (updateStepInfo.getStatus() == InstallArtifactInfo.Status.FAILURE) {
                        installResponse.setStatus(ResponseCode.ERROR);
                        installResponse.setMessage(updateStepInfo.getMessage());
                        installArtifactInfo.setStatus(InstallArtifactInfo.Status.FAILURE);
                    }
                } catch (Exception e) {
                    installArtifactInfo.setStatus(InstallArtifactInfo.Status.FAILURE);
                    installResponse.setStatus(ResponseCode.ERROR);
                    installResponse.setMessage(e.getMessage());
                }

                if (installResponse.getStatus() == ResponseCode.ERROR) {
                    if (isInstall) {
                        logEventToSaasCodenvy(createImArtifactInstallUnsuccessWithTime(artifactName, versionNumber, installResponse.getMessage()));
                    }

                    getConsole().printError(" [FAIL]", true);
                    getConsole().printResponseExitInError(installResponse);
                    return null;
                } else {
                    getConsole().printSuccessWithoutCodenvyPrompt(" [OK]");
                }
            } finally {
                getConsole().hideProgressor();
            }
        }

        // only OK response can be here
        if (lastStep == finalStep) {
            if (isInstall) {
                logEventToSaasCodenvy(createImArtifactInstallSuccessWithTime(artifactName, versionNumber));
            }

            getConsole().println(toJson(installResponse));

            final String updateInfo = getUpdateInfo();
            if (!isInstall && updateInfo != null) {
                getConsole().printWarning(updateInfo, isInteractive());
            }

            if (isInteractive() && artifactName.equals(InstallManagerArtifact.NAME)) {
                getConsole().pressAnyKey("'Installation Manager CLI' is being updated! Press any key to exit...\n");
                getConsole().exit(0);
            }
        }

        return null;
    }

    @VisibleForTesting
    void removeUpdateInfoFile() {
        Path infoPath = getPathToUpdateInfoFile();
        FileUtils.deleteQuietly(infoPath.toFile());
    }

    @VisibleForTesting
    Path getPathToUpdateInfoFile() {
        return Paths.get(getProperty(INSTALLATION_MANAGER_BASE_DIR),
                         UPDATE_INFO);
    }

    @Nullable
    private String getUpdateInfo() {
        String info = null;
        Path infoPath = getPathToUpdateInfoFile();
        if (Files.exists(infoPath)) {
            try {
                info = FileUtils.readFileToString(infoPath.toFile());
            } catch (IOException e) {
                LOG.log(Level.WARNING, format("Can't read update info from the file '%s'. Error: %s.", infoPath.toFile(), e.getMessage()));
                return null;
            }
        }

        return info;
    }

    @VisibleForTesting
    boolean isInstall(Artifact artifact) throws IOException {
        return (installStep != null && forceInstall)
               || !artifact.getInstalledVersion().isPresent();
    }

    private Void doExecuteListInstalledArtifacts() throws IOException, JsonParseException {
        Collection<InstallArtifactInfo> installedVersions = getFacade().getInstalledVersions();
        InstallResponse installResponse = new InstallResponse();
        installResponse.setArtifacts(installedVersions);
        installResponse.setStatus(ResponseCode.OK);
        getConsole().printResponseExitInError(installResponse);
        return null;
    }

    private void setInstallProperties(InstallOptions options, boolean isInstall) throws IOException {
        Map<String, String> properties = getConfigManager().prepareInstallProperties(configFilePath,
                                                                                     binaries == null ? null : Paths.get(binaries),
                                                                                     installType,
                                                                                     createArtifact(artifactName),
                                                                                     Version.valueOf(versionNumber),
                                                                                     isInstall);
        options.setConfigProperties(properties);
    }

    private int getFirstInstallStep() {
        if (installStep == null) {
            return 0;
        }

        return installStep - 1;
    }

    private int getLastInstallationStep(int maxStep) {
        if (installStep == null) {
            return maxStep;
        }

        return installStep - 1;
    }

}
