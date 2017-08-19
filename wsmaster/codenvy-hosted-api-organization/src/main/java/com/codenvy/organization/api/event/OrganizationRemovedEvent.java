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
package com.codenvy.organization.api.event;

import static com.codenvy.organization.shared.event.EventType.ORGANIZATION_REMOVED;

import com.codenvy.organization.shared.event.EventType;
import com.codenvy.organization.shared.event.OrganizationEvent;
import com.codenvy.organization.shared.model.Member;
import com.codenvy.organization.shared.model.Organization;
import java.util.List;

/**
 * Defines organization removed event.
 *
 * @author Anton Korneta
 */
public class OrganizationRemovedEvent implements OrganizationEvent {

  private final String initiator;
  private final Organization organization;
  private final List<? extends Member> members;

  public OrganizationRemovedEvent(
      String initiator, Organization organization, List<? extends Member> members) {
    this.initiator = initiator;
    this.organization = organization;
    this.members = members;
  }

  @Override
  public EventType getType() {
    return ORGANIZATION_REMOVED;
  }

  @Override
  public Organization getOrganization() {
    return organization;
  }

  public List<? extends Member> getMembers() {
    return members;
  }

  /** Returns name of user who initiated organization removal */
  public String getInitiator() {
    return initiator;
  }
}
