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
package com.codenvy.onpremises;

import org.everrest.core.impl.RuntimeDelegateImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.ext.RuntimeDelegate;

/** Initialize components of cloud ide site. */
public class OnPremisesIdeServletContextListener implements ServletContextListener {
    /** Class logger. */
    private static final Logger LOG = LoggerFactory.getLogger(OnPremisesIdeServletContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
