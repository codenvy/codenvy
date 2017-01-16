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
package com.codenvy.cli.preferences.file;

import com.codenvy.cli.preferences.PreferencesAPI;
import com.codenvy.cli.preferences.PreferencesProvider;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author Florent Benoit
 */
public class Activator implements BundleActivator {

    private PreferencesProvider fileProvider;

    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being started.
     * @throws Exception
     *         If this method throws an exception, this bundle is
     *         marked as stopped and the Framework will remove this bundle's
     *         listeners, unregister all services registered by this bundle, and
     *         release all services used by this bundle.
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.fileProvider = new FilePreferencesProvider();
        PreferencesAPI.addPreferencesProvider(fileProvider);
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the {@code BundleActivator.start} method
     * started. There should be no active threads that were started by this
     * bundle when this bundle returns. A stopped bundle must not call any
     * Framework objects.
     * <p/>
     * <p/>
     * This method must complete and return to its caller in a timely manner.
     *
     * @param context
     *         The execution context of the bundle being stopped.
     * @throws Exception
     *         If this method throws an exception, the bundle is still
     *         marked as stopped, and the Framework will remove the bundle's
     *         listeners, unregister all services registered by the bundle, and
     *         release all services used by the bundle.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
    if (fileProvider != null) {
        PreferencesAPI.removePreferencesProvider(fileProvider);
    }
    }
}
