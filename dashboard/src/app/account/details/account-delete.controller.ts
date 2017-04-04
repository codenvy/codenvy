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
/**
 * @ngdoc controller
 * @name account.profile.controller:AccountProfileController
 * @description This class is handling the controller for the delete account widget
 * @author Oleksii Orel
 */
export class AccountDeleteController {

  private $location: ng.ILocationService;
  private $mdDialog: ng.material.IDialogService;
  private cheUser: any;
  private cheNotification: any;
  private confirmDialogService: any;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, $mdDialog: ng.material.IDialogService, cheUser: any, cheNotification: any, confirmDialogService: any) {
    this.$location = $location;
    this.$mdDialog = $mdDialog;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.confirmDialogService = confirmDialogService;
  }

  /**
   * Delete account
   */
  deleteAccount(): void {
    let content = 'This is irreversible. Please confirm your want to delete your account.';
    let promise = this.confirmDialogService.showConfirmDialog('Remove account', content, 'Delete');

    promise.then(() => {
      this.cheUser.deleteCurrentUser().then(() => {
        this.cheUser.logout().then(() => {
          this.$location.path('/site/account-deleted');
        });
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Account deletion failed.');
      });
    });
  }
}
