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
package com.codenvy.machine.agent;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.ExtendedMachine;
import org.eclipse.che.api.environment.server.InfrastructureProvisioner;
import org.eclipse.che.api.environment.server.exception.EnvironmentException;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

import static java.lang.String.format;

/**
 * Infrastructure provisioner that finds another infrastructure provisioner needed for current type of installation
 * and forward provisioning to it.
 * </p>
 * Different strategies of provisioning are switched by property {@link #INFRASTRUCTURE_TYPE_PROPERTY}.
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
    public void provision(Environment envConfig, CheServicesEnvironmentImpl internalEnv) throws EnvironmentException {
        environmentBasedInfraProvisioner.provision(envConfig, internalEnv);
    }

    @Override
    public void provision(ExtendedMachine machineConfig, CheServiceImpl internalMachine) throws EnvironmentException {
        environmentBasedInfraProvisioner.provision(machineConfig, internalMachine);
    }
}
