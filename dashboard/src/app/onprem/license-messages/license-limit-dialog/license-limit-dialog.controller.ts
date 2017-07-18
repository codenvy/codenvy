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
