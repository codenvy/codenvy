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
package com.codenvy.plugin.webhooks.vsts;

import com.codenvy.plugin.webhooks.vsts.shared.Repository;
import com.codenvy.plugin.webhooks.vsts.shared.StorageDocument;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Base64;

import static java.lang.String.format;

/**
 * Wrapper class for calls to Visual Studio Team Services REST API
 *
 * @author Stephane Tournie
 */
public class VSTSConnection {

    private static final Logger LOG = LoggerFactory.getLogger(VSTSConnection.class);

    private static final String PROTOCOL  = "https";
    private static final String PUBLISHER = "codenvy";
    private static final String EXTENSION = "codenvy-extension";

    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public VSTSConnection(final HttpJsonRequestFactory httpJsonRequestFactory) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    /**
     * Store a factory URL as a project setting into VSTS extension storage
     *
     * @param visualStudioHost
     *         the VSTS host
     * @param account
     *         the VSTS account
     * @param collection
     *         the VSTS collection
     * @param apiVersion
     *         the VSTS API version to use
     * @param credentials
     *         the VSTS credentials to use
     * @param settingKey
     *         the name of the storage key
     * @param factoryUrl
     *         the factory URL that will be stored
     * @throws ServerException
     */
    public void storeFactorySetting(final String visualStudioHost, final String account, final String collection, final String apiVersion,
                                    final Pair<String, String> credentials, final String settingKey, final String factoryUrl)
            throws ServerException {
        final StorageDocument document = DtoFactory.newDto(StorageDocument.class).withId(settingKey).withValue(factoryUrl).withEtag("-1");

        final String extensionStorageUrl = extensionStorageHttpUrl(visualStudioHost, account, collection);

        final String userCredentials = credentials.first + ":" + credentials.second;
        final String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        HttpJsonRequest httpJsonRequest =
                httpJsonRequestFactory.fromUrl(extensionStorageUrl).usePutMethod().setBody(document).setAuthorizationHeader(basicAuth)
                                      .addQueryParam("api-version", apiVersion);
        StorageDocument newDocument;
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            newDocument = response.asDto(StorageDocument.class);
            LOG.debug("Factory URL stored on VSTS: {}", newDocument);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Get URL of repository name
     *
     * @param repositoryIdUrl
     *         the repository id URL
     * @param apiVersion
     *         the VSTS API version to use
     * @return the repository name URL
     */
    public String getRepositoryNameUrl(String repositoryIdUrl, String apiVersion, final Pair<String, String> credentials)
            throws ServerException {
        final String userCredentials = credentials.first + ":" + credentials.second;
        final String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(repositoryIdUrl).useGetMethod().setAuthorizationHeader(
                basicAuth).addQueryParam("api-version", apiVersion);
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            Repository repository = response.asDto(Repository.class);
            LOG.debug("Repository obtained: {}", repository);
            return repository.getRemoteUrl();

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
    }

    /**
     * Build VSTS extension storage URL
     *
     * @param visualStudioHost
     *         the VSTS host
     * @param account
     *         the VSTS account
     * @param collection
     *         the VSTS collection
     * @return the VSTS extension storage URL
     */
    private String extensionStorageHttpUrl(final String visualStudioHost, final String account, final String collection) {
        //https://vsts-test.visualstudio.com/DefaultCollection/_apis
        //https://vsts-test.extmgmt.visualstudio.com/DefaultCollection/_apis
        final String host = account + ".extmgmt." + visualStudioHost + ".com";
        final String collectionPath = collection + "/_apis";
        final String extensionStoragePath = "ExtensionManagement/InstalledExtensions/" + PUBLISHER + "/" + EXTENSION +
                                            "/Data/Scopes/Default/Current/Collections/$settings/Documents";
        return format("%s://%s/%s/%s", PROTOCOL, host, collectionPath, extensionStoragePath);
    }
}
