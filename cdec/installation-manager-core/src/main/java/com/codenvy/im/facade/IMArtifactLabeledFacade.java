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
import com.codenvy.im.artifacts.VersionLabel;
import com.codenvy.im.managers.BackupManager;
import com.codenvy.im.managers.ConfigManager;
import com.codenvy.im.managers.DownloadManager;
import com.codenvy.im.managers.DownloadNotStartedException;
import com.codenvy.im.managers.InstallManager;
import com.codenvy.im.managers.LdapManager;
import com.codenvy.im.managers.NodeManager;
import com.codenvy.im.managers.StorageManager;
import com.codenvy.im.response.AbstractArtifactInfo;
import com.codenvy.im.response.ArtifactInfo;
import com.codenvy.im.response.DownloadArtifactInfo;
import com.codenvy.im.response.DownloadProgressResponse;
import com.codenvy.im.response.InstallArtifactInfo;
import com.codenvy.im.response.UpdateArtifactInfo;
import com.codenvy.im.saas.SaasAuthServiceProxy;
import com.codenvy.im.saas.SaasRepositoryServiceProxy;
import com.codenvy.im.utils.HttpTransport;
import com.codenvy.im.utils.Version;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.json.JsonParseException;

import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;

/**
 * Sets {@link com.codenvy.im.artifacts.VersionLabel} to every artifact info.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class IMArtifactLabeledFacade extends InstallationManagerFacade {

    @Inject
    public IMArtifactLabeledFacade(@Named("saas.api.endpoint") String saasServerEndpoint,
                                   HttpTransport transport,
                                   ConfigManager configManager,
                                   SaasAuthServiceProxy saasAuthServiceProxy,
                                   SaasRepositoryServiceProxy saasRepositoryServiceProxy,
                                   LdapManager ldapManager,
                                   NodeManager nodeManager,
                                   BackupManager backupManager,
                                   StorageManager storageManager,
                                   InstallManager installManager,
                                   DownloadManager downloadManager) {
        super(transport,
              configManager,
              saasAuthServiceProxy,
              saasRepositoryServiceProxy,
              ldapManager,
              nodeManager,
              backupManager,
              storageManager,
              installManager,
              downloadManager);
    }

    @Override
    public void requestAuditReport(String authToken, String host) throws IOException {
        super.requestAuditReport(authToken, host);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<InstallArtifactInfo> getInstalledVersions() throws IOException {
        Collection<InstallArtifactInfo> installedVersions = super.getInstalledVersions();
        setVersionLabel(installedVersions);
        return installedVersions;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<UpdateArtifactInfo> getUpdates() throws IOException {
        Collection<UpdateArtifactInfo> updates = super.getUpdates();
        setVersionLabel(updates);
        return updates;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<DownloadArtifactInfo> getDownloads(@Nullable Artifact artifact, @Nullable Version version) throws IOException {
        Collection<DownloadArtifactInfo> downloads = super.getDownloads(artifact, version);
        setVersionLabel(downloads);
        return downloads;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<ArtifactInfo> getArtifacts() throws IOException {
        Collection<ArtifactInfo> artifacts = super.getArtifacts();
        setVersionLabel(artifacts);
        return artifacts;
    }

    /** {@inheritDoc} */
    @Override
    public List<UpdateArtifactInfo> getAllUpdatesAfterInstalledVersion(@Nullable Artifact artifact) throws IOException, JsonParseException {
        List<UpdateArtifactInfo> updates = super.getAllUpdatesAfterInstalledVersion(artifact);
        setVersionLabel(updates);
        return updates;
    }

    /** {@inheritDoc} */
    @Override
    public DownloadProgressResponse getDownloadProgress() throws DownloadNotStartedException, IOException {
        DownloadProgressResponse response = super.getDownloadProgress();

        // avoid requests if artifact hasn't been downloaded
        if (response.getStatus() != DownloadArtifactInfo.Status.DOWNLOADING) {
            setVersionLabel(response.getArtifacts());
        }
        return response;
    }

    protected void setVersionLabel(Collection<? extends AbstractArtifactInfo> infos) throws IOException {
        Iterator<? extends AbstractArtifactInfo> iter = infos.iterator();

        while (iter.hasNext()) {
            AbstractArtifactInfo info = iter.next();
            VersionLabel versionLabel = fetchVersionLabel(info.getArtifact(), info.getVersion());
            info.setLabel(versionLabel);
        }
    }

    /**
     * @return label value, or null if (1) label is unknown or (2) label is absent among the VersionLabel constants
     */
    @Nullable
    protected VersionLabel fetchVersionLabel(@NotNull String artifactName, @NotNull String versionNumber) throws IOException {
        Artifact artifact = createArtifact(artifactName);
        Version version = Version.valueOf(versionNumber);

        return artifact.getLabel(version).orElse(null);
    }
}
