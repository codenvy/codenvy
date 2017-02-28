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
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyTeamEventsManager} from '../../../components/api/codenvy-team-events-manager.factory';

/**
 * @ngdoc controller
 * @name list.teams:ListTeamsController
 * @description This class is handling the controller for the list of teams
 * @author Ann Shumilova
 */
export class ListTeamsController {

  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Team events manager.
   */
  private codenvyTeamEventsManager: CodenvyTeamEventsManager;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Team resources API interaction.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
   /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * List of teams.
   */
  private teams: Array<any>;
  /**
   * Map of team members.
   */
  private teamMembers: Map<string, number>;
  /**
   * Map of team resources.
   */
  private teamResources: Map<string, any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Selected status of teams in the list.
   */
  private teamsSelectedStatus: any;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * No selected workspace state.
   */
  private isNoSelected: boolean;
  /**
   * All selected workspace state.
   */
  private isAllSelected: boolean;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyPermissions: CodenvyPermissions, codenvyResourcesDistribution: CodenvyResourcesDistribution,
              cheNotification: any, codenvyTeamEventsManager: CodenvyTeamEventsManager, confirmDialogService: any, $scope: ng.IScope,
              $q: ng.IQService, $location: ng.ILocationService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyTeamEventsManager = codenvyTeamEventsManager;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;

    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
    this.$q = $q;
    this.$location = $location;

    this.teams = [];
    this.isLoading = true;

    this.teamsSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;
    this.fetchTeams();


    let refreshHandler = () => {
      this.fetchTeams();
    };

    this.codenvyTeamEventsManager.addDeleteHandler(refreshHandler);
    this.codenvyTeamEventsManager.addRenameHandler(refreshHandler);

    $scope.$on('$destroy', () => {
      this.codenvyTeamEventsManager.removeRenameHandler(refreshHandler);
      this.codenvyTeamEventsManager.removeDeleteHandler(refreshHandler);
    });
  }

  /**
   * Fetches the list of teams.
   */
  fetchTeams(): void {
    this.isLoading = true;
    this.codenvyTeam.fetchTeams().then(() => {
      this.isLoading = false;
      this.processTeams();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processTeams();
      } else {
        let message = error.data && error.data.message ? error.data.message : 'Failed to retrieve teams.';
        this.cheNotification.showError(message);
      }
    });
  }

  /**
   * Process team - retrieving additional data.
   */
  processTeams(): void {
    this.teams = this.codenvyTeam.getTeams();
    this.teamMembers = new Map();
    this.teamResources = new Map();

    let promises = [];
    this.teams.forEach((team: any) => {
      let promiseMembers = this.codenvyPermissions.fetchTeamPermissions(team.id).then(() => {
        this.teamMembers.set(team.id, this.codenvyPermissions.getTeamPermissions(team.id).length);
      }, (error: any) => {
        if (error.status === 304) {
          this.teamMembers.set(team.id, this.codenvyPermissions.getTeamPermissions(team.id).length);
        }
      });
      promises.push(promiseMembers);

      let promiseResource = this.codenvyResourcesDistribution.fetchTeamResources(team.id).then(() => {
        this.processResource(team.id);
      }, (error: any) => {
        if (error.status === 304) {
          this.processResource(team.id);
        }
      });

      promises.push(promiseResource);
    });

    this.$q.all(promises).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Process team resources.
   *
   * @param teamId team's id
   */
  processResource(teamId: string): void {
    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(teamId, CodenvyResourceLimits.RAM);
    this.teamResources.set(teamId, ramLimit ? ramLimit.amount : undefined);
  }

  /**
   * Returns the number of team's members.
   *
   * @param teamId team's id
   * @returns {any} number of team members to display
   */
  getMembersCount(teamId: string): any {
    if (this.teamMembers && this.teamMembers.size > 0) {
      return this.teamMembers.get(teamId) || '-';
    }
    return '-';
  }

  /**
   * Returns the RAM limit value.
   *
   * @param teamId team's id
   * @returns {any}
   */
  getRamCap(teamId: string): any {
    if (this.teamResources && this.teamResources.size > 0) {
      let ram = this.teamResources.get(teamId);
      return ram ? (ram / 1024) : null;
    }
    return null;
  }

  /**
   * Returns <code>true</code> if all teams in list are checked.
   *
   * @returns {boolean}
   */
  isAllTeamsSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all teams in list are not checked.
   *
   * @returns {boolean}
   */
  isNoTeamsSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all teams in list selected.
   */
  selectAllTeams(): void {
    this.teams.forEach((team: any) => {
      this.teamsSelectedStatus[team.id] = true;
    });
  }

  /**
   *  Make all teams in list deselected.
   */
  deselectAllTeams(): void {
    this.teams.forEach((team: any) => {
      this.teamsSelectedStatus[team.id] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllTeams();
      this.isBulkChecked = false;
    } else {
      this.selectAllTeams();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update teams selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.teamsSelectedStatus).forEach((key: string) => {
      if (this.teamsSelectedStatus[key]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Redirects to new team creation page.
   */
  createNewTeam(): void {
    this.$location.path('/team/create');
  }

  /**
   * Delete all selected teams.
   */
  removeTeams(): void {
    let teamsSelectedStatusKeys = Object.keys(this.teamsSelectedStatus);
    let checkedTeamsKeys = [];

    if (!teamsSelectedStatusKeys.length) {
      this.cheNotification.showError('No such team.');
      return;
    }

    teamsSelectedStatusKeys.forEach((key) => {
      if (this.teamsSelectedStatus[key] === true) {
        checkedTeamsKeys.push(key);
      }
    });

    if (!checkedTeamsKeys.length) {
      this.cheNotification.showError('No such team.');
      return;
    }

    let confirmationPromise = this.showDeleteTeamsConfirmation(checkedTeamsKeys.length);
    confirmationPromise.then(() => {
      this.isLoading = true;
      let promises = [];

      checkedTeamsKeys.forEach((teamId: string) => {
        this.teamsSelectedStatus[teamId] = false;

        let promise = this.codenvyTeam.deleteTeam(teamId).then(() => {
          //
        }, (error: any) => {
          this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete team ' + teamId + '.');
        });

        promises.push(promise);
      });

      this.$q.all(promises).finally(() => {
        this.fetchTeams();
        this.updateSelectedStatus();
      });
    });
  }

  /**
   * Show confirmation popup before teams deletion.
   *
   * @param numberToDelete number of teams to be deleted
   * @returns {*}
   */
  showDeleteTeamsConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' teams?';
    } else {
      content += 'this selected team?';
    }

    return this.confirmDialogService.showConfirmDialog('Delete teams', content, 'Delete');
  }
}
