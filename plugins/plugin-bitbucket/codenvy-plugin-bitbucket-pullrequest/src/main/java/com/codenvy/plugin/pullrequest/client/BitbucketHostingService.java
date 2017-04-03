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
package com.codenvy.plugin.pullrequest.client;

import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.HostingServiceTemplates;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoCommitsInPullRequestException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoPullRequestException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.NoUserForkException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.PullRequestAlreadyExistsException;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.ServiceUtil;
import org.eclipse.che.plugin.pullrequest.client.vcs.hosting.VcsHostingService;
import org.eclipse.che.plugin.pullrequest.shared.dto.HostUser;
import org.eclipse.che.plugin.pullrequest.shared.dto.PullRequest;
import org.eclipse.che.plugin.pullrequest.shared.dto.Repository;
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
import org.eclipse.che.ide.rest.RestContext;

import javax.inject.Inject;
import java.util.List;

import static org.eclipse.che.api.promises.client.js.Promises.reject;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestBranch;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLinks;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestLocation;
import static org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest.BitbucketPullRequestRepository;
import static org.eclipse.che.ide.rest.HTTPStatus.BAD_REQUEST;
import static org.eclipse.che.ide.util.ExceptionUtils.getErrorCode;
import static org.eclipse.che.ide.util.StringUtils.containsIgnoreCase;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

/**
 * {@link VcsHostingService} implementation for Bitbucket.
 *
 * @author Kevin Pollet
 */
public class BitbucketHostingService implements VcsHostingService {

    public static final String SERVICE_NAME     = "Bitbucket";
    public static final String DEFAULT_ENDPOINT = "https://bitbucket.org";

    private static final int    MAX_FORK_CREATION_ATTEMPT                 = 10;
    private static final String REPOSITORY_EXISTS_ERROR_MESSAGE           = "You already have a repository with this name.";
    private static final String NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE     = "There are no changes to be pulled";
    private static final String PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE = "Only one pull request may be open for a given source " +
                                                                            "and target branch";
    private static final String REPOSITORY_GIT_EXTENSION                  = ".git";


    private final AppContext             appContext;
    private final DtoFactory             dtoFactory;
    private final BitbucketClientService bitbucketClientService;
    private final String                 baseUrl;

    private HostingServiceTemplates templates;
    private String                  remoteUrl;
    private String                  bitbucketEndpoint;

    @Inject
    public BitbucketHostingService(final AppContext appContext,
                                   final DtoFactory dtoFactory,
                                   final BitbucketClientService bitbucketClientService,
                                   final BitBucketTemplates templates,
                                   final BitBucketServerTemplates bitbucketServerTemplates,
                                   @RestContext final String baseUrl) {
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.bitbucketClientService = bitbucketClientService;
        this.templates = templates;
        this.baseUrl = baseUrl;

        bitbucketClientService.getBitbucketEndpoint().then(new Operation<String>() {
            @Override
            public void apply(String endpoint) throws OperationException {
                BitbucketHostingService.this.bitbucketEndpoint = endpoint;
                if (!isHosted()) {
                    BitbucketHostingService.this.templates = bitbucketServerTemplates;
                }
            }
        });
    }

    @Override
    public VcsHostingService init(String remoteUrl) {
        this.remoteUrl = remoteUrl;
        return this;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public String getHost() {
        return bitbucketEndpoint;
    }

    @Override
    public boolean isHostRemoteUrl(final String remoteUrl) {
        String hostName = bitbucketEndpoint.split("/")[2];
        if (hostName.contains(":")) {
            hostName = hostName.substring(0, hostName.indexOf(":"));
        }
        return remoteUrl.contains(hostName);
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
                                                     //Bitbucket Server adds '~' to authenticated user, need to substring it.
                                                     String name = username.startsWith("~") ? username.substring(1) : username;
                                                     if (name.equals(author.getUsername()) && branchName.equals(branch.getName())) {
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
        final BitbucketPullRequestLocation destination =
                dtoFactory.createDto(BitbucketPullRequestLocation.class).withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class)
                                                                                              .withName(baseBranchName))
                          .withRepository(dtoFactory.createDto(BitbucketPullRequestRepository.class)
                                                    .withFullName(owner + '/' + repository));

        final BitbucketPullRequestLocation sources =
                dtoFactory.createDto(BitbucketPullRequestLocation.class)
                          .withBranch(dtoFactory.createDto(BitbucketPullRequestBranch.class).withName(headBranchName))
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
                                     .catchErrorPromise(error -> {
                                         final String message = error.getMessage();
                                         if (isNullOrEmpty(message)) {
                                             return reject(error);
                                         }
                                         if (getErrorCode(error.getCause()) == BAD_REQUEST
                                             && containsIgnoreCase(message, NO_CHANGES_TO_BE_PULLED_ERROR_MESSAGE)) {
                                             return reject(JsPromiseError.create(new NoCommitsInPullRequestException(headBranchName,
                                                                                                                     baseBranchName)));
                                         } else if (containsIgnoreCase(message, PULL_REQUEST_ALREADY_EXISTS_ERROR_MESSAGE)) {
                                             return reject(JsPromiseError.create(new PullRequestAlreadyExistsException(headBranchName)));
                                         }
                                         return reject(error);
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
                                             return reject(exception);
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

    @Override
    public String getRepositoryNameFromUrl(final String url) {
        String[] split = url.split("/");
        String repositoryName = split[split.length - 1];
        if (repositoryName.endsWith(REPOSITORY_GIT_EXTENSION)) {
            return repositoryName.substring(0, repositoryName.length() - REPOSITORY_GIT_EXTENSION.length());
        } else {
            return repositoryName;
        }
    }

    @Override
    public String getRepositoryOwnerFromUrl(final String url) {
        String[] split = url.split("/");
        String result = split[split.length - 2];
        if (result.contains(":")) {
            result = result.substring(result.indexOf(":") + 1);
        }
        return result;
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
                                             return reject(JsPromiseError.create(new NoUserForkException(user)));
                                         }
                                     });
    }

    @Override
    public Promise<HostUser> getUserInfo() {
        return bitbucketClientService.getUser()
                                     .then((Function<BitbucketUser, HostUser>)user -> dtoFactory.createDto(HostUser.class)
                                                                                                .withId(user.getUuid())
                                                                                                .withName(user.getDisplayName())
                                                                                                .withLogin((isHosted() ? "" : "~") +
                                                                                                           user.getUsername())
                                                                                                .withUrl(user.getLinks()
                                                                                                             .getSelf()
                                                                                                             .getHref()));
    }

    @Override
    public String makeSSHRemoteUrl(final String username, final String repository) {
        if (isHosted()) {
            return templates.sshUrlTemplate(username, repository);
        } else {
            return templates.sshUrlTemplate("ssh://git@" + remoteUrl.split("/")[2] + "/" + username, repository);
        }
    }

    @Override
    public String makeHttpRemoteUrl(final String username, final String repository) {
        if (isHosted()) {
            return templates.httpUrlTemplate(username, repository);
        } else {
            return templates.httpUrlTemplate(bitbucketEndpoint + "/scm/" + username, repository);
        }
    }

    @Override
    public String makePullRequestUrl(final String username,
                                     final String repository,
                                     final String pullRequestNumber) {
        if (isHosted()) {
            return templates.pullRequestUrlTemplate(username, repository, pullRequestNumber);
        } else {
            return templates.pullRequestUrlTemplate(bitbucketEndpoint + "/projects/" + username, repository, pullRequestNumber);
        }
    }

    @Override
    public String formatReviewFactoryUrl(final String reviewFactoryUrl) {
        final String protocol = Window.Location.getProtocol();
        final String host = Window.Location.getHost();

        return templates.formattedReviewFactoryUrlTemplate(protocol, host, reviewFactoryUrl);
    }

    @Override
    public Promise<HostUser> authenticate(final CurrentUser user) {
        final Workspace workspace = this.appContext.getWorkspace();
        if (workspace == null) {
            return reject(JsPromiseError.create("Error accessing current workspace"));
        }
        String oauthPath = "https://bitbucket.org".equals(bitbucketEndpoint) ? "/oauth/authenticate?oauth_provider=bitbucket&userId=" :
                           "/oauth/1.0/authenticate?oauth_provider=bitbucket-server&request_method=post&signature_method=rsa&userId=";

        final String authUrl = baseUrl
                               + oauthPath
                               + user.getProfile().getUserId()
                               + "&redirect_after_login="
                               + Window.Location.getProtocol() + "//"
                               + Window.Location.getHost() + "/ws/"
                               + workspace.getConfig().getName();
        return ServiceUtil.performWindowAuth(this, authUrl);
    }

    @Override
    public Promise<PullRequest> updatePullRequest(String owner, String repository, PullRequest pullRequest) {
        return bitbucketClientService.updatePullRequest(owner, repository, valueOf(pullRequest))
                                     .then((Function<BitbucketPullRequest, PullRequest>)this::valueOf);
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
                         .withTitle(bitbucketPullRequest.getTitle())
                         .withVersion(bitbucketPullRequest.getVersion())
                         .withDescription(bitbucketPullRequest.getDescription())
                         .withUrl(pullRequestSelfLink != null ? pullRequestSelfLink.getHref() : null)
                         .withHtmlUrl(pullRequestHtmlLink != null ? pullRequestHtmlLink.getHref() : null)
                         .withNumber(pullRequestId)
                         .withState(bitbucketPullRequest.getState().name())
                         .withHeadRef(pullRequestBranch.getName());
    }

    /**
     * Convert an instance of {@link PullRequest} into a {@link BitbucketPullRequest}.
     */
    private BitbucketPullRequest valueOf(final PullRequest pullRequest) {
        return dtoFactory.createDto(BitbucketPullRequest.class)
                         .withId(Integer.valueOf(pullRequest.getId()))
                         .withTitle(pullRequest.getTitle())
                         .withVersion(pullRequest.getVersion())
                         .withDescription(pullRequest.getDescription());
    }

    /**
     * Return the HTTPS clone url for the given {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     *
     * @param bitbucketRepository
     *         the {@link org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository}.
     * @return the HTTPS clone url from the clone links or {@code null} if none.
     */
    private String getCloneHttpsUrl(final BitbucketRepository bitbucketRepository) {
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

    /**
     * Returns {@code true} if the repository is cloned from bitbucket.org,
     * otherwise returns {@code false} if the repository is cloned from Bitbucket Server instance.
     */
    private boolean isHosted() {
        return DEFAULT_ENDPOINT.equals(bitbucketEndpoint);
    }
}
