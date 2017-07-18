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
package com.codenvy.plugin.webhooks.vsts.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PullRequestUpdatedResourceLinks {
    /**
     * Get self link.
     *
     * @return {@link Link} self
     */
    Link getSelf();

    void setSelf(final Link self);

    PullRequestUpdatedResourceLinks withSelf(final Link self);

    /**
     * Get workItemUpdates link.
     *
     * @return {@link Link} workItemUpdates
     */
    Link getWorkItemUpdates();

    void setWorkItemUpdates(final Link workItemUpdates);

    PullRequestUpdatedResourceLinks withWorkItemUpdates(final Link workItemUpdates);

    /**
     * Get workItemRevisions link.
     *
     * @return {@link Link} workItemRevisions
     */
    Link getWorkItemRevisions();

    void setWorkItemRevisions(final Link workItemRevisions);

    PullRequestUpdatedResourceLinks withWorkItemRevisions(final Link workItemRevisions);

    /**
     * Get workItemHistory link.
     *
     * @return {@link Link} workItemHistory
     */
    Link getWorkItemHistory();

    void setWorkItemHistory(final Link workItemHistory);

    PullRequestUpdatedResourceLinks withWorkItemHistory(final Link workItemHistory);

    /**
     * Get html link.
     *
     * @return {@link Link} html
     */
    Link getHtml();

    void setHtml(final Link html);

    PullRequestUpdatedResourceLinks withHtml(final Link html);

    /**
     * Get workItemType link.
     *
     * @return {@link Link} workItemType
     */
    Link getWorkItemType();

    void setWorkItemType(final Link workItemType);

    PullRequestUpdatedResourceLinks withWorkItemType(final Link workItemType);

    /**
     * Get fields link.
     *
     * @return {@link Link} fields
     */
    Link getFields();

    void setFields(final Link fields);

    PullRequestUpdatedResourceLinks withFields(final Link fields);
}
