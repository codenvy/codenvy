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
import {CodenvyPermissions} from '../../../../components/api/codenvy-permissions.factory';
import {CodenvyOrganization} from '../../../../components/api/codenvy-organizations.factory';
import {OrganizationsPermissionService} from '../../organizations-permission.service';
import {CodenvyOrganizationActions} from '../../../../components/api/codenvy-organization-actions';
import {CodenvyOrganizationRoles} from '../../../../components/api/codenvy-organization-roles';

/**
 * @ngdoc controller
 * @name organization.details.members:ListOrganizationMembersController
 * @description This class is handling the controller for the list of organization's members.
 * @author Oleksii Orel
 */
export class ListOrganizationMembersController {
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
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
  private lodash: any;
  /**
   * Organization's members list.
   */
  private members: Array<codenvy.IMember>;
  /**
   * Members list of parent organization (comes from directive's scope)
   */
  private parentOrganizationMembers: Array<che.IUser>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * Filter for members list.
   */
  private memberFilter: any;
  /**
   * Current organization (comes from directive's scope).
   */
  private organization: codenvy.IOrganization;
  /**
   * Organization permission service.
   */
  private organizationsPermissionService: OrganizationsPermissionService;
  /**
   * Has update permission.
   */
  private hasUpdatePermission;
  /**
   * Selection and filtration helper
   */
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyPermissions: CodenvyPermissions, cheUser: any, cheProfile: any, codenvyOrganization: CodenvyOrganization,
              confirmDialogService: any, $mdDialog: angular.material.IDialogService, $q: ng.IQService, cheNotification: any,
              lodash: any, $location: ng.ILocationService, organizationsPermissionService: OrganizationsPermissionService,
              $scope: ng.IScope, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;
    this.cheUser = cheUser;
    this.codenvyOrganization = codenvyOrganization;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.$location = $location;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
    this.organizationsPermissionService = organizationsPermissionService;

    this.members = [];

    this.memberFilter = {name: ''};
    const helperId = 'list-organization-members';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.formUsersList();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter organization members.
   */
  onSearchChanged(str: string): void {
    this.memberFilter.name = str;
    this.cheListHelper.applyFilter('name', this.memberFilter);
  }


  /**
   * Fetches the list of organization members.
   */
  fetchMembers(): void {
    if (!this.organization || !this.organization.id) {
      return;
    }
    this.isLoading = true;
    this.codenvyPermissions.fetchOrganizationPermissions(this.organization.id).then(() => {
      this.formUsersList();
    }, (error: any) => {
      let errorMessage = error && error.data && error.data.message ? error.data.message : 'Failed to retrieve organization permissions.'
      this.cheNotification.showError(errorMessage);
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formUsersList(): void {
    const permissions = this.codenvyPermissions.getOrganizationPermissions(this.organization.id);
    this.members = [];

    const promises: Array<ng.IPromise<any>> = [];

    permissions.forEach((permission: any) => {
      let userId = permission.userId;
      let userProfile = this.cheProfile.getProfileById(userId);

      if (userProfile) {
        this.formUserItem(userProfile, permission);
      } else {
        const promise = this.cheProfile.fetchProfileById(userId).then(() => {
          this.formUserItem(this.cheProfile.getProfileById(userId), permission);
        });
        promises.push(promise);
      }
    });

    this.$q.all(promises).finally(() => {
      this.cheListHelper.setList(this.members, 'id');
    });

    this.hasUpdatePermission = this.organizationsPermissionService.isUserAllowedTo(CodenvyOrganizationActions.UPDATE.toString(), this.organization.id);
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param userProfile {che.IProfile} user's profile
   * @param permissions {codenvy.IPermissions} data
   */
  formUserItem(userProfile: che.IProfile, permissions: codenvy.IPermissions): void {
    const member = <codenvy.IMember>angular.copy(userProfile);
    member.id = userProfile.userId;
    member.name = this.cheProfile.getFullName(userProfile.attributes);
    member.permissions = permissions;
    this.members.push(member);
  }

  /**
   * Selects which dialog should be shown.
   */
  selectAddMemberDialog() {
    if (this.organization.parent) {
      this.showMembersListDialog();
    } else {
      this.showMemberDialog(null);
    }
  }

  /**
   * Shows dialog for adding new member to the organization.
   */
  showMemberDialog(member: codenvy.IMember): void {
    this.$mdDialog.show({
      controller: 'OrganizationMemberDialogController',
      controllerAs: 'organizationMemberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: this.members,
        callbackController: this,
        member: angular.copy(member),
        parentOrganizationId: this.organization.parent,
        parentOrganizationMembers: this.parentOrganizationMembers
      },
      templateUrl: 'app/organizations/organization-details/organization-member-dialog/organization-member-dialog.html'
    });
  }

  /**
   * Shows dialog to select members from list to a sub-organization.
   *
   */
  showMembersListDialog(): void {
    this.$mdDialog.show({
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'OrganizationSelectMembersDialogController',
      controllerAs: 'organizationSelectMembersDialogController',
      locals: {
        callbackController: this,
        parentOrganizationMembers: this.parentOrganizationMembers,
        members: this.members,
      },
      templateUrl: 'app/organizations/organization-details/organization-select-members-dialog/organization-select-members-dialog.html'
    });
  }

  /**
   * Add new members to the organization.
   *
   * @param {Array<codenvy.IMember>} members members to be added
   * @param {string} role member role
   */
  addMembers(members: Array<codenvy.IMember>, role: string): void {
    let promises = [];
    let unregistered = [];

    members.forEach((member: codenvy.IMember) => {
      if (member.id) {
        let actions = CodenvyOrganizationRoles[role].actions;
        let permissions = {
          instanceId: this.organization.id,
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
  editMember(member: codenvy.IMember): void {
    this.showMemberDialog(member);
  }

  /**
   * Performs member's permissions update.
   *
   * @param member member to update permissions
   */
  updateMember(member: codenvy.IMember): void {
    if (member.permissions.actions.length > 0) {
      this.storePermissions(member.permissions);
    } else {
      this.removePermissions(member);
    }
  }

  /**
   * Stores provided permissions.
   *
   * @param permissions {codenvy.IPermissions}
   */
  storePermissions(permissions: codenvy.IPermissions): void {
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
    const selectedMembers = this.cheListHelper.getSelectedItems(),
          selectedMemberIds = selectedMembers.map((member: codenvy.IMember) => {
            return member.id;
          });

    if (!selectedMemberIds.length) {
      this.cheNotification.showError('No such developers.');
      return;
    }

    const confirmationPromise = this.showDeleteMembersConfirmation(selectedMemberIds.length);
    confirmationPromise.then(() => {
      const removeMembersPromises = [];
      let removalError;
      let isCurrentUser = false;
      for (let i = 0; i < selectedMemberIds.length; i++) {
        const id = selectedMemberIds[i];
        this.cheListHelper.itemsSelectionStatus[id] = false;
        if (id === this.cheUser.getUser().id) {
          isCurrentUser = true;
        }
        const promise = this.codenvyPermissions.removeOrganizationPermissions(this.organization.id, id);
        promise.catch((error: any) => {
          removalError = error;
        });
        removeMembersPromises.push(promise);
      }

      this.$q.all(removeMembersPromises).finally(() => {
        if (isCurrentUser) {
          this.processCurrentUserRemoval();
        } else {
          this.fetchMembers();
        }

        if (removalError) {
          this.cheNotification.showError(removalError.data && removalError.data.message ? removalError.data.message : 'User removal failed.');
        }
      });
    });
  }

  /**
   * Call user permissions removal. Show the dialog
   * @param member
   */
  removeMember(member: codenvy.IMember): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove member', 'Would you like to remove member  ' + member.email + ' ?', 'Delete');

    promise.then(() => {
      this.removePermissions(member);
    });
  }

  /**
   * Returns true if the member is owner for current organization.
   * @param member
   *
   * @returns {boolean}
   */
  isOwner(member: codenvy.IMember): boolean {
    if (!this.organization || !member) {
      return false;
    }

    return this.organization.qualifiedName.split('/')[0] === member.name;
  }

  /**
   * Returns string with member roles.
   * @param member
   *
   * @returns {string} string format of roles array
   */
  getMemberRoles(member: codenvy.IMember): string {
    if (!member) {
      return '';
    }
    if (this.isOwner(member)) {
      return 'Organization Owner';
    }
    let roles = this.codenvyOrganization.getRolesFromActions(member.permissions.actions);
    let titles = [];
    let processedActions = [];
    roles.forEach((role: any) => {
      titles.push(role.title);
      processedActions = processedActions.concat(role.actions);
    });

    return titles.join(', ');
  }

  /**
   * Returns string with member other actions.
   * @param member
   *
   * @returns {string} string format of roles array
   */
  getOtherActions(member: codenvy.IMember): string {
    if (!member) {
      return '';
    }
    let roles = this.codenvyOrganization.getRolesFromActions(member.permissions.actions);
    let processedActions = [];
    roles.forEach((role: any) => {
      processedActions = processedActions.concat(role.actions);
    });

    return this.lodash.difference(member.permissions.actions, processedActions).join(', ');
  }

  /**
   * Process the removal of current user from organization.
   */
  processCurrentUserRemoval(): void {
    this.$location.path('/organizations');
  }

  /**
   * Removes user permissions for current organization
   *
   * @param member {codenvy.IMember}
   */
  removePermissions(member: codenvy.IMember): void {
    this.isLoading = true;
    this.codenvyPermissions.removeOrganizationPermissions(member.permissions.instanceId, member.id).then(() => {
      if (member.id === this.cheUser.getUser().id) {
        this.processCurrentUserRemoval();
      } else {
        this.fetchMembers();
      }
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove user ' + member.email + ' permissions.');
    });
  }

  /**
   * Show confirmation popup before members removal
   * @param numberToDelete {number}
   * @returns {ng.IPromise<any>}
   */
  showDeleteMembersConfirmation(numberToDelete: number): ng.IPromise<any> {
    let confirmTitle = 'Would you like to remove ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' members?';
    } else {
      confirmTitle += 'the selected member?';
    }

    return this.confirmDialogService.showConfirmDialog('Remove members', confirmTitle, 'Delete');
  }
}
