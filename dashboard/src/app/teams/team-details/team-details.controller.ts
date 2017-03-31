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
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyTeamEventsManager} from '../../../components/api/codenvy-team-events-manager.factory';
import {TeamDetailsService} from './team-details.service';

enum Tab {Settings, Members, Workspaces}

/**
 * Controller for a managing team details.
 *
 * @author Ann Shumilova
 */
export class TeamDetailsController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Team events manager.
   */
  private codenvyTeamEventsManager: CodenvyTeamEventsManager;
  /**
   * Team resources API interaction.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Current team's name. Comes from route path params.
   */
  private teamName: string;
  /**
   * Current team's data.
   */
  private team: any;
  /**
   * Current team's owner.
   */
  private owner: any;
  /**
   * The list of allowed user actions.
   */
  private allowedUserActions: Array<string>;
  /**
   * New team's name (for renaming widget).
   */
  private newName: string;
  /**
   * Index of the selected tab.
   */
  private selectedTabIndex: number;
  /**
   * Team limits.
   */
  private limits: any;
  /**
   * Copy of limits before letting to modify, to be able to compare.
   */
  private limitsCopy: any;
  /**
   * Page loading state.
   */
  private isLoading: boolean;

  private teamForm: ng.IFormController;

  private hasTeamAccess: boolean;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyResourcesDistribution: CodenvyResourcesDistribution, codenvyPermissions: CodenvyPermissions,
              cheUser: any, $route: ng.route.IRouteService, $location: ng.ILocationService, $rootScope: che.IRootScopeService,
              $scope: ng.IScope, confirmDialogService: any, codenvyTeamEventsManager: CodenvyTeamEventsManager, cheNotification: any,
              lodash: any, teamDetailsService: TeamDetailsService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyTeamEventsManager = codenvyTeamEventsManager;
    this.cheUser = cheUser;
    this.teamName = $route.current.params.teamName;
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.cheNotification = cheNotification;
    this.lodash = lodash;

    $rootScope.showIDE = false;

    this.allowedUserActions = [];

    let page = $route.current.params.page;
    if (!page) {
      $location.path('/team/' + this.teamName);
    } else {
      this.selectedTabIndex = Tab.Settings;
      switch (page) {
        case 'settings':
          this.selectedTabIndex = Tab.Settings;
          break;
        case 'developers':
          this.selectedTabIndex = Tab.Members;
          break;
        case 'workspaces':
          this.selectedTabIndex = Tab.Workspaces;
          break;
        default:
          $location.path('/team/' + this.teamName);
          break;
      }
    }

    let deleteHandler = (info: any) => {
      if (this.team && (this.team.id === info.organization.id)) {
        this.$location.path('/workspaces');
      }
    };
    this.codenvyTeamEventsManager.addDeleteHandler(deleteHandler);

    let renameHandler = (info: any) => {
      if (this.team && (this.team.id === info.organization.id)) {
        this.$location.path('/team/' + info.organization.qualifiedName);
      }
    };
    this.codenvyTeamEventsManager.addRenameHandler(renameHandler);

    $scope.$on('$destroy', () => {
      this.codenvyTeamEventsManager.removeRenameHandler(renameHandler);
      this.codenvyTeamEventsManager.removeDeleteHandler(deleteHandler);
    });

    this.isLoading = true;
    this.hasTeamAccess = true;

    this.team = teamDetailsService.getTeam();
    this.owner = teamDetailsService.getOwner();

    if (this.team) {
      this.newName = angular.copy(this.team.name);
      if (this.owner) {
        this.fetchUserPermissions();
      } else {
        teamDetailsService.fetchOwnerByTeamName(this.teamName).then((owner: any) => {
          this.owner = owner;
        }, (error: any) => {
          this.isLoading = false;
          cheNotification.showError(error && error.data && error.data.message !== null ? error.data.message : 'Failed to find team owner.');
        }).finally(() => {
          this.fetchUserPermissions();
        });
      }
    }
  }

  /**
   * Fetches permission of user in current team.
   */
  fetchUserPermissions(): void {
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.allowedUserActions = this.processUserPermissions();
      this.fetchLimits();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.allowedUserActions = this.processUserPermissions();
        this.fetchLimits();
      } else if (error.status === 403) {
        this.allowedUserActions = [];
        this.hasTeamAccess = false;
      }
      this.isLoading = false;
    });
  }

  /**
   * Process permissions to retrieve current user actions.
   *
   * @returns {Array} current user allowed actions
   */
  processUserPermissions(): Array<string> {
    let userId = this.cheUser.getUser().id;
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);
    let userPermissions = this.lodash.find(permissions, (permission: any) => {
      return permission.userId === userId;
    });
    return userPermissions ? userPermissions.actions : [];
  }

  /**
   * Checks whether user is allowed to perform pointed action.
   *
   * @param value action
   * @returns {boolean} <code>true</code> if allowed
   */
  isUserAllowedTo(value: string): boolean {
    return this.allowedUserActions ? this.allowedUserActions.indexOf(value) >= 0 : false;
  }

  /**
   * Returns whether current user can change team resource limits.
   *
   * @returns {boolean} <code>true</code> if can change resource limits
   */
  canChangeResourceLimits(): boolean {
    return (this.codenvyTeam.getPersonalAccount() && this.team) ? this.codenvyTeam.getPersonalAccount().id === this.team.parent : false;
  }

  /**
   * Returns whether current user can leave team (owner of the team is not allowed to leave it).
   *
   * @returns {boolean} <code>true</code> if can leave team
   */
  canLeaveTeam(): boolean {
    return (this.codenvyTeam.getPersonalAccount() && this.team) ? this.codenvyTeam.getPersonalAccount().id !== this.team.parent : false;
  }

  /**
   * Fetches defined team's limits (workspace, runtime, RAM caps, etc).
   */
  fetchLimits(): void {
    this.isLoading = true;
    this.codenvyResourcesDistribution.fetchTeamResources(this.team.id).then(() => {
      this.isLoading = false;
      this.processResources();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processResources();
      } else if (error.status === 404) {
        this.limits = {};
        this.limitsCopy = angular.copy(this.limits);
      }
    });
  }

  /**
   * Process resources limits.
   */
  processResources(): void {
    let ramLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RAM);
    let workspaceLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.WORKSPACE);
    let runtimeLimit = this.codenvyResourcesDistribution.getTeamResourceByType(this.team.id, CodenvyResourceLimits.RUNTIME);

    this.limits = {};
    this.limits.workspaceCap = workspaceLimit ? workspaceLimit.amount : undefined;
    this.limits.runtimeCap = runtimeLimit ? runtimeLimit.amount : undefined;
    this.limits.ramCap = ramLimit ? ramLimit.amount / 1024 : undefined;
    this.limitsCopy = angular.copy(this.limits);
  }

  /**
   * Confirms and performs team's deletion.
   *
   * @param event
   */
  deleteTeam(event: MouseEvent): void {
    let promise = this.confirmDialogService.showConfirmDialog('Delete team',
      'Would you like to delete team \'' + this.team.name + '\'?', 'Delete');

    promise.then(() => {
      let promise = this.codenvyTeam.deleteTeam(this.team.id);
      promise.then(() => {
        this.$location.path('/');
        this.codenvyTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Team deletion failed.');
      });
    });
  }

  /**
   * Confirms and performs removing user from current team.
   *
   */
  leaveTeam(): void {
    let promise = this.confirmDialogService.showConfirmDialog('Leave team',
      'Would you like to leave team \'' + this.team.name + '\'?', 'Leave');

    promise.then(() => {
      let promise = this.codenvyPermissions.removeTeamPermissions(this.team.id, this.cheUser.getUser().id);
      promise.then(() => {
        this.$location.path('/');
        this.codenvyTeam.fetchTeams();
      }, (error: any) => {
        this.cheNotification.showError(error.data.message !== null ? error.data.message : 'Leave team failed.');
      });
    });
  }

  /**
   * Update team's details.
   *
   */
  updateTeamName(): void {
    if (this.newName && this.team && this.newName !== this.team.name) {
      this.team.name = this.newName;
      this.codenvyTeam.updateTeam(this.team).then((team: any) => {
        this.codenvyTeam.fetchTeams().then(() => {
          this.$location.path('/team/' + team.qualifiedName);
        });
      }, (error: any) => {
        this.cheNotification.showError((error.data && error.data.message !== null) ? error.data.message : 'Rename team failed.');
      });
    }
  }

  /**
   * Update resource limits.
   *
   */
  updateLimits(): void {
    if (!this.team || !this.limits || angular.equals(this.limits, this.limitsCopy)) {
      return;
    }

    let resources = this.codenvyResourcesDistribution.getTeamResources(this.team.id);

    resources = angular.copy(resources);

    let resourcesToRemove = [CodenvyResourceLimits.TIMEOUT];

    if (this.limits.ramCap !== null && this.limits.ramCap !== undefined) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RAM, (this.limits.ramCap * 1024));
    } else {
      resourcesToRemove.push(CodenvyResourceLimits.RAM);
    }

    if (this.limits.workspaceCap !== null && this.limits.workspaceCap !== undefined) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.WORKSPACE, this.limits.workspaceCap);
    } else {
      resourcesToRemove.push(CodenvyResourceLimits.WORKSPACE);
    }

    if (this.limits.runtimeCap !== null && this.limits.runtimeCap !== undefined) {
      resources = this.codenvyResourcesDistribution.setTeamResourceLimitByType(resources, CodenvyResourceLimits.RUNTIME, this.limits.runtimeCap);
    } else {
      resourcesToRemove.push(CodenvyResourceLimits.RUNTIME);
    }

    // if the timeout resource will be send in this case - it will set the timeout for the current team, and the updating timeout of
    // parent team will not affect the current team, so to avoid this - remove timeout resource if present:
    this.lodash.remove(resources, (resource: any) => {
      return resourcesToRemove.indexOf(resource.type) >= 0;
    });


    this.isLoading = true;
    this.codenvyResourcesDistribution.distributeResources(this.team.id, resources).then(() => {
      this.fetchLimits();
    }, (error: any) => {
      let errorMessage = 'Failed to set update team CAPs.';
      this.cheNotification.showError((error.data && error.data.message !== null) ? errorMessage + '</br>Reason: ' + error.data.message : errorMessage);
      this.fetchLimits();
    });
  }

  /**
   * Returns whether save button is disabled.
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return this.teamForm && this.teamForm.$invalid;
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return (this.selectedTabIndex === Tab.Settings && !this.isLoading) && (!angular.equals(this.team.name, this.newName) ||
      !angular.equals(this.limits, this.limitsCopy));
  }

  updateTeam(): void {
    this.updateTeamName();
    this.updateLimits();
  }

  cancelChanges(): void {
    this.newName = angular.copy(this.team.name);
    this.limits = angular.copy(this.limitsCopy);
  }
}
