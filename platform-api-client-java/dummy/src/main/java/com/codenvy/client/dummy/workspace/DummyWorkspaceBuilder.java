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
package com.codenvy.client.dummy.workspace;

import java.util.UUID;

/**
 * @author Florent Benoit
 */
public class DummyWorkspaceBuilder {

    private DummyWorkspace dummyWorkspace;


    public DummyWorkspaceBuilder(String name) {
        this.dummyWorkspace = new DummyWorkspace(name);
        this.dummyWorkspace.setId(UUID.randomUUID().toString());

    }


    public DummyWorkspace build() {
        return dummyWorkspace;
    }
}
