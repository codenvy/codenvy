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
package com.codenvy.client.dummy.runner;

import com.codenvy.client.model.RunnerState;

/**
 * @author Florent Benoit
 */
public class DummyRunnerStatusBuilder {

    private DummyRunnerStatus dummyRunnerStatus;


    public DummyRunnerStatusBuilder(long taskId) {
        this.dummyRunnerStatus = new DummyRunnerStatus(taskId);
    }

    public DummyRunnerStatusBuilder withStatus(RunnerState runnerState) {
        dummyRunnerStatus.setStatus(runnerState);
        return this;
    }

    public DummyRunnerStatusBuilder withLog(String log) {
        dummyRunnerStatus.setLog(log);
        return this;
    }


    public DummyRunnerStatus build() {
        return dummyRunnerStatus;
    }
}
