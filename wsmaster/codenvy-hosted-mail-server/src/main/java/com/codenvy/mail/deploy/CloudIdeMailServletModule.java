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
package com.codenvy.mail.deploy;

import org.eclipse.che.inject.DynaModule;
import com.google.inject.servlet.ServletModule;

import org.everrest.guice.servlet.GuiceEverrestServlet;

/**
 * Servlet module composer for api war.
 */
@DynaModule
public class CloudIdeMailServletModule extends ServletModule {
    @Override
    protected void configureServlets() {
        serve("/*").with(GuiceEverrestServlet.class);
    }
}