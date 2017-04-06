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
