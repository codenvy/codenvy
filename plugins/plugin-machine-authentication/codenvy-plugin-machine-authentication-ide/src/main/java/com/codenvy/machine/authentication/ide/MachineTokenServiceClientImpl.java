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
package com.codenvy.machine.authentication.ide;


import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;

/**
 * Implementation for {@link MachineTokenServiceClient}.
 *
 * @author Anton Korneta
 */
public class MachineTokenServiceClientImpl implements MachineTokenServiceClient {

    private final AppContext             appContext;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final String                 baseUrl;


    @Inject
    public MachineTokenServiceClientImpl(@RestContext String restContext,
                                         AppContext appContext,
                                         AsyncRequestFactory asyncRequestFactory,
                                         DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.appContext = appContext;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.baseUrl = restContext + "/machine/token";
    }

    public Promise<MachineTokenDto> getMachineToken() {
        return asyncRequestFactory.createGetRequest(baseUrl + '/' + appContext.getWorkspaceId())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(MachineTokenDto.class));
    }

    @Override
    public Promise<UserDescriptor> getUserByToken(String token) {
        return asyncRequestFactory.createGetRequest(baseUrl + "/user/" + token)
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class));
    }
}
