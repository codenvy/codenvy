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

import com.codenvy.client.model.Version;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@linkplain com.codenvy.client.core.DefaultVersionClient Version Client} tests.
 *
 * @author Florent Benoit
 */
public class VersionClientIT extends AbstractIT {


    //@Test
    public void testGetVersion() {
        final Version version = codenvy.version()
                                       .current()
                                       .execute();

        assertNotNull(version);

        assertEquals("Codenvy, S.A.", version.implementationVendor());
        assertNotNull(version.implementationVersion());
        assertEquals("Codenvy, S.A.", version.specificationVendor());
        assertEquals("1.0-beta1", version.specificationVersion());
        assertEquals("Codenvy REST API", version.specificationTitle());
        assertNotNull(version.scmRevision());

    }
}
