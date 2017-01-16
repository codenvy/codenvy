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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Is used when downloading is finished.
 * No progress info, just a final result as {@link com.codenvy.im.response.ResponseCode}
 *
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"artifacts", "message", "status"})
public class DownloadResponse implements Response {

    private ResponseCode               status;
    private String                     message;
    private Collection<DownloadArtifactInfo> artifacts;

    public DownloadResponse() {
    }

    public DownloadResponse(DownloadProgressResponse downloadProgressResponse) {
        this.artifacts = new ArrayList<>(downloadProgressResponse.getArtifacts());
        this.status = downloadProgressResponse.getStatus() == DownloadArtifactInfo.Status.FAILED ? ResponseCode.ERROR : ResponseCode.OK;
        this.message = downloadProgressResponse.getMessage();
    }

    public ResponseCode getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Collection<DownloadArtifactInfo> getArtifacts() {
        return artifacts;
    }

    public void setStatus(ResponseCode status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setArtifacts(Collection<DownloadArtifactInfo> artifacts) {
        this.artifacts = artifacts;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DownloadResponse)) {
            return false;
        }

        DownloadResponse that = (DownloadResponse)o;

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
