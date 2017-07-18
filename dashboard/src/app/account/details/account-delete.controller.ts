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
/**
 * @ngdoc controller
 * @name account.profile.controller:AccountProfileController
 * @description This class is handling the controller for the delete account widget
 * @author Oleksii Orel
 */
export class AccountDeleteController {

  private $window: ng.IWindowService;
  private $mdDialog: ng.material.IDialogService;
  private cheUser: any;
  private isLoading: boolean;
  private cheNotification: any;
  private confirmDialogService: any;

  private isLoading = false;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($window: ng.IWindowService, $mdDialog: ng.material.IDialogService, cheUser: any, cheNotification: any, confirmDialogService: any) {
    this.$window = $window;
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
      this.isLoading = true;
      this.cheUser.deleteCurrentUser().then(() => {
        this.cheUser.logout().finally(() => {
          this.isLoading = false;
          this.$window.location.href = '/site/account-deleted';
        });
      }, (error: any) => {
        this.isLoading = false;
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Account deletion failed.');
      });
    });
  }
}
