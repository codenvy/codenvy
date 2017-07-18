/*******************************************************************************
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.license.shared.model;

import com.codenvy.api.license.shared.dto.IssueDto;

import java.util.List;

/**
 * Describes legality of usage of Codenvy according to actual license.
 *
 * @author Dmytro Nochevnov
 */
public interface Legality {

    /**
     * Check if Codenvy usage is legal.
     */
    boolean getIsLegal();

    /**
     * Get list of issues related to actual license.
     */
    List<IssueDto> getIssues();
}
