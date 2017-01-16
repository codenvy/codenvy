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
package com.codenvy.im.utils;

import com.codenvy.im.saas.SaasUserServiceProxy;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;

public class TestUserUtils {
    @Mock
    private HttpTransport transport;

    private SaasUserServiceProxy saasUserServiceProxy;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        saasUserServiceProxy = new SaasUserServiceProxy("", transport);
    }

    @Test
    public void testGetUserEmail() throws IOException {
        doReturn("{\"email\": \"userEmail\"}").when(transport).doGet(endsWith("/user"), eq("token"));

        assertEquals(saasUserServiceProxy.getUserEmail("token"), "userEmail");
    }
}
