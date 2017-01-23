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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;

import org.eclipse.che.api.environment.server.InfrastructureProvisioner;

/**
 * @author Alexander Garagatyi
 */
public class WorkspaceInfrastructureModule extends AbstractModule {
    @Override
    protected void configure() {
        MapBinder<String, InfrastructureProvisioner> infrastructureProvisionerBinder =
                MapBinder.newMapBinder(binder(),
                                       String.class,
                                       InfrastructureProvisioner.class);

        infrastructureProvisionerBinder.addBinding("in-container")
                                       .to(CodenvyInContainerInfrastructureProvisioner.class);
    }
}
