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


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test if API loading of {@link CodenvyAPI} is working
 *
 * @author Florent Benoit
 */
@RunWith(MockitoJUnitRunner.class)
public class CodenvyAPITest {

    @Test
    public void checkFindValidImplementation() {
        CodenvyClient client = CodenvyAPI.getClient();
        assertNotNull(client);

        assertTrue(client instanceof DummyCodenvyClient);
    }


    @Test(expected = CodenvyException.class)
    public void checkUnableToFindValidImplementation() {
        ClassLoader classLoader = new URLClassLoader(new URL[0], null);
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            CodenvyAPI.getClient();
            fail("Without having implementation in classpath this has to fail !");
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }


    }


}
