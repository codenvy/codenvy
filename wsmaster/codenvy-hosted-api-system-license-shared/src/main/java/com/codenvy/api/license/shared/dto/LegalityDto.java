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

import com.codenvy.api.license.shared.model.Legality;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Dmytro Nochevnov
 */
@DTO
public interface LegalityDto extends Legality {

    @Override
    boolean getIsLegal();

    void setIsLegal(boolean isLegal);

    LegalityDto withIsLegal(boolean isLegal);

    @Override
    List<IssueDto> getIssues();

    void setIssues(List<IssueDto> issues);

    LegalityDto withIssues(List<IssueDto> issues);
}
