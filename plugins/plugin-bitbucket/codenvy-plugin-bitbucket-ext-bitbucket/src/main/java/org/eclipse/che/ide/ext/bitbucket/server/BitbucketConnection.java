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
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepository;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketRepositoryFork;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketUser;

import javax.validation.constraints.NotNull;
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
 * Connection to Bitbucket rest API.
 *
 * @author Igor Vinokur
 */
public abstract class BitbucketConnection {

    /** @see Bitbucket#getUser(String) */
    abstract BitbucketUser getUser(String username) throws ServerException, IOException, BitbucketException;

    /** @see Bitbucket#getRepository(String, String) */
    abstract BitbucketRepository getRepository(@NotNull final String owner,
                                               @NotNull final String repositorySlug) throws IOException,
                                                                                            BitbucketException,
                                                                                            ServerException;

    /** @see Bitbucket#getRepositoryPullRequests(String, String) */
    abstract List<BitbucketPullRequest> getRepositoryPullRequests(@NotNull final String owner,
                                                                  @NotNull final String repositorySlug) throws ServerException,
                                                                                                               IOException,
                                                                                                               BitbucketException;

    /** @see Bitbucket#openPullRequest(String, String, BitbucketPullRequest) */
    abstract BitbucketPullRequest openPullRequest(@NotNull final String owner,
                                                  @NotNull final String repositorySlug,
                                                  @NotNull final BitbucketPullRequest pullRequest) throws ServerException,
                                                                                                          IOException,
                                                                                                          BitbucketException;

    /** @see Bitbucket#openPullRequest(String, String, BitbucketPullRequest) */
    abstract public List<BitbucketRepository> getRepositoryForks(@NotNull final String owner,
                                                                 @NotNull final String repositorySlug) throws IOException,
                                                                                                              BitbucketException,
                                                                                                              ServerException,
                                                                                                              IllegalArgumentException;

    /** @see Bitbucket#forkRepository(String, String, String, boolean) */
    abstract public BitbucketRepositoryFork forkRepository(@NotNull final String owner,
                                                           @NotNull final String repositorySlug,
                                                           @NotNull final String forkName,
                                                           final boolean isForkPrivate) throws IOException,
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

    <O> O parseJsonResponse(final String json, final Class<O> clazz) throws ServerException {
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
