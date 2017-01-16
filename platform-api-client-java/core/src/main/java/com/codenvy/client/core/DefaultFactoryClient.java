/*******************************************************************************
 * Copyright (c) [2012] - [2017] Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.client.core;

import com.codenvy.client.FactoryClient;
import com.codenvy.client.Request;
import com.codenvy.client.core.auth.AuthenticationManager;
import com.codenvy.client.core.model.DefaultFactory;
import com.codenvy.client.model.Factory;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.User;
import com.google.common.reflect.TypeToken;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

/**
 * The Codenvy factory API client.
 *
 * @author Florent Benoit
 */
public class DefaultFactoryClient extends AbstractClient implements FactoryClient {

    /**
     * Constructs an instance of {@link com.codenvy.client.core.DefaultFactoryClient}.
     *
     * @param url
     *         the Codenvy platform URL.
     * @param authenticationManager
     *         the {@link com.codenvy.client.core.auth.AuthenticationManager}.
     * @throws NullPointerException
     *         if url or authenticationManager parameter is {@code null}.
     */
    DefaultFactoryClient(String url, AuthenticationManager authenticationManager) {
        super(url, "factory", authenticationManager);
    }


    /**
     * Returns factory data
     *
     * @param jsonContent
     *         the content of the JSON factory
     * @return factory information.
     */

    @Override
    public Request<Factory> save(String jsonContent) {

        // ok there we need to check if there is a logo element in the json content
        // and if it's there we will add the image as binary content of the form
        URL logoURL = null;
        JsonReader jSonReader = Json.createReader(new StringReader(jsonContent));
        JsonObject jsonObject = jSonReader.readObject();
        if (jsonObject != null) {
            JsonObject button = jsonObject.getJsonObject("button");
            if (button != null) {
                String type = button.getString("type");
                if ("logo".equals(type)) {
                    JsonObject attributes = button.getJsonObject("attributes");
                    if (attributes != null) {
                        String logoPath = attributes.getString("logo");
                        if (logoPath != null && !logoPath.isEmpty()) {
                            // found a logo
                            try {
                                logoURL = new URL(logoPath);
                            } catch (MalformedURLException e) {
                                throw new IllegalStateException("Invalid logo content found in the JSON file.", e);
                            }
                        }
                    }
                }
            }
        }


        final FormDataMultiPart formDataMultiPart = new FormDataMultiPart().field("factoryUrl", jsonContent);

        // do we have a logo
        if (logoURL != null) {
            try  {
                URLConnection urlConnection = logoURL.openConnection();
                String contentType = urlConnection.getContentType();
                FormDataBodyPart formDataBodyPart = new FormDataBodyPart(FormDataContentDisposition
                                                                                 .name("image").fileName("example").build(),
                                                                         logoURL.openStream(),
                                                                         MediaType.valueOf(contentType));

                formDataMultiPart.bodyPart(formDataBodyPart);
            } catch (IOException e) {
                throw new IllegalStateException("Invalid logo content found in the JSON file.", e);
            }
        }

        final Invocation request = getWebTarget().request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPost(Entity.entity(formDataMultiPart, MULTIPART_FORM_DATA));

        return new SimpleRequest<Factory>(request, DefaultFactory.class, getAuthenticationManager());
    }

    /**
     * Returns factory content of a project
     *
     * @param projectReference
     * @return
     *         factory content.
     */
    @Override
    public Request<String> export(ProjectReference projectReference) {

        final Invocation request = getWebTarget().path(projectReference.workspaceId())
                                                 .path(projectReference.name())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<String>(request, getAuthenticationManager());
    }

    /**
     * Deletes a factory based on its id
     *
     * @param factoryId
     *         the ID of the factory
     * @return void request
     */
    @Override
    public Request<Void> delete(String factoryId) {
        final Invocation request = getWebTarget().path(factoryId)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildDelete();

        return new SimpleRequest<Void>(request, getAuthenticationManager());
    }

    /**
     * Updates a factory by providing its id and JSON content
     *
     * @param factoryId
     *         the ID of the factory
     * @param jsonContent
     *         the content of the JSON file
     * @return void request
     */
    @Override
    public Request<Void> update(String factoryId, String jsonContent) {

        JsonReader jSonReader = Json.createReader(new StringReader(jsonContent));
        JsonObject jsonObject = jSonReader.readObject();

        final Invocation request = getWebTarget().path(factoryId)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildPut(Entity.entity(jsonObject, APPLICATION_JSON));

        return new SimpleRequest<Void>(request, getAuthenticationManager());

    }

    /**
     * List factories ID available
     * @param user the user to use for listing factories
     * @return the list of factories
     */
    @Override
    public Request<List<String>> list(User user) {
        checkNotNull(user);

        final Invocation request = getWebTarget().path("find").queryParam("creator.userId", user.id())
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();


        Type collectionType = new TypeToken<List<String>>() {}.getType();


        return new RequestResponseAdaptor<>(new SimpleRequest<>(request, Response.class, getAuthenticationManager()),
                                            new Adaptor<List<String>, Response>() {
                                                @Override
                                                public List<String> adapt(Response response) {
                                                    List<String> factories = new ArrayList<>();
                                                    if (SUCCESSFUL == response.getStatusInfo().getFamily()) {
                                                        JsonStructure jsonStructure  = response.readEntity(JsonStructure.class);
                                                        if (jsonStructure != null) {
                                                            if (ARRAY == jsonStructure.getValueType()) {
                                                                JsonArray jsonArray = (JsonArray)jsonStructure;
                                                                String substring = "/api/factory/";
                                                                int substringLength = substring.length();
                                                                for (JsonValue jsonValue : jsonArray) {
                                                                    if (OBJECT == jsonValue.getValueType()) {
                                                                        JsonObject jsonObject = (JsonObject) jsonValue;
                                                                        String href = jsonObject.getString("href");
                                                                        if (href != null) {
                                                                            int indexOf = href.indexOf(substring);
                                                                            if (indexOf > 0) {
                                                                                factories.add(href.substring(indexOf + substringLength));
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        return factories;
                                                    }
                                                    throw CodenvyErrorExceptionHelper.from(response);
                                                }
                                            });

    }

    /**
     * Gets a factory based on its id
     *
     * @param factoryId
     *         the ID of the factory
     * @return factory information
     */
    @Override
    public Request<Factory> get(String factoryId) {
        final Invocation request = getWebTarget().path(factoryId)
                                                 .request()
                                                 .accept(APPLICATION_JSON)
                                                 .buildGet();

        return new SimpleRequest<Factory>(request, DefaultFactory.class, getAuthenticationManager());
    }
}
