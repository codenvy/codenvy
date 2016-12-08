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
package com.codenvy.resource.shared.dto;

import com.codenvy.resource.model.ProvidedResources;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface ProvidedResourcesDto extends ProvidedResources {
    @Override
    String getProviderId();

    void setProviderId(String providerId);

    ProvidedResourcesDto withProviderId(String providerId);

    @Override
    String getId();

    void setId(String id);

    ProvidedResourcesDto withId(String id);

    @Override
    String getOwner();

    void setOwner(String owner);

    ProvidedResourcesDto withOwner(String owner);

    @Override
    Long getStartTime();

    void setStartTime(Long startTime);

    ProvidedResourcesDto withStartTime(Long startTime);

    @Override
    Long getEndTime();

    void setEndTime(Long endTime);

    ProvidedResourcesDto withEndTime(Long endTime);

    @Override
    List<ResourceDto> getResources();

    void setResources(List<ResourceDto> resources);

    ProvidedResourcesDto withResources(List<ResourceDto> resources);
}
