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
package com.codenvy.im.utils;

import com.codenvy.im.commands.CommandException;
import com.codenvy.im.commands.SimpleCommand;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import static com.codenvy.im.commands.SimpleCommand.createCommand;
import static java.lang.String.format;

/**
 * Generates ssh key.
 *
 * @author Anatoliy Bazko
 */
public class SshKey {
    private static final String GENERATE_STRATEGY = "ssh-keygen -q -P '' -t rsa -f %s";

    private String privatePart;
    private String publicPart;

    public SshKey() throws IOException {
        try {
            generate();
        } catch (CommandException e) {
            invalidateKey();
            throw new IOException("Can't generate ssh key", e);
        }
    }

    private void generate() throws CommandException {
        String file = new BigInteger(120, new SecureRandom()).toString();

        SimpleCommand command = createCommand(format(GENERATE_STRATEGY, file));
        command.execute();

        command = createCommand(format("cat %s", file));
        privatePart = command.execute();

        command = createCommand(format("cat %s.pub", file));
        publicPart = command.execute();

        command = createCommand(format("rm %1$s; rm %1$s.pub", file));
        command.execute();
    }

    /**
     * @return private part of the ssh key or null if key is invalid due to some reason
     */
    @Nullable
    public String getPrivatePart() {
        return privatePart;
    }

    /**
     * @return public part of the ssh key or null if key is invalid due to some reason
     */
    @Nullable
    public String getPublicPart() {
        return publicPart;
    }

    private void invalidateKey() {
        privatePart = null;
        publicPart = null;
    }
}
