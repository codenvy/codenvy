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
package com.codenvy.plugin.webhooks;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.List;

import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Wrapper class for calls to Codenvy factory REST API
 *
 * @author Stephane Tournie
 */
public class FactoryConnection {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryConnection.class);

    private final HttpJsonRequestFactory httpJsonRequestFactory;
    private final String                 baseUrl;

    @Inject
    public FactoryConnection(HttpJsonRequestFactory httpJsonRequestFactory, @Named("che.api") String baseUrl) {
        this.httpJsonRequestFactory = httpJsonRequestFactory;
        this.baseUrl = baseUrl;
    }

    /**
     * Get a given factory
     *
     * @param factoryId
     *         the id of the factory
     * @return the expected factory or null if an error occurred during the call to 'getFactory'
     * @throws ServerException
     */
    public FactoryDto getFactory(final String factoryId) throws ServerException {
        String url = fromUri(baseUrl).path(FactoryService.class).path(FactoryService.class, "getFactory")
                                     .build(factoryId).toString();
        FactoryDto factory;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url)
                                                                .useGetMethod();
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            factory = response.asDto(FactoryDto.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return factory;
    }

    /**
     * Find a factory
     *
     * @param factoryName
     *         the name of the factory
     * @param userId
     *         the id of the user that owns the factory
     * @return the expected factory or null if an error occurred during the call to 'getFactory'
     * @throws ServerException
     */
    public List<FactoryDto> findFactory(final String factoryName, final String userId) throws ServerException {
        String url = fromUri(baseUrl).path(FactoryService.class).path(FactoryService.class, "getFactoryByAttribute").build().toString();
        List<FactoryDto> factories;
        HttpJsonRequest httpJsonRequest =
                httpJsonRequestFactory.fromUrl(url)
                                      .useGetMethod()
                                      .addQueryParam("name", factoryName)
                                      .addQueryParam("creator.userId", userId);
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            factories = response.asList(FactoryDto.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return factories;
    }

    /**
     * Update a given factory
     *
     * @param factory
     *         the factory to update
     * @return the updated factory or null if an error occurred during the call to 'updateFactory'
     * @throws ServerException
     */
    public FactoryDto updateFactory(final FactoryDto factory) throws ServerException {
        final String factoryId = factory.getId();
        final String url = fromUri(baseUrl).path(FactoryService.class).path(FactoryService.class, "updateFactory")
                                           .build(factoryId).toString();

        FactoryDto newFactory;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url)
                                                                .usePutMethod()
                                                                .setBody(factory);
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            newFactory = response.asDto(FactoryDto.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return newFactory;
    }

    /**
     * Save a new factory
     *
     * @param factory
     *         the factory to create
     * @return the created factory or null if an error occurred during the call to 'saveFactory'
     * @throws ServerException
     */
    public FactoryDto saveFactory(final FactoryDto factory) throws ServerException {
        final String url = fromUri(baseUrl).path(FactoryService.class).build().toString();
        FactoryDto newFactory;
        HttpJsonRequest httpJsonRequest = httpJsonRequestFactory.fromUrl(url)
                                                                .usePostMethod()
                                                                .setBody(factory);
        try {
            HttpJsonResponse response = httpJsonRequest.request();
            newFactory = response.asDto(FactoryDto.class);

        } catch (IOException | ApiException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new ServerException(e.getLocalizedMessage());
        }
        return newFactory;
    }
}
