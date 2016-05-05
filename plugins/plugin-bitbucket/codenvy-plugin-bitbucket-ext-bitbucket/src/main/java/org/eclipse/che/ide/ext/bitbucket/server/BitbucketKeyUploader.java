
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

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.git.impl.nativegit.GitUrl;
import org.eclipse.che.git.impl.nativegit.ssh.SshKeyUploader;
import org.eclipse.che.ide.ext.bitbucket.shared.BitbucketKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;
import static org.eclipse.che.ide.rest.HTTPHeader.AUTHORIZATION;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_LENGTH;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;
import static org.eclipse.che.ide.rest.HTTPMethod.GET;
import static org.eclipse.che.ide.rest.HTTPMethod.POST;
import static org.eclipse.che.ide.rest.HTTPStatus.FORBIDDEN;
import static org.eclipse.che.ide.rest.HTTPStatus.OK;

/**
 * Uploads keys to Bitbucket.
 *
 * @author Kevin Pollet
 */
@Singleton
public class BitbucketKeyUploader implements SshKeyUploader {
    private static final Logger  LOG                   = LoggerFactory.getLogger(BitbucketKeyUploader.class);
    private static final Pattern BITBUCKET_URL_PATTERN = Pattern.compile(".*bitbucket\\.org.*");
    private static final String  OAUTH_PROVIDER_NAME   = "bitbucket";

    private final OAuthTokenProvider tokenProvider;

    @Inject
    public BitbucketKeyUploader(@NotNull final OAuthTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean match(final String url) {
        return GitUrl.isSSH(url) && BITBUCKET_URL_PATTERN.matcher(url).matches();
    }

    @Override
    public void uploadKey(String publicKey) throws IOException, UnauthorizedException {

        final StringBuilder answer = new StringBuilder();
        final String publicKeyString = new String(publicKey.getBytes());
        final String sshKeysUrl = "https://api.bitbucket.org/1.0/users/ssh-keys";

        final List<BitbucketKey> bitbucketUserPublicKeys = getUserPublicKeys(sshKeysUrl, answer);
        for (final BitbucketKey oneBitbucketUserPublicKey : bitbucketUserPublicKeys) {
            if (publicKeyString.startsWith(oneBitbucketUserPublicKey.getKey())) {
                return;
            }
        }

        final Map<String, String> postParams = new HashMap<>(2);
        postParams.put("label", GitUrl.getCodenvyTimeStampKeyLabel());
        postParams.put("key", new String(publicKey.getBytes()));

        final String postBody = JsonHelper.toJson(postParams);

        LOG.debug("Upload public key: {}", postBody);

        int responseCode;
        HttpURLConnection conn = null;
        final OAuthToken token = tokenProvider.getToken(OAUTH_PROVIDER_NAME, getUserId());
        try {

            conn = (HttpURLConnection)new URL(sshKeysUrl).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(POST);
            conn.setRequestProperty(ACCEPT, APPLICATION_JSON);

            if (token != null) {
                conn.setRequestProperty(AUTHORIZATION, "Bearer " + token.getToken());
            }
            conn.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
            conn.setRequestProperty(CONTENT_LENGTH, String.valueOf(postBody.length()));
            conn.setDoOutput(true);

            try (OutputStream out = conn.getOutputStream()) {
                out.write(postBody.getBytes());
            }
            responseCode = conn.getResponseCode();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        LOG.debug("Upload key response code: {}", responseCode);

        if (responseCode != OK) {
            final String exceptionMessage = String.format("%d: Failed to upload public key to https://bitbucket.org", responseCode);

            if (responseCode == FORBIDDEN) {
                throw new UnauthorizedException(exceptionMessage);
            }
            throw new IOException(exceptionMessage);
        }
    }

    private List<BitbucketKey> getUserPublicKeys(final String requestUrl, final StringBuilder answer) throws IOException {
        HttpURLConnection conn = null;
        final OAuthToken token = tokenProvider.getToken(OAUTH_PROVIDER_NAME, getUserId());
        try {

            conn = (HttpURLConnection)new URL(requestUrl).openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(GET);
            conn.setRequestProperty(ACCEPT, APPLICATION_JSON);
            if (token != null) {
                conn.setRequestProperty(AUTHORIZATION, "Bearer " + token.getToken());
            }
            if (conn.getResponseCode() == OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        answer.append(line).append('\n');
                    }
                }

                return DtoFactory.getInstance().createListDtoFromJson(answer.toString(), BitbucketKey.class);
            }
            return Collections.emptyList();

        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return Collections.emptyList();

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getUserId() {
        return EnvironmentContext.getCurrent().getUser().getId();
    }
}
