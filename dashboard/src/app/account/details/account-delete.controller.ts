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
 * @ngdoc controller
 * @name account.profile.controller:AccountProfileCtrl
 * @description This class is handling the controller for the delete account widget
 * @author Oleksii Orel
 */
export class AccountDeleteCtrl {
  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($window, $mdDialog, codenvyUser, cheNotification) {
    this.$window = $window;
    this.$mdDialog = $mdDialog;
    this.codenvyUser = codenvyUser;
    this.cheNotification = cheNotification;
  }

  deleteAccount() {
    let confirm = this.$mdDialog.confirm()
      .title('Delete Account')
      .content('Account deletion cannot be undone. Please confirm you want to delete your account.')
      .ariaLabel('Remove account')
      .ok('Delete it!')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      this.codenvyUser.deleteCurrentUser().then(() => {
        this.codenvyUser.logout().then(() => {
          this.$window.location = '/site/account-deleted';
        });
      }, (error) => {
        this.cheNotification.showError(error.data.message ? error.data.message : 'Account deletion failed.');
      });
    });
  }
}
