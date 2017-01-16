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
import com.google.common.collect.ImmutableSet;

import java.util.Set;
import java.util.regex.Pattern;

import static com.codenvy.im.commands.ReadRedHatVersionCommand.fetchRedHatVersion;
import static java.lang.String.format;

/** @author Anatoliy Bazko */
public class OSUtils {
    public static String VERSION;
    public static final String      RED_HAT_OS_VERSION = "RedHatOSVersion";
    public static final Set<String> SUPPORTED_VERSIONS = ImmutableSet.of("6", "7");

    static {
        try {
            VERSION = detectVersion();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    /** @return the major number of OS version where Codenvy is being installed, for instance: 6 */
    public static String getVersion() {
        return VERSION;
    }

    protected static String detectVersion() throws CommandException {
        String version = System.getProperty(RED_HAT_OS_VERSION); // major version only
        if (version == null) {
            version = fetchRedHatVersion();
        }

        return parseMajorVersion(version);
    }

    /**
     * @throws IllegalArgumentException
     *         if version parameter is incorrect
     * @throws IllegalStateException
     *         if version is unsupported
     */
    protected static String parseMajorVersion(String version) throws IllegalArgumentException, IllegalStateException {
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("OS version is unknown");
        }

        String[] str = version.split(Pattern.quote("."));
        if (str.length == 0) {
            throw new IllegalArgumentException("OS version is unknown");
        }

        String ver = str[0];
        if (!SUPPORTED_VERSIONS.contains(ver)) {
            throw new IllegalStateException(format("OS version %s is unsupported", ver));
        }

        return ver;
    }
}
