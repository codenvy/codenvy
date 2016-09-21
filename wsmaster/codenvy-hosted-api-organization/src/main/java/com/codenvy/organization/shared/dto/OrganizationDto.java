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
package com.codenvy.organization.shared.dto;

import com.codenvy.organization.shared.model.Organization;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface OrganizationDto extends Organization, Hyperlinks {
    @Override
    String getId();

    void setId(String id);

    OrganizationDto withId(String id);

    @Override
    String getName();

    void setName(String name);

    OrganizationDto withName(String name);

    @Override
    String getParent();

    void setParent(String parent);

    OrganizationDto withParent(String parent);

    @Override
    OrganizationDto withLinks(List<Link> links);
}
