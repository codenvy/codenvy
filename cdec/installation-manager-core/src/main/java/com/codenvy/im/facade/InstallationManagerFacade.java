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
package com.codenvy.im.facade;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.CDECArtifact;
import com.codenvy.im.event.Event;
import com.codenvy.im.managers.BackupConfig;
import com.codenvy.im.managers.BackupManager;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadAlreadyStartedException;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.managers.DownloadNotStartedException;
import com.codenvy.im.managers.InstallManager;
import com.codenvy.im.managers.InstallOptions;
import com.codenvy.im.managers.InstallType;
import com.codenvy.im.managers.InstallationNotStartedException;
import com.codenvy.im.managers.LdapManager;
import com.codenvy.im.managers.NodeConfig;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.StorageManager;
import com.codenvy.im.response.ArtifactInfo;
import com.codenvy.im.response.BackupInfo;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.InstallArtifactStepInfo;
import com.codenvy.im.response.NodeInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.saas.SaasAuthServiceProxy;
import com.codenvy.im.saas.SaasRepositoryServiceProxy;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.InjectorBootstrap;
import com.codenvy.im.utils.Version;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;
import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
@Singleton
public class InstallationManagerFacade {
    protected final HttpTransport              transport;
    protected final SaasAuthServiceProxy       saasAuthServiceProxy;
    protected final ConfigManager              configManager;
    protected final SaasRepositoryServiceProxy saasRepositoryServiceProxy;
    protected final LdapManager                ldapManager;
    protected final NodeManager                nodeManager;
    protected final BackupManager              backupManager;
    protected final StorageManager             storageManager;
    protected final InstallManager             installManager;
    protected final DownloadManager            downloadManager;

    @Inject
    public InstallationManagerFacade(HttpTransport transport,
                                     ConfigManager configManager,
                                     SaasAuthServiceProxy saasAuthServiceProxy,
                                     SaasRepositoryServiceProxy saasRepositoryServiceProxy,
                                     LdapManager ldapManager,
                                     NodeManager nodeManager,
                                     BackupManager backupManager,
                                     StorageManager storageManager,
                                     InstallManager installManager,
                                     DownloadManager downloadManager) {
        this.configManager = configManager;
        this.saasRepositoryServiceProxy = saasRepositoryServiceProxy;
        this.installManager = installManager;
        this.downloadManager = downloadManager;
        this.transport = transport;
        this.saasAuthServiceProxy = saasAuthServiceProxy;
        this.ldapManager = ldapManager;
        this.nodeManager = nodeManager;
        this.backupManager = backupManager;
        this.storageManager = storageManager;
    }

    /**
     * Makes a request to download Audit report and save it to audit directory.
     */
    public void requestAuditReport(String authToken, String host) throws IOException {

        Path path = Paths.get(InjectorBootstrap.INJECTOR.getInstance(Key.get(String.class, Names.named("installation-manager.audit_dir"))));

        if (configManager.getHostUrl().equals(host)) {
            transport.downloadWithoutProxy(host + "/api/audit", authToken, path, MediaType.TEXT_PLAIN);
        } else {
            transport.download(host + "/api/audit", authToken, path, MediaType.TEXT_PLAIN);
        }

    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#startDownload(com.codenvy.im.artifacts.Artifact, com.codenvy.im.utils.Version)
     */
    public void startDownload(@Nullable Artifact artifact, @Nullable Version version) throws InterruptedException,
                                                                                             DownloadAlreadyStartedException,
                                                                                             IOException {
        downloadManager.startDownload(artifact, version);
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#stopDownload()
     */
    public void stopDownload() throws DownloadNotStartedException, InterruptedException {
        downloadManager.stopDownload();
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getDownloadProgress()
     */
    public DownloadProgressResponse getDownloadProgress() throws DownloadNotStartedException, IOException {
        return downloadManager.getDownloadProgress();
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getDownloadIdInProgress()
     */
    public String getDownloadIdInProgress() throws DownloadNotStartedException {
        return downloadManager.getDownloadIdInProgress();
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getDownloadedArtifacts()
     */
    public Collection<DownloadArtifactInfo> getDownloads(@Nullable final Artifact artifact, @Nullable final Version version) throws IOException {
        Map<Artifact, SortedMap<Version, Path>> downloadedArtifacts = downloadManager.getDownloadedArtifacts();

        return FluentIterable.from(downloadedArtifacts.entrySet())
                             .transformAndConcat(entry -> {
                                 SortedMap<Version, Path> versions = entry.getValue();

                                 List<DownloadArtifactInfo> l = new ArrayList<>(versions.size());
                                 for (Map.Entry<Version, Path> versionEntry : versions.entrySet()) {
                                     DownloadArtifactInfo info = new DownloadArtifactInfo();
                                     info.setArtifact(entry.getKey().getName());
                                     info.setFile(versionEntry.getValue().toString());
                                     info.setVersion(versionEntry.getKey().toString());

                                     try {
                                         if (installManager.isInstallable(entry.getKey(), versionEntry.getKey())) {
                                             info.setStatus(DownloadArtifactInfo.Status.READY_TO_INSTALL);
                                         } else {
                                             info.setStatus(DownloadArtifactInfo.Status.DOWNLOADED);
                                         }
                                     } catch (IOException e) {
                                         info.setStatus(DownloadArtifactInfo.Status.DOWNLOADED);
                                     }

                                     l.add(info);
                                 }

                                 return l;
                             }).filter(downloadArtifactInfo -> artifact == null || downloadArtifactInfo.getArtifact().equals(artifact.getName()))
                             .filter(downloadArtifactInfo -> version == null || downloadArtifactInfo.getVersion().equals(version.toString())).toList();
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getUpdates()
     */
    public Collection<UpdateArtifactInfo> getUpdates() throws IOException {
        Map<Artifact, Version> updates = downloadManager.getUpdates();

        return updates.entrySet().stream()
                      .map(entry -> {
                          Artifact artifact = entry.getKey();
                          Version version = entry.getValue();

                          UpdateArtifactInfo info = UpdateArtifactInfo.createInstance(artifact.getName(), version.toString());
                          try {
                              if (downloadManager.getDownloadedVersions(artifact).containsKey(version)) {
                                  info.setStatus(UpdateArtifactInfo.Status.DOWNLOADED);
                              } else {
                                  info.setStatus(UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
                              }
                          } catch (IOException e) {
                              // simply ignore
                          }

                          return info;
                      })
                      .collect(Collectors.toList());
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getAllUpdates
     */
    public List<UpdateArtifactInfo> getAllUpdates(Artifact artifact) throws IOException, JsonParseException {
        return getAllUpdates(artifact, false);
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getAllUpdates
     */
    public List<UpdateArtifactInfo> getAllUpdatesAfterInstalledVersion(Artifact artifact) throws IOException, JsonParseException {
        return getAllUpdates(artifact, true);
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#getAllUpdates
     */
    private List<UpdateArtifactInfo> getAllUpdates(Artifact artifact, boolean afterInstalledVersion) throws IOException, JsonParseException {
        Collection<Map.Entry<Artifact, Version>> allUpdates = downloadManager.getAllUpdates(artifact, afterInstalledVersion);

        List<UpdateArtifactInfo> infos = new ArrayList<>();

        infos.addAll(allUpdates.stream()
                               .map(entry -> {
                                   Artifact artifact1 = entry.getKey();
                                   Version version = entry.getValue();

                                   UpdateArtifactInfo info = UpdateArtifactInfo.createInstance(artifact1.getName(), version.toString());
                                   try {
                                       if (downloadManager.getDownloadedVersions(artifact1).containsKey(version)) {
                                           info.setStatus(UpdateArtifactInfo.Status.DOWNLOADED);
                                       } else {
                                           info.setStatus(UpdateArtifactInfo.Status.AVAILABLE_TO_DOWNLOAD);
                                       }
                                   } catch (IOException e) {
                                       // simply ignore
                                   }

                                   return info;
                               })
                               .collect(Collectors.toList())
        );

        return infos;
    }

    /**
     * @see com.codenvy.im.managers.InstallManager#getInstalledArtifacts()
     */
    public Collection<InstallArtifactInfo> getInstalledVersions() throws IOException {
        Map<Artifact, Version> installedArtifacts = installManager.getInstalledArtifacts();

        return installedArtifacts.entrySet().stream()
                                 .map(entry -> InstallArtifactInfo.createInstance(entry.getKey().getName(),
                                                                                  entry.getValue().toString(),
                                                                                  InstallArtifactInfo.Status.SUCCESS))
                                 .collect(Collectors.toList());
    }

    /**
     * Merges two lists of downloaded and installed artifacts and returns a single one.
     */
    public Collection<ArtifactInfo> getArtifacts() throws IOException {
        Set<ArtifactInfo> infos = new TreeSet<>();

        final Map<Artifact, Version> installedArtifacts = installManager.getInstalledArtifacts();
        final Map<Artifact, SortedMap<Version, Path>> downloadedArtifacts = downloadManager.getDownloadedArtifacts();

        infos.addAll(installedArtifacts.entrySet().stream()
                                       .map(entry -> {
                                           ArtifactInfo info = new ArtifactInfo();
                                           info.setArtifact(entry.getKey().getName());
                                           info.setVersion(entry.getValue().toString());
                                           info.setStatus(ArtifactInfo.Status.INSTALLED);
                                           return info;
                                       }).collect(Collectors.toList()));

        infos.addAll(downloadedArtifacts.entrySet().stream()
                                   .flatMap(entry -> {
                                       SortedMap<Version, Path> versions = entry.getValue();

                                       List<ArtifactInfo> l = new ArrayList<>(versions.size());
                                       for (Map.Entry<Version, Path> versionEntry : versions.entrySet()) {
                                           if (!installedArtifacts.containsKey(entry.getKey())
                                               || !installedArtifacts.get(entry.getKey()).equals(versionEntry.getKey())) {

                                               ArtifactInfo info = new ArtifactInfo();

                                               info.setArtifact(entry.getKey().getName());
                                               info.setVersion(versionEntry.getKey().toString());

                                               try {
                                                   if (installManager.isInstallable(entry.getKey(), versionEntry.getKey())) {
                                                       info.setStatus(ArtifactInfo.Status.READY_TO_INSTALL);
                                                   } else {
                                                       info.setStatus(ArtifactInfo.Status.DOWNLOADED);
                                                   }
                                               } catch (IOException e) {
                                                   info.setStatus(ArtifactInfo.Status.DOWNLOADED);
                                               }

                                               l.add(info);
                                           }
                                       }

                                       return l.stream();
                                   }).collect(Collectors.toList()));

        return infos;
    }

    /**
     * @see com.codenvy.im.managers.InstallManager#getInstallInfo
     */
    public List<String> getInstallInfo(Artifact artifact, InstallType installType) throws IOException {
        return installManager.getInstallInfo(artifact, installType);
    }

    /**
     * @see com.codenvy.im.managers.InstallManager#getUpdateInfo
     */
    public List<String> getUpdateInfo(Artifact artifact, InstallType installType) throws IOException {
        return installManager.getUpdateInfo(artifact, installType);
    }

    /**
     * @throws java.io.FileNotFoundException
     *         if binaries to install given artifact not found
     * @see com.codenvy.im.managers.InstallManager#performInstallStep
     */
    public String install(@NotNull Artifact artifact,
                          @NotNull Version version,
                          @NotNull InstallOptions installOptions) throws IOException {
        SortedMap<Version, Path> downloadedVersions = downloadManager.getDownloadedVersions(artifact);
        if (!downloadedVersions.containsKey(version)) {
            throw new FileNotFoundException(format("Binaries to install %s:%s not found", artifact.getName(), version.toString()));
        }

        return installManager.performInstallStep(artifact, version, downloadedVersions.get(version), installOptions, true);
    }

    /**
     * @throws java.io.FileNotFoundException
     *         if binaries to install given artifact not found
     * @see com.codenvy.im.managers.InstallManager#performInstallStep
     */
    public String install(@NotNull Artifact artifact,
                          @NotNull Version version,
                          @NotNull Path binaries,
                          @NotNull InstallOptions installOptions) throws IOException {
        return installManager.performInstallStep(artifact, version, binaries, installOptions, false);
    }

    /**
     * @throws java.io.FileNotFoundException
     *         if binaries to install given artifact not found
     * @see com.codenvy.im.managers.InstallManager#performUpdateStep
     */
    public String update(@NotNull Artifact artifact,
                         @NotNull Version version,
                         @NotNull InstallOptions installOptions) throws IOException {
        SortedMap<Version, Path> downloadedVersions = downloadManager.getDownloadedVersions(artifact);
        if (!downloadedVersions.containsKey(version)) {
            throw new FileNotFoundException(format("Binaries to install %s:%s not found", artifact.getName(), version.toString()));
        }

        return installManager.performUpdateStep(artifact, version, downloadedVersions.get(version), installOptions, true);
    }

    /**
     * @throws java.io.FileNotFoundException
     *         if binaries to install given artifact not found
     * @see com.codenvy.im.managers.InstallManager#performUpdateStep
     */
    public String update(@NotNull Artifact artifact,
                         @NotNull Version version,
                         @NotNull Path binaries,
                         @NotNull InstallOptions installOptions) throws IOException {
        return installManager.performUpdateStep(artifact, version, binaries, installOptions, false);
    }


    /**
     * @see com.codenvy.im.managers.InstallManager#waitForStepCompleted(String)
     */
    public void waitForInstallStepCompleted(String stepId) throws InstallationNotStartedException, InterruptedException {
        installManager.waitForStepCompleted(stepId);
    }

    /**
     * @return configuration of installed artifact
     */
    public Map<String, String> getArtifactConfig(Artifact artifact) throws IOException {
        return artifact.getConfig();
    }

    /**
     * @see com.codenvy.im.managers.NodeManager#add(String)
     */
    public NodeInfo addNode(@NotNull String dns) throws IOException {
        Artifact artifact = createArtifact(CDECArtifact.NAME);
        Version version = installManager.getInstalledArtifacts().get(artifact);

        if (version == null) {
            throw new IllegalStateException("Unknown installed Codenvy version.");
        }

        NodeConfig nodeConfig = nodeManager.add(dns);

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setType(nodeConfig.getType());
        nodeInfo.setHost(nodeConfig.getHost());

        return nodeInfo;
    }

    /**
     * @see com.codenvy.im.managers.NodeManager#remove(String)
     */
    public NodeInfo removeNode(@NotNull String dns) throws IOException {
        NodeConfig nodeConfig = nodeManager.remove(dns);

        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setType(nodeConfig.getType());
        nodeInfo.setHost(nodeConfig.getHost());

        return nodeInfo;
    }

    /**
     * @see com.codenvy.im.managers.NodeManager#getNodes()
     */
    public Map<String, List<String>> getNodes() throws IOException {
        return nodeManager.getNodes();
    }

    /**
     * @see com.codenvy.im.managers.BackupManager#backup(com.codenvy.im.managers.BackupConfig)
     */
    public BackupInfo backup(@NotNull BackupConfig config) throws IOException {
        BackupConfig backupConfig = backupManager.backup(config);

        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setArtifact(backupConfig.getArtifactName());
        backupInfo.setFile(backupConfig.getBackupFile());
        backupInfo.setVersion(backupConfig.getArtifactVersion());

        return backupInfo;
    }

    /**
     * @see com.codenvy.im.managers.BackupManager#restore(com.codenvy.im.managers.BackupConfig)
     */
    public BackupInfo restore(@NotNull BackupConfig backupConfig) throws IOException {
        backupManager.restore(backupConfig);

        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setArtifact(backupConfig.getArtifactName());
        backupInfo.setFile(backupConfig.getBackupFile());
        backupInfo.setVersion(backupConfig.getArtifactVersion());

        return backupInfo;
    }

    /**
     * @see com.codenvy.im.saas.SaasAuthServiceProxy#login(org.eclipse.che.api.auth.shared.dto.Credentials)
     */
    public Token loginToCodenvySaaS(@NotNull Credentials credentials) throws IOException, AuthenticationException {
        return saasAuthServiceProxy.login(credentials);
    }

    /**
     * @see com.codenvy.im.saas.SaasAuthServiceProxy#logout(String)
     */
    public void logoutFromCodenvySaaS(@NotNull String authToken) throws IOException {
        saasAuthServiceProxy.logout(authToken);
    }

    /**
     * @see com.codenvy.im.managers.LdapManager#getNumberOfUsers()
     */
    public long getNumberOfUsers() throws IOException {
        return ldapManager.getNumberOfUsers();
    }

    /**
     * @see com.codenvy.im.managers.StorageManager#storeProperties(java.util.Map)
     */
    public void storeStorageProperties(@Nullable Map<String, String> newProperties) throws IOException {
        storageManager.storeProperties(newProperties);
    }

    /**
     * @see com.codenvy.im.managers.StorageManager#loadProperties()
     */
    public Map<String, String> loadStorageProperties() throws IOException {
        return storageManager.loadProperties();
    }

    /**
     * @see com.codenvy.im.managers.StorageManager#loadProperty(String)
     */
    @Nullable
    public String loadStorageProperty(@Nullable String key) throws IOException {
        return key != null ? storageManager.loadProperty(key) : null;
    }

    /**
     * @see com.codenvy.im.managers.StorageManager#storeProperty(String, String)
     */
    public void storeStorageProperty(@Nullable String key, @Nullable String value) throws IOException {
        if (key != null && value != null) {
            storageManager.storeProperty(key, value);
        }
    }

    /**
     * @see com.codenvy.im.managers.StorageManager#deleteProperty(String)
     */
    public void deleteStorageProperty(@Nullable String name) throws IOException {
        if (name != null) {
            storageManager.deleteProperty(name);
        }
    }

    /** Update artifact config property */
    public void updateArtifactConfig(Artifact artifact, Map<String, String> properties) throws IOException {
        doUpdateArtifactConfig(artifact, properties);
    }

    protected void doUpdateArtifactConfig(Artifact artifact, Map<String, String> properties) throws IOException {
        artifact.updateConfig(properties);
    }

    /**
     * @see com.codenvy.im.managers.DownloadManager#deleteArtifact(com.codenvy.im.artifacts.Artifact, com.codenvy.im.utils.Version)
     */
    public void deleteDownloadedArtifact(Artifact artifact, Version version) throws IOException {
        downloadManager.deleteArtifact(artifact, version);
    }

    /**
     * @see com.codenvy.im.managers.InstallManager#getUpdateStepInfo(String)
     */
    public InstallArtifactStepInfo getUpdateStepInfo(String stepId) throws InstallationNotStartedException {
        return installManager.getUpdateStepInfo(stepId);
    }

    /**
     * @see com.codenvy.im.artifacts.Artifact#getLatestInstallableVersion()
     */
    public Version getLatestInstallableVersion(Artifact artifact) throws IOException {
        return artifact.getLatestInstallableVersion();
    }

    /**
     * @see com.codenvy.im.managers.InstallManager#performReinstall(com.codenvy.im.artifacts.Artifact)
     */
    public void reinstall(Artifact artifact) throws IOException {
        installManager.performReinstall(artifact);
    }

    /**
     * @see com.codenvy.im.saas.SaasRepositoryServiceProxy#logAnalyticsEvent(com.codenvy.im.event.Event, String)
     */
    public void logSaasAnalyticsEvent(Event event) throws IOException {
        logSaasAnalyticsEvent(event, null);
    }

    /**
     * @see com.codenvy.im.saas.SaasRepositoryServiceProxy#logAnalyticsEvent(com.codenvy.im.event.Event, String)
     */
    public void logSaasAnalyticsEvent(Event event, @Nullable String authToken) throws IOException {
        saasRepositoryServiceProxy.logAnalyticsEvent(event, authToken);
    }
}
