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
 * State of a waiting action
 * @author Florent Benoit
 */
public interface WaitingActionConditionState<T> {

    /**
     * @return current object
     */
    T current();

    /**
     * Update the text displayed in the action
     * @param newText the action text
     */
    void updatedText(String newText);

    /**
     * If called, wait action will be finished
     */
    void setComplete();
}
