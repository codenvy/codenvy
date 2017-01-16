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
package com.codenvy.im.facade;

import com.codenvy.im.saas.SaasUserCredentials;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/** @author Dmytro Nochevnov */
public class TestUserCredentials {

    @Test
    public void testInstantiationWithToken() {
        SaasUserCredentials credentials = new SaasUserCredentials("token");
        assertEquals(credentials.getToken(), "token");
    }

    @Test
    public void testNoArgInstantiation() {
        SaasUserCredentials credentials = new SaasUserCredentials();
        assertNull(credentials.getToken());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        SaasUserCredentials credentials = new SaasUserCredentials("token");

        SaasUserCredentials clone = credentials.clone();

        credentials.setToken("another");
        assertEquals(clone.getToken(), "token");
    }
}
