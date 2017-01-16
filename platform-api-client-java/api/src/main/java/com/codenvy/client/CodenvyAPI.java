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
package com.codenvy.client;

import java.util.ServiceLoader;

/**
 * Entry point to get the codenvy client.
 *
 * @author Florent Benoit
 */
public class CodenvyAPI {

    /**
     * Unique instance of this client.
     */
    private static volatile CodenvyClient client;

    /**
     * Utility class so no public constructor.
     */
    private CodenvyAPI() {

    }

    /**
     * Sets the Codenvy client {@link com.codenvy.client.CodenvyClient} instance.
     *
     * @param codenvyClient
     *         the instance of the codenvy client
     * @return the previous instance of {@link com.codenvy.client.CodenvyClient}
     */
    public static synchronized CodenvyClient setClient(CodenvyClient codenvyClient) {
        CodenvyClient previous = CodenvyAPI.client;
        CodenvyAPI.client = codenvyClient;
        return previous;
    }


    /**
     * @return the Codenvy Client.
     */
    public static synchronized CodenvyClient getClient() {
        if (client == null) {
            // Use context classloader to find implementation
            ServiceLoader<CodenvyClient> codenvyClient =
                    ServiceLoader.load(CodenvyClient.class, Thread.currentThread().getContextClassLoader());
            if (!codenvyClient.iterator().hasNext()) {
                throw new CodenvyException("Unable to find an implementation of '" + CodenvyClient.class.getName() +
                                           "'. Check Implementation bundle is available on the platform or that implementation jar contains META-INF/services/com.codenvy.client.CodenvyClient key.");
            }

            // take first one
            client = codenvyClient.iterator().next();
        }

        return client;
    }


}
