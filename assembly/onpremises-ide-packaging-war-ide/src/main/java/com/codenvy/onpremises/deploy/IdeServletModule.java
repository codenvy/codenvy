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
package com.codenvy.onpremises.deploy;

import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;

/**
 * Servlet module composer for ide war.
 *
 * @author Alexander Garagatyi
 */
@DynaModule
public class IdeServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        filter("/*").through(com.codenvy.auth.sso.client.LoginFilter.class);
        filter("/*").through(com.codenvy.onpremises.DashboardRedirectionFilter.class);
        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    }
}
