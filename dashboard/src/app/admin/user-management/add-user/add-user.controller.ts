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
 * This class is handling the controller for the add user
 * @author Oleksii Orel
 */
export class AdminsAddUserCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog, codenvyUser, cheNotification) {
    this.$mdDialog = $mdDialog;
    this.codenvyUser = codenvyUser;
    this.cheNotification = cheNotification;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the add button of the dialog(create new user).
   */
  createUser() {
    let promise = this.codenvyUser.createUser(this.newUserName, this.newUserEmail, this.newUserPassword);

    promise.then(() => {
      this.$mdDialog.hide();
      this.callbackController.updateUsers();
      this.cheNotification.showInfo('User successfully created.');
    }, (error) => {
      this.cheNotification.showError(error.data.message ? error.data.message : '.');
    });
  }

}
