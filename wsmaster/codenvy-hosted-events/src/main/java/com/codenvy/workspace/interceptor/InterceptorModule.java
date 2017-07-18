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
package com.codenvy.workspace.interceptor;

import com.codenvy.auth.sso.server.organization.UserCreator;
import com.codenvy.user.interceptor.CreateUserInterceptor;
import com.codenvy.user.interceptor.UserCreatorInterceptor;
import com.google.inject.AbstractModule;

import org.eclipse.che.api.user.server.UserService;

import static com.google.inject.matcher.Matchers.subclassesOf;
import static org.eclipse.che.inject.Matchers.names;

/**
 * Package api interceptors in guice container.
 *
 * @author Sergii Kabashniuk
 * @author Yevhenii Voevodin
 * @author Anatoliy Bazko
 */
public class InterceptorModule extends AbstractModule {

    @Override
    protected void configure() {
        final UserCreatorInterceptor userCreatorInterceptor = new UserCreatorInterceptor();
        requestInjection(userCreatorInterceptor);
        bindInterceptor(subclassesOf(UserCreator.class), names("createUser"), userCreatorInterceptor);

        final CreateUserInterceptor createUserInterceptor = new CreateUserInterceptor();
        requestInjection(createUserInterceptor);
        bindInterceptor(subclassesOf(UserService.class), names("create"), createUserInterceptor);
    }
}
