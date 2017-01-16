/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.CodenvyAPI;
import com.codenvy.client.CodenvyClient;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Dictionary;
import java.util.Properties;

/**
 * OSGi Activator that will register the Codenvy Client
 *
 * @author Florent Benoit
 */
public class Activator implements BundleActivator {


    /**
     * Previous Codenvy client implementation
     */
    private CodenvyClient previous;

    /**
     * Called when this bundle is started so the Framework can perform the bundle-specific activities necessary to start this
     * bundle. This method can be used to register services or to allocate any resources that this bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being started.
     * @throws Exception
     *         If this method throws an exception, this bundle is marked as stopped and the Framework will remove this
     *         bundle's listeners, unregister all services registered by this bundle, and release all services used by this bundle.
     */
    public void start(BundleContext context) throws Exception {

        // build implementation
        DefaultCodenvyClient defaultCodenvyClient = new DefaultCodenvyClient();

        // Sets it first in the API/SPI
        this.previous = CodenvyAPI.setClient(defaultCodenvyClient);

        // Also register it as OSGi Service...

        // add as property the implementation name
        Dictionary props = new Properties();
        props.put(CodenvyClient.class.getName(), DefaultCodenvyClient.class.getName());

        // register
        context.registerService(CodenvyClient.class.getName(), defaultCodenvyClient, props);
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the bundle-specific activities necessary to stop the
     * bundle. In general, this method should undo the work that the <code>BundleActivator.start</code> method started. There
     * should be no active threads that were started by this bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being stopped.
     * @throws Exception
     *         If this method throws an exception, the bundle is still marked as stopped, and the Framework will remove
     *         the bundle's listeners, unregister all services registered by the bundle, and release all services used by the bundle.
     */
    public void stop(BundleContext context) throws Exception {
        // unregister
        CodenvyAPI.setClient(previous);
    }
}
