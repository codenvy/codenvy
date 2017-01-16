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

/**
 * Allow to get details about the version of the API
 *
 * @author Florent Benoit
 */
public interface Version {

    /**
     * @return specification vendor
     */
    String specificationVendor();

    /**
     * @return implementation vendor
     */
    String implementationVendor();

    /**
     * @return specification title
     */
    String specificationTitle();

    /**
     * @return specification version
     */
    String specificationVersion();

    /**
     * @return implementation version
     */
    String implementationVersion();

    /**
     * @return scm revision
     */
    String scmRevision();

}
