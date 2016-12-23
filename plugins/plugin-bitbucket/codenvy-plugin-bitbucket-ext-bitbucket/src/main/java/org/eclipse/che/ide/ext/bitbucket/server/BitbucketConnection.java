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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequest;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketPullRequests;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositories;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.net.URLDecoder.decode;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static org.eclipse.che.commons.json.JsonNameConventions.CAMEL_UNDERSCORE;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPMethod.GET;
import static org.eclipse.che.ide.rest.HTTPMethod.POST;
import static org.eclipse.che.ide.rest.HTTPStatus.OK;

/**
 * Connection for retrieving data from Bitbucket.
 *
 * @author Igor Vinokur
 */
public abstract class BitbucketConnection {

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
    public abstract BitbucketUser getUser(String username) throws ServerException, IOException, BitbucketException;

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
    public abstract BitbucketRepository getRepository(String owner, String repositorySlug) throws IOException,
                                                                                                  BitbucketException,
                                                                                                  ServerException;

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
    public abstract List<BitbucketPullRequest> getRepositoryPullRequests(String owner, String repositorySlug) throws ServerException,
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
    public abstract BitbucketPullRequest openPullRequest(String owner,
                                                         String repositorySlug,
                                                         BitbucketPullRequest pullRequest) throws ServerException,
                                                                                                  IOException,
                                                                                                  BitbucketException;

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
    public abstract List<BitbucketRepository> getRepositoryForks(String owner, String repositorySlug) throws IOException,
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
    public abstract BitbucketRepositoryFork forkRepository(String owner,
                                                           String repositorySlug,
                                                           String forkName,
                                                           boolean isForkPrivate) throws IOException,
                                                                                         BitbucketException,
                                                                                         ServerException;

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
    abstract void authorizeRequest(HttpURLConnection http, String requestMethod, String requestUrl) throws IOException;

    String getUserId() {
        return EnvironmentContext.getCurrent().getSubject().getUserId();
    }

    <T> T getBitbucketPage(final String url,
                           final Class<T> pageClass) throws IOException, BitbucketException, ServerException {
        final String response = getJson(url, OK);
        return parseJsonResponse(response, pageClass);
    }

    String getJson(final String url, final int success) throws IOException, BitbucketException {
        return doRequest(GET, url, success, null, null);
    }

    String postJson(final String url, final int success, final String data) throws IOException, BitbucketException {
        return doRequest(POST, url, success, APPLICATION_JSON, data);
    }

    String doRequest(final String requestMethod,
                     final String requestUrl,
                     final int success,
                     final String contentType,
                     final String data) throws IOException, BitbucketException {
        HttpURLConnection http = null;

        try {

            http = (HttpURLConnection)new URL(requestUrl).openConnection();
            http.setInstanceFollowRedirects(false);
            http.setRequestMethod(requestMethod);
            http.setRequestProperty(ACCEPT, APPLICATION_JSON);

            final Map<String, String> requestParameters = new HashMap<>();
            if (data != null && APPLICATION_FORM_URLENCODED.equals(contentType)) {
                final String[] parameters = data.split("&");

                for (final String oneParameter : parameters) {
                    final String[] oneParameterKeyAndValue = oneParameter.split("=");
                    if (oneParameterKeyAndValue.length == 2) {
                        requestParameters.put(oneParameterKeyAndValue[0], decode(oneParameterKeyAndValue[1], "UTF-8"));
                    }
                }
            }

            authorizeRequest(http, requestMethod, requestUrl);

            if (data != null && !data.isEmpty()) {
                http.setRequestProperty(CONTENT_TYPE, contentType);
                http.setDoOutput(true);

                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(http.getOutputStream()))) {
                    writer.write(data);
                }
            }

            if (http.getResponseCode() != success) {
                throw fault(http);
            }

            String result;
            try (InputStream input = http.getInputStream()) {
                result = readBody(input, http.getContentLength());
            }

            return result;

        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    <T> T parseJsonResponse(final String json, final Class<T> clazz) throws ServerException {
        try {
            return JsonHelper.fromJson(json, clazz, null, CAMEL_UNDERSCORE);
        } catch (JsonParseException e) {
            throw new ServerException(e);
        }
    }

    private BitbucketException fault(final HttpURLConnection http) throws IOException {
        final int responseCode = http.getResponseCode();

        try (final InputStream stream = (responseCode >= 400 ? http.getErrorStream() : http.getInputStream())) {

            String body = null;
            if (stream != null) {
                final int length = http.getContentLength();
                body = readBody(stream, length);
            }

            return new BitbucketException(responseCode, body, http.getContentType());
        }
    }

    private String readBody(final InputStream input, final int contentLength) throws IOException {
        String body = null;
        if (contentLength > 0) {
            byte[] b = new byte[contentLength];
            int off = 0;
            int i;
            while ((i = input.read(b, off, contentLength - off)) > 0) {
                off += i;
            }
            body = new String(b);
        } else if (contentLength < 0) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int point;
            while ((point = input.read(buf)) != -1) {
                bout.write(buf, 0, point);
            }
            body = bout.toString();
        }
        return body;
    }
}
