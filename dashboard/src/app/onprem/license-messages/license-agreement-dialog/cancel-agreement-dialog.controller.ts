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
 * @name license.messages.controller:CancelAgreementController
 * @description This class is handling the controller for a dialog box about cancel a license agreement.
 * @author Oleksii Orel
 */
export class CancelAgreementController {
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
