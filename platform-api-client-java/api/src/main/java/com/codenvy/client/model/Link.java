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
 * The link model interface.
 *
 * @author Kevin Pollet
 */
public interface Link {

    /**
     * @return URL of this link
     */
    String href();

    /**
     * @return name of this link
     */
    String rel();

    /**
     * @return producer of this link
     */
    String produces();

    /**
     * @return consumer of this link
     */
    String consumes();

    /**
     * @return name of the method of this link
     */
    String method();

}
