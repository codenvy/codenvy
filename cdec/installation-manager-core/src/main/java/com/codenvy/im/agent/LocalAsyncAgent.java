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
package com.codenvy.im.agent;

import static com.codenvy.im.agent.AgentException.create;

/**
 * Execute local command in asynchronous mode.
 * @author Dmytro Nochevnov
 */
public class LocalAsyncAgent extends AbstractAgent {
    public LocalAsyncAgent() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(String command) throws AgentException {
        ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", command);

        try {
            pb.redirectErrorStream(true);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.start();

            return null;
        } catch (Exception e) {
            String errMessage = String.format("Can't execute command '%s'.", command);
            throw create(errMessage, e);
        }
    }
}
