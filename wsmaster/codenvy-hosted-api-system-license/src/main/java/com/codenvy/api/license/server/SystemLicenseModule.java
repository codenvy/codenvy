/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
