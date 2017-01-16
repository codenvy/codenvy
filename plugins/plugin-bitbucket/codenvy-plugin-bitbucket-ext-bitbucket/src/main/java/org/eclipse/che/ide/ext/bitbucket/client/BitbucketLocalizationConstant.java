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