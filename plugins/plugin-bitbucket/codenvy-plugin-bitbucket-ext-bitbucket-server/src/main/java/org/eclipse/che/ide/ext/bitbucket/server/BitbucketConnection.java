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
package org.eclipse.che.ide.ext.bitbucket.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequests;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;

/**
 * Connection for retrieving data from Bitbucket.
 *
 * @author Igor Vinokur
 */
public interface BitbucketConnection {

  /**
   * Get user information.
   *
   * @return {@link BitbucketUser} object that describes received user
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  BitbucketUser getUser() throws ServerException, IOException, BitbucketException;

  /**
   * Get Bitbucket repository information.
   *
   * @param owner the repository owner
   * @param repositorySlug url compatible repository name
   * @return {@link BitbucketRepository} object that describes received repository
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  BitbucketRepository getRepository(String owner, String repositorySlug)
      throws IOException, BitbucketException, ServerException;

  /**
   * Get Bitbucket repository pull requests.
   *
   * @param owner the repositories owner
   * @param repositorySlug url compatible repository name
   * @return {@link BitbucketPullRequests} object that describes received pull requests
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  List<BitbucketPullRequest> getRepositoryPullRequests(String owner, String repositorySlug)
      throws ServerException, IOException, BitbucketException;

  /**
   * Open a pull request in the Bitbucket repository.
   *
   * @param owner the repository owner
   * @param repositorySlug url compatible repository name
   * @param pullRequest {@link BitbucketPullRequest} object that describes pull request parameters
   * @return {@link BitbucketPullRequest} object that describes opened pull request.
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  BitbucketPullRequest openPullRequest(
      String owner, String repositorySlug, BitbucketPullRequest pullRequest)
      throws ServerException, IOException, BitbucketException;

  /**
   * Update a pull request in the Bitbucket repository.
   *
   * @param owner the repository owner
   * @param repositorySlug url compatible repository name
   * @param pullRequest {@link BitbucketPullRequest} object that describes pull request parameters
   *     to be updated
   * @return {@link BitbucketPullRequest} object that describes updated pull request
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned error
   */
  BitbucketPullRequest updatePullRequest(
      String owner, String repositorySlug, BitbucketPullRequest pullRequest)
      throws ServerException, IOException, BitbucketException;

  /**
   * Get Bitbucket repository forks.
   *
   * @param owner the repository owner
   * @param repositorySlug url compatible repository name
   * @return {@link BitbucketRepositories} object that describes received forks
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  List<BitbucketRepository> getRepositoryForks(String owner, String repositorySlug)
      throws IOException, BitbucketException, ServerException;

  /**
   * Fork a Bitbucket repository.
   *
   * @param owner the repository owner
   * @param repositorySlug url compatible repository name
   * @param forkName the fork name
   * @param isForkPrivate if the fork must be private
   * @return {@link BitbucketRepositoryFork} object that describes created fork
   * @throws ServerException if any error occurs when parse Json response
   * @throws IOException if any i/o errors occurs
   * @throws BitbucketException if Bitbucket returned unexpected or error status for request
   */
  BitbucketRepositoryFork forkRepository(
      String owner, String repositorySlug, String forkName, boolean isForkPrivate)
      throws IOException, BitbucketException, ServerException;

  /**
   * Add authorization header to given HTTP connection.
   *
   * @param http HTTP connection
   * @param requestMethod request method. Is needed when using oAuth1
   * @param requestUrl request url. Is needed when using oAuth1
   * @throws IOException if i/o error occurs when try to refresh expired oauth token
   */
  void authorizeRequest(HttpURLConnection http, String requestMethod, String requestUrl)
      throws IOException;
}
