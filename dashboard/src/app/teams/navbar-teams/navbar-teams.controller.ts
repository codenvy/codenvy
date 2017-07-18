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
