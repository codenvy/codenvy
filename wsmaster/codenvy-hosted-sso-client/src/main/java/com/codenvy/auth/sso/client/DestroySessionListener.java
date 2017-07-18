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
package com.codenvy.auth.sso.client;

import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Removes HttpSession from SSO client session store, if it invalidated by
 * container.
 *
 * @author Andrey Parfonov
 * @author Sergii Kabashniuk
 */
public class DestroySessionListener implements HttpSessionListener {
    static final         String INJECTOR_NAME = Injector.class.getName();
    private static final Logger LOG           = LoggerFactory.getLogger(DestroySessionListener.class);

    @Override
    public final void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        LOG.debug("Removing session {} from SSO client session store", se.getSession().getId());
        ServletContext servletContext = se.getSession().getServletContext();

        SessionStore sessionStore = getInstance(SessionStore.class, servletContext);
        if (sessionStore == null) {
            LOG.error("Unable to remove session from SSO Store. Session store is not configured in servlet context.");
        } else {

            //If principal in session it means logout by session timeout.
            SsoClientPrincipal principal = (SsoClientPrincipal)se.getSession().getAttribute("principal");
            if (principal != null) {
                ServerClient client = getInstance(ServerClient.class, servletContext);
                if (client != null) {
                    LOG.debug("Logout by session timeout. Notify sso server about client {} - {} - {} logout",
                              principal.getName(), principal.getClientUrl(), principal.getToken());
                    //notify sso server to unregistered current client
                    client.unregisterClient(principal.getToken(), principal.getClientUrl());
                }
            }

            sessionStore.removeSessionById(se.getSession().getId());
        }
    }

    /** Searches  component in servlet context when with help of guice injector. */
    private <T> T getInstance(Class<T> type, ServletContext servletContext) {
        T result = (T)servletContext.getAttribute(type.getName());
        if (result == null) {
            Injector injector = (Injector)servletContext.getAttribute(INJECTOR_NAME);
            if (injector != null) {
                result = injector.getInstance(type);
            }
        }
        return result;

    }

    ;


}
