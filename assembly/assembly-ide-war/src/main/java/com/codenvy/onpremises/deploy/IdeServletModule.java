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
package com.codenvy.onpremises.deploy;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;

import static com.codenvy.api.license.filter.SystemLicenseLoginFilter.ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL;
import static com.codenvy.api.license.filter.SystemLicenseLoginFilter.FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL;
import static com.codenvy.api.license.filter.SystemLicenseLoginFilter.NO_USER_INTERACTION;

/**
 * Servlet module composer for ide war.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class IdeServletModule extends ServletModule {
    @Override
    protected void configureServlets() {

        bind(com.xemantic.tadedon.servlet.CacheDisablingFilter.class).in(Singleton.class);
        bind(com.xemantic.tadedon.servlet.CacheForcingFilter.class).in(Singleton.class);
        filterRegex("^.*\\.nocache\\..*$", "^.*/_app/.*$").through(com.xemantic.tadedon.servlet.CacheDisablingFilter.class);
        filterRegex("^.*\\.cache\\..*$").through(com.xemantic.tadedon.servlet.CacheForcingFilter.class);
        filterRegex("^/(?!_app/.*.svg).*$").through(com.codenvy.auth.sso.client.LoginFilter.class);
        filterRegex("^/(?!_app/.*.svg).*$").through(com.codenvy.api.license.filter.SystemLicenseLoginFilter.class);
        filter("/*").through(com.codenvy.onpremises.DashboardRedirectionFilter.class);
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());

        bindConstant().annotatedWith(Names.named(NO_USER_INTERACTION)).to(false);
        bindConstant().annotatedWith(Names.named(ACCEPT_FAIR_SOURCE_LICENSE_PAGE_URL))
                      .to("/site/auth/accept-fair-source-license");
        bindConstant().annotatedWith(Names.named(FAIR_SOURCE_LICENSE_IS_NOT_ACCEPTED_ERROR_PAGE_URL))
                      .to("/site/error/fair-source-license-is-not-accepted-error");
    }
}
