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

import com.codenvy.im.utils.HttpTransport;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.user.shared.dto.UserDto;

import javax.inject.Named;
import java.io.IOException;

import static com.codenvy.im.utils.Commons.combinePaths;
import static com.codenvy.im.utils.Commons.createDtoFromJson;

/**
 * @author Dmytro Nochevnov
 * @author Anatoliy Bazko
 */
@Singleton
public class SaasUserServiceProxy {

    private final String        saasApiEndpoint;
    private final HttpTransport transport;

    @Inject
    public SaasUserServiceProxy(@Named("saas.api.endpoint") String saasApiEndpoint,
                                HttpTransport transport) {
        this.saasApiEndpoint = saasApiEndpoint;
        this.transport = transport;
    }

    /** @return the current user's email */
    public String getUserEmail(String accessToken) throws IOException {
        String requestUrl = combinePaths(saasApiEndpoint, "user");
        UserDto descriptor = createDtoFromJson(transport.doGet(requestUrl, accessToken), UserDto.class);
        return descriptor.getEmail();
    }
}
