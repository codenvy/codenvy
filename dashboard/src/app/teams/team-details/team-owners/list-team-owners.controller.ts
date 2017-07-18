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
import {CodenvyTeam} from '../../../../components/api/codenvy-team.factory';
import {CodenvyPermissions} from '../../../../components/api/codenvy-permissions.factory';
import {TeamDetailsService} from '../team-details.service';

/**
 * @ngdoc controller
 * @name teams.members:ListTeamOwnersController
 * @description This class is handling the controller for the list of team's owners.
 * @author Ann Shumilova
 */
export class ListTeamOwnersController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * User profile API interaction.
   */
  private cheProfile: any;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Team's owners string.
   */
  private owners: string;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Current team's owner (comes from directive's scope).
   */
  private owner: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, cheUser: any, codenvyPermissions: CodenvyPermissions, cheProfile: any, cheNotification: any,
              lodash: any, teamDetailsService: TeamDetailsService) {
    this.codenvyTeam = codenvyTeam;
    this.cheUser = cheUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

    this.isLoading = true;
    this.owner = teamDetailsService.getOwner();
    this.processOwner();
  }

  /**
   * Process owner.
   */
  processOwner(): void {
    if (!this.owner) {
      return;
    }
    let profile = this.cheProfile.getProfileById(this.owner.id);
    if (profile) {
      this.formUserItem(profile);
    } else {
      this.cheProfile.fetchProfileById(this.owner.id).then(() => {
        this.formUserItem(this.cheProfile.getProfileById(this.owner.id));
      });
    }
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user: any): void {
    let name = this.cheProfile.getFullName(user.attributes) + ' (' + user.email + ')';
    this.owners = name;
  }
}
