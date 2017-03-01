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
 * @name teams.members:ListTeamMembersController
 * @description This class is handling the controller for the list of team's members.
 * @author Ann Shumilova
 */
export class ListTeamMembersController {

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
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: any;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Location service.
   */
  $location: ng.ILocationService;
  /**
   * Lodash library.
   */
  private lodash: _.LoDashStatic;
  /**
   * Team's members list.
   */
  private members: Array<any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Filter for members list.
   */
  private memberFilter: any;
  /**
   * Selected status of members in list.
   */
  private membersSelectedStatus: any;
  /**
   * Bulk operation state.
   */
  private isBulkChecked: boolean;
  /**
   * No selected members state.
   */
  private isNoSelected: boolean;
  /**
   * All selected members state.
   */
  private isAllSelected: boolean;
  /**
   * Current team (comes from directive's scope).
   */
  private team: any;
  /**
   * Current team's owner (comes from directive's scope).
   */
  private owner: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyPermissions: CodenvyPermissions, codenvyUser: CodenvyUser, cheProfile: any, confirmDialogService: any,
              $mdDialog: angular.material.IDialogService, $q: ng.IQService, cheNotification: any, lodash: _.LoDashStatic, $location: ng.ILocationService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;
    this.codenvyUser = codenvyUser;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$location = $location;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;

    this.members = [];
    this.isLoading = true;

    this.memberFilter = {name: ''};

    this.membersSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;

    this.fetchMembers();
  }

  /**
   * Fetches the lis of team members.
   */
  fetchMembers(): void {
    this.isLoading = true;
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.isLoading = false;
      this.formUserList();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.formUserList();
      } else {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve team permissions.');
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formUserList(): void {
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);
    this.members = [];

    let noOwnerPermissions = true;

    permissions.forEach((permission) => {
      let userId = permission.userId;
      let user = this.cheProfile.getProfileFromId(userId);

      if (userId === this.owner.id) {
        noOwnerPermissions = false;
      }

      if (user) {
        this.formUserItem(user, permission);
      } else {
        this.cheProfile.fetchProfileId(userId).then(() => {
          this.formUserItem(this.cheProfile.getProfileFromId(userId), permission);
        });
      }
    });

    if (noOwnerPermissions) {
      let user = this.cheProfile.getProfileFromId(this.owner.id);

      if (user) {
        this.formUserItem(user, null);
      } else {
        this.cheProfile.fetchProfileId(this.owner.id).then(() => {
          this.formUserItem(this.cheProfile.getProfileFromId(this.owner.id), null);
        });
      }
    }
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user: any, permissions: any): void {
    user.name = this.cheProfile.getFullName(user.attributes);
    let userItem = angular.copy(user);
    userItem.permissions = permissions;
    this.members.push(userItem);
  }

  /**
   * Return <code>true</code> if all members in list are checked.
   * @returns {boolean}
   */
  isAllMembersSelected(): boolean {
    return this.isAllSelected;
  }

  /**
   * Returns <code>true</code> if all members in list are not checked.
   * @returns {boolean}
   */
  isNoMemberSelected(): boolean {
    return this.isNoSelected;
  }

  /**
   * Make all members in list selected.
   */
  selectAllMembers(): void {
    this.members.forEach((member: any) => {
      if (this.owner.id !== member.userId) {
        this.membersSelectedStatus[member.userId] = true;
      }
    });
  }

  /**
   * Make all members in list deselected.
   */
  deselectAllMembers(): void {
    this.members.forEach((member: any) => {
      this.membersSelectedStatus[member.userId] = false;
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllMembers();
      this.isBulkChecked = false;
    } else {
      this.selectAllMembers();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update members selected status.
   */
  updateSelectedStatus(): void {
    this.isNoSelected = true;
    this.isAllSelected = true;

    Object.keys(this.membersSelectedStatus).forEach((key) => {
      if (this.membersSelectedStatus[key]) {
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
   * Shows dialog for adding new member to the team.
   */
  showMemberDialog(member: any): void {
    this.$mdDialog.show({
      controller: 'MemberDialogController',
      controllerAs: 'memberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: this.members,
        callbackController: this,
        member: member
      },
      templateUrl: 'app/teams/member-dialog/member-dialog.html'
    });
  }

  /**
   * Add new members to the team.
   *
   * @param members members to be added
   * @param roles member roles
   */
  addMembers(members: Array<any>, roles: Array<any>): void {
    let promises = [];
    let unregistered = [];

    members.forEach((member: any) => {
      if (member.id) {
        let actions = this.codenvyTeam.getActionsFromRoles(roles);
        let permissions = {
          instanceId: this.team.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };
        let promise = this.codenvyPermissions.storePermissions(permissions);
        promises.push(promise);
      } else {
        unregistered.push(member.email);
      }
    });

    this.isLoading = true;
    this.$q.all(promises).then(() => {
      this.fetchMembers();
    }).finally(() => {
      this.isLoading = false;
      if (unregistered.length > 0) {
        this.cheNotification.showError('User' + (unregistered.length > 1 ? 's ' : ' ') + unregistered.join(', ') + (unregistered.length > 1 ? ' are' : ' is') + ' not registered in the system.');
      }
    });
  }

  /**
   * Perform edit member permissions.
   *
   * @param member
   */
  editMember(member: any): void {
    this.showMemberDialog(member);
  }

  /**
   * Performs member's permissions update.
   *
   * @param member member to update permissions
   */
  updateMember(member: any): void {
    if (member.permissions.actions.length > 0) {
      this.storePermissions(member.permissions);
    } else {
      this.removePermissions(member);
    }
  }

  /**
   * Stores provided permissions.
   *
   * @param permissions
   */
  storePermissions(permissions: any): void {
    this.isLoading = true;
    this.codenvyPermissions.storePermissions(permissions).then(() => {
      this.fetchMembers();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Set user permissions failed.');
    });
  }

  /**
   * Remove all selected members.
   */
  removeSelectedMembers(): void {
    let membersSelectedStatusKeys = Object.keys(this.membersSelectedStatus);
    let checkedKeys = [];

    if (!membersSelectedStatusKeys.length) {
      this.cheNotification.showError('No such developers.');
      return;
    }

    membersSelectedStatusKeys.forEach((key) => {
      if (this.membersSelectedStatus[key] === true) {
        checkedKeys.push(key);
      }
    });

    if (!checkedKeys.length) {
      this.cheNotification.showError('No such developers.');
      return;
    }

    let confirmationPromise = this.showDeleteMembersConfirmation(checkedKeys.length);
    confirmationPromise.then(() => {
      let removalError;
      let removeMembersPromises = [];
      let currentUserPromise;
      for (let i = 0; i < checkedKeys.length; i++) {
        let id = checkedKeys[i];
        this.membersSelectedStatus[id] = false;
        if (id === this.codenvyUser.getUser().id) {
          currentUserPromise = this.codenvyPermissions.removeTeamPermissions(this.team.id, id);
          continue;
        }
        let promise = this.codenvyPermissions.removeTeamPermissions(this.team.id, id).then(() => {},
          (error: any) => {
            removalError = error;
        });
        removeMembersPromises.push(promise);
      };

      if (currentUserPromise) {
        removeMembersPromises.push(currentUserPromise);
      }

      this.$q.all(removeMembersPromises).finally(() => {
        if (currentUserPromise) {
          this.processCurrentUserRemoval();
        } else {
          this.fetchMembers();
        }

        this.updateSelectedStatus();
        if (removalError) {
          this.cheNotification.showError(removalError.data && removalError.data.message ? removalError.data.message : 'User removal failed.');
        }
      });
    });
  }

  /**
   * Process the removal of current user from team.
   */
  processCurrentUserRemoval(): void {
    this.$location.path('/workspaces');
    this.codenvyTeam.fetchTeams();
  }

  /**
   * Removes user permissions for current team
   *
   * @param user user
   */
  removePermissions(user: any) {
    this.isLoading = true;
    this.codenvyPermissions.removeTeamPermissions(user.permissions.instanceId, user.userId).then(() => {
      if (user.userId === this.codenvyUser.getUser().id) {
        this.processCurrentUserRemoval();
      } else {
        this.fetchMembers();
      }
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove user ' + user.email + ' permissions.');
    });
  }

  /**
   * Show confirmation popup before members removal
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteMembersConfirmation(numberToDelete: number): any {
    let confirmTitle = 'Would you like to remove ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' members?';
    } else {
      confirmTitle += 'the selected member?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove members', confirmTitle, 'Delete');
  }
}
