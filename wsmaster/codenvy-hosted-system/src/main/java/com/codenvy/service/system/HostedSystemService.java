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
package com.codenvy.service.system;

import com.codenvy.service.system.dto.SystemRamLimitDto;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.system.server.SystemManager;
import org.eclipse.che.api.system.server.SystemService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Extends system REST API.
 *
 * @author Igor Vinokur
 * @author Yevhenii Voevodin
 */
@Path("/system")
public class HostedSystemService extends SystemService {

    private final SystemRamInfoProvider systemRamInfoProvider;

    @Inject
    public HostedSystemService(SystemManager manager, SystemRamInfoProvider systemRamInfoProvider) {
        super(manager);
        this.systemRamInfoProvider = systemRamInfoProvider;
    }

    @GET
    @Path("/ram/limit")
    @Produces(MediaType.APPLICATION_JSON)
    public SystemRamLimitDto getSystemRamLimitStatus() throws ServerException {
        return newDto(SystemRamLimitDto.class)
                .withSystemRamLimitExceeded(systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded());
    }
}
