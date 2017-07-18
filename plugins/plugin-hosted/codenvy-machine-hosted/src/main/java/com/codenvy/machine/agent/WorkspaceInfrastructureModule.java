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
