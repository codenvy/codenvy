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
package com.codenvy.plugin.webhooks.github.shared;

import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.dto.shared.JsonFieldName;

@DTO
public interface Repository {

    public String HTML_URL_FIELD   = "html_url";
    public String FULL_NAME_FIELD  = "full_name";
    public String CREATED_AT_FIELD = "created_at";

    /**
     * Get repository's name.
     *
     * @return {@link String} name
     */
    String getName();

    void setName(String name);

    /**
     * Get repository's location.
     *
     * @return {@link String} url
     */
    String getUrl();

    void setUrl(String url);

    /**
     * Get repository's home page.
     *
     * @return {@link String} home page
     */
    String getHomepage();

    void setHomepage(String homePage);

    /**
     * Get the number of repository's forks.
     *
     * @return forks
     */
    int getForks();

    void setForks(int forks);

    /**
     * Get repository's language.
     *
     * @return {@link String} language
     */
    String getLanguage();

    void setLanguage(String language);

    /**
     * Get fork state.
     *
     * @return {@link Boolean} <code>true</code> id forked
     */
    boolean isFork();

    void setFork(boolean isFork);

    /**
     * Get the number of repository's watchers.
     *
     * @return {@link Integer} the number of watchers
     */
    int getWatchers();

    void setWatchers(int watchers);

    /**
     * Get repository's size.
     *
     * @return {@link Integer} size
     */
    int getSize();

    void setSize(int size);

    /**
     * Get repository's description.
     *
     * @return {@link String} description
     */
    String getDescription();

    void setDescription(String description);

    /**
     * Get HTML URL.
     *
     * @return {@link String} HTML URL
     */
    @JsonFieldName(HTML_URL_FIELD)
    String getHtmlUrl();

    void setHtmlUrl(String htmlUrl);

    /**
     * Get full name.
     *
     * @return {@link String} fullName
     */
    @JsonFieldName(FULL_NAME_FIELD)
    String getFullName();

    void setFullName(String fullName);

    /**
     * Get created at.
     *
     * @return {@link String} createdAt
     */
    @JsonFieldName(CREATED_AT_FIELD)
    String getCreatedAt();

    void setCreatedAt(String createdAt);
}
