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
import {LicenseMessagesService} from '../../../onprem/license-messages/license-messages.service';
import {AdminsUserManagementCtrl} from '../user-management.controller';
import {CodenvyLicense} from '../../../../components/api/codenvy-license.factory';


/**
 * This class is handling the controller for the add user
 * @author Oleksii Orel
 */
export class AdminsAddUserController {
  $mdDialog: ng.material.IDialogService;
  cheNotification: any;
  cheUser: any;
  codenvyLicense: CodenvyLicense;
  callbackController: AdminsUserManagementCtrl;
  licenseMessagesService: LicenseMessagesService;
  newUserName: string;
  newUserEmail: string;
  newUserPassword: string;

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, cheUser: any, codenvyLicense: CodenvyLicense, cheNotification: any,
              licenseMessagesService: LicenseMessagesService) {
    this.$mdDialog = $mdDialog;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.licenseMessagesService = licenseMessagesService;
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort(): void {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the add button of the dialog(create new user).
   */
  createUser(): void {
    let promise = this.cheUser.createUser(this.newUserName, this.newUserEmail, this.newUserPassword);

    promise.then(() => {
      this.codenvyLicense.fetchLicenseLegality();//fetch license legality
      this.$mdDialog.hide();
      this.callbackController.updateUsers();
      this.cheNotification.showInfo('User successfully created.');
      this.licenseMessagesService.fetchMessages();
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : '.');
    });
  }

}
