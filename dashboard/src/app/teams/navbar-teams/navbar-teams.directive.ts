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
 * @name teams.directive:NavbarTeams
 * @description This class is handling the directive of for listing teams in navbar.
 * @author Ann Shumilova
 */
export class NavbarTeams implements ng.IDirective {

  restrict: string = 'E';
  templateUrl: string = 'app/teams/navbar-teams/navbar-teams.html';

  controller: string = 'NavbarTeamsController';
  controllerAs: string = 'navbarTeamsController';
  bindToController: boolean = true;

  constructor() {
  }
}
