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

import com.codenvy.im.managers.Config;
import com.codenvy.im.managers.ConfigManager;

import java.io.IOException;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public class LdapManagerHelperCodenvy4Impl extends LdapManagerHelper {

    public LdapManagerHelperCodenvy4Impl(ConfigManager configManager) {
        super(configManager);
    }

    @Override
    public String getRootPrincipal() throws IOException {
        return configManager.loadInstalledCodenvyConfig().getValue(Config.JAVA_NAMING_SECURITY_PRINCIPAL);
    }

    @Override
    public String getNameOfObjectToChangePassword() throws IOException {
        Config config = configManager.loadInstalledCodenvyConfig();
        return format("%s=%s,%s",
               config.getValue(Config.USER_LDAP_USER_DN),
               config.getValue(Config.ADMIN_LDAP_MAIL),
               config.getValue(Config.USER_LDAP_USER_CONTAINER_DN));
    }

    @Override
    public String getRealm() {
        return null;
    }
}
