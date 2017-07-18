/*
 * Copyright (c) [2015] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';
import {CodenvyOrganizationActions} from './codenvy-organization-actions';

/**
 * This is class of member's roles in organization.
 *
 * @author Oleksii Orel
 */
export class CodenvyOrganizationRoles {

  static get MEMBER(): codenvy.IRole {
    return {
      'name': 'MEMBER',
      'title': 'Member',
      'description': 'Can create workspaces in organization and use resources.',
      'actions': [CodenvyOrganizationActions.CREATE_WORKSPACES]
    };
  }

  static get ADMIN(): codenvy.IRole {
    return {
      'name': 'ADMIN',
      'title': 'Admin',
      'description': 'Can edit the organization’s settings, manage members and sub-organizations.',
      'actions': [
        CodenvyOrganizationActions.UPDATE,
        CodenvyOrganizationActions.SET_PERMISSIONS,
        CodenvyOrganizationActions.MANAGE_RESOURCES,
        CodenvyOrganizationActions.MANAGE_WORKSPACES,
        CodenvyOrganizationActions.CREATE_WORKSPACES,
        CodenvyOrganizationActions.DELETE,
        CodenvyOrganizationActions.MANAGE_SUB_ORGANIZATION]
    };
  }

  static getRoles(): Array<string> {
    return [
      CodenvyOrganizationRoles.MEMBER.name,
      CodenvyOrganizationRoles.ADMIN.name
    ];
  }

  static getValues(): Array<codenvy.IRole> {
    return [
      CodenvyOrganizationRoles.MEMBER,
      CodenvyOrganizationRoles.ADMIN
    ];
  }

}
