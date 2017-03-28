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
package org.eclipse.che.ide.ext.bitbucket.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequests;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

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
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    BitbucketUser getUser() throws ServerException, IOException, BitbucketException;

    /**
     * Get Bitbucket repository information.
     *
     * @param owner
     *         the repository owner
     * @param repositorySlug
     *         the repository name
     * @return {@link BitbucketRepository} object that describes received repository
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    BitbucketRepository getRepository(String owner, String repositorySlug) throws IOException, BitbucketException, ServerException;

    /**
     * Get Bitbucket repository pull requests.
     *
     * @param owner
     *         the repositories owner
     * @param repositorySlug
     *         the repository name
     * @return {@link BitbucketPullRequests} object that describes received pull requests
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    List<BitbucketPullRequest> getRepositoryPullRequests(String owner, String repositorySlug) throws ServerException,
                                                                                                     IOException,
                                                                                                     BitbucketException;

    /**
     * Open a pull request in the Bitbucket repository.
     *
     * @param owner
     *         the repository owner
     * @param repositorySlug
     *         the repository name
     * @param pullRequest
     *         {@link BitbucketPullRequest} object that describes pull request parameters
     * @return {@link BitbucketPullRequest} object that describes opened pull request.
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    BitbucketPullRequest openPullRequest(String owner, String repositorySlug, BitbucketPullRequest pullRequest) throws ServerException,
                                                                                                                       IOException,
                                                                                                                       BitbucketException;

    /**
     * Update a pull request in the Bitbucket repository.
     *
     * @param owner
     *         the repository owner
     * @param repositorySlug
     *         the repository name
     * @param pullRequest
     *         {@link BitbucketPullRequest} object that describes pull request parameters to be updated
     * @return {@link BitbucketPullRequest} object that describes updated pull request
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned error
     */
    BitbucketPullRequest updatePullRequest(String owner,
                                           String repositorySlug,
                                           BitbucketPullRequest pullRequest) throws ServerException, IOException, BitbucketException;

    /**
     * Get Bitbucket repository forks.
     *
     * @param owner
     *         the repository owner
     * @param repositorySlug
     *         the repository name
     * @return {@link BitbucketRepositories} object that describes received forks
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    List<BitbucketRepository> getRepositoryForks(String owner, String repositorySlug) throws IOException,
                                                                                             BitbucketException,
                                                                                             ServerException;

    /**
     * Fork a Bitbucket repository.
     *
     * @param owner
     *         the repository owner
     * @param repositorySlug
     *         the repository name
     * @param forkName
     *         the fork name
     * @param isForkPrivate
     *         if the fork must be private
     * @return {@link BitbucketRepositoryFork} object that describes created fork
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    BitbucketRepositoryFork forkRepository(String owner,
                                           String repositorySlug,
                                           String forkName,
                                           boolean isForkPrivate) throws IOException, BitbucketException, ServerException;

    /**
     * Add authorization header to given HTTP connection.
     *
     * @param http
     *         HTTP connection
     * @param requestMethod
     *         request method. Is needed when using oAuth1
     * @param requestUrl
     *         request url. Is needed when using oAuth1
     * @throws IOException
     *         if i/o error occurs when try to refresh expired oauth token
     */
    void authorizeRequest(HttpURLConnection http, String requestMethod, String requestUrl) throws IOException;
}
