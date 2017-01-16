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
 * @name license.messages.controller:LicenseLimitController
 * @description This class is handling the controller for a dialog box about license limit.
 * @author Oleksii Orel
 */
export class LicenseLimitController {
  $mdDialog: ng.material.IDialogService;
  $cookies: ng.cookies.ICookiesService;
  message: string;
  key: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $cookies: ng.cookies.ICookiesService) {
    this.$mdDialog = $mdDialog;
    this.$cookies = $cookies;

  }

  /**
   * It will hide the dialog box.
   */
  hide(): void {
    let now: Date = new Date();
    this.$cookies.put(this.key, 'true', {
      expires: new Date(now.getFullYear() + 10, now.getMonth(), now.getDate())// set the expiration to 10 years
    });
    this.$mdDialog.hide();
  }
}
