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
package com.codenvy.auth.sso.client;

import com.codenvy.machine.authentication.server.MachineTokenRegistry;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Retrieves master {@link Subject} based on the machine token
 * Machine token detection is simple and based on the machine token prefix,
 * so if token is prefixed with 'machine' then the mechanism is triggered
 * otherwise method call delegated to the super {@link HttpSsoServerClient#getSubject(String, String)}.
 *
 * <p>Note that this component must be deployed to api war only.
 *
 * @author Yevhenii Voevodin
 */
@Singleton
public class MachineSsoServerClient extends HttpSsoServerClient {
    private static final Logger LOG = LoggerFactory.getLogger(MachineSsoServerClient.class);

    private final MachineTokenRegistry tokenRegistry;
    private final UserManager          userManager;

    @Inject
    public MachineSsoServerClient(@Named("che.api") String apiEndpoint,
                                  HttpJsonRequestFactory requestFactory,
                                  MachineTokenRegistry tokenRegistry,
                                  UserManager userManager) {
        super(apiEndpoint, requestFactory);
        this.tokenRegistry = tokenRegistry;
        this.userManager = userManager;
    }

    @Override
    public Subject getSubject(String token, String clientUrl) {
        if (!token.startsWith("machine")) {
            return super.getSubject(token, clientUrl);
        }
        try {
            final User user = userManager.getById(tokenRegistry.getUserId(token));
            return new SubjectImpl(user.getName(), user.getId(), token, false);
        } catch (ApiException x) {
            LOG.warn(x.getLocalizedMessage(), x);
        }
        return null;
    }
}
