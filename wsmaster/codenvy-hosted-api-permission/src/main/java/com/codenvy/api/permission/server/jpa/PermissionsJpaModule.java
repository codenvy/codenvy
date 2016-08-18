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
package com.codenvy.api.permission.server.jpa;

import com.codenvy.api.permission.server.spi.PermissionsDao;
import com.google.inject.AbstractModule;

/**
 * @author Max Shaposhnik
 *
 */
public class PermissionsJpaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PermissionsDao.class).to(JpaCommonPermissionsDao.class);
        bind(JpaCommonPermissionsDao.RemovePermissionsBeforeUserRemovedEventSubscriber.class).asEagerSingleton();
    }
}
