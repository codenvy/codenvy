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
import com.codenvy.im.commands.Command;
import com.codenvy.im.utils.TarUtils;
import com.codenvy.im.utils.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static com.codenvy.im.managers.BackupConfig.removeGzipExtension;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;

/** @author Dmytro Nochevnov */
@Singleton
public class BackupManager {

    private String defaultBackupDir;

    @Inject
    public BackupManager(@Named("installation-manager.backup_dir") String defaultBackupDir) throws IOException {
        this.defaultBackupDir = defaultBackupDir;
    }

    /**
     * Backup due to config
     * @param initialConfig initial backup config
     * @return updated backup config
     */
    public BackupConfig backup(BackupConfig initialConfig) throws IOException, IllegalStateException {
        BackupConfig backupConfig = initialConfig.clone();
        if (isNullOrEmpty(backupConfig.getBackupDirectory())) {
            backupConfig.setBackupDirectory(defaultBackupDir);
        }

        Artifact artifact = getArtifact(backupConfig.getArtifactName());
        Files.createDirectories(Paths.get(backupConfig.getBackupDirectory()));

        try {
            Path backupFile = backupConfig.generateBackupFilePath();
            backupConfig.setBackupFile(backupFile.toString());

            Optional<Version> artifactVersion = artifact.getInstalledVersion();
            if (!artifactVersion.isPresent()) {
                throw new IllegalStateException("Artifact version is unavailable");
            }
            backupConfig.setArtifactVersion(artifactVersion.get().toString());
            backupConfig.storeConfigIntoBackup();

            Command backupCommand = artifact.getBackupCommand(backupConfig);
            backupCommand.execute();

            Path compressedBackupFile = BackupConfig.addGzipExtension(backupFile);
            TarUtils.compressFile(backupFile, compressedBackupFile);

            Files.deleteIfExists(backupFile);
            backupConfig.setBackupFile(compressedBackupFile.toString());
        } catch(Exception e) {
            throw new BackupException(e.getMessage(), e);
        }

        return backupConfig;
    }

    /** Restore due to config */
    public void restore(BackupConfig initialConfig) throws IOException, IllegalArgumentException {
        BackupConfig backupConfig = initialConfig.clone();
        if (backupConfig.getBackupFile() == null
            || backupConfig.getBackupFile().isEmpty()) {
            throw new IllegalArgumentException("Backup file is unknown.");
        }

        Path compressedBackupFile = Paths.get(backupConfig.getBackupFile());
        if (!Files.exists(compressedBackupFile)) {
            throw new IllegalArgumentException(format("Backup file '%s' doesn't exist.", compressedBackupFile));
        }

        try {
            Artifact artifact = getArtifact(backupConfig.getArtifactName());
            Path tempDir = backupConfig.obtainArtifactTempDirectory();

            TarUtils.uncompress(compressedBackupFile, tempDir);

            String backupFileName = removeGzipExtension(compressedBackupFile).getFileName().toString();
            Path backupFile = tempDir.resolve(backupFileName);
            backupConfig.setBackupFile(backupFile.toString());
            backupConfig.setBackupDirectory(tempDir.toString());

            BackupConfig storedBackupConfig = backupConfig.extractConfigFromBackup();
            checkBackup(artifact, storedBackupConfig);

            Command restoreCommand = artifact.getRestoreCommand(backupConfig);
            restoreCommand.execute();
        } catch(IllegalArgumentException | IllegalStateException | BackupException e) {
            throw e;
        } catch(Exception e) {
            throw new BackupException(e.getMessage(), e);
        }
    }

    protected void checkBackup(Artifact restoringArtifact, BackupConfig storedBackupConfig) throws IllegalArgumentException, IllegalStateException {
        String backedUpArtifactName = storedBackupConfig.getArtifactName();
        String restoringArtifactName = restoringArtifact.getName();
        if (!restoringArtifactName.equals(backedUpArtifactName)) {
            throw new IllegalArgumentException(format("Backed up artifact '%s' doesn't equal to restoring artifact '%s'",
                                                      backedUpArtifactName,
                                                      restoringArtifactName));
        }

        String backedUpArtifactVersion = storedBackupConfig.getArtifactVersion();
        Optional<Version> restoringArtifactVersion;
        String nullVersionErrorMessage = format("It is impossible to get version of restoring artifact '%s'", restoringArtifactName);
        try {
            restoringArtifactVersion = restoringArtifact.getInstalledVersion();
        } catch (IOException e) {
            throw new IllegalStateException(nullVersionErrorMessage);
        }
        if (!restoringArtifactVersion.isPresent()) {
            throw new IllegalStateException(nullVersionErrorMessage);
        }

        if (!restoringArtifactVersion.get().toString().equals(backedUpArtifactVersion)) {
            throw new IllegalArgumentException(format("Version of backed up artifact '%s' doesn't equal to restoring version '%s'",
                                                      backedUpArtifactVersion,
                                                      restoringArtifactVersion.get()));
        }
    }

    protected Artifact getArtifact(String artifactName) throws ArtifactNotFoundException {
        return createArtifact(artifactName);
    }

}
