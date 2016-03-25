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
package com.codenvy.plugin.webhooks.github.shared;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface Commit {
    String getId();

    void setId(final String id);

    Commit withId(final String id);


    boolean getDistinct();

    void setDistinct(final boolean distinct);

    Commit withDistinct(final boolean distinct);


    String getMessage();

    void setMessage(final String message);

    Commit withMessage(final String message);


    String getTimestamp();

    void setTimestamp(final String timestamp);

    Commit withTimestamp(final String timestamp);


    String getUrl();

    void setUrl(final String url);

    Commit withUrl(final String url);


    Author getAuthor();

    void setAuthor(final Author author);

    Commit withAuthor(final Author author);


    Committer getCommitter();

    void setCommitter(final Committer committer);

    Commit withCommitter(final Committer committer);
}
