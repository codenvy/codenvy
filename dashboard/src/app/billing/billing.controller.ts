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
import {CodenvyPayment, ICreditCard} from '../../components/api/codenvy-payment.factory';
import {BillingService} from './billing.service';

interface ICreditCardProblem {
  message: string;
  [propName: string]: string;
}

enum Tab {Summary, Card, Invoices}

/**
 * @ngdoc controller
 * @name billing.controller:BillingController
 * @description This class is handling the controller for billing information.
 * @author Oleksii Kurinnyi
 * @author Ann Shumilova
 */
export class BillingController {
  $location: ng.ILocationService;
  $log: ng.ILogService;
  $mdDialog: ng.material.IDialogService;
  $q: ng.IQService;
  confirmDialogService: any;
  cheAPI: any;
  codenvyPayment: CodenvyPayment;
  cheTeam: che.api.ICheTeam;
  cheNotification: any;
  billingService: BillingService;

  creditCard: ICreditCard;
  origCreditCard: ICreditCard;
  cardInfoForm: ng.IFormController;
  selectedTabIndex: number;
  accountId: string;
  loading: boolean;

  tab: Object = Tab;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($location: ng.ILocationService, $log: ng.ILogService, $mdDialog: ng.material.IDialogService, $q: ng.IQService,
               $rootScope: che.IRootScopeService, confirmDialogService: any, cheAPI: any,
               codenvyPayment: CodenvyPayment, cheTeam: che.api.ICheTeam, cheNotification: any, billingService: BillingService) {
    this.$location = $location;
    this.$log = $log;
    this.$mdDialog = $mdDialog;
    this.$q = $q;
    this.confirmDialogService = confirmDialogService;
    this.cheAPI = cheAPI;
    this.codenvyPayment = codenvyPayment;
    this.cheTeam = cheTeam;
    this.cheNotification = cheNotification;
    this.billingService = billingService;

    $rootScope.showIDE = false;

    this.accountId = '';

    let tabIndex = parseInt(Tab[this.$location.search().tab], 10);
    this.selectedTabIndex = tabIndex ? tabIndex : Tab.Summary;

    this.fetchCreditCard();
  }

  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchAccountId(): ng.IPromise<any> {
    return this.cheTeam.fetchTeams().then(() => {
      this.accountId = this.cheTeam.getPersonalAccount().id;
    }, (error: any) => {
      if (error.status === 304) {
        this.accountId = this.cheTeam.getPersonalAccount().id;
      }
    });
  }

  /**
   * Fetches and stores a credit card.
   *
   */
  fetchCreditCard(): ng.IPromise<any> {
    this.loading = true;

    return this.fetchAccountId().then(() => {
      return this.billingService.fetchCreditCard(this.accountId);
    }).then((creditCard: ICreditCard) => {
      this.creditCard = creditCard;
      this.origCreditCard = angular.copy(this.creditCard);
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Gets credit card and creates a copy
   */
  getCreditCard(): void {
    this.creditCard = this.billingService.getCreditCard(this.accountId);
    this.origCreditCard = angular.copy(this.creditCard);
  }

  /**
   * Callback when credit card has been changed.
   *
   * @param creditCard {ICreditCard}
   */
  creditCardChanged(creditCard: ICreditCard): void {
    this.creditCard = angular.copy(creditCard);
  }

  /**
   * Deletes existing credit card.
   */
  creditCardDeleted(): void {
    let promise = this.confirmDialogService.showConfirmDialog('Remove billing information',
      'Are you sure you want to delete all your billing information? This is irreversible.', 'Delete');

    promise.then(() => {
      this.loading = true;
      this.billingService.removeCreditCard(this.creditCard.accountId, this.creditCard.token).then(() => {
        return this.fetchCreditCard();
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete the credit card.');
      }).finally(() => {
        this.loading = false;
      });
    });
  }

  /**
   * Adds new credit card or updates an existing one.
   */
  saveCard(): void {
    this.loading = true;

    let savePromise;
    if (this.creditCard.token) {
      // update exiting card
      savePromise = this.billingService.updateCreditCard(this.accountId, this.creditCard);
    } else {
      // add new card
      savePromise = this.billingService.addCreditCard(this.accountId, this.creditCard);
    }

    savePromise.then((data: any) => {
      if (data && data.problems && data.problems.length) {
        this.showProblems(data.problems);
      } else {
        return this.fetchCreditCard();
      }
    }, (error: any) => {
      if (error && error.problems && error.problems.length) {
        this.showProblems(error.problems);
      } else {
        let problem: ICreditCardProblem = {message: 'Failed to save the credit card.'};
        this.showProblems([problem]);
      }
    }).finally(() => {
      this.loading = false;
    });
  }

  /**
   * Cancels credit card information changes
   */
  cancelCard(): void {
    this.creditCard = angular.copy(this.origCreditCard);
  }

  /**
   * Registers card info form
   *
   * @param form {ng.IFormController}
   */
  registerCardInfoForm(form: ng.IFormController): void {
    this.cardInfoForm = form;
  }

  /**
   * Returns true if form on Card tab is valid
   *
   * @return {boolean}
   */
  isSaveButtonDisabled(): boolean {
    return !(this.cardInfoForm && this.cardInfoForm.$valid)
      || angular.equals(this.creditCard, this.origCreditCard);
  }

  /**
   * Returns true if "Save" button should be visible
   *
   * @return {boolean}
   */
  isSaveButtonVisible(): boolean {
    return this.selectedTabIndex === Tab.Card && !this.loading;
  }

  /**
   * Creates popup with list of error messages.
   *
   * @param {ICreditCardProblem[]} problems
   */
  showProblems(problems: ICreditCardProblem[]): void {
    let messages: string[] = [];
    problems.forEach((problem: {message: string, [propName: string]: string}) => {
      messages.push(problem.message);
    });

    this.$mdDialog.show({
      controller: 'ErrorPopupController',
      controllerAs: 'errorPopupController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        messages: messages
      },
      templateUrl: 'app/billing/error-popup/error-popup.html'
    });
  }

  /**
   * Changes search part of URL.
   * Updates credit card when Card info tab is selected.
   *
   * @param {number} tabIndex tab ID
   */
  onSelectTab(tabIndex: number): void {
    this.$location.search('tab', Tab[tabIndex]);

    if (tabIndex === Tab.Card) {
      this.getCreditCard();
    }
  }

}
