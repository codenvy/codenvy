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
package com.codenvy.service.http;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.classic.util.JNDIUtil;

import org.slf4j.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * ServletContextListener  used to terminate all work inside of logback LoggerContext.
 * It's analogue ContextDetachingSCL, but we can't used since of class loading issues.
 * (no servlet api in endorsed folder there all logback classes are located)
 *
 * @author Sergii Kabashniuk
 */
public class Slf4jLoggerListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        String loggerContextName = null;

        try {
            Context ctx = JNDIUtil.getInitialContext();
            loggerContextName = JNDIUtil.lookup(ctx, ClassicConstants.JNDI_CONTEXT_NAME);
        } catch (NamingException ignored) {
        }

        if (loggerContextName != null) {
            System.out.println("About to detach context named " + loggerContextName);

            ContextSelector selector = ContextSelectorStaticBinder.getSingleton().getContextSelector();
            if (selector == null) {
                System.out.println("Selector is null, cannot detach context. Skipping.");
                return;
            }
            LoggerContext context = selector.getLoggerContext(loggerContextName);
            if (context != null) {
                Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
                logger.warn("Stopping logger context " + loggerContextName);
                selector.detachLoggerContext(loggerContextName);
                // when the web-app is destroyed, its logger context should be stopped
                context.stop();
            } else {
                System.out.println("No context named " + loggerContextName + " was found.");
            }
        }
    }
}
