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

import com.codenvy.im.artifacts.VersionLabel;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"artifact", "version", "label", "status"})
public class InstallArtifactInfo extends AbstractArtifactInfo {
    public enum Status {
        SUCCESS,
        FAILURE,
        IN_PROGRESS
    }

    private Status status;

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public static InstallArtifactInfo createInstance(String artifact, String version, VersionLabel label, Status status) {
        InstallArtifactInfo info = new InstallArtifactInfo();

        info.setArtifact(artifact);
        info.setVersion(version);
        info.setLabel(label);
        info.setStatus(status);

        return info;
    }

    public static InstallArtifactInfo createInstance(String artifactName, String versionNumber, Status status) {
        return createInstance(artifactName, versionNumber, null, status);
    }
}

