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
import {ShareWorkspaceController} from '../share-workspace.controller';

/**
 * Controller for a permission user item.
 *
 * @author Ann Shumilova
 */
export class UserItemController {

  user: { id: string; email: string; permissions: { actions: Array<string> } };

  private confirmDialogService: any;
  private codenvyPermissions: CodenvyPermissions;
  private $mdDialog: ng.material.IDialogService;
  private callback: ShareWorkspaceController;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(confirmDialogService: any, codenvyPermissions: CodenvyPermissions, $mdDialog: ng.material.IDialogService) {
    this.confirmDialogService = confirmDialogService;
    this.codenvyPermissions = codenvyPermissions;
    this.$mdDialog = $mdDialog;
  }

  /**
   * Call user permissions removal. Show the dialog.
   */
  removeUser(): void {
    let content = 'Please confirm removal for the member \'' + this.user.email + '\'.';
    let promise = this.confirmDialogService.showConfirmDialog('Remove the member', content, 'Delete');

    promise.then(() => {
      // callback is set in scope definition:
      this.callback.removePermissions(this.user);
    });
  }

  /**
   * Returns string with user actions.
   *
   * @returns {string} string format of actions array
   */
  getUserActions(): string {
    // user is set in scope definition:
    return this.user.permissions.actions.join(', ');
  }
}

