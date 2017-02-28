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
package org.eclipse.che.security.oauth;


import org.eclipse.che.security.oauth.shared.User;

/**
 * Represents LinkedIn user.
 * @author Max Shaposhnik
 */
public class LinkedInUser implements User {
    private String email;
    private String name;

    @Override
    public final String getId() {
        return email;
    }

    @Override
    public final void setId(String id) {
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        setId(email);
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "LinkedInUser{" +
               "id='" + getId() + '\'' +
               ", email='" + email + '\'' +
               ", name='" + name + '\'' +
               '}';
    }
}
