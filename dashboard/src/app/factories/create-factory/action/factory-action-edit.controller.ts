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
import {FactoryActionBoxController} from './factory-action-box.controller';

/**
 * @ngdoc controller
 * @name factory.directive:FactoryActionDialogEditController
 * @description This class is handling the controller for editing action of a factory
 * @author Florent Benoit
 */
export class FactoryActionDialogEditController {
  isName: boolean;
  isFile: boolean;
  selectedValue: { name: string; file: string };

  private $mdDialog: ng.material.IDialogService;
  private index: number;
  private callbackController: FactoryActionBoxController;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService) {
    this.$mdDialog = $mdDialog;

    // this.selectedValue = this.selectedAction.id === 'runCommand' ? this.selectedAction.properties.name : this.selectedAction.properties.file;
    console.log('>>>  this.selectedValue: ', this.selectedValue);
    this.isName = angular.isDefined(this.selectedValue.name);
    this.isFile = angular.isDefined(this.selectedValue.file);
  }

  /**
   * Callback of the edit button of the dialog.
   */
  edit() {
    this.$mdDialog.hide();
    this.callbackController.callbackEditAction(this.index, this.selectedValue);
  }


  /**
   * Callback of the cancel button of the dialog.
   */
  abort() {
    this.$mdDialog.hide();
  }
}
