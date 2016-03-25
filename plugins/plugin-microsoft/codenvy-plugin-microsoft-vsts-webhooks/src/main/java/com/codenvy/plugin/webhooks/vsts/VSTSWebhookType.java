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


enum VSTSWebhookType {

    WORK_ITEM_CREATED_WEBHOOK("work-item-created"),
    PULL_REQUEST_UPDATED_WEBHOOK("pull-request-updated");

    private final String name;

    VSTSWebhookType(final String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
