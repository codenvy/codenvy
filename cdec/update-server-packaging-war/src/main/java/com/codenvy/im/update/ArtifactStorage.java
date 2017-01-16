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
package com.codenvy.im.update;

import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.ArtifactProperties;
import com.codenvy.im.utils.Commons;
import com.codenvy.im.utils.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.commons.io.IOUtils;

import org.eclipse.che.commons.annotation.Nullable;
import javax.inject.Named;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import static com.codenvy.im.artifacts.ArtifactProperties.ARTIFACT_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.AUTHENTICATION_REQUIRED_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.FILE_NAME_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.SUBSCRIPTION_PROPERTY;
import static com.codenvy.im.artifacts.ArtifactProperties.VERSION_PROPERTY;
import static com.codenvy.im.utils.Commons.getVersionsList;
import static java.lang.String.format;
import static java.nio.file.Files.newOutputStream;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ArtifactStorage {

    public static final String PROPERTIES_FILE = ".properties";

    private final String repositoryDir;

    @Inject
    public ArtifactStorage(@Named("update-server.repository.path") String repositoryDir) throws IOException {
        this.repositoryDir = repositoryDir;
        Files.createDirectories(Paths.get(repositoryDir));
    }

    /**
     * @return the latest available version of the artifact in the repository filtered by certain label.
     * @throws com.codenvy.im.artifacts.ArtifactNotFoundException
     *         if artifact is absent in the repository
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    public String getLatestVersion(String artifact, @Nullable String expectedLabel) throws IOException {
        Path dir = getArtifactDir(artifact);
        TreeSet<Version> versions = getVersionsList(dir);
        if (versions.isEmpty()) {
            throw ArtifactNotFoundException.from(artifact);
        }

        if (expectedLabel == null) {
            return versions.last().toString();
        }

        // filter versions by expected label and then return last one
        Optional<Version> lastVersion = versions.descendingSet()
                                                .stream()
                                                .filter((version) -> {
                                                    try {
                                                        Optional<String> label = getProperty(artifact, version, ArtifactProperties.LABEL_PROPERTY);
                                                        return label.isPresent() && label.get().toUpperCase().equals(expectedLabel.toUpperCase());
                                                    } catch (IOException e) {
                                                        throw new RuntimeException(e);
                                                    }

                                                }).findFirst();

        if (!lastVersion.isPresent()) {
            throw new ArtifactNotFoundException(format("There is no version of artifact %s with label '%s'", artifact, expectedLabel));
        }

        return lastVersion.get().toString();
    }

    protected Optional<String> getProperty(String artifact, Version version, String propertyName) throws IOException {
        Properties properties = loadProperties(artifact, version.toString());
        return Optional.ofNullable((String) properties.get(propertyName));
    }

    /**
     * @return available artifact versions beginning from the given one, excluded
     */
    public Collection<Version> getVersions(String artifact, @Nullable String fromVersionNumber) throws IOException {
        Version fromVersion = Commons.createVersionOrNull(fromVersionNumber);
        Path dir = getArtifactDir(artifact);

        TreeSet<Version> versions = getVersionsList(dir);

        if (fromVersion != null) {
            Iterator<Version> iter = versions.iterator();
            while (iter.hasNext()) {
                Version version = iter.next();
                if (fromVersion.compareTo(version) >= 0) {
                    iter.remove();
                }
            }
        }
        return versions;
    }

    /**
     * Uploads artifact into the repository.
     *
     * @throws java.io.IOException
     *         if an I/O error occurs
     */

    public void upload(final InputStream in, String artifact, String version, String fileName, Properties props) throws IOException {
        props.put(FILE_NAME_PROPERTY, fileName);
        props.put(VERSION_PROPERTY, version);
        props.put(ARTIFACT_PROPERTY, artifact);
        storeProperties(artifact, version, props);

        try (OutputStream out = newOutputStream(getArtifact(artifact, version, fileName))) {
            IOUtils.copyLarge(in, out);
        } finally {
            in.close();
        }
    }

    /**
     * Downloads artifact from the repository.
     *
     * @throws com.codenvy.im.artifacts.ArtifactNotFoundException
     *         if artifact is absent in the repository
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    public InputStream download(String artifact, String version) throws IOException {
        Path path = getArtifact(artifact, version);
        if (!Files.exists(path)) {
            throw new ArtifactNotFoundException(artifact, version);
        }

        return new BufferedInputStream(Files.newInputStream(path));
    }

    /**
     * Loads the properties of the artifact.
     *
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    public Properties loadProperties(String artifact, String version) throws IOException {
        Properties props = new Properties();
        Path propertiesFile = getPropertiesFile(artifact, version);
        if (!Files.exists(propertiesFile)) {
            throw new ArtifactNotFoundException(artifact, version);
        }

        try (InputStream in = new BufferedInputStream(Files.newInputStream(propertiesFile))) {
            props.load(in);
        }

        return props;
    }

    /**
     * Stores the properties of the artifact.
     *
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    public void storeProperties(String artifact, String version, Properties props) throws IOException {
        Path propertiesFile = getPropertiesFile(artifact, version);
        Path dir = propertiesFile.getParent();

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        try (OutputStream out = new BufferedOutputStream(newOutputStream(propertiesFile))) {
            props.store(out, null);
        }
    }

    /**
     * Indicates if user has to be authenticated to download artifact.
     *
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    protected boolean isAuthenticationRequired(String artifact, String version) throws IOException {
        return "true".equalsIgnoreCase((String)loadProperties(artifact, version).get(AUTHENTICATION_REQUIRED_PROPERTY));
    }

    /**
     * @return the subscription name which is required to download artifact
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    protected String getRequiredSubscription(String artifact, String version) throws IOException {
        return (String)loadProperties(artifact, version).get(SUBSCRIPTION_PROPERTY);
    }


    /**
     * @return the file name under which artifact is stored in the repository, method doesn't check if artifact exists
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    protected String getFileName(String artifact, String version) throws IOException {
        return (String)loadProperties(artifact, version).get(FILE_NAME_PROPERTY);
    }

    /**
     * @return the path to the artifact
     * @throws java.io.IOException
     *         if an I/O error occurs
     */
    protected Path getArtifact(String artifact, String version) throws IOException {
        String fileName = getFileName(artifact, version);
        return getArtifact(artifact, version, fileName);
    }

    protected Path getArtifact(String artifact, String version, String fileName) {
        return getArtifactDir(artifact, version).resolve(fileName);
    }

    protected Path getRepositoryDir() {
        return Paths.get(repositoryDir);
    }

    protected Path getArtifactDir(String artifact) {
        return getRepositoryDir().resolve(artifact);
    }

    protected Path getArtifactDir(String artifact, String version) {
        return Paths.get(repositoryDir, artifact, version);
    }

    protected Path getPropertiesFile(String artifact, String version) {
        return getArtifactDir(artifact, version).resolve(PROPERTIES_FILE);
    }
}
