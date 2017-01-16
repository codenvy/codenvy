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
package com.codenvy.im.commands;

import com.codenvy.im.artifacts.Artifact;
import com.codenvy.im.utils.Version;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Command runs until codenvy becames alive and be of expected version.
 *
 * @author Anatoliy Bazko
 * @author Dmytro Nochevnov
 */
public class WaitOnAliveArtifactOfCorrectVersionCommand implements Command {
    public static final int TIMEOUT_MILLIS = 500;
    private final Artifact artifact;
    private final Version  expectedVersion;

    private static final Logger LOG = Logger.getLogger(WaitOnAliveArtifactOfCorrectVersionCommand.class.getSimpleName());

    public WaitOnAliveArtifactOfCorrectVersionCommand(Artifact artifact, Version expectedVersion) {
        this.artifact = artifact;
        this.expectedVersion = expectedVersion;
    }

    /** {@inheritDoc} */
    @Override
    public String execute() throws CommandException {
        LOG.log(Level.INFO, toString());

        for (; ; ) {
            try {
                if (! artifact.isAlive()) {
                    continue;
                }

                if (! expectedVersion.equals(artifact.getInstalledVersion().orElse(null))) {
                    continue;
                }

                break;
            } catch (IOException e) {
                // ignore because it is correct exception until Codenvy API server starts
            }

            try {
                Thread.sleep(TIMEOUT_MILLIS);
            } catch (InterruptedException e) {
                // ignore to allow being successful interrupted by PuppetErrorInterrupter
                break;
            }
        }

        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getDescription() {
        return toString();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return format("Expected to be installed '%s' of the version '%s'", artifact, expectedVersion);
    }

}