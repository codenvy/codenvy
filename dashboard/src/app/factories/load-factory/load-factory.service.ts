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
 * This class is handling the service for the factory loading.
 * @author Ann Shumilova
 */
export class LoadFactoryService {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout, $compile) {
    this.$timeout = $timeout;
    this.$compile = $compile;
    this.init = false;


    this.loadFactoryInProgress = false;

    this.currentProgressStep = 0;


    this.loadingSteps = [
      {text: 'Loading factory', inProgressText: '', logs: '', hasError: false},
      {text: 'Initializing workspace', inProgressText: 'Provision workspace and associating it with the existing user', logs: '', hasError: false},
      {text: 'Starting workspace runtime', inProgressText: 'Retrieving the stack\'s image and launching it', logs: '', hasError: false},
      {text: 'Starting workspace agent', inProgressText: 'Agents provide RESTful services like intellisense and SSH', logs: '', hasError: false},
      {text: 'Open IDE', inProgressText: '', logs: '', hasError: false}
    ];

    this.popupVisible = false;
    this.initPopup = false;
  }

  getStepText(stepNumber) {
    let entry = this.loadingSteps[stepNumber];
    if (this.currentProgressStep >= stepNumber) {
      return entry.inProgressText;
    } else {
      return entry.text;
    }
  }

  getFactoryLoadingSteps() {
    return this.loadingSteps;
  }

  setCurrentProgressStep(currentProgressStep) {
    this.currentProgressStep = currentProgressStep;
  }

  goToNextStep() {
    this.currentProgressStep++;
  }

  getCurrentProgressStep() {
    return this.currentProgressStep;
  }

  resetLoadProgress() {
    this.loadingSteps.forEach((step) => {
      step.logs = '';
    step.hasError = false;
  });
  this.currentProgressStep = 0;

  this.loadFactoryInProgress = false;
  }

  isLoadFactoryInProgress() {
    return this.loadFactoryInProgress;
  }

  setLoadFactoryInProgress(value) {
    this.loadFactoryInProgress = value;
  }
}
