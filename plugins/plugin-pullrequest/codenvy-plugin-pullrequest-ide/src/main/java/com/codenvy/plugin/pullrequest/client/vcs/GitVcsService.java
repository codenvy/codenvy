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
package com.codenvy.plugin.pullrequest.client.vcs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.git.gwt.client.GitServiceClient;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.api.git.shared.CheckoutRequest;
import org.eclipse.che.api.git.shared.PushResponse;
import org.eclipse.che.api.git.shared.Remote;
import org.eclipse.che.api.git.shared.Revision;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Git backed implementation for {@link VcsService}.
 */
@Singleton
public class GitVcsService implements VcsService {
    private static final String BRANCH_UP_TO_DATE_ERROR_MESSAGE = "Everything up-to-date";

    private final GitServiceClient       service;
    private final DtoFactory             dtoFactory;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final AppContext             appContext;

    @Inject
    public GitVcsService(final DtoFactory dtoFactory,
                         final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                         final GitServiceClient service,
                         final AppContext appContext) {
        this.dtoFactory = dtoFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.service = service;
        this.appContext = appContext;
    }

    @Override
    public void addRemote(@NotNull final ProjectConfigDto project, @NotNull final String remote, @NotNull final String remoteUrl,
                          @NotNull final AsyncCallback<Void> callback) {

        service.remoteAdd(appContext.getWorkspaceId(), project, remote, remoteUrl, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void checkoutBranch(@NotNull final ProjectConfigDto project, @NotNull final String name,
                               final boolean createNew, @NotNull final AsyncCallback<String> callback) {

        service.checkout(appContext.getWorkspaceId(),
                         project,
                         dtoFactory.createDto(CheckoutRequest.class)
                                   .withName(name)
                                   .withCreateNew(createNew),
                         new AsyncRequestCallback<String>() {
                             @Override
                             protected void onSuccess(final String branchName) {
                                 callback.onSuccess(branchName);
                             }

                             @Override
                             protected void onFailure(final Throwable exception) {
                                 callback.onFailure(exception);
                             }
                         });
    }

    @Override
    public void commit(@NotNull final ProjectConfigDto project, final boolean includeUntracked, @NotNull final String commitMessage,
                       @NotNull final AsyncCallback<Void> callback) {
        try {

            service.add(appContext.getWorkspaceId(), project, !includeUntracked, null, new RequestCallback<Void>() {
                @Override
                protected void onSuccess(Void aVoid) {

                    service.commit(appContext.getWorkspaceId(), project, commitMessage, true, false, new AsyncRequestCallback<Revision>() {
                        @Override
                        protected void onSuccess(final Revision revision) {
                            callback.onSuccess(null);
                        }

                        @Override
                        protected void onFailure(final Throwable exception) {
                            callback.onFailure(exception);
                        }
                    });
                }

                @Override
                protected void onFailure(final Throwable exception) {
                    callback.onFailure(exception);
                }
            });

        } catch (final WebSocketException exception) {
            callback.onFailure(exception);
        }
    }

    @Override
    public void deleteRemote(@NotNull final ProjectConfigDto project, @NotNull final String remote,
                             @NotNull final AsyncCallback<Void> callback) {
        service.remoteDelete(appContext.getWorkspaceId(), project, remote, new AsyncRequestCallback<String>() {
            @Override
            protected void onSuccess(final String notUsed) {
                callback.onSuccess(null);
            }

            @Override
            protected void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    @Override
    public void getBranchName(@NotNull final ProjectConfigDto project, @NotNull final AsyncCallback<String> callback) {
        service.status(appContext.getWorkspaceId(), project,
                       new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
                           @Override
                           protected void onSuccess(final Status status) {
                               callback.onSuccess(status.getBranchName());
                           }

                           @Override
                           protected void onFailure(final Throwable exception) {
                               callback.onFailure(exception);
                           }
                       });
    }

    @Override
    public Promise<String> getBranchName(ProjectConfigDto project) {
        return service.status(appContext.getWorkspaceId(), project)
                      .then(new Function<Status, String>() {
                          @Override
                          public String apply(Status status) throws FunctionException {
                              return status.getBranchName();
                          }
                      });
    }

    @Override
    public void hasUncommittedChanges(@NotNull final ProjectConfigDto project, @NotNull final AsyncCallback<Boolean> callback) {
        service.status(appContext.getWorkspaceId(), project,
                       new AsyncRequestCallback<Status>(dtoUnmarshallerFactory.newUnmarshaller(Status.class)) {
                           @Override
                           protected void onSuccess(final Status status) {
                               callback.onSuccess(!status.isClean());
                           }

                           @Override
                           protected void onFailure(final Throwable exception) {
                               callback.onFailure(exception);
                           }
                       });
    }

    @Override
    public void isLocalBranchWithName(@NotNull final ProjectConfigDto project, @NotNull final String branchName,
                                      @NotNull final AsyncCallback<Boolean> callback) {

        listLocalBranches(project, new AsyncCallback<List<Branch>>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final List<Branch> branches) {
                for (final Branch oneBranch : branches) {
                    if (oneBranch.getDisplayName().equals(branchName)) {
                        callback.onSuccess(true);
                        return;
                    }
                }
                callback.onSuccess(false);
            }
        });
    }

    @Override
    public void listLocalBranches(@NotNull final ProjectConfigDto project, @NotNull final AsyncCallback<List<Branch>> callback) {
        listBranches(project, null, callback);
    }

    @Override
    public void listRemotes(@NotNull final ProjectConfigDto project, @NotNull final AsyncCallback<List<Remote>> callback) {
        final Unmarshallable<List<Remote>> unMarshaller
                = dtoUnmarshallerFactory.newListUnmarshaller(org.eclipse.che.api.git.shared.Remote.class);
        service.remoteList(appContext.getWorkspaceId(), project, null, false,
                           new AsyncRequestCallback<List<Remote>>(unMarshaller) {
                               @Override
                               protected void onSuccess(final List<Remote> remotes) {
                                   final List<Remote> result = new ArrayList<>();
                                   for (final Remote remote : remotes) {
                                       result.add(fromGitRemote(remote));
                                   }
                                   callback.onSuccess(result);
                               }

                               @Override
                               protected void onFailure(final Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    @Override
    public Promise<List<Remote>> listRemotes(ProjectConfigDto project) {
        return service.remoteList(appContext.getWorkspaceId(), project, null, false);
    }

    @Override
    public void pushBranch(@NotNull final ProjectConfigDto project,
                           @NotNull final String remote,
                           @NotNull final String localBranchName,
                           @NotNull final AsyncCallback<PushResponse> callback) {
        service.push(appContext.getWorkspaceId(), project, Arrays.asList(localBranchName), remote, true,
                     new AsyncRequestCallback<PushResponse>() {
                         @Override
                         protected void onSuccess(final PushResponse result) {
                             callback.onSuccess(result);
                         }

                         @Override
                         protected void onFailure(final Throwable exception) {
                             if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(exception.getMessage())) {
                                 callback.onFailure(new BranchUpToDateException(localBranchName));

                             } else {
                                 callback.onFailure(exception);
                             }
                         }
                     });
    }

    @Override
    public Promise<PushResponse> pushBranch(final ProjectConfigDto project, final String remote, final String localBranchName) {
        return service.push(appContext.getWorkspaceId(), project, Collections.singletonList(localBranchName), remote, true)
                      .catchErrorPromise(new Function<PromiseError, Promise<PushResponse>>() {
                          @Override
                          public Promise<PushResponse> apply(PromiseError error) throws FunctionException {
                              if (BRANCH_UP_TO_DATE_ERROR_MESSAGE.equalsIgnoreCase(error.getMessage())) {
                                  return Promises.reject(JsPromiseError.create(new BranchUpToDateException(localBranchName)));
                              } else {
                                  return Promises.reject(error);
                              }
                          }
                      });
    }

    /**
     * List branches of a given type.
     *
     * @param project
     *         the project descriptor.
     * @param whichBranches
     *         null -> list local branches; "r" -> list remote branches; "a" -> list all branches.
     * @param callback
     *         callback when the operation is done.
     */
    private void listBranches(final ProjectConfigDto project, final String whichBranches, final AsyncCallback<List<Branch>> callback) {
        final Unmarshallable<List<Branch>> unMarshaller =
                dtoUnmarshallerFactory.newListUnmarshaller(Branch.class);
        service.branchList(appContext.getWorkspaceId(), project, whichBranches,
                           new AsyncRequestCallback<List<Branch>>(unMarshaller) {
                               @Override
                               protected void onSuccess(final List<Branch> branches) {
                                   final List<Branch> result = new ArrayList<>();
                                   for (final Branch branch : branches) {
                                       result.add(fromGitBranch(branch));
                                   }
                                   callback.onSuccess(result);
                               }

                               @Override
                               protected void onFailure(final Throwable exception) {
                                   callback.onFailure(exception);
                               }
                           });
    }

    /**
     * Converts a git branch DTO to an abstracted {@link org.eclipse.che.api.git.shared.Branch} object.
     *
     * @param gitBranch
     *         the object to convert.
     * @return the converted object.
     */
    private Branch fromGitBranch(final Branch gitBranch) {
        final Branch branch = GitVcsService.this.dtoFactory.createDto(Branch.class);
        branch.withActive(gitBranch.isActive()).withRemote(gitBranch.isRemote())
              .withName(gitBranch.getName()).withDisplayName(gitBranch.getDisplayName());
        return branch;
    }

    /**
     * Converts a git remote DTO to an abstracted {@link org.eclipse.che.api.git.shared.Remote} object.
     *
     * @param gitRemote
     *         the object to convert.
     * @return the converted object.
     */
    private Remote fromGitRemote(final Remote gitRemote) {
        final Remote remote = GitVcsService.this.dtoFactory.createDto(Remote.class);
        remote.withName(gitRemote.getName()).withUrl(gitRemote.getUrl());
        return remote;
    }
}
