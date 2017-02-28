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
 * @ngdoc directive
 * @name teams.members:ListTeamMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-team-members team="ctrl.team"></list-team-members>` for displaying list of members
 *
 * @usage
 *   <list-team-members team="ctrl.team"></list-team-members>
 *
 * @author Ann Shumilova
 */
export class ListTeamMembers implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-members/list-team-members.html';

  controller: string = 'ListTeamMembersController';
  controllerAs: string = 'listTeamMembersController';
  bindToController: boolean = true;

  scope: any = {
    team: '=',
    owner: '=',
    editable: '='
  };

  constructor () {
  }
}
