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
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyInvite} from '../../../components/api/codenvy-invite.factory';

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
   * Invite API interaction.
   */
  private codenvyInvite: CodenvyInvite;
  /**
   * User API interaction.
   */
  private cheUser: any;
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
  constructor(codenvyTeam: CodenvyTeam, codenvyInvite: CodenvyInvite, cheUser: any, codenvyPermissions: CodenvyPermissions, cheNotification: any,
              $location: ng.ILocationService, $q: ng.IQService, lodash: any, $log: ng.ILogService, $rootScope: che.IRootScopeService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyInvite = codenvyInvite;
    this.cheUser = cheUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$q = $q;
    this.lodash = lodash;
    this.$log = $log;

    $rootScope.showIDE = false;

    this.teamName = '';
    this.isLoading = true;
    this.members = [];

    if (cheUser.getUser()) {
      this.owner = cheUser.getUser().email;
      this.accountName = cheUser.getUser().name;
      this.isLoading = false;
    } else {
      cheUser.fetchUser().then(() => {
        this.owner = cheUser.getUser().email;
        this.accountName = cheUser.getUser().name;
        this.isLoading = false;
      }, (error: any) => {
        if (error.status === 304) {
          this.owner = cheUser.getUser().email;
          this.accountName = cheUser.getUser().name;
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
      let actions = this.codenvyTeam.getActionsFromRoles(member.roles);
      if (member.id) {
        let permissions = {
          instanceId: team.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };

        let promise = this.codenvyPermissions.storePermissions(permissions);
        promises.push(promise);
      } else {
        let promise = this.codenvyInvite.inviteToTeam(team.id, member.email, actions);
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
