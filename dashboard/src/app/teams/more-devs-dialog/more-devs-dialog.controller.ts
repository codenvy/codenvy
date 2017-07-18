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
import {BillingService} from '../../billing/billing.service';
import {ICreditCard} from '../../../components/api/codenvy-payment.factory';

enum Step {
  ONE = 1,
  TWO
}

/**
 * @ngdoc controller
 * @name teams.member:MoreDevsDialogController
 * @description This class is handling the controller for buying more seats dialog.
 * @author Oleksii Kurinnyi
 */
export class MoreDevsDialogController {
  /**
   * Service for displaying dialogs.
   */
  $mdDialog: ng.material.IDialogService;
  /**
   * Angular promise service.
   */
  $q: ng.IQService;
  /**
   *  Billing service.
   */
  billingService: BillingService;
  /**
   * Notification service.
   */
  cheNotification: any;

  /**
   * Current account's id
   */
  private accountId: string;
  /**
   * Processing state of adding member.
   */
  private isLoading: boolean;
  /**
   * Credit card data.
   */
  creditCard: ICreditCard;
  /**
   * Steps to use them in dialog template.
   */
  step: Object;
  /**
   * Current step of wizard.
   */
  currentStep: number;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, $q: ng.IQService, cheNotification: any, billingService: BillingService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.billingService = billingService;
    this.cheNotification = cheNotification;

    this.step = Step;
    this.currentStep = Step.ONE;

    // temporary
    this.oneDevCost = 20;
    this.teamHasSeats = 8;
    this.teamHasDevs = 6;
    this.value = 5;
    this.minValue = 1;
    this.maxValue = 10;

    this.fetchCreditCard();
  }

  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchAccountId(): ng.IPromise<any> {
   /* return this.billingService.fetchAccountId().then((accountId: string) => {
      this.accountId = accountId;
    });*/ //TODO
  }

  /**
   * Gets credit card.
   *
   * @return {ng.IPromise<any>}
   */
  fetchCreditCard(): ng.IPromise<any> {
    this.isLoading = true;

    return this.fetchAccountId().then(() => {
      return this.billingService.fetchCreditCard(this.accountId);
    }).then((creditCard: ICreditCard) => {
      this.creditCard = creditCard;
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Adds new credit card or updates an existing one.
   */
  saveCard(): ng.IPromise<any> {
    this.isLoading = true;

    return this.billingService.addCreditCard(this.accountId, this.creditCard).then(() => {
      return this.fetchCreditCard();
    }, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to save the credit card.');
    }).finally(() => {
      this.isLoading = false;
    })
  }

  /**
   * Hides the this dialog.
   */
  hide(): void {
    this.$mdDialog.hide();
  }

  /**
   * Requests more seats.
   */
  getMoreSeats(): void {
    if (!this.creditCard && this.currentStep === Step.ONE) {
      this.currentStep = Step.TWO;
      return;
    }

    this.isLoading = true;

    let savePromise;
    if (!this.creditCard.token) {
      savePromise = this.saveCard();
    } else {
      let defer = this.$q.defer();
      savePromise = defer.promise;
      defer.resolve();
    }

    savePromise.then(() => {
      // todo
    }).finally(() => {
      this.isLoading = false;
    });
  }
}
