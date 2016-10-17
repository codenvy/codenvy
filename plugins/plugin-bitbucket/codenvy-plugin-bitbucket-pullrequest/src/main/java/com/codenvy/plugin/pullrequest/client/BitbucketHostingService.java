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
package com.codenvy.plugin.pullrequest.client;

import com.codenvy.plugin.pullrequest.client.vcs.hosting.HostingServiceTemplates;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoCommitsInPullRequestException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoPullRequestException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.NoUserForkException;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.ServiceUtil;
import com.codenvy.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import com.codenvy.plugin.pullrequest.shared.dto.HostUser;
import com.codenvy.plugin.pullrequest.shared.dto.PullRequest;
import com.codenvy.plugin.pullrequest.shared.dto.Repository;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.bitbucket.client.BitbucketClientService;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketLink;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.RestContext;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.gwt.regexp.shared.RegExp.compile;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestBranch;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLinks;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLocation;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestRepository;
import static org.eclipse.che.ide.rest.HTTPStatus.BAD_REQUEST;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;
import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;

/**
 * {@link VcsHostingService} implementation for Bitbucket.
 *
 * @author Kevin Pollet
 */
public class BitbucketHostingService implements VcsHostingService {

    public static final String SERVICE_NAME = "Bitbucket";

    private static final int    MAX_FORK_CREATION_ATTEMPT             = 10;
    private static final String REPOSITORY_EXISTS_ERROR_MESSAGE       = "You already have a repository with this name.";
    private static final String NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE = "There are no changes to be pulled";

    /*
     * URL related constants.
     */

    private static final String SSH_URL_PREFIX           = "git@bitbucket\\.org:";
    private static final String HTTPS_URL_PREFIX         = "https:\\/\\/([^@]+@)?bitbucket\\.org\\/";
    private static final String OWNER_REPO_REGEX         = "([^\\/]+)\\/([^\\/]+)";
    private static final String REPOSITORY_GIT_EXTENSION = ".git";
    private static final RegExp SSH_URL_REGEXP           = compile(SSH_URL_PREFIX + OWNER_REPO_REGEX);
    private static final RegExp HTTPS_URL_REGEXP         = compile(HTTPS_URL_PREFIX + OWNER_REPO_REGEX);


    private final AppContext              appContext;
    private final DtoUnmarshallerFactory  dtoUnmarshallerFactory;
    private final DtoFactory              dtoFactory;
    private final BitbucketClientService  bitbucketClientService;
    private final HostingServiceTemplates templates;
    private final String                  baseUrl;

    @Inject
    public BitbucketHostingService(@NotNull final AppContext appContext,
                                   @NotNull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   @NotNull final DtoFactory dtoFactory,
                                   @NotNull final BitbucketClientService bitbucketClientService,
                                   @NotNull final BitBucketTemplates templates,
                                   @NotNull @RestContext final String baseUrl) {
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;
        this.bitbucketClientService = bitbucketClientService;
        this.templates = templates;
        this.baseUrl = baseUrl;
    }

    @Override
    public VcsHostingService init(String remoteUrl) {
        return this;
    }

    @NotNull
    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @NotNull
    @Override
    public String getHost() {
        return "bitbucket.org";
    }

    @Override
    public boolean isHostRemoteUrl(@NotNull final String remoteUrl) {
        return SSH_URL_REGEXP.test(remoteUrl) || HTTPS_URL_REGEXP.test(remoteUrl);
    }

    @Override
    public Promise<PullRequest> getPullRequest(final String owner,
                                               final String repository,
                                               final String username,
                                               final String branchName) {
        return bitbucketClientService.getRepositoryPullRequests(owner, repository)
                                     .thenPromise(new Function<List<BitbucketPullRequest>, Promise<PullRequest>>() {
                                         @Override
                                         public Promise<PullRequest> apply(List<BitbucketPullRequest> pullRequests)
                                                 throws FunctionException {
                                             for (final BitbucketPullRequest pullRequest : pullRequests) {
                                                 final BitbucketUser author = pullRequest.getAuthor();
                                                 final BitbucketPullRequestLocation source = pullRequest.getSource();
                                                 if (author != null && source != null) {
                                                     final BitbucketPullRequestBranch branch = source.getBranch();
                                                     if (username.equals(author.getUsername()) && branchName.equals(branch.getName())) {
                                                         return Promises.resolve(valueOf(pullRequest));
                                                     }
                                                 }
                                             }
                                             return Promises.reject(JsPromiseError.create(new NoPullRequestException(branchName)));
                                         }
                                     });
    }

    @Override
    public Promise<PullRequest> createPullRequest(final String owner,
                                                  final String repository,
                                                  final String username,
                                                  final String headBranchName,
                                                  final String baseBranchName,
                                                  final String title,
                                                  final String body) {
        final BitbucketPullRequestLocation destination = dtoFactory.createDto(BitbucketPullRequestLocation.class)
                                                                   .withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class)
                                                                                         .withName(baseBranchName))
                                                                   .withRepository(dtoFactory.createDto(BitbucketPullRequestRepository.class)
                                                                                             .withFullName(owner + '/' + repository));

        final BitbucketPullRequestLocation sources = dtoFactory.createDto(BitbucketPullRequestLocation.class)
                                                               .withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class)
                                                                                     .withName(headBranchName))
                                                               .withRepository(dtoFactory.createDto(BitbucketPullRequestRepository.class)
                                                                                         .withFullName(username + '/' + repository));
        final BitbucketPullRequest pullRequest = dtoFactory.createDto(BitbucketPullRequest.class)
                                                           .withTitle(title)
                                                           .withDescription(body)
                                                           .withDestination(destination)
                                                           .withSource(sources);
        return bitbucketClientService.openPullRequest(owner, repository, pullRequest)
                                     .then(new Function<BitbucketPullRequest, PullRequest>() {
                                         @Override
                                         public PullRequest apply(BitbucketPullRequest arg) throws FunctionException {
                                             return valueOf(arg);
                                         }
                                     })
                                     .catchError(new Operation<PromiseError>() {
                                         @Override
                                         public void apply(PromiseError e) throws OperationException {
                                             if (getErrorCode(e.getCause()) == BAD_REQUEST
                                                 && e.getMessage() != null
                                                 && containsIgnoreCase(e.getMessage(), NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE)) {
                                                 Promises.reject(JsPromiseError.create(new NoCommitsInPullRequestException(headBranchName,
                                                                                                                           baseBranchName)));
                                             } else {
                                                 Promises.reject(e);
                                             }
                                         }
                                     });
    }

    @Override
    public Promise<Repository> fork(final String owner, final String repository) {
        return getRepository(owner, repository).thenPromise(new Function<Repository, Promise<Repository>>() {
            @Override
            public Promise<Repository> apply(final Repository repository) throws FunctionException {
                return fork(owner, repository.getName(), 0, repository.isPrivateRepo()).thenPromise(
                        new Function<BitbucketRepositoryFork, Promise<Repository>>() {
                            @Override
                            public Promise<Repository> apply(BitbucketRepositoryFork bitbucketRepositoryFork) throws FunctionException {
                                return Promises.resolve(dtoFactory.createDto(Repository.class)
                                                                  .withName(bitbucketRepositoryFork.getName())
                                                                  .withFork(true)
                                                                  .withParent(repository)
                                                                  .withPrivateRepo(bitbucketRepositoryFork.isIsPrivate()));
                            }
                        });
            }
        });
    }

    private Promise<BitbucketRepositoryFork> fork(final String owner,
                                                  final String repository,
                                                  final int number,
                                                  final boolean isForkPrivate) {
        final String forkName = number == 0 ? repository : (repository + "-" + number);
        return bitbucketClientService.forkRepository(owner, repository, forkName, isForkPrivate)
                                     .catchErrorPromise(new Function<PromiseError, Promise<BitbucketRepositoryFork>>() {
                                         @Override
                                         public Promise<BitbucketRepositoryFork> apply(PromiseError exception) throws FunctionException {
                                             if (number < MAX_FORK_CREATION_ATTEMPT && exception instanceof ServerException) {
                                                 final ServerException serverException = (ServerException)exception;
                                                 final String exceptionMessage = serverException.getMessage();

                                                 if (serverException.getHTTPStatus() == BAD_REQUEST
                                                     && exceptionMessage != null
                                                     && containsIgnoreCase(exceptionMessage, REPOSITORY_EXISTS_ERROR_MESSAGE)) {

                                                     return fork(owner, repository, number + 1, isForkPrivate);
                                                 }

                                             }
                                             return Promises.reject(exception);
                                         }
                                     });
    }

    @Override
    public Promise<Repository> getRepository(String owner, String repositoryName) {
        return bitbucketClientService.getRepository(owner, repositoryName)
                                     .then(new Function<BitbucketRepository, Repository>() {
                                         @Override
                                         public Repository apply(BitbucketRepository bbRepo) throws FunctionException {
                                             return valueOf(bbRepo);
                                         }
                                     });
    }

    @NotNull
    @Override
    public String getRepositoryNameFromUrl(@NotNull final String url) {
        String result;
        if (SSH_URL_REGEXP.test(url)) {
            result = SSH_URL_REGEXP.exec(url).getGroup(2);
        } else {
            result = HTTPS_URL_REGEXP.exec(url).getGroup(3);
        }
        if (result != null && result.endsWith(REPOSITORY_GIT_EXTENSION)) {
            return result.substring(0, result.length() - REPOSITORY_GIT_EXTENSION.length());
        } else {
            return result;
        }
    }

    @NotNull
    @Override
    public String getRepositoryOwnerFromUrl(@NotNull final String url) {
        if (SSH_URL_REGEXP.test(url)) {
            return SSH_URL_REGEXP.exec(url).getGroup(1);
        }
        return HTTPS_URL_REGEXP.exec(url).getGroup(2);
    }

    @Override
    public Promise<Repository> getUserFork(final String user,
                                           final String owner,
                                           final String repository) {
        return bitbucketClientService.getRepositoryForks(owner, repository)
                                     .thenPromise(new Function<List<BitbucketRepository>, Promise<Repository>>() {
                                         @Override
                                         public Promise<Repository> apply(List<BitbucketRepository> repositories) throws FunctionException {
                                             for (final BitbucketRepository repository : repositories) {
                                                 final BitbucketUser owner = repository.getOwner();

                                                 if (owner != null && user.equals(owner.getUsername())) {
                                                     return Promises.resolve(valueOf(repository));
                                                 }
                                             }
                                             return Promises.reject(JsPromiseError.create(new NoUserForkException(user)));
                                         }
                                     });
    }

    @Override
    public Promise<HostUser> getUserInfo() {
        return bitbucketClientService.getUser()
                                     .then(new Function<BitbucketUser, HostUser>() {
                                         @Override
                                         public HostUser apply(BitbucketUser user) throws FunctionException {
                                             return dtoFactory.createDto(HostUser.class)
                                                              .withId(user.getUuid())
                                                              .withName(user.getDisplayName())
                                                              .withLogin(user.getUsername())
                                                              .withUrl(user.getLinks().getSelf().getHref());
                                         }
                                     });
    }

    @NotNull
    @Override
    public String makeSSHRemoteUrl(@NotNull final String username, @NotNull final String repository) {
        return templates.sshUrlTemplate(username, repository);
    }

    @NotNull
    @Override
    public String makeHttpRemoteUrl(@NotNull final String username, @NotNull final String repository) {
        return templates.httpUrlTemplate(username, repository);
    }

    @NotNull
    @Override
    public String makePullRequestUrl(@NotNull final String username,
                                     @NotNull final String repository,
                                     @NotNull final String pullRequestNumber) {
        return templates.pullRequestUrlTemplate(username, repository, pullRequestNumber);
    }

    @NotNull
    @Override
    public String formatReviewFactoryUrl(@NotNull final String reviewFactoryUrl) {
        final String protocol = Window.Location.getProtocol();
        final String host = Window.Location.getHost();

        return templates.formattedReviewFactoryUrlTemplate(protocol, host, reviewFactoryUrl);
    }

    @Override
    public Promise<HostUser> authenticate(final CurrentUser user) {
        final Workspace workspace = this.appContext.getWorkspace();
        if (workspace == null) {
            return Promises.reject(JsPromiseError.create("Error accessing current workspace"));
        }
        final String authUrl = baseUrl
                               + "/oauth/authenticate?oauth_provider=bitbucket&userId=" + user.getProfile().getUserId()
                               + "&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + workspace.getConfig().getName();
        return ServiceUtil.performWindowAuth(this, authUrl);
    }

    @Override
    public Promise<PullRequest> updatePullRequest(String owner, String repository, PullRequest pullRequest) {
        return Promises.reject(JsPromiseError.create("Update pullRequest not implemented for " + getName()));
    }

    /**
     * Converts an instance of {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository} into a {@link
     * Repository}.
     *
     * @param bitbucketRepository
     *         the Bitbucket repository to convert.
     * @return the corresponding {@link Repository} instance or {@code null} if given
     * bitbucketRepository is {@code null}.
     */
    private Repository valueOf(final BitbucketRepository bitbucketRepository) {
        if (bitbucketRepository == null) {
            return null;
        }

        final BitbucketRepository bitbucketRepositoryParent = bitbucketRepository.getParent();
        final Repository parent = bitbucketRepositoryParent == null ? null :
                                  dtoFactory.createDto(Repository.class)
                                            .withFork(bitbucketRepositoryParent.getParent() != null)
                                            .withName(bitbucketRepositoryParent.getName())
                                            .withParent(null)
                                            .withPrivateRepo(bitbucketRepositoryParent.isIsPrivate())
                                            .withCloneUrl(getParentCloneHttpsUrl(bitbucketRepositoryParent));

        return dtoFactory.createDto(Repository.class)
                         .withFork(bitbucketRepositoryParent != null)
                         .withName(bitbucketRepository.getName())
                         .withParent(parent)
                         .withPrivateRepo(bitbucketRepository.isIsPrivate())
                         .withCloneUrl(getCloneHttpsUrl(bitbucketRepository));

    }

    private String getParentCloneHttpsUrl(BitbucketRepository bitbucketRepositoryParent) {
        String parentOwner = bitbucketRepositoryParent.getFullName().split("/")[0];
        String parentName = bitbucketRepositoryParent.getName();
        return makeHttpRemoteUrl(parentOwner, parentName);
    }

    /**
     * Converts an instance of {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest} into a {@link
     * PullRequest}.
     *
     * @param bitbucketPullRequest
     *         the bitbucket pull request to convert.
     * @return the corresponding {@link PullRequest} instance or {@code null} if
     * given bitbucketPullRequest is {@code null}.
     */
    private PullRequest valueOf(final BitbucketPullRequest bitbucketPullRequest) {
        if (bitbucketPullRequest == null) {
            return null;
        }

        final String pullRequestId = String.valueOf(bitbucketPullRequest.getId());
        final BitbucketPullRequestLocation pullRequestSource = bitbucketPullRequest.getSource();
        final BitbucketPullRequestBranch pullRequestBranch = pullRequestSource != null ? pullRequestSource.getBranch() : null;
        final BitbucketPullRequestLinks pullRequestLinks = bitbucketPullRequest.getLinks();
        final BitbucketLink pullRequestHtmlLink = pullRequestLinks != null ? pullRequestLinks.getHtml() : null;
        final BitbucketLink pullRequestSelfLink = pullRequestLinks != null ? pullRequestLinks.getSelf() : null;

        return dtoFactory.createDto(PullRequest.class)
                         .withId(pullRequestId)
                         .withUrl(pullRequestSelfLink != null ? pullRequestSelfLink.getHref() : null)
                         .withHtmlUrl(pullRequestHtmlLink != null ? pullRequestHtmlLink.getHref() : null)
                         .withNumber(pullRequestId)
                         .withState(bitbucketPullRequest.getState().name())
                         .withHeadRef(pullRequestBranch.getName());
    }

    /**
     * Return the HTTPS clone url for the given {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     *
     * @param bitbucketRepository
     *         the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     * @return the HTTPS clone url from the clone links or {@code null} if none.
     */
    private String getCloneHttpsUrl(@NotNull final BitbucketRepository bitbucketRepository) {
        if (bitbucketRepository.getLinks() != null && bitbucketRepository.getLinks().getClone() != null) {
            for (final BitbucketLink oneCloneLink : bitbucketRepository.getLinks().getClone()) {
                if (oneCloneLink.getName() != null && "https".equals(oneCloneLink.getName())) {
                    return oneCloneLink.getHref();
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "BitbucketHostingService";
    }
}
