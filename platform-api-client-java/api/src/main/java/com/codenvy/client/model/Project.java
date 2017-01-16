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
package com.codenvy.client.model;

import com.codenvy.client.model.project.BuildersDescription;
import com.codenvy.client.model.project.RunnersDescription;

import java.util.List;
import java.util.Map;

/**
 * Project description is containing the data of a project
 *
 * @author Florent Benoit
 */
public interface Project extends ProjectReference {

    /**
     * @return user permissions for this project
     */
    List<String> permissions();

    /**
     * @return the attributes for this given project
     */
    Map<String, List<String>> attributes();


    /**
     * @return the description of the runners
     */
    RunnersDescription runners();

    /**
     * @return the description of the builders
     */
    BuildersDescription builders();

}
