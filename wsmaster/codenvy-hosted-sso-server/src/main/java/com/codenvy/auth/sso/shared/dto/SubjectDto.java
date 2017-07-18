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
package com.codenvy.auth.sso.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface SubjectDto {
    String getName();

    void setName(String name);

    SubjectDto withName(String name);

    String getId();

    void setId(String id);

    SubjectDto withId(String id);

    String getToken();

    void setToken(String token);

    SubjectDto withToken(String token);
}
