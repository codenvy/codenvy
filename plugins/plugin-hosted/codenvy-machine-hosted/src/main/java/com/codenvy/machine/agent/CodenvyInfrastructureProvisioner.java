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
package com.codenvy.machine.agent;

import org.eclipse.che.api.environment.server.InfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static java.lang.String.format;

/**
 * Infrastructure provisioner that finds another infrastructure provisioner needed for current type of installation
 * and forward provisioning to it.
 * </p>
 * Different strategies of provisioning are switched by property {@value #INFRASTRUCTURE_TYPE_PROPERTY}.
 *
 * @author Alexander Garagatyi
 */
public class CodenvyInfrastructureProvisioner implements InfrastructureProvisioner {
    public static final String INFRASTRUCTURE_TYPE_PROPERTY = "codenvy.infrastructure";

    private final InfrastructureProvisioner environmentBasedInfraProvisioner;

    @Inject
    public CodenvyInfrastructureProvisioner(@Named(INFRASTRUCTURE_TYPE_PROPERTY) String codenvyInfrastructure,
                                            Map<String, InfrastructureProvisioner> infrastructureProvisioners) {
        if (!infrastructureProvisioners.containsKey(codenvyInfrastructure)) {
            throw new RuntimeException(format("Property '%s' has illegal value '%s'. Valid values: '%s'",
                                              INFRASTRUCTURE_TYPE_PROPERTY,
                                              codenvyInfrastructure,
                                              infrastructureProvisioners.keySet()));
        }
        environmentBasedInfraProvisioner = infrastructureProvisioners.get(codenvyInfrastructure);
    }

    @Override
    public void provision(EnvironmentImpl envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        environmentBasedInfraProvisioner.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachineImpl machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        environmentBasedInfraProvisioner.provision(machineConfig, internalMachine);
    }
}
