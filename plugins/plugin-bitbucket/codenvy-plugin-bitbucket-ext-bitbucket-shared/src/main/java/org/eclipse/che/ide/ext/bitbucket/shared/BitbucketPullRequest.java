/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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
