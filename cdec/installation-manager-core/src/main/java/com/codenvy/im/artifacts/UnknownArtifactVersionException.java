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
package com.codenvy.im.artifacts;

import static java.lang.String.format;

/**
 * @author Dmytro Nochevnov
 */
public class UnknownArtifactVersionException extends RuntimeException {
    private static final String ERROR_MESSAGE_TEMPLATE = "It is impossible to recognize the version of artifact '%s'.";
    
    public UnknownArtifactVersionException(Artifact artifact) {
        super(format(ERROR_MESSAGE_TEMPLATE, artifact.getName()));
    }
    
    public static UnknownArtifactVersionException of(Artifact artifact) {
        return new UnknownArtifactVersionException(artifact);
    }
}
