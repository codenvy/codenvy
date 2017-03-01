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
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyUser} from '../../../components/api/codenvy-user.factory';

/**
 * @ngdoc controller
 * @name teams.create.controller:CreateTeamController
 * @description This class is handling the controller for the new team creation.
 * @author Ann Shumilova
 */
export class CreateTeamController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * User API interaction.
   */
  private codenvyUser: CodenvyUser;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Current team's name.
   */
  private teamName: string;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * The list of users to invite.
   */
  private members: Array<any>;
  /**
   * Owner'e email.
   */
  private owner: string;
  /**
   * Account name.
   */
  private accountName: string;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyUser: CodenvyUser, codenvyPermissions: CodenvyPermissions, cheNotification: any,
              $location: ng.ILocationService, $q: ng.IQService, lodash: any, $log: ng.ILogService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyUser = codenvyUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$q = $q;
    this.lodash = lodash;
    this.$log = $log;

    this.teamName = '';
    this.isLoading = true;
    this.members = [];

    if (codenvyUser.getUser()) {
      this.owner = codenvyUser.getUser().email;
      this.accountName = codenvyUser.getUser().name;
      this.isLoading = false;
    } else {
      codenvyUser.fetchUser().then(() => {
        this.owner = codenvyUser.getUser().email;
        this.accountName = codenvyUser.getUser().name;
        this.isLoading = false;
      }, (error: any) => {
        if (error.status === 304) {
          this.owner = codenvyUser.getUser().email;
          this.accountName = codenvyUser.getUser().name;
          this.isLoading = false;
        } else {
          this.$log.error('Failed to retrieve current user:', error);
        }
      });
    }
  }

  /**
   * Performs new team creation.
   */
  createTeam(): void {
    this.isLoading = true;
    this.codenvyTeam.createTeam(this.teamName).then((data: any) => {
      this.addPermissions(data, this.members);
      this.codenvyTeam.fetchTeams();
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to create team ' + this.teamName + '.';
      this.cheNotification.showError(message);
    });
  }

  /**
   * Add permissions for members in pointed team.
   *
   * @param team team
   * @param members members to be added to team
   */
  addPermissions(team: any, members: Array<any>) {
    let promises = [];
    members.forEach((member: any) => {
      if (member.id) {
        let actions = this.codenvyTeam.getActionsFromRoles(member.roles);
        let permissions = {
          instanceId: team.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };

        let promise = this.codenvyPermissions.storePermissions(permissions);
        promises.push(promise);
      }
    });

     this.$q.all(promises).then(() => {
       this.isLoading = false;
       this.$location.path('/team/' + team.qualifiedName);
     }, (error: any) => {
       this.isLoading = false;
       let message = error.data && error.data.message ? error.data.message : 'Failed to create team ' + this.teamName + '.';
       this.cheNotification.showError(message);
     });
  }
}
