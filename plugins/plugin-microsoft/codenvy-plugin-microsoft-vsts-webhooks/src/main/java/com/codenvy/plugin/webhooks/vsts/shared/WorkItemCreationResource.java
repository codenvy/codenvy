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
