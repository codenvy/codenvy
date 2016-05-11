/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.im.artifacts.helper;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Dmytro Nochevnov
 */
public class SystemProxySettingsTest {

    @Test
    public void shouldBeEmpty() {
        SystemProxySettings instance = new SystemProxySettings();
        assertTrue(instance.isEmpty());
    }

    @Test
    public void shouldCreateObject() {
        System.setProperty("http.proxyUser", "user1");
        System.setProperty("http.proxyPassword", "paswd1");
        System.setProperty("http.proxyHost", "host1");
        System.setProperty("http.proxyPort", "8081");

        System.setProperty("https.proxyUser", "user2");
        System.setProperty("https.proxyPassword", "paswd2");
        System.setProperty("https.proxyHost", "host2");
        System.setProperty("https.proxyPort", "8082");

        SystemProxySettings instance = SystemProxySettings.create();

        assertFalse(instance.isEmpty());

        assertEquals(instance.getHttpUser(), "user1");
        assertEquals(instance.getHttpPassword(), "paswd1");
        assertEquals(instance.getHttpHost(), "host1");
        assertEquals(instance.getHttpPort(), "8081");

        assertEquals(instance.getHttpsUser(), "user2");
        assertEquals(instance.getHttpsPassword(), "paswd2");
        assertEquals(instance.getHttpsHost(), "host2");
        assertEquals(instance.getHttpsPort(), "8082");
    }

    @AfterMethod
    public void tearDown() throws Exception {
        System.clearProperty("http.proxyUser");
        System.clearProperty("http.proxyPassword");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");

        System.clearProperty("https.proxyUser");
        System.clearProperty("https.proxyPassword");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
    }
}
