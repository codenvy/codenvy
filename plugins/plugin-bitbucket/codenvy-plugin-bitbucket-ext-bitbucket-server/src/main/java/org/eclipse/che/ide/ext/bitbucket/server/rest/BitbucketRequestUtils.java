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
package org.eclipse.che.ide.ext.bitbucket.server.rest;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketConnection;
import org.eclipse.che.ide.ext.bitbucket.server.BitbucketException;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.eclipse.che.commons.json.JsonNameConventions.CAMEL_UNDERSCORE;

/**
 * Utility class for performing HTTP requests to Bitbucket API.
 *
 * @author Igor Vinokur
 */
public class BitbucketRequestUtils {

    /**
     * Returns Bitbucket page object from Bitbucket API request.
     *
     * @param connection
     *         {@link BitbucketConnection} connection object to authorize the HTTP request
     * @param url
     *         request url
     * @param pageClass
     *         Bitbucket page object to return
     * @throws ServerException
     *         if any error occurs when parse Json response
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    public static <T> T getBitbucketPage(BitbucketConnection connection, String url, Class<T> pageClass) throws IOException,
                                                                                                                BitbucketException,
                                                                                                                ServerException {
        final String response = getJson(connection, url, OK.getStatusCode());
        return parseJsonResponse(response, pageClass);
    }

    /**
     * Returns a Json in String format from GET Bitbucket API request.
     *
     * @param connection
     *         {@link BitbucketConnection} connection object to authorize the HTTP request
     * @param url
     *         request url
     * @param success
     *         expected success status code
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    public static String getJson(BitbucketConnection connection, String url, int success) throws IOException, BitbucketException {
        return doRequest(connection, GET, url, success, null, null);
    }

    /**
     * Returns a Json in String format from POST Bitbucket API request.
     *
     * @param connection
     *         {@link BitbucketConnection} connection object to authorize the HTTP request
     * @param url
     *         request url
     * @param success
     *         expected success status code
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    public static String postJson(BitbucketConnection connection, String url, int success, String data) throws IOException,
                                                                                                               BitbucketException {
        return doRequest(connection, POST, url, success, APPLICATION_JSON, data);
    }

    /**
     * Returns a Json in String format from specified Bitbucket API request.
     *
     * @param connection
     *         {@link BitbucketConnection} connection object to authorize the HTTP request
     * @param requestMethod
     *         HTTP request method
     * @param url
     *         request url
     * @param success
     *         expected success status code
     * @throws IOException
     *         if any i/o errors occurs
     * @throws BitbucketException
     *         if Bitbucket returned unexpected or error status for request
     */
    public static String doRequest(BitbucketConnection connection,
                                   String requestMethod,
                                   String url,
                                   int success,
                                   String contentType,
                                   String data) throws IOException, BitbucketException {
        HttpURLConnection http = null;

        try {

            http = (HttpURLConnection)new URL(url).openConnection();
            http.setInstanceFollowRedirects(false);
            http.setRequestMethod(requestMethod);
            http.setRequestProperty(ACCEPT, APPLICATION_JSON);

            connection.authorizeRequest(http, requestMethod, url);

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

    /**
     * Returns specified object described from given Json.
     *
     * @param json
     *         given String Json
     * @param clazz
     *         class of the object to return
     * @throws ServerException
     *         if any error occurs when parse Json response
     */
    public static <T> T parseJsonResponse(final String json, final Class<T> clazz) throws ServerException {
        try {
            return JsonHelper.fromJson(json, clazz, null, CAMEL_UNDERSCORE);
        } catch (JsonParseException e) {
            throw new ServerException(e);
        }
    }

    private static BitbucketException fault(final HttpURLConnection http) throws IOException {
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

    private static String readBody(final InputStream input, final int contentLength) throws IOException {
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
