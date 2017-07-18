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
package com.codenvy.plugin.pullrequest.client;

import com.google.gwt.i18n.client.Messages;

/**
 * @author Mihail Kuznyetsov
 */
public interface MicrosoftTemplates extends Messages {

    @Messages.DefaultMessage("https://{0}.visualstudio.com/{1}/_git/{2}.git")
    String httpUrlTemplate(String accountName, String collection, String repository);

    @Messages.DefaultMessage("https://{0}.visualstudio.com/{1}/{2}/_git/{3}.git")
    @Key("httpUrlTemplateWithProjectAndRepo")
    String httpUrlTemplate(String accountName, String collection, String username, String repository);

    @Messages.DefaultMessage("https://{0}.visualstudio.com/{1}/_git/{2}/pullrequest/{3}")
    String pullRequestUrlTemplate(String accountName, String collection, String repository, String pullRequestNumber);

    @Messages.DefaultMessage("https://{0}.visualstudio.com/{1}/{2}/_git/{3}/pullrequest/{4}")
    @Key("pullRequestUrlTemplateWithProjectAndRepo")
    String pullRequestUrlTemplate(String accountName, String collection, String username, String repository, String pullRequestNumber);
}
