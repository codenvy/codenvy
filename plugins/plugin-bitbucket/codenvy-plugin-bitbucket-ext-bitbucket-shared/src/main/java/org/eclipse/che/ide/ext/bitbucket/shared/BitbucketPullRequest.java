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
 * Represents a Bitbucket pull request.
 *
 * @author Kevin Pollet
 */
@DTO
public interface BitbucketPullRequest {
    int getId();

    void setId(int id);

    BitbucketPullRequest withId(int id);

    int getVersion();

    void setVersion(int version);

    BitbucketPullRequest withVersion(int version);

    String getTitle();

    void setTitle(String title);

    BitbucketPullRequest withTitle(String title);

    String getDescription();

    void setDescription(String description);

    BitbucketPullRequest withDescription(String description);

    State getState();

    void setState(State state);

    BitbucketPullRequest withState(State state);

    BitbucketPullRequestLinks getLinks();

    void setLinks(BitbucketPullRequestLinks links);

    BitbucketPullRequest withLinks(BitbucketPullRequestLinks links);

    BitbucketPullRequestLocation getSource();

    void setSource(BitbucketPullRequestLocation source);

    BitbucketPullRequest withSource(BitbucketPullRequestLocation source);

    BitbucketPullRequestLocation getDestination();

    void setDestination(BitbucketPullRequestLocation destination);

    BitbucketPullRequest withDestination(BitbucketPullRequestLocation destination);

    BitbucketUser getAuthor();

    void setAuthor(BitbucketUser author);

    BitbucketPullRequest withAuthor(BitbucketUser author);

    enum State {
        OPEN,
        DECLINED,
        MERGED
    }

    @DTO
    interface BitbucketPullRequestLinks {
        BitbucketLink getSelf();

        void setSelf(BitbucketLink self);

        BitbucketLink getHtml();

        void setHtml(BitbucketLink html);
    }

    @DTO
    interface BitbucketPullRequestLocation {
        BitbucketPullRequestBranch getBranch();

        void setBranch(BitbucketPullRequestBranch branch);

        BitbucketPullRequestLocation withBranch(BitbucketPullRequestBranch branch);

        BitbucketPullRequestRepository getRepository();

        void setRepository(BitbucketPullRequestRepository repository);

        BitbucketPullRequestLocation withRepository(BitbucketPullRequestRepository repository);
    }

    @DTO
    interface BitbucketPullRequestRepository {
        String getFullName();

        void setFullName(String fullName);

        BitbucketPullRequestRepository withFullName(String fullName);
    }

    @DTO
    interface BitbucketPullRequestBranch {
        String getName();

        void setName(String name);

        BitbucketPullRequestBranch withName(String name);
    }
}
