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
package com.codenvy.im.saas;

import com.codenvy.im.utils.HttpException;
import com.codenvy.im.utils.HttpTransport;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.auth.AuthenticationException;
import org.eclipse.che.api.auth.shared.dto.Credentials;
import org.eclipse.che.api.auth.shared.dto.Token;

import javax.inject.Named;
import java.io.IOException;

import static com.codenvy.im.utils.Commons.combinePaths;
import static com.codenvy.im.utils.Commons.createDtoFromJson;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
@Singleton
public class SaasAuthServiceProxy {
    private final String        saasApiEndpoint;
    private final HttpTransport transport;

    @Inject
    public SaasAuthServiceProxy(@Named("saas.api.endpoint") String saasApiEndpoint,
                                HttpTransport transport) {
        this.saasApiEndpoint = saasApiEndpoint;
        this.transport = transport;
    }

    /**
     * Logins to Codenvy SaaS.
     *
     * @throws org.eclipse.che.api.auth.AuthenticationException
     *         if login fails
     * @throws IOException
     *         if unexpected error occurred
     */
    public Token login(Credentials credentials) throws AuthenticationException, IOException {
        String requestUrl = combinePaths(saasApiEndpoint, "/auth/login");

        String response;
        try {
            response = transport.doPost(requestUrl, credentials);
        } catch (HttpException e) {
            throw new AuthenticationException();
        }

        return createDtoFromJson(response, Token.class);
    }

    /**
     * Logout from Codenvy SaaS.
     *
     * @throws IOException
     *         if unexpected error occurred
     */
    public void logout(String authToken) throws IOException {
        String requestUrl = combinePaths(saasApiEndpoint, "/auth/logout?token=" + authToken);
        transport.doPost(requestUrl, null);
    }
}
