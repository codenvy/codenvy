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
package com.codenvy.api.invite;

import com.codenvy.shared.invite.dto.InviteDto;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericResourceMethod;

import javax.ws.rs.Path;

import static com.codenvy.api.permission.server.AbstractPermissionsDomain.SET_PERMISSIONS;

/**
 * Restricts access to methods of {@link InviteService} by users' permissions.
 *
 * <p>Filter contains rules for protecting of all methods of {@link InviteService}.<br>
 * In case when requested method is unknown filter throws {@link ForbiddenException}.
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/invite{path:(/.*)?}")
public class InviteServicePermissionsFilter extends CheMethodInvokerFilter {
    static final String INVITE_METHOD      = "invite";
    static final String GET_INVITES_METHOD = "getInvites";
    static final String REMOVE_METHOD      = "remove";

    @Override
    protected void filter(GenericResourceMethod genericMethodResource, Object[] arguments) throws ApiException {
        String methodName = genericMethodResource.getMethod().getName();
        String domain;
        String instance;
        switch (methodName) {
            case INVITE_METHOD:
                InviteDto inviteDto = (InviteDto)arguments[0];
                domain = inviteDto.getDomainId();
                instance = inviteDto.getInstanceId();
                break;
            case GET_INVITES_METHOD:
            case REMOVE_METHOD:
                domain = ((String)arguments[0]);
                instance = ((String)arguments[1]);
                break;
            default:
                throw new ForbiddenException("User is not authorized to perform specified operation");
        }

        if (!EnvironmentContext.getCurrent().getSubject().hasPermission(domain, instance, SET_PERMISSIONS)) {
            throw new ForbiddenException("User is not authorized to invite into specified instance");
        }
    }
}
