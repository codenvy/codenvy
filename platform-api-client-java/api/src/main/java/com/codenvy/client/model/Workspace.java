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
 * This interface represents the workspace resource on Codenvy.
 *
 * @author Florent Benoit
 */
public interface Workspace {

    /**
     * @return ID of this workspace
     */
    String id();

    /**
     * @return name of this workspace reference
     */
    String name();

    /**
     * @return Organization ID
     */
    String organizationId();

    /**
     * @return true if the workspace is a temporary workspace
     */
    boolean isTemporary();
}
