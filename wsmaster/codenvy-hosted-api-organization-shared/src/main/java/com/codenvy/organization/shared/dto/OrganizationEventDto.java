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

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.OrganizationEvent;
import org.eclipse.che.api.core.notification.EventOrigin;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO for {@link OrganizationEvent}.
 *
 * @author Anton Korneta
 */
@DTO
@EventOrigin("organization")
public interface OrganizationEventDto extends OrganizationEvent {

  @Override
  OrganizationDto getOrganization();

  void setOrganization(OrganizationDto organization);

  OrganizationEventDto withOrganization(OrganizationDto organization);

  void setType(EventType eventType);

  OrganizationEventDto withType(EventType eventType);
}
