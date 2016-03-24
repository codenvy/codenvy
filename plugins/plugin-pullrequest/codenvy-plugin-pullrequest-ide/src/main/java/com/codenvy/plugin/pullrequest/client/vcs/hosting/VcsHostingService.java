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
package com.codenvy.plugin.pullrequest.client.vcs.hosting;

import com.codenvy.plugin.pullrequest.client.dto.HostUser;
import com.codenvy.plugin.pullrequest.client.dto.PullRequest;
import com.codenvy.plugin.pullrequest.client.dto.Repository;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.CurrentUser;

import javax.validation.constraints.NotNull;

/**
 * Represents a repository host
 *
 * @author Kevin Pollet
 */
public interface VcsHostingService {

    /**
     * Initializes new implementation if additional data from remote url is required
     * @param remoteUrl
     * @return
     */
    public VcsHostingService init(String remoteUrl);

    /**
     * Returns the VCS Host name.
     *
     * @return the VCS Host name never {@code null}.
     */
    @NotNull
    String getName();

    /**
     * Returns the VCS Host.
     *
     * @return the VCS Host never {@code null}.
     */
    @NotNull
    String getHost();

    /**
     * Checks if the given remote URL is hosted by this service.
     *
     * @param remoteUrl
     *         the remote url to check.
     * @return {@code true} if the given remote url is hosted by this service, {@code false} otherwise.
     */
    boolean isHostRemoteUrl(@NotNull String remoteUrl);

    /**
     * Get a pull request by qualified name.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param branchName
     *         pull request branch name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getPullRequest(String, String, String, String)}
     */
    @Deprecated
    void getPullRequest(@NotNull String owner,
                        @NotNull String repository,
                        @NotNull String username,
                        @NotNull String branchName,
                        @NotNull AsyncCallback<PullRequest> callback);

    /**
     * Get a pull request by qualified name.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param branchName
     *         pull request branch name.
     */
    Promise<PullRequest> getPullRequest(@NotNull String owner,
                                        @NotNull String repository,
                                        @NotNull String username,
                                        @NotNull String branchName);

    /**
     * Creates a pull request.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param headBranchName
     *         the head branch name.
     * @param baseBranchName
     *         the base branch name.
     * @param title
     *         the pull request title.
     * @param body
     *         the pull request body.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #createPullRequest(String, String, String, String, String, String, String)}
     */
    @Deprecated
    void createPullRequest(@NotNull String owner,
                           @NotNull String repository,
                           @NotNull String username,
                           @NotNull String headBranchName,
                           @NotNull String baseBranchName,
                           @NotNull String title,
                           @NotNull String body,
                           @NotNull AsyncCallback<PullRequest> callback);

    /**
     * Creates a pull request.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param username
     *         the user name.
     * @param headBranchName
     *         the head branch name.
     * @param baseBranchName
     *         the base branch name.
     * @param title
     *         the pull request title.
     * @param body
     *         the pull request body.
     */
    Promise<PullRequest> createPullRequest(String owner,
                                           String repository,
                                           String username,
                                           String headBranchName,
                                           String baseBranchName,
                                           String title,
                                           String body);

    /**
     * Forks the given repository for the current user.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #fork(String, String)}
     */
    @Deprecated
    void fork(@NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Forks the given repository for the current user.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     */
    Promise<Repository> fork(String owner, String repository);

    /**
     * Returns the information of the given repository.
     *
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @deprecated use {@link #getRepository(String, String)}
     */
    @Deprecated
    void getRepository(@NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Returns the promise which either resolves repository or rejects with an error.
     *
     * @param owner
     *         the owner of the repositoryName
     * @param repositoryName
     *         the name of the repository
     */
    Promise<Repository> getRepository(String owner, String repositoryName);

    /**
     * Returns the repository name from the given url.
     *
     * @param url
     *         the url.
     * @return the repository name, never {@code null}.
     */
    @NotNull
    String getRepositoryNameFromUrl(@NotNull String url);

    /**
     * Returns the repository owner from the given url.
     *
     * @param url
     *         the url.
     * @return the repository owner, never {@code null}.
     */
    @NotNull
    String getRepositoryOwnerFromUrl(@NotNull String url);

    /**
     * Returns the repository fork of the given user.
     *
     * @param user
     *         the  user.
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getUserFork(String, String, String)}
     */
    @Deprecated
    void getUserFork(@NotNull String user, @NotNull String owner, @NotNull String repository, @NotNull AsyncCallback<Repository> callback);

    /**
     * Returns the repository fork of the given user.
     *
     * @param user
     *         the  user.
     * @param owner
     *         the repository owner.
     * @param repository
     *         the repository name.
     */
    Promise<Repository> getUserFork(String user, String owner, String repository);

    /**
     * Returns the user information on the repository host.
     *
     * @param callback
     *         callback called when operation is done.
     * @deprecated use {@link #getUserInfo()}
     */
    @Deprecated
    void getUserInfo(@NotNull AsyncCallback<HostUser> callback);

    /**
     * Returns the user information on the repository host.
     */
    Promise<HostUser> getUserInfo();

    /**
     * Makes the remote SSH url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url.
     */
    String makeSSHRemoteUrl(@NotNull String username, @NotNull String repository);

    /**
     * Makes the remote HTTP url for the given username and repository.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @return the remote url.
     */
    String makeHttpRemoteUrl(@NotNull String username, @NotNull String repository);

    /**
     * Makes the pull request url for the given username, repository and pull request number.
     *
     * @param username
     *         the user name.
     * @param repository
     *         the repository name.
     * @param pullRequestNumber
     *         the pull request number.
     * @return the remote url.
     */
    String makePullRequestUrl(@NotNull String username, @NotNull String repository, @NotNull String pullRequestNumber);

    /**
     * Use the VCS hosting comment markup language to format the review factory URL.
     *
     * @param reviewFactoryUrl
     *         the review factory URL to format.
     * @return the formatted review factory URL.
     */
    @NotNull
    String formatReviewFactoryUrl(@NotNull String reviewFactoryUrl);

    /**
     * Authenticate on the hosting service.
     *
     * @param user
     *         the user to authenticate
     * @param callback
     *         what to do once authentication is done
     * @deprecated use {@link #authenticate(CurrentUser)}
     */
    @Deprecated
    void authenticate(@NotNull CurrentUser user, @NotNull AsyncCallback<HostUser> callback);

    /**
     * Authenticates the current user on the hosting service.
     *
     * @param user
     *         the user to authenticate
     * @return the promise which resolves host user or rejects with an error
     */
    Promise<HostUser> authenticate(CurrentUser user);

    /**
     * Update pull request information e.g. title, description
     *
     * @param owner
     *         repository owner
     * @param repository
     *         name of repository
     * @param pullRequest
     *         pull request for update
     * @return updated pull request
     */
    Promise<PullRequest> updatePullRequest(String owner, String repository, PullRequest pullRequest);
}
