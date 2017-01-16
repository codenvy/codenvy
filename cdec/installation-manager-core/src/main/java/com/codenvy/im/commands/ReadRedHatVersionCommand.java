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

import com.codenvy.im.agent.LocalAgent;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;

import javax.inject.Named;

import static java.lang.String.format;

/**
 * This command retrieves the version of RedHat distribution Linux.
 * File format for instance: 'CentOS Linux release 7.0.1406 (Core)'
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class ReadRedHatVersionCommand extends SimpleCommand {

    @Inject
    public ReadRedHatVersionCommand(@Named("os.redhat_release_file") String releaseFile) {
        super(format("if [ ! -f %1$s ]; then" +
                     "     exit 1;" +
                     " else" +
                     "     cat %1$s | sed 's/.*\\s\\([0-9\\.]*\\)\\s.*/\\1/';" +
                     " fi", releaseFile),
              new LocalAgent(),
              "Gets CentOS version");
    }

    /**
     * @see com.codenvy.im.commands.SimpleCommand#fetchValue
     */
    @Nullable
    public static String fetchRedHatVersion() {
        return fetchValue(ReadRedHatVersionCommand.class);
    }
}
