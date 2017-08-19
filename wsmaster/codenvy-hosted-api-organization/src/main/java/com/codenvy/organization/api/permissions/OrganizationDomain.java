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
package com.codenvy.organization.api.permissions;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.organization.spi.impl.MemberImpl;
import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * Domain for storing organizations' permissions
 *
 * @author Sergii Leschenko
 */
public class OrganizationDomain extends AbstractPermissionsDomain<MemberImpl> {
  public static final String DOMAIN_ID = "organization";

  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  public static final String MANAGE_SUBORGANIZATIONS = "manageSuborganizations";
  public static final String MANAGE_RESOURCES = "manageResources";
  public static final String CREATE_WORKSPACES = "createWorkspaces";
  public static final String MANAGE_WORKSPACES = "manageWorkspaces";

  private static final List<String> ACTIONS =
      ImmutableList.of(
          SET_PERMISSIONS,
          UPDATE,
          DELETE,
          MANAGE_SUBORGANIZATIONS,
          MANAGE_RESOURCES,
          CREATE_WORKSPACES,
          MANAGE_WORKSPACES);

  /** Returns all the available actions for {@link OrganizationDomain}. */
  public static List<String> getActions() {
    return ACTIONS;
  }

  public OrganizationDomain() {
    super(DOMAIN_ID, ACTIONS);
  }

  @Override
  protected MemberImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new MemberImpl(userId, instanceId, allowedActions);
  }
}
