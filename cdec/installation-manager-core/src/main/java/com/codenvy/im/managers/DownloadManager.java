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
import com.codenvy.im.artifacts.InstallManagerArtifact;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.HttpException;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codenvy.im.artifacts.ArtifactProperties.FILE_NAME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.MD5_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.SIZE_PROPERTY;
import static com.codenvy.im.utils.Commons.calculateMD5Sum;
import static com.codenvy.im.utils.Commons.combinePaths;
import static com.codenvy.im.utils.Commons.fromJson;
import static com.codenvy.im.utils.Commons.getProperException;
import static com.codenvy.im.utils.Version.valueOf;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newOutputStream;
import static org.apache.commons.io.FileUtils.deleteDirectory;

/**
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
@Singleton
public class DownloadManager {

    private static final Logger LOG = Logger.getLogger(DownloadManager.class.getSimpleName());  // use java.util.logging instead of slf4j 
    // to prevent echo error message into the CLI console

    private final String        updateEndpoint;
    private final HttpTransport transport;
    private final Path          downloadDir;
    private final Set<Artifact> artifacts;

    protected DownloadProgress downloadProgress;

    @Inject
    public DownloadManager(@Named("installation-manager.update_server_endpoint") String updateEndpoint,
                           @Named("installation-manager.download_dir") String downloadDir,
                           HttpTransport transport,
                           Set<Artifact> artifacts) throws IOException {
        this.updateEndpoint = updateEndpoint;
        this.transport = transport;
        this.downloadDir = Paths.get(downloadDir);
        this.artifacts = new Commons.ArtifactsSet(artifacts); // keep order
        checkRWPermissions(this.downloadDir);
    }

    /** Starts downloading */
    public void startDownload(@Nullable final Artifact artifact, @Nullable final Version version) throws IOException,
                                                                                                         InterruptedException,
                                                                                                         DownloadAlreadyStartedException {

        validateIfDownloadInProgress();
        validateIfConnectionAvailable();

        final CountDownLatch latcher = new CountDownLatch(1);

        Thread downloadThread = new Thread("download thread") {
            public void run() {
                download(artifact, version, latcher);
            }
        };
        downloadThread.setDaemon(true);
        downloadThread.start();

        latcher.await();
    }

    public String getDownloadIdInProgress() throws DownloadNotStartedException {
        if (downloadProgress == null || downloadProgress.isDownloadingFinished()) {
            throw new DownloadNotStartedException();
        }
        return downloadProgress.getUuid();
    }

    private void validateIfDownloadInProgress() throws DownloadAlreadyStartedException {
        if (downloadProgress != null && !downloadProgress.isDownloadingFinished()) {
            throw new DownloadAlreadyStartedException();
        }
    }

    /** Interrupts downloading */
    public void stopDownload() throws DownloadNotStartedException, InterruptedException {
        if (downloadProgress == null || downloadProgress.isDownloadingFinished()) {
            throw new DownloadNotStartedException();
        }

        downloadProgress.getDownloadThread().interrupt();
        downloadProgress.getDownloadThread().join();
    }

    /** @return the current status of downloading process */
    public DownloadProgressResponse getDownloadProgress() throws DownloadNotStartedException, IOException {
        if (downloadProgress == null) {
            throw new DownloadNotStartedException();
        }

        List<DownloadArtifactInfo> infos = downloadProgress.getDownloadedArtifacts();
        for (Map.Entry<Artifact, Version> a : downloadProgress.getArtifacts2Download().entrySet()) {
            DownloadArtifactInfo info = new DownloadArtifactInfo();
            info.setArtifact(a.getKey().getName());
            info.setVersion(a.getValue().toString());
            info.setStatus(DownloadArtifactInfo.Status.DOWNLOADING);
            infos.add(info);
        }

        if (downloadProgress.getDownloadStatus() == DownloadArtifactInfo.Status.FAILED) {
            return new DownloadProgressResponse(downloadProgress.getDownloadStatus(),
                                                downloadProgress.getErrorMessage(),
                                                downloadProgress.getProgress(),
                                                infos);
        }

        return new DownloadProgressResponse(downloadProgress.getDownloadStatus(),
                                            downloadProgress.getProgress(),
                                            infos);
    }

    protected void download(@Nullable Artifact artifact,
                            @Nullable Version version,
                            CountDownLatch latcher) {
        invalidateDownloadDescriptor();

        try {
            Map<Artifact, Version> updatesToDownload = getLatestUpdatesToDownload(artifact, version);

            createDownloadDescriptor(updatesToDownload);
            checkEnoughDiskSpace(downloadProgress.getExpectedSize());

            latcher.countDown();

            for (Map.Entry<Artifact, Version> e : updatesToDownload.entrySet()) {
                Artifact artToDownload = e.getKey();
                Version verToDownload = e.getValue();

                try {
                    Path pathToBinaries = download(artToDownload, verToDownload);
                    validateMd5Sum(artToDownload, verToDownload, pathToBinaries);

                    DownloadArtifactInfo downloadArtifactDesc = new DownloadArtifactInfo(artToDownload,
                                                                                         verToDownload,
                                                                                         pathToBinaries,
                                                                                         DownloadArtifactInfo.Status.DOWNLOADED);
                    downloadProgress.addDownloadedArtifact(downloadArtifactDesc);
                    saveArtifactProperties(artToDownload, verToDownload, pathToBinaries);
                } catch (Exception exp) {
                    LOG.log(Level.SEVERE, exp.getMessage(), exp);
                    DownloadArtifactInfo info = new DownloadArtifactInfo(artToDownload,
                                                                         verToDownload,
                                                                         DownloadArtifactInfo.Status.FAILED);
                    downloadProgress.addDownloadedArtifact(info);
                    downloadProgress.setDownloadStatus(DownloadArtifactInfo.Status.FAILED, exp);
                    return;
                }
            }

            downloadProgress.setDownloadStatus(DownloadArtifactInfo.Status.DOWNLOADED);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);

            if (downloadProgress == null) {
                downloadProgress = new DownloadProgress(Collections.<Path, Long>emptyMap(), Collections.<Artifact, Version>emptyMap());
            }
            downloadProgress.setDownloadStatus(DownloadArtifactInfo.Status.FAILED, e);

            if (latcher.getCount() == 1) {
                latcher.countDown();
            }
        }
    }

    protected void validateMd5Sum(Artifact artToDownload, Version verToDownload, Path pathToBinaries) throws IOException {
        String md5sum = artToDownload.getProperty(verToDownload, MD5_PROPERTY);
        if (!md5sum.equals(calculateMD5Sum(pathToBinaries))) {
            throw new IOException("File corrupted. Redownload artifact.");
        }
    }

    /**
     * Download the specific version of the artifact.
     *
     * @return path to downloaded artifact
     * @throws java.io.IOException
     *         if an I/O error occurred
     * @throws java.lang.IllegalStateException
     *         if the subscription is invalid or expired
     */
    protected Path download(Artifact artifact, Version version) throws IOException, IllegalStateException {
        try {
            String requestUrl = combinePaths(updateEndpoint, "/repository/public/download/" + artifact.getName() + "/" + version);

            Path artifactDownloadDir = getDownloadDirectory(artifact, version);
            deleteDirectory(artifactDownloadDir.toFile());

            return transport.download(requestUrl, artifactDownloadDir, MediaType.APPLICATION_OCTET_STREAM);
        } catch (IOException e) {
            throw getProperException(e, artifact);
        }
    }

    /** Checks if FS has enough free space, for instance to download artifacts */
    protected void checkEnoughDiskSpace(long requiredSize) throws IOException {
        long freeSpace = downloadDir.toFile().getFreeSpace();
        if (freeSpace < requiredSize) {
            throw new IOException(format("Not enough disk space. Required %d bytes but available only %d bytes", requiredSize, freeSpace));
        }
    }

    /** Checks connection to server is available */
    public void validateIfConnectionAvailable() throws IOException {
        transport.doGet(combinePaths(updateEndpoint, "repository/properties/" + InstallManagerArtifact.NAME));
    }

    /**
     * @return path to artifact into the local repository
     * @throws java.io.IOException
     *         if an I/O error occurred
     */
    protected Path getPathToBinaries(Artifact artifact, Version version) throws IOException {
        Map properties = artifact.getProperties(version);
        String fileName = properties.get(FILE_NAME_PROPERTY).toString();

        return getDownloadDirectory(artifact, version).resolve(fileName);
    }

    /**
     * @return size in bytes of the artifact
     * @throws java.io.IOException
     *         if an I/O error occurred
     */
    protected Long getBinariesSize(Artifact artifact, Version version) throws IOException, NumberFormatException {
        Map properties = artifact.getProperties(version);
        String size = properties.get(SIZE_PROPERTY).toString();

        return Long.valueOf(size);
    }

    /**
     * @return downloaded artifacts from the local repository
     * @throws IOException
     *         if an I/O error occurs
     */
    public Map<Artifact, SortedMap<Version, Path>> getDownloadedArtifacts() throws IOException {
        Map<Artifact, SortedMap<Version, Path>> downloaded = new LinkedHashMap<>(artifacts.size());

        for (Artifact artifact : artifacts) {
            SortedMap<Version, Path> versions = getDownloadedVersions(artifact);

            if (!versions.isEmpty()) {
                downloaded.put(artifact, versions);
            }
        }

        return downloaded;
    }

    /**
     * Determines the latest versions to download taking into account if artifacts have been already downloaded.
     */
    public Map<Artifact, Version> getLatestUpdatesToDownload(@Nullable Artifact artifact, @Nullable Version version) throws IOException {
        if (artifact == null) { // all artifacts the latest versions
            Map<Artifact, Version> latestUpdates = getUpdates();
            Map<Artifact, Version> artifact2Downloads = new TreeMap<>(latestUpdates);

            for (Map.Entry<Artifact, Version> e : latestUpdates.entrySet()) {
                Artifact eachArtifact = e.getKey();
                Version eachVersion = e.getValue();

                if (getDownloadedVersions(eachArtifact).containsKey(eachVersion)) {
                    artifact2Downloads.remove(eachArtifact);
                }
            }

            return artifact2Downloads;
        } else {
            if (version == null) {
                version = artifact.getLatestInstallableVersion();
                if (version == null) {
                    return Collections.emptyMap(); // nothing to download
                }
            }

            if (getDownloadedVersions(artifact).containsKey(version)) {
                return Collections.emptyMap();
            }

            return ImmutableMap.of(artifact, version);
        }
    }

    /**
     * Gets a list of artifacts to download.
     * Only newer versions if the installed artifacts will be returned.
     *
     * @throws java.io.IOException
     *         if an I/O error occurred
     */
    public Map<Artifact, Version> getUpdates() throws IOException {
        Map<Artifact, Version> newVersions = new LinkedHashMap<>();

        Version newVersion;
        for (Artifact artifact : artifacts) {
            try {
                newVersion = artifact.getLatestInstallableVersion();
            } catch (HttpException e) {
                // ignore update server error when there is no version of certain artifact there
                if (e.getStatus() == javax.ws.rs.core.Response.Status.NOT_FOUND.getStatusCode()) {
                    continue;
                }
                throw e;
            }

            if (newVersion != null) {
                newVersions.put(artifact, newVersion);
            }
        }

        return newVersions;
    }

    /**
     * Gets all updates for given artifact.
     *
     * @param artifact artifact which versions should be returned;
     * @param afterInstalledVersion <b>true</b> to return versions newer than installed one; <b>false</b> - to return all versions.
     * @throws java.io.IOException
     *         if an I/O error occurred
     */
    public Collection<Map.Entry<Artifact, Version>> getAllUpdates(@Nullable final Artifact artifact, boolean afterInstalledVersion) throws IOException {
        ArrayList<Map.Entry<Artifact, Version>> allUpdates = new ArrayList<>();

        Set<Artifact> artifacts2Check;
        if (artifact != null) {
            artifacts2Check = ImmutableSet.of(artifact);
        } else {
            artifacts2Check = Collections.unmodifiableSet(artifacts);
        }

        for (final Artifact art2Check : artifacts2Check) {
            Optional<Version> installedVersion = art2Check.getInstalledVersion();
            boolean fromVersion = afterInstalledVersion && installedVersion.isPresent();
            String requestUrl = combinePaths(updateEndpoint,
                                             "repository/updates",
                                             art2Check + (fromVersion ? "?fromVersion=" + installedVersion.get().toString() : ""));
            try {
                List<String> l = fromJson(transport.doGet(requestUrl), List.class);
                allUpdates.addAll(FluentIterable.from(l).transform(new Function<String, Map.Entry<Artifact, Version>>() {
                    @Override
                    public Map.Entry<Artifact, Version> apply(String version) {
                        return new AbstractMap.SimpleEntry<>(art2Check, Version.valueOf(version));
                    }
                }).toList());
            } catch (JsonParseException e) {
                throw new IOException(e);
            }
        }

        return allUpdates;
    }

    private void checkRWPermissions(Path downloadDir) throws IOException {
        try {
            createDirectories(downloadDir);

            Path tmp = downloadDir.resolve("tmp.tmp");
            createFile(tmp);
            delete(tmp);
        } catch (IOException e) {
            throw new IOException("Installation Manager probably doesn't have r/w permissions to " + downloadDir.toString(), e);
        }
    }

    private Path getDownloadDirectory(Artifact artifact, Version version) {
        return downloadDir.resolve(artifact.getName()).resolve(version.toString());
    }

    private Path getDownloadDirectory(Artifact artifact) {
        return downloadDir.resolve(artifact.getName());
    }

    /**
     * @return sorted set of the downloaded version of the given artifact
     * @throws IOException
     *         if an I/O error occurs
     */
    public SortedMap<Version, Path> getDownloadedVersions(Artifact artifact) throws IOException {
        SortedMap<Version, Path> versions = new TreeMap<>(new Version.ReverseOrderComparator());

        Path artifactDir = getDownloadDirectory(artifact);

        if (exists(artifactDir)) {
            try (DirectoryStream<Path> paths = newDirectoryStream(artifactDir)) {

                for (Path versionDir : paths) {
                    try {
                        if (isDirectory(versionDir)) {
                            Version version = valueOf(versionDir.getFileName().toString());

                            Map<String, String> properties = artifact.getProperties(version);
                            String fileName = properties.get(FILE_NAME_PROPERTY);

                            Path binaryFile = versionDir.resolve(fileName);
                            Path propertyFile = versionDir.resolve(Artifact.ARTIFACT_PROPERTIES_FILE_NAME);
                            if (exists(binaryFile) && exists(propertyFile)) {  // check is artifact completely downloaded
                                versions.put(version, binaryFile);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // maybe it isn't a version directory
                    }
                }
            }
        }

        return versions;
    }

    /** Factory method */
    protected void createDownloadDescriptor(Map<Artifact, Version> artifacts) throws IOException {
        Map<Path, Long> binaries = new LinkedHashMap<>();

        for (Map.Entry<Artifact, Version> e : artifacts.entrySet()) {
            Artifact artifact = e.getKey();
            Version version = e.getValue();

            Path pathToBinaries = getPathToBinaries(artifact, version);
            Long binariesSize = getBinariesSize(artifact, version);

            binaries.put(pathToBinaries, binariesSize);
        }

        downloadProgress = new DownloadProgress(binaries, artifacts);
    }

    /** Delete binaries of already downloaded artifact */
    public void deleteArtifact(Artifact artifact, Version version) throws IOException {
        if (downloadProgress != null) {
            Map<Artifact, Version> artifacts2Download = downloadProgress.getArtifacts2Download();
            if (artifacts2Download.containsKey(artifact) &&
                artifacts2Download.get(artifact).equals(version)) {
                throw new IllegalStateException(
                        format("Artifact '%s' version '%s' is being downloaded and cannot be deleted.", artifact.getName(), version.toString()));
            }
        }

        Path pathToBinary = getPathToBinaries(artifact, version);
        if (exists(pathToBinary)) {
            deleteDirectory(pathToBinary.getParent().toFile());
        }
    }

    protected void invalidateDownloadDescriptor() {
        downloadProgress = null;
    }

    /** Save artifact properties into the ".properties" file in the artifact download directory */
    protected void saveArtifactProperties(Artifact artToDownload, Version verToDownload, Path pathToBinaries) throws IOException {
        Map<String, String> propertiesMap = artToDownload.getProperties(verToDownload);
        Path pathToPropertiesFile = pathToBinaries.getParent().resolve(Artifact.ARTIFACT_PROPERTIES_FILE_NAME);

        Properties properties = new Properties();
        properties.putAll(propertiesMap);

        try (OutputStream out = newOutputStream(pathToPropertiesFile)) {
            properties.store(out, null);
        }
    }
}
