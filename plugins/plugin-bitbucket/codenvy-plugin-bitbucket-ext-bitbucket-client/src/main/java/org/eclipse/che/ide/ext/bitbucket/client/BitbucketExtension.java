/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.bitbucket.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;

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
  public BitbucketExtension(
      @NotNull final SshKeyUploaderRegistry uploaderRegistry,
      @NotNull final BitbucketSshKeyProvider bitbucketSshKeyProvider) {

    uploaderRegistry.registerUploader(BITBUCKET_HOST, bitbucketSshKeyProvider);
  }
}
