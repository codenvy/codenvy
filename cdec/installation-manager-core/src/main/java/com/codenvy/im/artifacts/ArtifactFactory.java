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

import javax.validation.constraints.NotNull;

import static com.codenvy.im.utils.InjectorBootstrap.INJECTOR;

/** @author Anatoliy Bazko */
public class ArtifactFactory {

    /** Artifact factory. */
    public static Artifact createArtifact(@NotNull String name) throws ArtifactNotFoundException {
        switch (name) {
            case CDECArtifact.NAME:
                return INJECTOR.getInstance(CDECArtifact.class);
            case InstallManagerArtifact.NAME:
                return INJECTOR.getInstance(InstallManagerArtifact.class);
        }

        throw ArtifactNotFoundException.from(name);
    }
}
