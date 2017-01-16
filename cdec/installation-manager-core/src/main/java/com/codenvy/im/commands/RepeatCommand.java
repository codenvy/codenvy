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

/**
 * Repeats command execution several times.
 * Prevents throwing exception immediately.
 *
 * @author Anatoliy Bazko
 */
public class RepeatCommand implements Command {
    private static final int DEFAULT_TRIES = 2;

    private final Command command;
    private final int     tries;

    public RepeatCommand(Command command) {
        this(command, DEFAULT_TRIES);
    }

    public RepeatCommand(Command command, int tries) {
        this.command = command;
        this.tries = tries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String execute() throws CommandException {
        CommandException exception = null;

        for (int i = 0; i < tries; i++) {
            try {
                return command.execute();
            } catch (CommandException e) {
                if (exception != null) {
                    e.addSuppressed(exception);
                }
                exception = e;
            }
        }

        throw exception;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Repeat " + tries + " times: " + command.getDescription();
    }

    @Override
    public String toString() {
        return "Repeat " + tries + " times: " + command.toString();
    }
}
