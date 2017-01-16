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
package org.eclipse.che.ide.ext.bitbucket.shared;

import javax.validation.constraints.NotNull;

/**
 * Ensure parameter preconditions.
 *
 * @author Kevin Pollet
 */
public final class Preconditions {
    /**
     * Disable instantiation.
     */
    private Preconditions() {
    }

    /**
     * Checks that the given expression is {@code true}.
     *
     * @param expression
     *         the expression.
     * @param parameterName
     *         the parameter name, cannot be {@code null}.
     * @throws IllegalArgumentException
     *         if the given expression is {@code false}.
     */
    public static void checkArgument(final boolean expression, @NotNull final String parameterName) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException("'" + parameterName + "' parameter is not valid");
        }
    }
}
