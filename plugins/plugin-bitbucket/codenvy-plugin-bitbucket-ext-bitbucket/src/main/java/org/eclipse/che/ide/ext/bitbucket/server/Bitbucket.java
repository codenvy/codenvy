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
package org.eclipse.che.ide.ext.bitbucket.server;

import static org.eclipse.che.ide.ext.bitbucket.shared.Preconditions.checkArgument;
import static org.eclipse.che.ide.ext.bitbucket.shared.StringHelper.isNullOrEmpty;

import org.eclipse.che.api.auth.oauth.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

/**
 * Contains methods for retrieving data from BITBUCKET and processing it before sending to client side.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
@Singleton
public class Bitbucket {

    private static final Logger LOG = LoggerFactory.getLogger(Bitbucket.class);

    private String              bitbucketEndpoint;
    private BitbucketConnection bitbucketConnection;

    @Inject
    public Bitbucket(OAuthTokenProvider tokenProvider,
                     OAuthAuthorizationHeaderProvider headerProvider,
                     HttpJsonRequestFactory requestFactory,
                     @Named("che.api") String apiEndpoint) {

        String endpoint = null;
        try {
            endpoint = requestFactory.fromUrl(apiEndpoint + "/bitbucket/endpoint")
                                     .useGetMethod()
                                     .request()
                                     .asString();
        } catch (Exception exception) {
            LOG.error(exception.getMessage());
        }
        endpoint = endpoint != null && endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1)
                                                              : endpoint;
        this.bitbucketEndpoint = endpoint;
        bitbucketConnection = "https://bitbucket.org".equals(endpoint) ? new BitbucketConnectionImpl(tokenProvider)
                                                                       : new BitbucketServerConnectionImpl(endpoint, headerProvider);
    }

    /**
     * Get host url of bitbucket.
     */
    public String getEndpoint() throws ServerException {
        return bitbucketEndpoint;
    }

    /**
     * Get authorized user information.
     *
     * @return the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser}.
     * @throws java.io.IOException
     *         if any i/o errors occurs.
     * @throws BitbucketException
     *         if Bitbucket server return unexpected or error status for request.
     * @throws ServerException
     *         if any error occurs when parse.
     */
    public BitbucketUser getUser(String username) throws IOException, BitbucketException, ServerException {
        return bitbucketConnection.getUser(username);
    }

    /**
     * Get Bitbucket repository information.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @return the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     * @throws java.io.IOException
     *         if any i/o errors occurs.
     * @throws BitbucketException
     *         if Bitbucket server return unexpected or error status for request.
     * @throws ServerException
     *         if any error occurs when parse.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public BitbucketRepository getRepository(@NotNull final String owner, @NotNull final String repositorySlug)
            throws IOException, BitbucketException, ServerException, IllegalArgumentException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");

        return bitbucketConnection.getRepository(owner, repositorySlug);
    }

    /**
     * Get Bitbucket repository forks.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @return the fork {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories}.
     * @throws java.io.IOException
     *         if any i/o errors occurs.
     * @throws BitbucketException
     *         if Bitbucket server return unexpected or error status for request.
     * @throws ServerException
     *         if any error occurs when parse.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public List<BitbucketRepository> getRepositoryForks(@NotNull final String owner, @NotNull final String repositorySlug)
            throws IOException, BitbucketException, ServerException, IllegalArgumentException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");

        return bitbucketConnection.getRepositoryForks(owner, repositorySlug);
    }

    /**
     * Fork a Bitbucket repository.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param forkName
     *         the fork name, cannot be {@code null}.
     * @param isForkPrivate
     *         if the fork must be private.
     * @return the fork {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork}.
     * @throws IOException
     *         if any i/o errors occurs.
     * @throws BitbucketException
     *         if Bitbucket server return unexpected or error status for request.
     * @throws ServerException
     *         if any error occurs when parse.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public BitbucketRepositoryFork forkRepository(@NotNull final String owner,
                                                  @NotNull final String repositorySlug,
                                                  @NotNull final String forkName,
                                                  final boolean isForkPrivate) throws IOException, BitbucketException, ServerException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");
        checkArgument(!isNullOrEmpty(forkName), "forkName");

        return bitbucketConnection.forkRepository(owner, repositorySlug, forkName, isForkPrivate);
    }


    /**
     * Get Bitbucket repository pull requests.
     *
     * @param owner
     *         the repositories owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @return the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequests}.
     * @throws IOException
     *         if any i/o errors occurs.
     * @throws BitbucketException
     *         if Bitbucket server return unexpected or error status for request.
     * @throws ServerException
     *         if any error occurs when parse.
     * @throws IllegalArgumentException
     *         if one parameter is not valid.
     */
    public List<BitbucketPullRequest> getRepositoryPullRequests(@NotNull final String owner, @NotNull final String repositorySlug)
            throws ServerException, IOException, BitbucketException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");

        return bitbucketConnection.getRepositoryPullRequests(owner, repositorySlug);
    }

    /**
     * Open the given {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest}.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param pullRequest
     *         the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest} to open, cannot be {@code null}.
     * @return the opened {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest}.
     */
    public BitbucketPullRequest openPullRequest(@NotNull final String owner,
                                                @NotNull final String repositorySlug,
                                                @NotNull final BitbucketPullRequest pullRequest)
            throws ServerException, IOException, BitbucketException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");
        checkArgument(pullRequest != null, "pullRequest");

        return bitbucketConnection.openPullRequest(owner, repositorySlug, pullRequest);
    }
}
