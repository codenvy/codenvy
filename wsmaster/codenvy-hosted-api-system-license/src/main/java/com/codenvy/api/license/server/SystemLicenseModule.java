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
package com.codenvy.api.license.server;

import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.filter.SystemLicenseServicePermissionsFilter;
import com.codenvy.api.license.server.filter.SystemLicenseWorkspaceFilter;
import com.codenvy.api.license.server.jpa.JpaSystemLicenseActionDao;
import com.google.inject.AbstractModule;

/**
 * @author Alexander Andrienko
 * @author Dmytro Nochevnov
 */
public class SystemLicenseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SystemLicenseService.class);
        bind(SystemLicenseServicePermissionsFilter.class);
        bind(SystemLicenseWorkspaceFilter.class);
        bind(SystemLicenseActionDao.class).to(JpaSystemLicenseActionDao.class);
        bind(SystemLicenseActionHandler.class).asEagerSingleton();
    }
}
