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
import {TeamDetailsService} from '../team-details.service';
/**
 * @ngdoc controller
 * @name teams.workspaces:ListTeamWorkspacesController
 * @description This class is handling the controller for the list of team's workspaces.
 * @author Ann Shumilova
 */
export class ListTeamWorkspacesController {

  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Workspace API interaction.
   */
  private cheWorkspace: any;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;

  private lodash: _.LoDashStatic;
  /**
   * List of team's workspaces
   */
  private workspaces: Array<any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Filter for workspace list.
   */
  private workspaceFilter: any;
  /**
   * Selected status of workspaces in the list.
   */
  private workspacesSelectedStatus: any;
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
   * Current team data (comes from directive's scope).
   */
  private team: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyPermissions: CodenvyPermissions, cheUser: any, cheWorkspace: any,
              cheNotification: any, lodash: _.LoDashStatic, $mdDialog: angular.material.IDialogService, $q: ng.IQService,
              teamDetailsService: TeamDetailsService) {
    this.codenvyTeam = codenvyTeam;
    this.cheWorkspace = cheWorkspace;
    this.cheNotification = cheNotification;
    this.codenvyPermissions = codenvyPermissions;
    this.cheUser = cheUser;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.lodash = lodash;

    this.workspaces = [];
    this.isLoading = true;

    this.workspaceFilter = {config: {name: ''}};
    this.workspacesSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;

    this.team = teamDetailsService.getTeam();

    this.fetchPermissions();
  }

  fetchPermissions(): void {
    if (!this.team) {
      return;
    }
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.processPermissions();
    }, (error: any) => {
      if (error.status === 304) {
        this.processPermissions();
      } else {
        this.cheNotification.showError('Failed to access workspaces of the ' + this.team.name + ' team.');
      }
    });
  }

  processPermissions(): void {
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);
    let currentUserPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === this.cheUser.getUser().id;
    });

    if (currentUserPermissions && currentUserPermissions.actions.indexOf('manageWorkspaces') >= 0) {
      this.fetchWorkspacesByNamespace();
    } else {
      this.fetchWorkspaces();
    }
  }

  fetchWorkspacesByNamespace(): void {
    this.cheWorkspace.fetchWorkspacesByNamespace(this.team.qualifiedName).then(() => {
      this.isLoading = false;
      this.workspaces = this.cheWorkspace.getWorkspacesByNamespace(this.team.qualifiedName);
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.workspaces = this.cheWorkspace.getWorkspacesByNamespace(this.team.qualifiedName);
      }
      //TODO
    });
  }

  /**
   * Fetches the list of teams workspaces.
   */
  fetchWorkspaces(): void {
    let promise = this.cheWorkspace.fetchWorkspaces();

    promise.then(() => {
        this.isLoading = false;
        this.workspaces = this.filterWorkspacesByNamespace();
      },
      (error: any) => {
        if (error.status === 304) {
          this.workspaces = this.filterWorkspacesByNamespace();
        }
        this.isLoading = false;
      });
  }

  /**
   * Filter workspaces by namespace.
   *
   * @returns {any}
   */
  filterWorkspacesByNamespace(): Array<any> {
    let workspaces = this.cheWorkspace.getWorkspaces();
    return this.lodash.filter(workspaces, (workspace: any) => {
      return workspace.namespace === this.team.qualifiedName;
    });
  }

  /**
   * Returns <code>true</code> if all workspaces in list are checked.
   *
   * @returns {boolean}
   */
  isAllWorkspacesSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all workspaces in list are not checked.
   *
   * @returns {boolean}
   */
  isNoWorkspacesSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all workspaces in list selected.
   */
  selectAllWorkspaces(): void {
    this.workspaces.forEach((workspace: any) => {
      this.workspacesSelectedStatus[workspace.id] = true;
    });
  }

  /**
   *  Make all workspaces in list deselected.
   */
  deselectAllWorkspaces(): void {
    this.workspaces.forEach((workspace: any) => {
      this.workspacesSelectedStatus[workspace.id] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllWorkspaces();
      this.isBulkChecked = false;
    } else {
      this.selectAllWorkspaces();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update workspace selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.workspacesSelectedStatus).forEach((key: string) => {
      if (this.workspacesSelectedStatus[key]) {
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
   * Delete all selected workspaces.
   */
  deleteSelectedWorkspaces(): void {
    let workspacesSelectedStatusKeys = Object.keys(this.workspacesSelectedStatus);
    let checkedWorkspacesKeys = [];

    if (!workspacesSelectedStatusKeys.length) {
      this.cheNotification.showError('No such workspace.');
      return;
    }

    workspacesSelectedStatusKeys.forEach((key: string) => {
      if (this.workspacesSelectedStatus[key] === true) {
        checkedWorkspacesKeys.push(key);
      }
    });

    let queueLength = checkedWorkspacesKeys.length;
    if (!queueLength) {
      this.cheNotification.showError('No such workspace.');
      return;
    }

    let confirmationPromise = this.showDeleteWorkspacesConfirmation(queueLength);
    confirmationPromise.then(() => {
      let numberToDelete = queueLength;
      let isError = false;
      let deleteWorkspacePromises = [];
      let workspaceName;

      checkedWorkspacesKeys.forEach((workspaceId: string) => {
        this.workspacesSelectedStatus[workspaceId] = false;

        let workspace = this.cheWorkspace.getWorkspaceById(workspaceId);
        workspaceName = workspace.config.name;
        let stoppedStatusPromise = this.cheWorkspace.fetchStatusChange(workspaceId, 'STOPPED');

        // stop workspace if it's status is RUNNING
        if (workspace.status === 'RUNNING') {
          this.cheWorkspace.stopWorkspace(workspaceId);
        }

        // delete stopped workspace
        let promise = stoppedStatusPromise.then(() => {
          return this.cheWorkspace.deleteWorkspaceConfig(workspaceId);
        }).then(() => {
            queueLength--;
          },
          (error: any) => {
            isError = true;
          });
        deleteWorkspacePromises.push(promise);
      });

      this.$q.all(deleteWorkspacePromises).finally(() => {
        this.fetchWorkspaces();
        this.updateSelectedStatus();
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(workspaceName + ' has been removed.');
          } else {
            this.cheNotification.showInfo('Selected workspaces have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before workspaces to delete
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteWorkspacesConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' workspaces?';
    } else {
      confirmTitle += 'this selected workspace?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove workspaces')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }
}
