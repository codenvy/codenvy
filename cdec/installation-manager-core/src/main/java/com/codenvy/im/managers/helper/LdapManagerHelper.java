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
package com.codenvy.im.managers.helper;

import com.codenvy.im.managers.ConfigManager;

import java.io.IOException;

/**
 * @author Dmytro Nochevnov
 */
abstract public class LdapManagerHelper {

    protected ConfigManager configManager;

    public LdapManagerHelper(ConfigManager configManager) {
        this.configManager = configManager;
    }

    abstract public String getRootPrincipal() throws IOException;

    abstract public String getNameOfObjectToChangePassword() throws IOException;

    abstract public String getRealm();
}
