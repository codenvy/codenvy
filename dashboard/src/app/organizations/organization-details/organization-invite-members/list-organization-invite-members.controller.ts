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
import {CodenvyOrganizationRoles} from '../../../../components/api/codenvy-organization-roles';

/**
 * @ngdoc controller
 * @name organization.details.invite-members:ListOrganizationInviteMembersController
 * @description This class is handling the controller for the list of invited organization members.
 * @author Oleksii Orel
 */
export class ListOrganizationInviteMembersController {
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * No members selected.
   */
  private isNoSelected: boolean;
  /**
   * Bulk operation checked state.
   */
  private isBulkChecked: boolean;
  /**
   * Status of selected members.
   */
  private membersSelectedStatus: any;
  /**
   * Number of selected members.
   */
  private membersSelectedNumber: number;
  /**
   * Members order by value.
   */
  private membersOrderBy: string;
  /**
   * List of members to be invited.
   */
  private members: Array<codenvy.IMember>;
  /**
   * Parent organization ID
   */
  private parentOrganizationId: string;
  /**
   * Members list of parent organization.
   */
  private parentOrganizationMembers: string[];
  /**
   * ID of user which is owner of the team
   */
  private ownerId: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: angular.material.IDialogService, lodash: any, cheUser: any) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;

    this.isNoSelected = true;
    this.isBulkChecked = false;
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
    this.membersOrderBy = 'email';

    // add current user to members list
    const user = cheUser.getUser();
    const member = user as codenvy.IMember;
    member.role = CodenvyOrganizationRoles.ADMIN.name;
    this.members = [member];
    this.buildMembersList();

    this.ownerId = user.id;
  }

  /**
   * Forms the list of members.
   */
  buildMembersList(): void {
    this.members.forEach((member: codenvy.IMember) => {
      member.roles = [CodenvyOrganizationRoles[member.role]];
    });
  }

  /**
   * Returns developer role value.
   *
   * @returns {string} string of the developer role value
   */
  getDeveloperRoleValue(): string {
    return CodenvyOrganizationRoles.MEMBER.name;
  }

  /**
   * Returns admin role value.
   *
   * @returns {string} string of the admin role value
   */
  getAdminRoleValue(): string {
    return CodenvyOrganizationRoles.ADMIN.name;
  }

  /**
   * Handler for member role changed in the list.
   * @param {codenvy.IMember} member
   */
  onChangeMemberRole(member: codenvy.IMember): void {
    member.roles[0] = CodenvyOrganizationRoles[member.role];
  }

  /**
   * Update members selected status
   */
  updateSelectedStatus(): void {
    this.membersSelectedNumber = 0;
    this.isBulkChecked = !!this.members.length;
    this.members.forEach((member: codenvy.IMember) => {
      if (this.membersSelectedStatus[member.email]) {
        this.membersSelectedNumber++;
      } else {
        this.isBulkChecked = false;
      }
    });
  }

  /**
   * Change bulk selection value.
   */
  changeBulkSelection(): void {
    if (this.isBulkChecked) {
      this.deselectAllMembers();
      this.isBulkChecked = false;
      return;
    }
    this.selectAllMembers();
    this.isBulkChecked = true;
  }

  /**
   * Check all members in list.
   */
  selectAllMembers(): void {
    this.membersSelectedNumber = this.members.length;
    this.members.forEach((member: codenvy.IMember) => {
      if (member.id === this.ownerId) {
        return;
      }
      this.membersSelectedStatus[member.email] = true;
    });
  }

  /**
   * Uncheck all members in list
   */
  deselectAllMembers(): void {
    this.membersSelectedStatus = {};
    this.membersSelectedNumber = 0;
  }

  /**
   * Adds member to the list.
   *
   * @param members {Array<codenvy.IMember>}
   * @param role {string} member role's name in organization
   */
  addMembers(members: Array<codenvy.IMember>, role: string): void {
    members.forEach((member: any) => {
      member.role = role;
      this.members.push(member);
    });
    this.buildMembersList();
  }

  /**
   * Selects which dialog should be shown.
   *
   * @param $event
   */
  selectAddMemberDialog($event: MouseEvent) {
    if (this.parentOrganizationId) {
      this.showMembersListDialog($event);
    } else {
      this.showMemberDialog($event);
    }
  }

  /**
   * Shows dialog to add new member to a root organization.
   *
   * @param $event
   */
  showMemberDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
      controller: 'OrganizationMemberDialogController',
      controllerAs: 'organizationMemberDialogController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        members: this.members,
        member: null,
        parentOrganizationId: this.parentOrganizationId,
        parentOrganizationMembers: this.parentOrganizationMembers,
        callbackController: this
      },
      templateUrl: 'app/organizations/organization-details/organization-member-dialog/organization-member-dialog.html'
    });
  }

  /**
   * Shows dialog to select members from list to a sub-organization.
   *
   * @param $event
   */
  showMembersListDialog($event: MouseEvent): void {
    this.$mdDialog.show({
      targetEvent: $event,
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
   * Removes selected members.
   */
  removeSelectedMembers(): void {
    this.lodash.remove(this.members, (member: codenvy.IMember) => {
      return this.membersSelectedStatus[member.email];
    });
    this.deselectAllMembers();
    this.isBulkChecked = false;
  }
}
