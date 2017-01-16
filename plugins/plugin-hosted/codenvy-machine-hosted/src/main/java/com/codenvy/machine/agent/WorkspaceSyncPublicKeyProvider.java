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
package com.codenvy.machine.agent;

import org.eclipse.che.commons.lang.IoUtil;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Reads content of public key that is used for workspaces projects synchronization from file.
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceSyncPublicKeyProvider implements Provider<String> {
    private String pubKey;

    @Inject
    public WorkspaceSyncPublicKeyProvider(@Named("workspace.backup.public_key_path") String pubKeyPath)
            throws IOException {

        pubKey = IoUtil.readAndCloseQuietly(new BufferedInputStream(new FileInputStream(new File(pubKeyPath))));
    }

    @Override
    public String get() {
        return pubKey;
    }
}
