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
package org.eclipse.che.ide.ext.bitbucket.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.ext.bitbucket.shared.Preconditions.checkArgument;
import static org.eclipse.che.ide.ext.bitbucket.shared.StringHelper.isNullOrEmpty;

/**
 * The Bitbucket service implementation to be use by the client.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketClientService {
    private static final String USER         = "/user";
    private static final String REPOSITORIES = "/repositories";
    private static final String SSH_KEYS     = "/ssh-keys";

    private final LoaderFactory          loaderFactory;
    private final AsyncRequestFactory    asyncRequestFactory;
    private final AppContext             appContext;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;

    @Inject
    protected BitbucketClientService(AppContext appContext,
                                     LoaderFactory loaderFactory,
                                     AsyncRequestFactory asyncRequestFactory,
                                     DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.loaderFactory = loaderFactory;
        this.asyncRequestFactory = asyncRequestFactory;
    }


    private String getBaseUrl() {
        return appContext.getDevMachine().getWsAgentBaseUrl() + "/bitbucket";
    }

    /**
     * Get authorized user information.
     *
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public void getUser(@NotNull AsyncRequestCallback<BitbucketUser> callback) throws IllegalArgumentException {
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + USER;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Returns the promise which resolves authorized user information or rejects with an error.
     */
    public Promise<BitbucketUser> getUser() {
        final String requestUrl = getBaseUrl() + USER;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BitbucketUser.class));
    }

    /**
     * Get Bitbucket repository information.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     * @deprecated use {@link #getRepository(String, String)}
     */
    @Deprecated
    public void getRepository(@NotNull final String owner,
                              @NotNull final String repositorySlug,
                              @NotNull final AsyncRequestCallback<BitbucketRepository> callback) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }


    /**
     * Get Bitbucket repository information.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public Promise<BitbucketRepository> getRepository(@NotNull final String owner,
                                                      @NotNull final String repositorySlug) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug;
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BitbucketRepository.class));
    }


    /**
     * Get Bitbucket repository forks.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     * @deprecated use {@link #getRepositoryForks(String, String)}
     */
    @Deprecated
    public void getRepositoryForks(@NotNull final String owner,
                                   @NotNull final String repositorySlug,
                                   @NotNull final AsyncRequestCallback<List<BitbucketRepository>> callback)
            throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug + "/forks";
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Get Bitbucket repository forks.
     *
     * @param owner
     *         the repository owner.
     * @param repositorySlug
     *         the repository name.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public Promise<List<BitbucketRepository>> getRepositoryForks(final String owner,
                                                                 final String repositorySlug) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");

        final String requestUrl = getBaseUrl() + REPOSITORIES + '/' + owner + '/' + repositorySlug + "/forks";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(BitbucketRepository.class));
    }

    /**
     * Fork a Bitbucket repository.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param forkName
     *         the fork name, cannot be {@code null} or empty.
     * @param isForkPrivate
     *         if the fork must be private.
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     * @deprecated use {@link #forkRepository(String, String, String, boolean)}
     */
    public void forkRepository(@NotNull final String owner,
                               @NotNull final String repositorySlug,
                               @NotNull final String forkName,
                               final boolean isForkPrivate,
                               @NotNull final AsyncRequestCallback<BitbucketRepositoryFork> callback) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");
        checkArgument(forkName != null && !isNullOrEmpty(forkName), "forkName");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug + "/fork" +
                                  "?forkName=" + forkName + "&isForkPrivate=" + isForkPrivate;
        asyncRequestFactory.createPostRequest(requestUrl, null).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Fork a Bitbucket repository.
     *
     * @param owner
     *         the repository owner.
     * @param repositorySlug
     *         the repository name.
     * @param forkName
     *         the fork name, cannot be.
     * @param isForkPrivate
     *         if the fork must be private.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public Promise<BitbucketRepositoryFork> forkRepository(final String owner,
                                                           final String repositorySlug,
                                                           final String forkName,
                                                           final boolean isForkPrivate) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");
        checkArgument(forkName != null && !isNullOrEmpty(forkName), "forkName");

        final String requestUrl = getBaseUrl() + REPOSITORIES + '/' + owner + '/' + repositorySlug + "/fork" +
                                  "?forkName=" + forkName + "&isForkPrivate=" + isForkPrivate;
        return asyncRequestFactory.createPostRequest(requestUrl, null)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BitbucketRepositoryFork.class));
    }

    /**
     * Get Bitbucket repository pull requests.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public void getRepositoryPullRequests(@NotNull final String owner,
                                          @NotNull final String repositorySlug,
                                          @NotNull final AsyncRequestCallback<List<BitbucketPullRequest>> callback)
            throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug + "/pullrequests";
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Get Bitbucket repository pull requests.
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param repositorySlug
     *         the repository name, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public Promise<List<BitbucketPullRequest>> getRepositoryPullRequests(final String owner,
                                                                         final String repositorySlug) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(repositorySlug != null, "repositorySlug");

        final String requestUrl = getBaseUrl() + REPOSITORIES + '/' + owner + '/' + repositorySlug + "/pullrequests";
        return asyncRequestFactory.createGetRequest(requestUrl)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newListUnmarshaller(BitbucketPullRequest.class));
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
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public void openPullRequest(@NotNull final String owner,
                                @NotNull final String repositorySlug,
                                @NotNull final BitbucketPullRequest pullRequest,
                                @NotNull final AsyncRequestCallback<BitbucketPullRequest> callback) throws IllegalArgumentException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");
        checkArgument(pullRequest != null, "pullRequest");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug + "/pullrequests";
        asyncRequestFactory.createPostRequest(requestUrl, pullRequest).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Open the pullRequest on the Bitbucket.
     *
     * @param owner
     *         the repository owner.
     * @param repositorySlug
     *         the repository name.
     * @param pullRequest
     *         the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest} to open.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public Promise<BitbucketPullRequest> openPullRequest(@NotNull final String owner,
                                                         @NotNull final String repositorySlug,
                                                         @NotNull final BitbucketPullRequest pullRequest) throws IllegalArgumentException {
        checkArgument(!isNullOrEmpty(owner), "owner");
        checkArgument(!isNullOrEmpty(repositorySlug), "repositorySlug");
        checkArgument(pullRequest != null, "pullRequest");
        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner + "/" + repositorySlug + "/pullrequests";
        return asyncRequestFactory.createPostRequest(requestUrl, pullRequest)
                                  .loader(loaderFactory.newLoader())
                                  .send(dtoUnmarshallerFactory.newUnmarshaller(BitbucketPullRequest.class));
    }

    /**
     * Get owner Bitbucket repositories
     *
     * @param owner
     *         the repository owner, cannot be {@code null}.
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public void getRepositories(@NotNull final String owner,
                                @NotNull final AsyncRequestCallback<BitbucketRepositories> callback) throws IllegalArgumentException {
        checkArgument(owner != null, "owner");
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + REPOSITORIES + "/" + owner;
        asyncRequestFactory.createGetRequest(requestUrl).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Generate and upload new public key if not exist on bitbucket.org.
     *
     * @param callback
     *         callback called when operation is done, cannot be {@code null}.
     * @throws java.lang.IllegalArgumentException
     *         if one parameter is not valid.
     */
    public void generateAndUploadSSHKey(@NotNull AsyncRequestCallback<Void> callback) throws IllegalArgumentException {
        checkArgument(callback != null, "callback");

        final String requestUrl = getBaseUrl() + SSH_KEYS;
        asyncRequestFactory.createPostRequest(requestUrl, null).loader(loaderFactory.newLoader()).send(callback);
    }

    /**
     * Updates pull request information e.g. title, description
     *
     * @param owner
     *         name of repository owner
     * @param repository
     *         name of repository
     * @param pullRequestId
     *         updated pull request id
     * @param pullRequest
     *         pull request for update
     * @return updated pull request
     */
    public Promise<BitbucketPullRequest> updatePullRequest(final String owner,
                                                           final String repository,
                                                           final String pullRequestId,
                                                           final BitbucketPullRequest pullRequest) {
        return Promises.reject(JsPromiseError.create("Not implemented yet"));
    }
}
