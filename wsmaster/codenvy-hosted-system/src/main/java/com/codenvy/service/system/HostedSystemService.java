/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
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
