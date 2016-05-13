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
package org.eclipse.che.ide.ext.microsoft.server;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.repackaged.com.google.common.annotations.Beta;
import com.google.inject.Singleton;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.microsoft.shared.VstsErrorCodes;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftVstsApiError;
import org.eclipse.che.ide.ext.microsoft.shared.dto.NewMicrosoftPullRequest;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftPullRequestList;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftRepository;
import org.eclipse.che.ide.ext.microsoft.shared.dto.MicrosoftUserProfile;
import org.everrest.core.impl.provider.json.JsonValue;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_CREATED;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Microsoft VSTS REST client.
 *
 * @author Mihail Kuznyetsov
 * @author Yevhenii Voevodin
 */
@Beta
@Singleton
public final class MicrosoftVstsRestClient {

    private final URLTemplates       templates;
    private final OAuthTokenProvider tokenProvider;

    @Inject
    public MicrosoftVstsRestClient(OAuthTokenProvider tokenProvider,
                                   URLTemplates templates) {
        this.tokenProvider = tokenProvider;
        this.templates = templates;
    }

    /**
     * Returns the current user profile.
     *
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftUserProfile getUserProfile() throws IOException, ServerException, UnauthorizedException {
        return doGet(templates.profileUrl(), MicrosoftUserProfile.class);
    }

    /**
     * Returns the repository with given {@code id} in given {@code project}.
     *
     * @param project
     *         the project name or id
     * @param name
     *         the name of the repository
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftRepository getRepository(String account, String collection, String project, String name)
            throws IOException, ServerException, UnauthorizedException {
        return doGet(templates.repositoryUrl(account, collection, project, name), MicrosoftRepository.class);
    }

    /**
     * Returns the list of active pull request in given repository.
     * Generates html url for each pull requests
     *
     * @param repositoryId
     *         the id of the repository
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public List<MicrosoftPullRequest> getPullRequests(String account, String collection, String project, String repository,
                                                      String repositoryId) throws IOException, ServerException, UnauthorizedException {
        return doGet(templates.pullRequestsUrl(account, collection, repositoryId), MicrosoftPullRequestList.class)
                .getValue()
                .stream()
                .peek(pr -> pr.setHtmlUrl(templates.pullRequestHtmlUrl(account,
                                                                       collection,
                                                                       project,
                                                                       repository,
                                                                       String.valueOf(pr.getPullRequestId()))))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new pull request.
     *
     * @param repositoryId
     *         repository where pull request should be created
     * @param newPr
     *         new pull request descriptor
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftPullRequest createPullRequest(String account, String collection, String repositoryId, NewMicrosoftPullRequest newPr)
            throws ServerException,
                   IOException,
                   UnauthorizedException {
        return doPost(templates.pullRequestsUrl(account, collection, repositoryId), HTTP_CREATED, newPr, MicrosoftPullRequest.class);
    }

    /**
     * Returns a pull request with given {@code repositoryId} and {@code prId}.
     *
     * @param repositoryId
     *         the id of the repository
     * @param prId
     *         the pull request id
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftPullRequest getPullRequest(String account, String collection, String repositoryId, String prId) throws ServerException,
                                                                                                                           IOException,
                                                                                                                           UnauthorizedException {
        return doGet(templates.pullRequestUrl(account, collection, repositoryId, prId), MicrosoftPullRequest.class);
    }

    /**
     * Returns a pull request with given id in a repository selected by
     * repository project name and repository name.
     *
     * @param projectName
     *         the name of the project
     * @param repoName
     *         the name of the repo
     * @param prId
     *         the pull request id
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftPullRequest getPullRequest(String account,
                                               String collection,
                                               String projectName,
                                               String repoName,
                                               String prId) throws ServerException,
                                                                   IOException,
                                                                   UnauthorizedException {
        return doGet(templates.pullRequestUrl(account, collection, projectName, repoName, prId), MicrosoftPullRequest.class);
    }

    /**
     * Updates existing pull request.
     * <p>
     * <p>This method uses merge strategy for the update, so {@code update} object
     * may contain the only information which should be updated.
     * e.g
     * <pre>{@code
     *  {
     *      "status": "completed"
     *  }
     *  }
     * </pre>
     *
     * @param repository
     *         the repository where pull request exists
     * @param prId
     *         pull request id
     * @param update
     *         pull request update
     * @throws IOException
     *         when any io error occurs
     * @throws ServerException
     *         when server responds with unexpected code
     * @throws UnauthorizedException
     *         when user in not authorized to call this method
     */
    public MicrosoftPullRequest updatePullRequests(String account,
                                                   String collection,
                                                   String repository,
                                                   String prId,
                                                   MicrosoftPullRequest update) throws ServerException,
                                                                                       IOException,
                                                                                       UnauthorizedException {
        final JsonValue response = doRequest(HttpMethods.PATCH,
                                             templates.pullRequestUrl(account, collection, repository, prId),
                                             HTTP_OK,
                                             "application/json",
                                             JsonHelper.toJson(update));
        return parseJsonResponse(response, MicrosoftPullRequest.class);
    }

    private String getUserId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }

    private <O> O parseJsonResponse(final JsonValue json, final Class<O> clazz) throws ServerException {
        try {
            return JsonHelper.fromJson(json, clazz, null);
        } catch (JsonParseException e) {
            throw new ServerException(e);
        }
    }

    private <T> T doGet(String url, Class<T> dto) throws ServerException, IOException, UnauthorizedException {
        final JsonValue value = doRequest("GET", url, 200, null, null);
        return parseJsonResponse(value, dto);
    }


    private <T> T doPost(String url, int expCode, Object data, Class<T> respType)
            throws ServerException, IOException, UnauthorizedException {
        final JsonValue value = doRequest("POST",
                                          url,
                                          expCode,
                                          "application/json",
                                          DtoFactory.getInstance().toJson(data));
        return respType == null ? null : parseJsonResponse(value, respType);
    }

    private JsonValue doRequest(String requestMethod, final String url, int responseCode, String contentType, String data)
            throws IOException, ServerException, UnauthorizedException {
        HttpURLConnection http = null;
        try {
            http = (HttpURLConnection)new URL(url).openConnection();
            if (HttpMethods.PATCH.equals(requestMethod)) {
                http.setRequestProperty("X-HTTP-Method-Override", requestMethod);
                requestMethod = HttpMethods.PUT;
            }
            http.setRequestMethod(requestMethod);

            final OAuthToken token = tokenProvider.getToken("microsoft", getUserId());
            if (token != null) {
                http.setRequestProperty("Authorization", "Bearer " + token.getToken());
            }

            if (data != null && !data.isEmpty()) {
                http.setRequestProperty("Content-Type", contentType);
                http.setRequestProperty("Content-Length", String.valueOf(data.length()));
                http.setDoOutput(true);

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(http.getOutputStream()))) {
                    writer.write(data);
                }
            }

            if (http.getResponseCode() != responseCode) {
                if (http.getResponseCode() == 203) {
                    throw new UnauthorizedException("Missing auth token");
                }
                final MicrosoftVstsApiError err = DtoFactory.getInstance()
                                                            .createDtoFromJson(http.getErrorStream(), MicrosoftVstsApiError.class);
                throw new ServerException(DtoFactory.newDto(ExtendedError.class)
                                                    .withMessage(err.getMessage())
                                                    .withErrorCode(VstsErrorCodes.getCodeByTypeKey(err.getTypeKey())));
            }
            JsonValue result;
            try (InputStream input = http.getInputStream()) {
                result = JsonHelper.parseJson(input);
                return result;
            } catch (JsonParseException e) {
                throw new ServerException(e);
            }
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }
}
