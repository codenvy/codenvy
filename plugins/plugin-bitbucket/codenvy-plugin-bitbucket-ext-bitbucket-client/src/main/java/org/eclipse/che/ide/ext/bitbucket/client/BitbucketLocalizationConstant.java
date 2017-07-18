/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.bitbucket.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Localization constants for the Bitbucket plugin.
 *
 * @author Kevin Pollet
 */
public interface BitbucketLocalizationConstant extends Messages {
    @Key("bitbucket.ssh.key.title")
    String bitbucketSshKeyTitle();

    @Key("bitbucket.ssh.key.label")
    String bitbucketSshKeyLabel();

    @Key("bitbucket.ssh.key.update.failed")
    String bitbucketSshKeyUpdateFailed();
}