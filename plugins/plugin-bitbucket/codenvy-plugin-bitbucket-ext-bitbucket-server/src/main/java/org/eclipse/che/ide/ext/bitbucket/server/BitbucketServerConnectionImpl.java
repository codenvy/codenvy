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

import static java.lang.String.valueOf;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.PUT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.eclipse.che.commons.json.JsonHelper.toJson;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.eclipse.che.ide.ext.bitbucket.server.BitbucketServerDTOConverter.convertToBitbucketPullRequest;
import static org.eclipse.che.ide.ext.bitbucket.server.BitbucketServerDTOConverter.convertToBitbucketRepository;
import static org.eclipse.che.ide.ext.bitbucket.server.BitbucketServerDTOConverter.convertToBitbucketServerPullRequest;
import static org.eclipse.che.ide.ext.bitbucket.server.BitbucketServerDTOConverter.convertToBitbucketUser;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.doRequest;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.getBitbucketPage;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.getJson;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.parseJsonResponse;
import static org.eclipse.che.ide.ext.bitbucket.server.rest.BitbucketRequestUtils.postJson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketLink;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerPullRequestsPage;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerRepositoriesPage;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketServerUser;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;
import org.eclipse.che.security.oauth.shared.OAuthAuthorizationHeaderProvider;

/**
 * Implementation of {@link BitbucketConnection} for Bitbucket Server.
 *
 * @author Igor Vinokur
 */
public class BitbucketServerConnectionImpl implements BitbucketConnection {

  private final URLTemplates urlTemplates;
  private final String bitbucketEndpoint;
  private final OAuthAuthorizationHeaderProvider headerProvider;

  BitbucketServerConnectionImpl(
      String bitbucketEndpoint, OAuthAuthorizationHeaderProvider headerProvider) {
    this.bitbucketEndpoint = bitbucketEndpoint;
    this.headerProvider = headerProvider;
    this.urlTemplates = new BitbucketServerURLTemplates(bitbucketEndpoint);
  }

  @Override
  public BitbucketUser getUser() throws ServerException, IOException, BitbucketException {
    //Need to check if user has permissions to retrieve full information from Bitbucket Server rest API.
    //Other requests will not fail with 403 error, but may return empty data.
    doRequest(this, GET, bitbucketEndpoint + "/rest/api/latest/users", null, null);

    //Bitbucket Server does not have direct API method to retrieve authenticated user.
    //Authenticated user exists in http clone url of any repository (http://<user>@bitbucketserver.com/scm/project/repository.git).
    String response = getJson(this, bitbucketEndpoint + "/rest/api/latest/repos");
    Optional<BitbucketLink> optional =
        parseJsonResponse(response, BitbucketServerRepositoriesPage.class)
            .getValues()
            .get(0)
            .getLinks()
            .getClone()
            .stream()
            .filter(clone -> "http".equals(clone.getName()))
            .findAny();
    if (optional.isPresent()) {
      String href = optional.get().getHref();
      String user = href.substring(href.indexOf("://") + 3, href.indexOf("@"));
      final String userResponse = getJson(this, urlTemplates.userUrl() + user);
      return convertToBitbucketUser(parseJsonResponse(userResponse, BitbucketServerUser.class));
    } else {
      throw new BitbucketException(
          NOT_FOUND.getStatusCode(), "Failed to retrieve authorized user", TEXT_PLAIN);
    }
  }

  @Override
  public BitbucketRepository getRepository(String owner, String repositorySlug)
      throws IOException, BitbucketException, ServerException {
    final String response = getJson(this, urlTemplates.repositoryUrl(owner, repositorySlug));
    return convertToBitbucketRepository(
        parseJsonResponse(response, BitbucketServerRepository.class));
  }

  @Override
  public List<BitbucketPullRequest> getRepositoryPullRequests(String owner, String repositorySlug)
      throws ServerException, IOException, BitbucketException {
    final List<BitbucketPullRequest> pullRequests = new ArrayList<>();
    BitbucketServerPullRequestsPage pullRequestsPage = null;

    do {
      final String url =
          urlTemplates.pullRequestUrl(owner, repositorySlug)
              + (pullRequestsPage != null
                  ? "?start=" + valueOf(pullRequestsPage.getNextPageStart())
                  : "");

      pullRequestsPage = getBitbucketPage(this, url, BitbucketServerPullRequestsPage.class);
      pullRequests.addAll(
          pullRequestsPage
              .getValues()
              .stream()
              .map(BitbucketServerDTOConverter::convertToBitbucketPullRequest)
              .collect(Collectors.toList()));

    } while (!pullRequestsPage.isIsLastPage());

    return pullRequests;
  }

  @Override
  public BitbucketPullRequest openPullRequest(
      String owner, String repositorySlug, BitbucketPullRequest pullRequest)
      throws ServerException, IOException, BitbucketException {
    final String url = urlTemplates.pullRequestUrl(owner, repositorySlug);
    final String response =
        postJson(this, url, toJson(convertToBitbucketServerPullRequest(pullRequest)));
    return convertToBitbucketPullRequest(
        parseJsonResponse(response, BitbucketServerPullRequest.class));
  }

  @Override
  public BitbucketPullRequest updatePullRequest(
      String owner, String repositorySlug, BitbucketPullRequest pullRequest)
      throws ServerException, IOException, BitbucketException {
    final String url =
        urlTemplates.updatePullRequestUrl(owner, repositorySlug, pullRequest.getId());
    String response = doRequest(this, PUT, url, APPLICATION_JSON, toJson(pullRequest));
    return convertToBitbucketPullRequest(
        parseJsonResponse(response, BitbucketServerPullRequest.class));
  }

  @Override
  public List<BitbucketRepository> getRepositoryForks(String owner, String repositorySlug)
      throws IOException, BitbucketException, ServerException, IllegalArgumentException {
    final List<BitbucketRepository> repositories = new ArrayList<>();
    BitbucketServerRepositoriesPage repositoriesPage = null;

    do {
      final String url =
          urlTemplates.forksUrl(owner, repositorySlug)
              + (repositoriesPage != null
                  ? "?start=" + valueOf(repositoriesPage.getNextPageStart())
                  : "");
      repositoriesPage = getBitbucketPage(this, url, BitbucketServerRepositoriesPage.class);
      repositories.addAll(
          repositoriesPage
              .getValues()
              .stream()
              .map(BitbucketServerDTOConverter::convertToBitbucketRepository)
              .collect(Collectors.toList()));
    } while (!repositoriesPage.isIsLastPage());

    return repositories;
  }

  @Override
  public BitbucketRepositoryFork forkRepository(
      String owner, String repositorySlug, String forkName, boolean isForkPrivate)
      throws IOException, BitbucketException, ServerException {
    final String url = urlTemplates.repositoryUrl(owner, repositorySlug);
    final String response =
        postJson(this, url, toJson(newDto(BitbucketServerRepository.class).withName(forkName)));
    return parseJsonResponse(response, BitbucketRepositoryFork.class);
  }

  @Override
  public void authorizeRequest(HttpURLConnection http, String requestMethod, String requestUrl) {
    String authorizationHeader =
        headerProvider.getAuthorizationHeader(
            "bitbucket-server",
            EnvironmentContext.getCurrent().getSubject().getUserId(),
            requestMethod,
            requestUrl,
            null);
    if (authorizationHeader != null) {
      http.setRequestProperty(AUTHORIZATION, authorizationHeader);
    }
  }
}
