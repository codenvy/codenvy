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
package com.codenvy.selenium.core;

import com.codenvy.selenium.core.client.OnpremTestAuthServiceClient;
import com.codenvy.selenium.core.provider.OnpremTestApiEndpointUrlProvider;
import com.codenvy.selenium.core.user.OnpremAdminTestUser;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Names;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.configuration.SeleniumTestConfiguration;
import org.eclipse.che.selenium.core.configuration.TestConfiguration;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.user.AdminTestUser;
import org.testng.ISuite;
import org.testng.ISuiteListener;

/**
 * @author Anatolii Bazko
 */
public class FairSourceLicenseAcceptorListener implements ISuiteListener {
    @Inject
    private HttpJsonRequestFactory     requestFactory;
    @Inject
    private TestApiEndpointUrlProvider apiEndpoint;
    @Inject
    private AdminTestUser              adminTestUser;

    @Override
    public void onStart(ISuite suite) {
        try {
            accept();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onFinish(ISuite suite) { }

    private void accept() throws Exception {
        Injector injector = Guice.createInjector(new FairSourceLicenseModule());
        injector.injectMembers(this);

        try {
            requestFactory.fromUrl(apiEndpoint.get() + "/license/system/fair-source-license")
                          .setAuthorizationHeader(adminTestUser.getAuthToken())
                          .usePostMethod()
                          .request();
        } catch (ConflictException ignored) {
            // already accepted
        }
    }

    private static class FairSourceLicenseModule extends AbstractModule {
        @Override
        protected void configure() {
            TestConfiguration config = new SeleniumTestConfiguration();
            config.getMap().forEach((key, value) -> bindConstant().annotatedWith(Names.named(key)).to(value));

            bind(TestApiEndpointUrlProvider.class).to(OnpremTestApiEndpointUrlProvider.class);
            bind(AdminTestUser.class).to(OnpremAdminTestUser.class);
            bind(TestAuthServiceClient.class).to(OnpremTestAuthServiceClient.class);
        }
    }
}
