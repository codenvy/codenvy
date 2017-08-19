/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.organization.shared.dto;

import com.codenvy.organization.shared.model.OrganizationDistributedResources;
import com.codenvy.resource.shared.dto.ResourceDto;
import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author Sergii Leschenko */
@DTO
public interface OrganizationDistributedResourcesDto extends OrganizationDistributedResources {
  @Override
  String getOrganizationId();

  void setOrganizationId(String organizationId);

  OrganizationDistributedResourcesDto withOrganizationId(String organizationId);

  @Override
  List<ResourceDto> getResourcesCap();

  void setResourcesCap(List<ResourceDto> resourcesCap);

  OrganizationDistributedResourcesDto withResourcesCap(List<ResourceDto> resourcesCap);
}
