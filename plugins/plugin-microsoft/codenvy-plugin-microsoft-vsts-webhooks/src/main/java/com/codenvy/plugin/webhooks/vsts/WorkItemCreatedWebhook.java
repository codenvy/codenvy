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
package com.codenvy.plugin.webhooks.vsts;

import org.eclipse.che.commons.lang.Pair;

/**
 * Wrapper that provides data for a configured VSTS 'work item created' webhook
 *
 * @author Stephane Tournie
 */
public class WorkItemCreatedWebhook {

    private final String               host;
    private final String               account;
    private final String               collection;
    private final String               apiVersion;
    private final Pair<String, String> credentials;

    public WorkItemCreatedWebhook(final String host, final String account, final String collection, final String apiVersion,
                                  final Pair<String, String> credentials) {
        this.host = host;
        this.account = account;
        this.collection = collection;
        this.apiVersion = apiVersion;
        this.credentials = credentials;
    }

    public String getHost() {
        return host;
    }

    public String getAccount() {
        return account;
    }

    public String getCollection() {
        return collection;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Pair<String, String> getCredentials() {
        return credentials;
    }
}
