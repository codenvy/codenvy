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

export class ErrorPopupController {
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * List of messages to show.
   */
  private messages: string[];

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: angular.material.IDialogService) {
    this.$mdDialog = $mdDialog;
  }

  /**
   * Hides the dialog.
   */
  hide() {
    this.$mdDialog.hide();
  }

}
