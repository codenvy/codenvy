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
import com.codenvy.im.utils.Version;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.nio.file.Path;

/**
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"artifact", "version", "label", "file", "status"})
public class DownloadArtifactInfo extends AbstractArtifactInfo {
    public enum Status {
        DOWNLOADED,
        DOWNLOADING,
        READY_TO_INSTALL,
        FAILED
    }

    private String file;
    private Status status;

    public DownloadArtifactInfo() {
    }

    public DownloadArtifactInfo(Artifact artifact, Version version, Path file, Status status) {
        setArtifact(artifact.getName());
        setVersion(version.toString());

        this.file = (file != null) ? file.toString() : null;
        this.status = status;
    }

    public DownloadArtifactInfo(Artifact artifact, Version version, Status status) {
        this(artifact, version, null, status);
    }

    public Status getStatus() {
        return status;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
