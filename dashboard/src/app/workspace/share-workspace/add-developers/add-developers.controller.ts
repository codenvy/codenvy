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
import {ShareWorkspaceController} from '../share-workspace.controller';

/**
 * This class is handling the controller for sharing a private workspace with developers.
 * @author Oleksii Kurinnyi
 */
export class AddDeveloperController {
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: ng.material.IDialogService;
  /**
   * true if user owns the workspace.
   */
  private canShare: boolean;
  /**
   * List of users to share the workspace.
   */
  private existingUsers: string[];
  /**
   * Parent controller.
   */
  private callbackController: ShareWorkspaceController;

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $mdDialog: ng.material.IDialogService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
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
    let users = [];
    this.existingUsers.forEach((userId: string) => {
      users.push({userId: userId, isTeamAdmin: false});
    });

    let permissionPromises = this.callbackController.shareWorkspace(users);

    this.$q.all(permissionPromises).then(() => {
      this.$mdDialog.hide();
    });
  }
}
