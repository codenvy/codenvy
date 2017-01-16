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
package com.codenvy.im.managers;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.commands.Command;
import com.codenvy.im.commands.CommandException;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.utils.Commons.getProperException;
import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class InstallManager {
    private static final Logger LOG = Logger.getLogger(DownloadManager.class.getSimpleName());

    protected final Set<Artifact>                        artifacts;
    protected final Map<String, InstallArtifactStepInfo> installations;

    @Inject
    public InstallManager(Set<Artifact> artifacts) {
        this.artifacts = new Commons.ArtifactsSet(artifacts); // keep order
        this.installations = new ConcurrentHashMap<>();
    }

    /**
     * Scans all installed artifacts and returns their versions.
     *
     * @throws java.io.IOException
     *         if an I/O error occurred
     */
    public Map<Artifact, Version> getInstalledArtifacts() throws IOException {
        Map<Artifact, Version> installed = new LinkedHashMap<>();
        for (Artifact artifact : artifacts) {
            try {
                Optional<Version> installedVersion = artifact.getInstalledVersion();
                if (installedVersion.isPresent()) {
                    installed.put(artifact, installedVersion.get());
                }
            } catch (IOException e) {
                throw getProperException(e, artifact);
            }
        }

        return installed;
    }

    /**
     * Installs specific artifact.
     *
     * @param artifact
     *        Artifact
     * @param version
     *        Version
     * @param pathToBinaries
     *        Path, path to binary
     * @param options
     *        installation option
     * @param isBinaryFromRepo
     *        true, if the binaries from repository, will be check it in remote repository
     *        false, if the binaries is local, no any checks in remote repository
     * @return
     * @throws IOException
               if an I/O error occurred
     */
    public String performInstallStep(final Artifact artifact,
                                     final Version version,
                                     final Path pathToBinaries,
                                     final InstallOptions options,
                                     boolean isBinaryFromRepo) throws IOException {
        return performStep(artifact, version, pathToBinaries, options, false, isBinaryFromRepo);
    }

    /**
     * Updates specific artifact.
     *
     * @param artifact
     *        Artifact
     * @param version
     *        Version
     * @param pathToBinaries
     *        Path, path to binary
     * @param options
     *        installation option
     * @param isBinaryFromRepo
     *        true, if the binaries from repository, will be check it in remote repository
     *        false, if the binaries is local, no any checks in remote repository
     * @return
     * @throws IOException
    if an I/O error occurred
     */
    public String performUpdateStep(Artifact artifact, Version version, Path pathToBinaries, InstallOptions options, boolean isBinaryFromRepo) throws IOException {
        return performStep(artifact, version, pathToBinaries, options, true, isBinaryFromRepo);
    }

    protected String performStep(final Artifact artifact,
                                 final Version version,
                                 final Path pathToBinaries,
                                 final InstallOptions options,
                                 final boolean isUpdate,
                                 final boolean isBinaryFromRepo) throws IOException {

        if (isBinaryFromRepo) {
            if (options.getStep() == 0 && !isInstallable(artifact, version)) {
                throw new IllegalStateException(format("%s:%s is not installable", artifact.getName(), version.toString()));
            }
        }

        final String stepId = UUID.randomUUID().toString();
        final InstallArtifactStepInfo info = new InstallArtifactStepInfo();
        info.setArtifact(artifact.getName());
        info.setVersion(version.toString());
        info.setStep(options.getStep());
        info.setStatus(InstallArtifactInfo.Status.IN_PROGRESS);

        installations.put(stepId, info);

        Thread thread = new Thread() {
            @Override
            public void run() {
                synchronized (info) {
                    try {
                        Command command = isUpdate ? artifact.getUpdateCommand(version, pathToBinaries, options)
                                                   : artifact.getInstallCommand(version, pathToBinaries, options);
                        executeCommand(command);

                        info.setStatus(InstallArtifactInfo.Status.SUCCESS);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, e.getMessage(), e);
                        info.setMessage(e.getMessage());
                        info.setStatus(InstallArtifactInfo.Status.FAILURE);
                    } finally {
                        info.notifyAll();
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();

        return stepId;
    }

    /**
     * Waits while installation step is completed.
     */
    public void waitForStepCompleted(String stepId) throws InterruptedException, InstallationNotStartedException {
        InstallArtifactStepInfo info = installations.get(stepId);
        if (info != null) {
            synchronized (info) {
                while (info.getStatus() == InstallArtifactInfo.Status.IN_PROGRESS) {
                    info.wait();
                }
            }
        } else {
            throw new InstallationNotStartedException();
        }
    }

    /**
     * @return installation step info
     */
    public InstallArtifactStepInfo getUpdateStepInfo(String stepId) throws InstallationNotStartedException {
        InstallArtifactStepInfo info = installations.get(stepId);
        if (info != null) {
            return info;
        } else {
            throw new InstallationNotStartedException();
        }
    }

    /** @return the list with descriptions of installation steps */
    public List<String> getInstallInfo(Artifact artifact, InstallType installType) throws IOException {
        return artifact.getInstallInfo(installType);
    }

    /** @return the list with descriptions of installation steps */
    public List<String> getUpdateInfo(Artifact artifact, InstallType installType) throws IOException {
        return artifact.getUpdateInfo(installType);
    }

    public boolean isInstallable(Artifact artifact, Version version) throws IOException {
        return artifact.isInstallable(version);
    }

    /**
     * Re-install artifact.
     */
    public void performReinstall(Artifact artifact) throws IOException {
        Command command = artifact.getReinstallCommand();
        executeCommand(command);
    }

    protected String executeCommand(Command command) throws CommandException {
        return command.execute();
    }
}
