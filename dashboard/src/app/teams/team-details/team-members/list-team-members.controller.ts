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
import {CodenvyInvite} from '../../../../components/api/codenvy-invite.factory';

/**
 * @ngdoc controller
 * @name teams.members:ListTeamMembersController
 * @description This class is handling the controller for the list of team's members.
 * @author Ann Shumilova
 */
export class ListTeamMembersController {
  /**
   * Location service.
   */
  $location: ng.ILocationService;

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
   * The editable (whether current user can edit members list and see invitations) state of the members (comes from outside).
   */
  private editable: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyTeam: CodenvyTeam, codenvyPermissions: CodenvyPermissions, codenvyInvite: CodenvyInvite, cheUser: any, cheProfile: any,
              confirmDialogService: any, $mdDialog: angular.material.IDialogService, $q: ng.IQService, cheNotification: any,
              lodash: _.LoDashStatic, $location: ng.ILocationService, teamDetailsService: TeamDetailsService) {
    this.codenvyTeam = codenvyTeam;
    this.codenvyInvite = codenvyInvite;
    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;
    this.cheUser = cheUser;
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

    this.owner = teamDetailsService.getOwner();
    this.team  = teamDetailsService.getTeam();

    this.refreshData(true, true);
  }

  /**
   * Refreshes both list of members and invitations based on provided parameters.
   *
   * @param fetchMembers if <code>true</code> need to refresh members
   * @param fetchInvitations if <code>true</code> need to refresh invitations
   */
  refreshData(fetchMembers: boolean, fetchInvitations: boolean): void {
    this.members = [];
    if (!this.team || !this.owner) {
      return;
    }

    if (fetchMembers) {
      this.fetchMembers();
    } else {
      this.formUserList();
    }

    // can fetch invites only admin or owner of the team:
    if (this.editable) {
      if (fetchInvitations) {
        this.fetchInvitations();
      } else {
        this.formInvitationList();
      }
    }
  }

  /**
   * Fetches the list of team members.
   */
  fetchMembers(): void {
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.isLoading = false;
      this.formUserList();
    }, (error: any) => {
      this.isLoading = false;
      if (error.status !== 304) {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve team permissions.');
      } else {
        this.formUserList();
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formUserList(): void {
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);

    let noOwnerPermissions = true;

    permissions.forEach((permission: any) => {
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
   * Fetches the list of team's invitations.
   */
  fetchInvitations(): void {
    this.codenvyInvite.fetchTeamInvitations(this.team.id).then((data: any) => {
      this.isLoading = false;
      this.formInvitationList();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve team invitations.');
    });
  }

  /**
   * Prepares invitations list to be displayed.
   */
  formInvitationList(): void {
    let invites = this.codenvyInvite.getTeamInvitations(this.team.id);

    invites.forEach((invite: any) => {
      let user = {userId: invite.email, name: 'Pending invitation', email: invite.email, permissions: invite, isPending: true};
      this.members.push(user);
    });
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

    Object.keys(this.membersSelectedStatus).forEach((key: string) => {
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
    let isInvite = false;
    let isAddMember = false;

    members.forEach((member: any) => {
      let actions = this.codenvyTeam.getActionsFromRoles(roles);
      if (member.id) {
        isAddMember = true;
        let permissions = {
          instanceId: this.team.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };
        let promise = this.codenvyPermissions.storePermissions(permissions);
        promises.push(promise);
      } else {
        isInvite = true;
        let promise = this.codenvyInvite.inviteToTeam(this.team.id, member.email, actions);
        promises.push(promise);
        unregistered.push(member.email);
      }
    });

    this.isLoading = true;
    this.$q.all(promises).then(() => {
      this.refreshData(isAddMember, isInvite);
    }).finally(() => {
      this.isLoading = false;
      if (unregistered.length > 0) {
        this.cheNotification.showInfo('User' + (unregistered.length > 1 ? 's ' : ' ') + unregistered.join(', ')
          + (unregistered.length > 1 ? ' are' : ' is') + ' not registered in the system. The email invitations were sent.');
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
    if (member.isPending) {
      if (member.permissions.actions.length > 0) {
        this.updateInvitation(member.permissions);
      } else {
        this.deleteInvitation(member);
      }
    } else {
      if (member.permissions.actions.length > 0) {
        this.storePermissions(member.permissions);
      } else {
        this.removePermissions(member);
      }
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
      this.refreshData(true, false);
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Set user permissions failed.');
    });
  }

  /**
   * Updates the team's invitaion.
   *
   * @param member member's invitation to be updated
   */
  updateInvitation(member: any): void {
    this.codenvyInvite.inviteToTeam(this.team.id, member.email, member.actions);
  }

  /**
   * Deletes send invitation to the team.
   *
   * @param member member to delete invitation
   */
  deleteInvitation(member: any): void {
    this.isLoading = true;
    this.codenvyInvite.deleteTeamInvitation(this.team.id, member.email).then(() => {
      this.refreshData(false, true);
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove invite send to ' + member.email + '.');
    });
  }

  /**
   * Remove all selected members.
   */
  removeSelectedMembers(): void {
    let membersSelectedStatusKeys = Object.keys(this.membersSelectedStatus);
    let checkedKeys = [];

    if (!membersSelectedStatusKeys.length) {
      this.cheNotification.showError('No such members.');
      return;
    }

    membersSelectedStatusKeys.forEach((key: string) => {
      if (this.membersSelectedStatus[key] === true) {
        checkedKeys.push(key);
      }
    });

    if (!checkedKeys.length) {
      this.cheNotification.showError('No such members.');
      return;
    }

    let confirmationPromise = this.showDeleteMembersConfirmation(checkedKeys.length);
    confirmationPromise.then(() => {
      let removalError;
      let removeMembersPromises = [];
      let currentUserPromise;
      let deleteInvite = false;
      let deleteMember = false;

      for (let i = 0; i < checkedKeys.length; i++) {
        let id = checkedKeys[i];
        this.membersSelectedStatus[id] = false;
        let member = this.getMemberById(id);
        if (member && member.isPending) {
          deleteInvite = true;
          let promise = this.codenvyInvite.deleteTeamInvitation(this.team.id, member.email);
          removeMembersPromises.push(promise);
          continue;
        }

        deleteMember = true;
        if (id === this.cheUser.getUser().id) {
          currentUserPromise = this.codenvyPermissions.removeTeamPermissions(this.team.id, id);
          continue;
        }

        let promise = this.codenvyPermissions.removeTeamPermissions(this.team.id, id).then(() => {
            ng.noop();
          },
          (error: any) => {
            removalError = error;
        });
        removeMembersPromises.push(promise);
      }

      if (currentUserPromise) {
        removeMembersPromises.push(currentUserPromise);
      }

      this.$q.all(removeMembersPromises).finally(() => {
        if (currentUserPromise) {
          this.processCurrentUserRemoval();
        } else {
          this.refreshData(deleteMember, deleteInvite);
        }

        this.updateSelectedStatus();
        if (removalError) {
          this.cheNotification.showError(removalError.data && removalError.data.message ? removalError.data.message : 'User removal failed.');
        }
      });
    });
  }

  /**
   * Finds member by it's id.
   *
   * @param id
   * @returns {any}
   */
  getMemberById(id: string): any {
    return this.lodash.find(this.members, (member: any) => {
      return member.userId === id;
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
      if (user.userId === this.cheUser.getUser().id) {
        this.processCurrentUserRemoval();
      } else {
        this.refreshData(true, false);
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
