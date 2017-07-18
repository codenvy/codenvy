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
import org.eclipse.che.dto.shared.JsonFieldName;

import java.util.List;

@DTO
public interface WorkItemCreationResource {

    public String LINKS_FIELD = "_links";

    /**
     * Get resource id.
     *
     * @return {@link String} id
     */
    String getId();

    void setId(final String id);

    WorkItemCreationResource withId(final String id);

    /**
     * Get resource revision.
     *
     * @return {@link String} rev
     */
    String getRev();

    void setRev(final String rev);

    WorkItemCreationResource withRev(final String rev);

    /**
     * Get resource fields.
     *
     * @return {@link Fields} fields
     */
    Fields getFields();

    void setFields(final Fields fields);

    WorkItemCreationResource withFields(final Fields fields);

    /**
     * Get resource relations.
     *
     * @return {@link java.util.List <Relation>} relations
     */
    List<Relation> getRelations();

    void setRelations(final List<Relation> relations);

    WorkItemCreationResource withRelations(final List<Relation> relations);

    /**
     * Get resource links.
     *
     * @return {@link PullRequestUpdatedResourceLinks} links
     */
    @JsonFieldName(LINKS_FIELD)
    PullRequestUpdatedResourceLinks getLinks();

    void setLinks(final PullRequestUpdatedResourceLinks links);

    WorkItemCreationResource withLinks(final PullRequestUpdatedResourceLinks links);

    /**
     * Get resource url.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(final String url);

    WorkItemCreationResource withUrl(final String url);
}
