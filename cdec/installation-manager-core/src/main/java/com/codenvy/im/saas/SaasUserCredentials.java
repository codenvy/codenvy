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
package com.codenvy.im.saas;

/**
 * AccountID refers to the account where user has account/owner role.
 *
 * @author Dmytro Nochevnov
 */
public class SaasUserCredentials implements Cloneable {
    private String token;

    public SaasUserCredentials() {
    }

    public SaasUserCredentials(String token) {
        this.token = token;
    }

    /** Getter for #token */
    public String getToken() {
        return token;
    }

    /** Setter for #token */
    public void setToken(String token) {
        this.token = token;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SaasUserCredentials)) {
            return false;
        }

        SaasUserCredentials that = (SaasUserCredentials)o;

        if (token != null ? !token.equals(that.token) : that.token != null) {
            return false;
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return token != null ? token.hashCode() : 0;
    }

    /** {@inheritDoc} */
    @Override
    public SaasUserCredentials clone() throws CloneNotSupportedException {
        return (SaasUserCredentials)super.clone();
    }
}
