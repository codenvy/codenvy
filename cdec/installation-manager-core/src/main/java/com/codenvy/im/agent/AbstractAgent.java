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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * @author Anatoliy Bazko
 */
public abstract class AbstractAgent implements Agent {
    private final Logger LOG = Logger.getLogger(this.getClass().getName());

    /**
     * Map<command, error-message-regex>
     */
    private static final Map<String, Pattern> IGNORING_ERRORS = ImmutableMap.of();

    protected String processOutput(String command, int exitStatus, InputStream in, InputStream error) throws Exception {
        String output = readOutput(in);
        String errorOutput = readOutput(error);

        if (exitStatus == 0) {
            return output;
        }

        if (checkIgnoringErrors(command, errorOutput)) {
            LOG.warning(format("Command '%s' faced ignoring error '%s'.", command, errorOutput));
            return output;
        }

        throw new Exception(format("Output: %s; Error: %s.", output, errorOutput));
    }

    private boolean checkIgnoringErrors(String command, String errorOutput) {
        if (getIgnoringErrors().containsKey(command)) {
            return getIgnoringErrors().get(command).matcher(errorOutput).matches();
        }

        return false;
    }

    private String readOutput(InputStream in) throws IOException {
        try {
            String output = IOUtils.toString(in, Charset.forName("UTF-8"));
            if (output.endsWith("\n")) {
                output = output.substring(0, output.length() - 1);
            }
            return output;
        } finally {
            in.close();
        }
    }
    
    protected Map<String, Pattern> getIgnoringErrors() {
        return IGNORING_ERRORS;
    }

}
