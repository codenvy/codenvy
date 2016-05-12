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
package com.codenvy.im.saas;

import com.codenvy.im.BaseTest;
import com.codenvy.im.utils.HttpException;
import com.codenvy.im.utils.HttpTransport;
import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;


/**
 * @author Anatoliy Bazko
 */
public class SaasAuthServiceProxyTest extends BaseTest {

    @Mock
    private HttpTransport        transport;
    private SaasAuthServiceProxy saasAuthServiceProxy;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        saasAuthServiceProxy = new SaasAuthServiceProxy(SAAS_API_ENDPOINT, transport);
    }

    @Test
    public void testLogin() throws Exception {
        Credentials credentials = DtoFactory.newDto(Credentials.class);
        doReturn("{\"value\":\"token\"}").when(transport).doPost(endsWith("/auth/login"), eq(credentials));

        Token token = saasAuthServiceProxy.login(credentials);

        assertEquals(token.getValue(), "token");
    }

    @Test
    public void testLogout() throws Exception {
        saasAuthServiceProxy.logout("auhToken");

        verify(transport).doPost(endsWith("/auth/logout?token=auhToken"), isNull());
    }

    @Test(expectedExceptions = AuthenticationException.class)
    public void loginShouldThrowAuthenticationException() throws Exception {
        Credentials credentials = DtoFactory.newDto(Credentials.class);
        doThrow(new HttpException(400, "error")).when(transport).doPost(endsWith("/auth/login"), eq(credentials));

        saasAuthServiceProxy.login(credentials);
    }

    @Test(expectedExceptions = IOException.class)
    public void loginShouldThrowIOException() throws Exception {
        Credentials credentials = DtoFactory.newDto(Credentials.class);
        doThrow(IOException.class).when(transport).doPost(endsWith("/auth/login"), eq(credentials));

        saasAuthServiceProxy.login(credentials);
    }
}
