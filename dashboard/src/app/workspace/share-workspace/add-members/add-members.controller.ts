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
import {CodenvyPermissions} from "../../../../components/api/codenvy-permissions.factory";

/**
 * This class is handling the controller for the add members popup
 * @author Oleksii Orel
 * @author Ann Shumilova
 */
export class AddMemberController {

  private $mdDialog: angular.material.IDialogService;
  private $q: ng.IQService;
  private lodash: _.LoDashStatic;
  private codenvyPermissions: CodenvyPermissions;
  private cheProfile: any;
  /**
   * Workspace namespace (is set from outside).
   */
  private namespace: string;
  /**
   * Callback handler (is set from outside).
   */
  private callbackController: any;
  /**
   * The list of users, that already have permissions in the workspace (is set from outside).
   */
  private users: Array<any>;
  private team: any;
  private isLoading: boolean;
  private members: Array<any>;

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
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $mdDialog: angular.material.IDialogService, lodash: _.LoDashStatic, codenvyTeam: CodenvyTeam,
              codenvyPermissions: CodenvyPermissions, cheProfile: any) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.codenvyPermissions = codenvyPermissions;
    this.cheProfile = cheProfile;

    this.team = codenvyTeam.getTeamByName(this.namespace);
    this.membersSelectedStatus = {};
    this.isBulkChecked = false;
    this.isNoSelected = true;
    this.isAllSelected = true;

    if (this.team) {
     this.fetchTeamMembers();
    }
  }

  fetchTeamMembers(): void {
    this.isLoading = true;
    this.codenvyPermissions.fetchTeamPermissions(this.team.id).then(() => {
      this.formMemberList();
    }, (error: any) => {
      if (error.status === 304) {
        this.formMemberList();
      } else {
        this.isLoading = false;
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formMemberList(): void {
    let permissions = this.codenvyPermissions.getTeamPermissions(this.team.id);
    let existingMembers = this.lodash.pluck(this.users, 'id');
    this.members = [];
    let promises = [];

    for (let i = 0; i < permissions.length; i++) {
      let permission = permissions[i];
      let userId = permission.userId;
      if (existingMembers.indexOf(userId) >= 0) {
        continue;
      }

      let user = this.cheProfile.getProfileFromId(userId);

      if (user) {
        this.formUserItem(user, permission);
      } else {
        let promise = this.cheProfile.fetchProfileId(userId).then(() => {
          this.formUserItem(this.cheProfile.getProfileFromId(userId), permission);
        });
        promises.push(promise);
      }
    };

    this.$q.all(promises).finally(() => {
      this.isLoading = false;
    });
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
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the share button of the dialog.
   */
  shareWorkspace() {
    let checkedUsers = [];

    Object.keys(this.membersSelectedStatus).forEach((key) => {
      if (this.membersSelectedStatus[key] === true) {
        checkedUsers.push(key);
      }
    });

    let permissionPromises = this.callbackController.shareWorkspace(checkedUsers);

    this.$q.all(permissionPromises).then(() => {
      this.$mdDialog.hide();
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
      this.membersSelectedStatus[member.userId] = true;
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

    this.isBulkChecked = (this.isAllSelected && Object.keys(this.membersSelectedStatus).length === this.members.length);
  }
}
