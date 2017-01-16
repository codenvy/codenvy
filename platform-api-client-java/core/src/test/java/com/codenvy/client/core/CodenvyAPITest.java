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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test if API loading of {@link com.codenvy.client.CodenvyAPI} find our own implementation
 * @author Florent Benoit
 */
@RunWith(MockitoJUnitRunner.class)
public class CodenvyAPITest {

    @Test
    public void checkFindValidImplementation() {
        CodenvyClient client = CodenvyAPI.getClient();
        assertNotNull(client);

        // Check that we've our implementation and not another one
        assertTrue(client instanceof DefaultCodenvyClient);
    }



}
