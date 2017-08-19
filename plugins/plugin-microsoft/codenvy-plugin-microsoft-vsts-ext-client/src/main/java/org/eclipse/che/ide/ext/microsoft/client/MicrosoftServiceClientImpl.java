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
package org.eclipse.che.ide.ext.microsoft.client;

import static com.google.gwt.http.client.RequestBuilder.PUT;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

import com.google.inject.Inject;
import java.util.List;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftRepository;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftUserProfile;
import org.eclipse.che.ide.ext.microsoft.shared.dto.NewMicrosoftPullRequest;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;

/** @author Anton Korneta */
public class MicrosoftServiceClientImpl implements MicrosoftServiceClient {

  private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
  private final AsyncRequestFactory asyncRequestFactory;
  private final AppContext appContext;
  private final LoaderFactory loaderFactory;

  @Inject
  public MicrosoftServiceClientImpl(
      DtoUnmarshallerFactory dtoUnmarshallerFactory,
      AsyncRequestFactory asyncRequestFactory,
      AppContext appContext,
      LoaderFactory loaderFactory) {
    this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
    this.asyncRequestFactory = asyncRequestFactory;
    this.appContext = appContext;
    this.loaderFactory = loaderFactory;
  }

  private String getBaseUrl() {
    return appContext.getDevMachine().getWsAgentBaseUrl() + "/microsoft";
  }

  @Override
  public Promise<MicrosoftRepository> getRepository(
      String account, String collection, String project, String repository) {
    return asyncRequestFactory
        .createGetRequest(
            getBaseUrl()
                + "/repository/"
                + account
                + '/'
                + collection
                + '/'
                + project
                + '/'
                + repository)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting VSTS repository"))
        .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftRepository.class));
  }

  @Override
  public Promise<List<MicrosoftPullRequest>> getPullRequests(
      String account, String collection, String project, String repository) {
    return asyncRequestFactory
        .createGetRequest(
            getBaseUrl()
                + "/pullrequests/"
                + account
                + '/'
                + collection
                + '/'
                + project
                + '/'
                + repository)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Getting VSTS pull request list"))
        .send(dtoUnmarshallerFactory.newListUnmarshaller(MicrosoftPullRequest.class));
  }

  @Override
  public Promise<MicrosoftPullRequest> createPullRequest(
      String account,
      String collection,
      String project,
      String repository,
      NewMicrosoftPullRequest input) {
    return asyncRequestFactory
        .createPostRequest(
            getBaseUrl()
                + "/pullrequests/"
                + account
                + '/'
                + collection
                + '/'
                + project
                + '/'
                + repository,
            input)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("Creating new pul request"))
        .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftPullRequest.class));
  }

  @Override
  public Promise<MicrosoftPullRequest> updatePullRequest(
      String account,
      String collection,
      String project,
      String repository,
      String pullRequestId,
      MicrosoftPullRequest pullRequest) {
    final String url =
        getBaseUrl()
            + "/pullrequests/"
            + account
            + '/'
            + collection
            + '/'
            + project
            + '/'
            + repository
            + '/'
            + pullRequestId;
    return asyncRequestFactory
        .createRequest(PUT, url, pullRequest, false)
        .header(ACCEPT, APPLICATION_JSON)
        .loader(loaderFactory.newLoader("updatePullRequest"))
        .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftPullRequest.class));
  }

  @Override
  public Promise<MicrosoftUserProfile> getUserProfile() {
    String url = getBaseUrl() + "/profile";
    return asyncRequestFactory
        .createGetRequest(url)
        .loader(loaderFactory.newLoader("Getting user profile"))
        .send(dtoUnmarshallerFactory.newUnmarshaller(MicrosoftUserProfile.class));
  }
}
