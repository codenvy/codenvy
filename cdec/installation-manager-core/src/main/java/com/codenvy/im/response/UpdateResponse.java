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

import java.util.Collection;

/**
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"artifacts", "message", "status"})
public class UpdateResponse implements Response {

    private ResponseCode                   status;
    private String                         message;
    private Collection<UpdateArtifactInfo> artifacts;

    public UpdateResponse() {
    }

    public ResponseCode getStatus() {
        return status;
    }

    public void setStatus(ResponseCode status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Collection<UpdateArtifactInfo> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Collection<UpdateArtifactInfo> artifacts) {
        this.artifacts = artifacts;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UpdateResponse)) {
            return false;
        }

        UpdateResponse that = (UpdateResponse)o;

        if (artifacts != null ? !artifacts.equals(that.artifacts) : that.artifacts != null) {
            return false;
        }
        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        if (status != that.status) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (artifacts != null ? artifacts.hashCode() : 0);
        return result;
    }
}
