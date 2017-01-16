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
package com.codenvy.client.model.git;

/**
 * Gets details about a git revision
 * @author Florent Benoit
 */
public interface Revision {

    /**
     * @return branch name
     */
    String getBranch();

    String getId();

    /**
     * @return commit message
     */
    String getMessage();

    /**
     * @return time of commit
     */
    long getCommitTime();

}
