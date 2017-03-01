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
import {CodenvyTeam} from '../../../../components/api/codenvy-team.factory';
import {CodenvyPermissions} from '../../../../components/api/codenvy-permissions.factory';
import {CodenvyUser} from '../../../../components/api/codenvy-user.factory';

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
  private codenvyUser: CodenvyUser;
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
  constructor(codenvyTeam: CodenvyTeam, codenvyUser: CodenvyUser, codenvyPermissions: CodenvyPermissions, cheProfile: any, cheNotification: any, lodash: any) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyUser = codenvyUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

    this.isLoading = true;
    this.processOwner();
  }

  /**
   * Process owner.
   */
  processOwner(): void {
    let profile = this.cheProfile.getProfileFromId(this.owner.id);
    if (profile) {
      this.formUserItem(profile);
    } else {
      this.cheProfile.fetchProfileId(this.owner.id).then(() => {
        this.formUserItem(this.cheProfile.getProfileFromId(this.owner.id));
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
