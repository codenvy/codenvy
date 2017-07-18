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
 * This is enum of team roles.
 *
 * @author Ann Shumilova
 */
export enum CodenvyTeamRoles {
  TEAM_MEMBER = <any> {
    'title': 'Team Developer',
    'description': 'Can create and use own workspaces.',
    'actions': [CodenvyOrganizationActions.CREATE_WORKSPACES]
  },
  TEAM_ADMIN = <any> {
    'title': 'Team Admin', 'description': 'Can edit the team’s settings, manage workspaces and members.',
    'actions': [
      CodenvyOrganizationActions.UPDATE,
      CodenvyOrganizationActions.SET_PERMISSIONS,
      CodenvyOrganizationActions.MANAGE_RESOURCES,
      CodenvyOrganizationActions.MANAGE_WORKSPACES,
      CodenvyOrganizationActions.CREATE_WORKSPACES,
      CodenvyOrganizationActions.DELETE,
      CodenvyOrganizationActions.MANAGE_SUB_ORGANIZATION]
  }
}

export namespace CodenvyTeamRoles {
  export function getValues(): any[] {
    return [CodenvyTeamRoles.TEAM_MEMBER, CodenvyTeamRoles.TEAM_ADMIN];
  }
}
