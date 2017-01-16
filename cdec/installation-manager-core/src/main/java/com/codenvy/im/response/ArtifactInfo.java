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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"artifact", "version", "label", "status"})
public class ArtifactInfo extends AbstractArtifactInfo {
    public enum Status {
        READY_TO_INSTALL,
        DOWNLOADED,
        INSTALLED
    }

    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
