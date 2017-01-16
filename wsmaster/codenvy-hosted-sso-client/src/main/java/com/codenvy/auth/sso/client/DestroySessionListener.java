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
