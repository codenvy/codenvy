/*
 *  [2015] - [2016] Codenvy, S.A.
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

/**
 * This class is handling the controller for the add members popup
 * @author Oleksii Orel
 */
export class AddMemberController {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($q, $mdDialog) {
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
    let permissionPromises = this.callbackController.shareWorkspace();

    this.$q.all(permissionPromises).then(() => {
      this.$mdDialog.hide();
    });
  }
}
