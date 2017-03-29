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
import {CodenvyTeam} from '../../../../../components/api/codenvy-team.factory';

/**
 * Controller for team member item..
 *
 * @author Ann Shumilova
 */
export class MemberItemController {
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Controller for handling callback events. (Comes from directive's scope).
   */
  private callback: any;
  /**
   * Member to be displayed. (Comes from directive's scope).
   */
  private member: any;
  /**
   * Whether current member is owner of the team. (Comes from directive's scope).
   */
  private isOwner: boolean;

  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Actions that are not part of any role.
   */
  private otherActions: Array<string>;
  /**
   * Confirm dialog service.
   */
  private confirmDialogService: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: angular.material.IDialogService, codenvyTeam: CodenvyTeam, lodash: any, confirmDialogService: any) {
    this.$mdDialog = $mdDialog;
    this.codenvyTeam = codenvyTeam;
    this.lodash = lodash;
    this.confirmDialogService = confirmDialogService;

    this.otherActions = [];
  }

  /**
   * Call user permissions removal. Show the dialog
   * @param  event - the $event
   */
  removeMember(event: MouseEvent): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove member', 'Would you like to remove member  ' + this.member.email + ' ?', 'Delete');

    promise.then(() => {
      if (this.member.isPending) {
        this.callback.deleteInvitation(this.member);
      } else {
        this.callback.removePermissions(this.member);
      }
    });
  }

  /**
   * Handler edit member user's request.
   */
  editMember(): void {
    this.callback.editMember(this.member);
  }

  /**
   * Returns string with member roles.
   *
   * @returns {string} string format of roles array
   */
  getMemberRoles(): string {
    if (this.isOwner) {
      return 'Team Owner';
    }

    let roles = this.codenvyTeam.getRolesFromActions(this.member.permissions.actions);
    let titles = [];
    let processedActions = []
    roles.forEach((role: any) => {
      titles.push(role.title);
      processedActions = processedActions.concat(role.actions);
    });

    this.otherActions = this.lodash.difference(this.member.permissions.actions, processedActions);
    return titles.join(', ');
  }

}

