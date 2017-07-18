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
package com.codenvy.api.license.shared.dto;

import com.codenvy.api.license.shared.model.Issue;
import org.eclipse.che.dto.shared.DTO;

/**
 * @author Dmytro Nochevnov
 */
@DTO
public interface IssueDto extends Issue {
    @Override
    Status getStatus();

    void setStatus(Status status);

    IssueDto withStatus(Status status);

    @Override
    String getMessage();

    void setMessage(String message);

    IssueDto withMessage(String message);
}
