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
package com.codenvy.machine.authentication.ide.inject;


import com.codenvy.machine.authentication.ide.MachineTokenServiceClient;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.ide.rest.AsyncRequestFactory;

/**
 * @author Anton Korneta
 */
@ExtensionGinModule
public class MachineAuthGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(AsyncRequestFactory.class).to(com.codenvy.machine.authentication.ide.MachineAsyncRequestFactory.class);
        bind(MachineTokenServiceClient.class).to(com.codenvy.machine.authentication.ide.MachineTokenServiceClientImpl.class)
                                             .in(Singleton.class);
        bind(CheWsAgentLinksModifier.class).to(com.codenvy.machine.authentication.ide.CodenvyMachineLinksModifier.class);
    }
}
