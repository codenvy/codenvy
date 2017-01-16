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
package com.codenvy.client;

import com.codenvy.client.model.Factory;
import com.codenvy.client.model.ProjectReference;
import com.codenvy.client.model.User;

import java.util.List;

/**
 * The Codenvy Factory API client.
 *
 * @author Florent Benoit
 */
public interface FactoryClient {

    /**
     * Returns factory data
     * @param  jsonContent the content of the JSON factory
     * @return factory information.
     */
    Request<Factory> save(String jsonContent);


    /**
     * Returns factory content of a project
     * @param  {@link ProjectReference} the project to export
     * @return factory content.
     */
    Request<String> export(ProjectReference projectReference);


    /**
     * Deletes a factory based on its id
     * @param factoryId the ID of the factory
     * @return void request
     */
    Request<Void> delete(String factoryId);


    /**
     * Updates a factory by providing its id and JSON content
     * @param factoryId the ID of the factory
     * @param jsonContent the content of the JSON file
     * @return void request
     */
    Request<Void> update(String factoryId, String jsonContent);


    /**
     * List factories ID available
     * @param user the user to use for listing factories
     * @return the list of factories
     */
    Request<List<String>> list(User user);


    /**
     * Gets a factory based on its id
     * @param factoryId the ID of the factory
     * @return factory information
     */
    Request<Factory> get(String factoryId);
}
