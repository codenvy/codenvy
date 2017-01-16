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

import com.codenvy.im.commands.Command;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.utils.Version;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Anatoliy Bazko
 */
public interface Artifact extends Comparable<Artifact> {

    String ARTIFACT_PROPERTIES_FILE_NAME = ".properties";

    /** @return the artifact name */
    String getName();

    /** @return current installed version of the artifact or null if artifact is not installed. */
    Optional<Version> getInstalledVersion() throws IOException;

    /** @return the priority of the artifact to install, update etc. */
    int getPriority();

    /** @return the some information about future installation process */
    List<String> getInstallInfo(InstallType installType) throws IOException;

    /** @return the some information about future installation process */
    List<String> getUpdateInfo(InstallType installType) throws IOException;

    /** @return list of commands to perform installation. */
    Command getInstallCommand(Version versionToInstall, Path pathToBinaries, InstallOptions installOptions) throws IOException;

    /** @return list of commands to perform installation. */
    Command getUpdateCommand(Version versionToUpdate, Path pathToBinaries, InstallOptions installOptions) throws IOException;

    /** @return list of commands to perform re-installation. */
    Command getReinstallCommand() throws IOException;

    /**
     * @return the latest version to install
     */
    Version getLatestInstallableVersion() throws IOException;

    /**
     * @return true if given version of the artifact can be installed, in general case versionToInstall should be greater than current installed and
     * version of the artifact
     */
    boolean isInstallable(Version versionToInstall) throws IOException;

    /**
     * @return true if given artifact is alive, that is active at the time of verification
     */
    boolean isAlive();

    /** @return properties stored at update server */
    Map<String, String> getProperties(Version version) throws IOException;

    /** @return property stored at update server */
    @Nullable
    String getProperty(Version version, String name) throws IOException;

    /** @return command to collect backup data at the certain backup directory */
    Command getBackupCommand(BackupConfig backupConfig) throws IOException;

    /** @return command to restore data from backup */
    Command getRestoreCommand(BackupConfig backupConfig) throws IOException;

    /** Updates artifact configuration */
    void updateConfig(Map<String, String> properties) throws IOException;

    /** @return config of installed artifact */
    Map<String, String> getConfig() throws IOException;

    /** Get label property of artifact */
    Optional<VersionLabel> getLabel(Version version) throws IOException;
}
