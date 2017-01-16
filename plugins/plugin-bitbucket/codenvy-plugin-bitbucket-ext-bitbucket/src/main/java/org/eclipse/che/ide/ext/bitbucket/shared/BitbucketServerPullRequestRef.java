/*
 *  [2012] - [2017] Codenvy, S.A.
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
package org.eclipse.che.ide.ext.bitbucket.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Represents Bitbucket Server pull request reference.
 *
 * @author Igor Vinokur.
 */
@DTO
public interface BitbucketServerPullRequestRef {
    String getId();

    void setId(String id);

    BitbucketServerPullRequestRef withId(String id);

    String getDisplayId();

    void setDisplayId(String displayId);

    BitbucketServerPullRequestRef withDisplayId(String displayId);

    String getLatestCommit();

    void setLatestCommit(String latestCommit);

    BitbucketServerPullRequestRef withLatestCommit(String latestCommit);

    BitbucketServerRepository getRepository();

    void setRepository(BitbucketServerRepository repository);

    BitbucketServerPullRequestRef withRepository(BitbucketServerRepository repository);
}
