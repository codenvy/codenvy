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
package com.codenvy.machine.authentication.ide;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.ide.api.machine.CheWsAgentLinksModifier;
import org.eclipse.che.ide.api.machine.DevMachine;

import static org.eclipse.che.api.machine.shared.Constants.WSAGENT_WEBSOCKET_REFERENCE;

/**
 * Inserts in each URL machine token.
 *
 * @author Anton Korneta
 */
@Singleton
public class CodenvyMachineLinksModifier extends CheWsAgentLinksModifier {
    private static final String MACHINE_TOKEN = "token";

    private String machineToken;

    @Override
    public void initialize(DevMachine devMachine) {
        Link link = devMachine.getMachineLink(WSAGENT_WEBSOCKET_REFERENCE);
        if (link != null) {
            for (LinkParameter parameter : link.getParameters()) {
                if (MACHINE_TOKEN.equals(parameter.getName())) {
                    machineToken = parameter.getDefaultValue();
                }
            }
        }
    }

    @Override
    public String modify(String agentUrl) {
        if (machineToken != null) {
            return agentUrl + (agentUrl.contains("?") ? '&' : '?') + "token=" + machineToken;
        }
        throw new RuntimeException("Failed to modify url, machine token in not specified");
    }
}
