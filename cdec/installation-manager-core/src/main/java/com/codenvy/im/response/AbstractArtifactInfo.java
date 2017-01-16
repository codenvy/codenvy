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
package com.codenvy.im.response;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.artifacts.ArtifactNotFoundException;
import com.codenvy.im.artifacts.VersionLabel;
import com.codenvy.im.utils.Version;

import static com.codenvy.im.artifacts.ArtifactFactory.createArtifact;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractArtifactInfo implements Info, Comparable<AbstractArtifactInfo> {
    private String       artifact;
    private String       version;
    private VersionLabel label;

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public VersionLabel getLabel() {
        return label;
    }

    public void setLabel(VersionLabel label) {
        this.label = label;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractArtifactInfo)) {
            return false;
        }

        AbstractArtifactInfo that = (AbstractArtifactInfo)o;

        if (artifact != null ? !artifact.equals(that.artifact) : that.artifact != null) {
            return false;
        }
        if (label != that.label) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = artifact != null ? artifact.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }


    /** {@inheritDoc} */
    @Override
    public int compareTo(AbstractArtifactInfo o) {
        if (!getArtifact().equals(o.getArtifact())) {
            try {
                Artifact thisArtifact = createArtifact(getArtifact());
                Artifact thatArtifact = createArtifact(o.getArtifact());
                return thatArtifact.compareTo(thisArtifact);
            } catch (ArtifactNotFoundException e) {
                throw new IllegalStateException(e);
            }
        } else {
            Version thisVersion = Version.valueOf(getVersion());
            Version thatVersion = Version.valueOf(o.getVersion());

            return thatVersion.compareTo(thisVersion);
        }
    }

}
