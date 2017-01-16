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
package com.codenvy.cli.security;


import com.codenvy.cli.command.builtin.AbsCommandTest;
import com.codenvy.cli.preferences.Preferences;
import com.codenvy.cli.preferences.file.FilePreferences;
import com.codenvy.client.auth.Credentials;
import com.codenvy.client.dummy.DummyCodenvyClient;

import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URISyntaxException;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Florent Benoit
 */
public class PreferencesDataStoreTest extends AbsCommandTest {

    private Preferences preferences;

    private PreferencesDataStore dataStoreFlorent;
    private PreferencesDataStore dataStoreStephane;



    public void init() throws URISyntaxException {

        FilePreferences filePreferences =
                new FilePreferences(new File(PreferencesDataStoreTest.class.getResource("preferences.json").toURI()));
        assertNotNull(filePreferences);

        // keep remotes
        this.preferences = filePreferences.walk("remotes");
        assertNotNull(preferences);
        DummyCodenvyClient codenvyClient = new DummyCodenvyClient();


        this.dataStoreFlorent = new PreferencesDataStore(preferences, "florent_dev", codenvyClient);
        this.dataStoreStephane = new PreferencesDataStore(preferences, "stephane_dev", codenvyClient);

    }

    @Test
    public void testGet() throws Exception {
        init();

        Credentials credentialsFlorent = dataStoreFlorent.getCredentials("dummy");
        assertNotNull(credentialsFlorent);
        assertEquals(credentialsFlorent.username(), "florent");
        assertEquals(credentialsFlorent.token().value(), "abc123");

        Credentials credentialsStephane = dataStoreStephane.getCredentials("dummy");
        assertNotNull(credentialsStephane);
        assertEquals(credentialsStephane.username(), "stephane");
        assertEquals(credentialsStephane.token().value(), "def456");

    }

    @Test(dependsOnMethods = "testGet")
    public void testPut() throws Exception {
        init();
        Credentials credentialsFlorent = dataStoreFlorent.getCredentials("dummy");

        // change username
        Credentials update = Mockito.mock(Credentials.class);
        doReturn("toto").when(update).username();
        doReturn(credentialsFlorent.token()).when(update).token();

        Credentials merged = dataStoreFlorent.put("florent_dev", update);

        assertEquals(merged.username(), "toto");

    }

    @Test(dependsOnMethods = "testPut")
    public void testOnlyOneInstanceGet() throws Exception {
        init();

        Credentials one = dataStoreFlorent.get("one");
        assertNotNull(one);
        Credentials two = dataStoreFlorent.get("two");
        assertNotNull(two);

        assertEquals(one, two);

    }

}
