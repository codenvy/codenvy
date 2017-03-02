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
 * @name teams.owners:ListTeamMembers
 * @restrict E
 * @element
 *
 * @description
 * `<list-team-owners team="ctrl.team"></list-team-owners>` for displaying list of owners
 *
 * @usage
 *   <list-team-owners team="ctrl.team"></list-team-owners>
 *
 * @author Ann Shumilova
 */
export class ListTeamOwners implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/team-details/team-owners/list-team-owners.html';

  controller: string = 'ListTeamOwnersController';
  controllerAs: string = 'listTeamOwnersController';
  bindToController: boolean = true;

  scope: any = {
    owner: '='
  };

  constructor () {
  }
}
