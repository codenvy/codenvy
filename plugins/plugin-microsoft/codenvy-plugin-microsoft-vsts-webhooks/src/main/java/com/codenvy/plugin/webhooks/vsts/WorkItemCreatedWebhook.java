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
