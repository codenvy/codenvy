/*
 *  [2015] - [2017] Codenvy, S.A.
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
'use strict';

/**
 * This is enum of team roles.
 *
 * @author Ann Shumilova
 */
export enum CodenvyTeamRoles {
  TEAM_MEMBER = <any> {'title': 'Team Developer', 'description': 'Can create and use own workspaces.', 'actions' : ['createWorkspaces']},
  TEAM_ADMIN = <any> {'title': 'Team Admin', 'description': 'Can edit the team’s settings, manage workspaces and members.',
    'actions' : ['update', 'setPermissions', 'manageResources', 'manageWorkspaces', 'createWorkspaces', 'delete']},
}

export namespace CodenvyTeamRoles {
  export function getValues(): any[] {
    return [CodenvyTeamRoles.TEAM_MEMBER, CodenvyTeamRoles.TEAM_ADMIN];
  }
}
