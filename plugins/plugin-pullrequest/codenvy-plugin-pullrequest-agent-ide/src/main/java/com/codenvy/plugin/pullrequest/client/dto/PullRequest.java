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
package com.codenvy.plugin.pullrequest.client.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface PullRequest {
    String getId();

    PullRequest withId(String id);

    String getUrl();

    PullRequest withUrl(String url);

    String getHtmlUrl();

    PullRequest withHtmlUrl(String htmlUrl);

    String getNumber();

    PullRequest withNumber(String number);

    String getState();

    PullRequest withState(String state);

    String getHeadRef();

    PullRequest withHeadRef(String head);

    String getDescription();

    PullRequest withDescription(String description);
}
