/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.auth.sso.server.organization;


import org.eclipse.che.api.user.server.dao.User;

import java.io.IOException;

/**
 * Temporary bridge between authentication and organization.
 * In future have to be replaced with direct HTTP calls.
 *
 * @author Sergii Kabashniuk
 */
public interface UserCreator {

    /**
     * Create new persistant user.
     *
     * @param email
     * @param userName
     * @param firstName
     * @param lastName
     * @throws IOException
     */
    User createUser(String email, String userName, String firstName, String lastName) throws IOException;

    /**
     * Create temporary user.
     *
     * @return - name of temporary user.
     * @throws IOException
     */
    User createTemporary() throws IOException;
}
