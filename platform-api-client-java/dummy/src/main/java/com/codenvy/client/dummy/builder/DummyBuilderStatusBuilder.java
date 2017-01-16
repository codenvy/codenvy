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
package com.codenvy.client.dummy.builder;

import com.codenvy.client.model.BuilderState;

/**
 * @author Florent Benoit
 */
public class DummyBuilderStatusBuilder {

    private DummyBuilderStatus dummyBuilderStatus;


    public DummyBuilderStatusBuilder(long taskId) {
        this.dummyBuilderStatus = new DummyBuilderStatus(taskId);
    }

    public DummyBuilderStatusBuilder withStatus(BuilderState builderState) {
        dummyBuilderStatus.setStatus(builderState);
        return this;
    }

    public DummyBuilderStatusBuilder withLog(String log) {
        dummyBuilderStatus.setLog(log);
        return this;
    }



    public DummyBuilderStatus build() {
        return dummyBuilderStatus;
    }
}
