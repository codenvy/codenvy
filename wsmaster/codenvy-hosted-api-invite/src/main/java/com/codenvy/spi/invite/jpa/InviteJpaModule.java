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
package com.codenvy.spi.invite.jpa;

import com.codenvy.spi.invite.InviteDao;
import com.google.inject.AbstractModule;

/**
 * @author Sergii Leschenko
 */
public class InviteJpaModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(InviteDao.class).to(JpaInviteDao.class);
    }
}
