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
package org.eclipse.che.ide.ext.git.server.nativegit;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.eclipse.che.api.git.CredentialsProvider;
import org.eclipse.che.inject.DynaModule;

/**
 * The module that contains configuration of the server side part of the Git extension.
 *
 * @author Kevin Pollet
 */
@DynaModule
public class BitbucketGitModule extends AbstractModule {
    @Override
    protected void configure() {
        Multibinder.newSetBinder(binder(), CredentialsProvider.class).addBinding().to(BitbucketOAuthCredentialProvider.class);
    }
}

