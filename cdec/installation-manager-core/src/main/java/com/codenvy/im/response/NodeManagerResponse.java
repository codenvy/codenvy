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
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"node", "message", "status"})
public class NodeManagerResponse implements Response {

    private ResponseCode status;
    private String       message;
    private NodeInfo     node;

    public NodeManagerResponse() {
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

    public NodeInfo getNode() {
        return node;
    }

    public void setNode(NodeInfo node) {
        this.node = node;
    }
}
