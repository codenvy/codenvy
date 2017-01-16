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

import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.InjectorBootstrap;
import com.codenvy.im.utils.Version;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static com.codenvy.im.artifacts.ArtifactProperties.PREVIOUS_VERSION_PROPERTY;
import static com.codenvy.im.utils.Commons.asMap;
import static com.codenvy.im.utils.Commons.combinePaths;
import static java.lang.String.format;
import static java.nio.file.Files.newInputStream;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractArtifact implements Artifact {
    private final   String        name;
    protected final HttpTransport transport;
    protected final ConfigManager configManager;
    protected final String        updateServerEndpoint;
    protected final Path          downloadDir;

    private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(AbstractArtifact.class.getSimpleName());

    public AbstractArtifact(String name,
                            String updateServerEndpoint,
                            String downloadDir,
                            HttpTransport transport,
                            ConfigManager configManager) {
        this.downloadDir = Paths.get(downloadDir);
        this.name = name;
        this.transport = transport;
        this.configManager = configManager;
        this.updateServerEndpoint = updateServerEndpoint;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractArtifact)) {
            return false;
        }

        AbstractArtifact that = (AbstractArtifact)o;

        return name.equals(that.name);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int compareTo(Artifact o) {
        return getPriority() - o.getPriority();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInstallable(Version versionToInstall) throws IOException {
        Optional<Version> installedVersion = getInstalledVersion();

        if (!installedVersion.isPresent()) {
            return true;
        }

        String allowedPreviousVersions = getProperty(versionToInstall, PREVIOUS_VERSION_PROPERTY);
        if (allowedPreviousVersions != null) {
            return installedVersion.get().isSuitedFor(allowedPreviousVersions);
        }

        return installedVersion.get().compareTo(versionToInstall) < 0;
    }

    /** {@inheritDoc} */
    @Nullable
    public Version getLatestInstallableVersion() throws IOException {
        List<Map.Entry<Artifact, Version>> allUpdates = getAllUpdates();
        if (allUpdates == null || allUpdates.isEmpty()) {
            return null;
        }

        Optional<Version> installedVersion = getInstalledVersion();
        if (!installedVersion.isPresent()) {
            return allUpdates.get(allUpdates.size() - 1).getValue();  // get last one so as list of updates should be sorted by version ascendantly
        }

        Version ver2Install = null;
        for (Map.Entry<Artifact, Version> entry : allUpdates) {
            Version version2Check = entry.getValue();

            String prevAllowedVersionNumber = getProperty(version2Check, PREVIOUS_VERSION_PROPERTY);
            if (installedVersion.get().isSuitedFor(prevAllowedVersionNumber)) {
                if (ver2Install == null || ver2Install.compareTo(version2Check) < 0) {
                    ver2Install = version2Check;
                }
            }
        }

        return ver2Install;
    }

    /**
     * @return stable versions only
     */
    protected List<Map.Entry<Artifact, Version>> getAllUpdates() throws IOException {
        DownloadManager downloadManager = getDownloadManager();
        return downloadManager.getAllUpdates(this, true)
                              .stream()
                              .filter((Map.Entry<Artifact, Version> update) -> {
                                  Artifact artifact = update.getKey();
                                  Version version = update.getValue();

                                  try {
                                      Optional<VersionLabel> label = artifact.getLabel(version);
                                      return VersionLabel.STABLE.equals(label.orElse(null));
                                  } catch (IOException e) {
                                      throw new RuntimeException(e.getMessage(), e);
                                  }
                              }).collect(Collectors.toList());
    }

    /**
     * for testing propose.
     */
    protected DownloadManager getDownloadManager() {
        return InjectorBootstrap.INJECTOR.getInstance(DownloadManager.class);
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, String> getProperties(Version version) throws IOException {
        Map<String, String> propertiesMap = fetchPropertiesFromLocalFile(version);
        if (propertiesMap != null) {
            return propertiesMap;
        }

        return fetchPropertiesFromUpdateServer(version);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getProperty(Version version, String name) throws IOException {
        Map<String, String> properties = getProperties(version);
        return properties.get(name);
    }

    @Override
    public Optional<VersionLabel> getLabel(Version version) throws IOException {
        try {
            String labelStr = getProperty(version, ArtifactProperties.LABEL_PROPERTY);
            if (labelStr == null) {
                return Optional.empty();
            }

            VersionLabel versionLabel = VersionLabel.valueOf(labelStr.toUpperCase());
            return Optional.of(versionLabel);
        } catch(IOException | IllegalArgumentException e) {
            LOG.log(Level.WARNING, format("Error of getting label of artifact %s:%s", name, version), e);
            return Optional.empty();
        }
    }

    /**
     * Try to load properties from directory with binaries.
     * @return null if there is no properties file in the update directory, or there was an exception.
     */
    @Nullable
    protected Map<String, String> fetchPropertiesFromLocalFile(Version version) {
        Path pathToProperties = getDownloadDirectory(version).resolve(Artifact.ARTIFACT_PROPERTIES_FILE_NAME);
        if (!Files.exists(pathToProperties)) {
            return null;
        }

        try (InputStream in = newInputStream(pathToProperties)) {
            Properties properties = new Properties();
            properties.load(in);

            Map<String, String> propertiesMap = new HashMap<>();

            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();

                propertiesMap.put(key, value);
            }

            return propertiesMap;
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * If version is not specified then the properties of the latest stable version will be retrieved
     */
    private Map<String, String> fetchPropertiesFromUpdateServer(@Nullable Version version) throws IOException {
        String requestUrl = combinePaths(updateServerEndpoint, "repository/properties", getName() + (version != null ? "/" + version.toString()
                                                                                                               : ""));
        try {
            Map m = asMap(transport.doGet(requestUrl));
            return Collections.checkedMap(m, String.class, String.class);
        } catch (JsonParseException e) {
            throw new IOException(e);
        }
    }

    private Path getDownloadDirectory(Version version) {
        return downloadDir.resolve(getName()).resolve(version.toString());
    }
}
