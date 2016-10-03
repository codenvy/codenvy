/*******************************************************************************
 * Copyright (c) [2012] - [2016] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.model.User;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@linkplain com.codenvy.client.core.DefaultUserClient UserService} tests.
 * 
 * @author Kevin Pollet
 * @author Stéphane Daviet
 */
public class UserClientIT extends AbstractIT {
    @Test
    public void testGetCurrentUser() {
        final User currentUser = codenvy.user()
                                        .current()
                                        .execute();

        Assert.assertNotNull(currentUser);
        Assert.assertNotNull(currentUser.id());
        Assert.assertNotNull(currentUser.email());
    }
}
