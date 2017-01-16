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

import com.codenvy.im.utils.Version;

import java.io.FileNotFoundException;

/**
 * @author Anatoliy Bazko
 */
public class ArtifactNotFoundException extends FileNotFoundException {
    public ArtifactNotFoundException(Artifact artifact, Version version) {
        this(artifact.toString(), version.toString());
    }

    public ArtifactNotFoundException(String artifact, String version) {
        super(String.format("Artifact %s:%s not found", artifact, version));
    }

    public ArtifactNotFoundException(Artifact artifact) {
        super(String.format("Artifact '%s' not found", artifact));
    }

    public ArtifactNotFoundException(String message) {
        super(message);
    }

    public static ArtifactNotFoundException from(String artifact) {
        return new ArtifactNotFoundException(String.format("Artifact '%s' not found", artifact));
    }
}
