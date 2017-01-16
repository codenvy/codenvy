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

import com.codenvy.client.model.factory.FactoryCreator;
import com.codenvy.client.model.factory.FactoryProject;

import java.util.List;

/**
 * @author Florent Benoit
 */
public interface Factory {

    String getV();

    List<Link> getLinks();

    Factory withWarnonclose(boolean warnonclose);

    String getKeepdirectory();

    /**
     * @return project
     */
    FactoryProject project();

    /**
     * @return the creator
     */
    FactoryCreator creator();

}
