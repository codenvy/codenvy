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
package com.codenvy.cli.command.builtin.helper;

/**
 * Allow to check when action needs to be finished
 * @author Florent Benoit
 */
public interface WaitingActionCondition<T> {

    /**
     * Check that wait action should be finished or not
     * @param checker provides data and interact with the user
     */
    void check(WaitingActionConditionState<T> checker);

}
