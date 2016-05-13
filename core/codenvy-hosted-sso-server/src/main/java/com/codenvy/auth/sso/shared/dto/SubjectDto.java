/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.auth.sso.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

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

    boolean isTemporary();

    void setTemporary(boolean isTemporary);

    SubjectDto withTemporary(boolean isTemporary);

    List<String> getRoles();

    void setRoles(List<String> roles);

    SubjectDto withRoles(List<String> roles);
}
