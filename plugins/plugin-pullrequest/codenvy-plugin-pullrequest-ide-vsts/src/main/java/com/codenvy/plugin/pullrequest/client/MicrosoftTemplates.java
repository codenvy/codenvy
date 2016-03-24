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
