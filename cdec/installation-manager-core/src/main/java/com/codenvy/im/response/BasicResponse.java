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

import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
@JsonPropertyOrder({"properties", "message", "status"})
public class BasicResponse implements Response {

    private Map<String, String> properties;
    private ResponseCode        status;
    private String              message;

    public BasicResponse(ResponseCode status) {
        this.status = status;
    }

    public BasicResponse() {
    }

    /** {@inheritDoc} */
    @Override
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

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static BasicResponse ok() {
        return new BasicResponse(ResponseCode.OK);
    }

    public static BasicResponse error(String message) {
        BasicResponse response = new BasicResponse(ResponseCode.ERROR);
        response.setMessage(message);
        return response;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BasicResponse)) {
            return false;
        }

        BasicResponse that = (BasicResponse)o;

        if (message != null ? !message.equals(that.message) : that.message != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
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
        int result = properties != null ? properties.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
