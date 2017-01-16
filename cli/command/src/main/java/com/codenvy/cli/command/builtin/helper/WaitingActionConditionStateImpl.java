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
 * @author Florent Benoit
 */
public class WaitingActionConditionStateImpl<T> implements WaitingActionConditionState<T> {

    private T current;
    private String newText;
    private boolean complete;

    public WaitingActionConditionStateImpl(T current) {
        this.current = current;
    }

    @Override
    public T current() {
        return current;
    }

    @Override
    public void updatedText(String newText) {
        this.newText = newText;

    }

    @Override
    public void setComplete() {
        this.complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    public String newText() {
        return newText;
    }
}
