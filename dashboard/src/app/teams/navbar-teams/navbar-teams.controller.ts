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
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';

/**
 * @ngdoc controller
 * @name teams.navbar.controller:NavbarTeamsController
 * @description This class is handling the controller for the teams section in navbar
 * @author Ann Shumilova
 */
export class NavbarTeamsController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam) {
    this.codenvyTeam = codenvyTeam;
    this.fetchTeams();
  }

  /**
   * Fetch the list of available teams.
   */
  fetchTeams(): void {
    this.codenvyTeam.fetchTeams();
  }

  getTeamDisplayName(team: any): string {
    return this.codenvyTeam.getTeamDisplayName(team);
  }

  /**
   * Get the list of available teams.
   *
   * @returns {Array<any>} teams array
   */
  getTeams(): Array<any> {
    return this.codenvyTeam.getTeams();
  }

  /**
   * Returns personal account of current user.
   *
   * @returns {any} personal account
   */
  getPersonalAccount(): any {
    return this.codenvyTeam.getPersonalAccount();
  }
}
