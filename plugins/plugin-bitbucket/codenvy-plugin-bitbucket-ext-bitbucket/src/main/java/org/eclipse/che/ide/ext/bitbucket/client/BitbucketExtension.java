/*
 *  [2012] - [2016] Codenvy, S.A.
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
package org.eclipse.che.ide.ext.bitbucket.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;

import javax.validation.constraints.NotNull;

/**
 * Extension adds Bitbucket support to the IDE Application.
 *
 * @author Kevin Pollet
 */
@Singleton
@Extension(title = "Bitbucket", version = "3.0.0")
public class BitbucketExtension {
    public static final String BITBUCKET_HOST = "bitbucket.org";

    @Inject
    public BitbucketExtension(@NotNull final SshKeyUploaderRegistry uploaderRegistry,
                              @NotNull final BitbucketSshKeyProvider bitbucketSshKeyProvider) {

        uploaderRegistry.registerUploader(BITBUCKET_HOST, bitbucketSshKeyProvider);
    }
}
