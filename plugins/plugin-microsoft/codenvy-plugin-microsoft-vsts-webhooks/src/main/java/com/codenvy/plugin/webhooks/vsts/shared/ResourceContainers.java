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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ResourceContainers {

    /**
     * Get collection container.
     *
     * @return {@link ResourceContainer} collection
     */
    ResourceContainer getCollection();

    void setCollection(final ResourceContainer collection);

    ResourceContainers withCollection(final ResourceContainer collection);

    /**
     * Get account container.
     *
     * @return {@link ResourceContainer} account
     */
    ResourceContainer getAccount();

    void setAccount(final ResourceContainer account);

    ResourceContainers withAccount(final ResourceContainer account);

    /**
     * Get project container.
     *
     * @return {@link ResourceContainer} project
     */
    ResourceContainer getProject();

    void setProject(final ResourceContainer project);

    ResourceContainers withProject(final ResourceContainer project);
}
