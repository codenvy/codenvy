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
 * @name license.messages.controller:LicenseAgreementController
 * @description This class is handling the controller for a dialog box about license agreement.
 * @author Oleksii Orel
 */
export class LicenseAgreementController {
  $mdDialog: ng.material.IDialogService;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * It will hide the dialog box and resolve.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * It will hide the dialog box and reject.
   */
  cancel(): void {
    this.$mdDialog.cancel();
  }
}
