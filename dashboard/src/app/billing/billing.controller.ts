/*
 *  [2015] - [2016] Codenvy, S.A.
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
/**
 * Created by user on 14.11.16.
 */
'use strict';
import {CodenvyPayment, ICreditCard} from '../../components/api/codenvy-payment.factory';

enum Tab {Summary, Card, Invoices}

export class BillingController {
  $log: ng.ILogService;
  $q: ng.IQService;
  cheAPI: any;
  codenvyPayment: CodenvyPayment;
  cheNotification: any;

  creditCard: ICreditCard;
  origCreditCard: ICreditCard;
  cardInfoForm: ng.IFormController;
  selectedTabIndex: number;
  accountId: string;
  loading: boolean = true;

  tab: Object = Tab;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($log: ng.ILogService, $q: ng.IQService, cheAPI: any, codenvyPayment: CodenvyPayment, cheNotification: any) {
    this.$log = $log;
    this.$q = $q;
    this.cheAPI = cheAPI;
    this.codenvyPayment = codenvyPayment;
    this.cheNotification = cheNotification;

    this.accountId = '';

    this.selectedTabIndex = Tab.Summary;

    this.fetchCreditCard();
  }

  /**
   * Gets account ID
   *
   * @returns {IPromise<any>}
   */
  fetchProfile(): ng.IPromise<any> {
    let defer             = this.$q.defer(),
        getProfilePromise = this.cheAPI.getProfile().getProfile();

    // get account ID (user ID)
    if (getProfilePromise.attributes) {
      this.accountId = getProfilePromise.userId;
      defer.resolve();
    } else {
      getProfilePromise.$promise.then((data: any) => {
        this.accountId = data.userId;
        defer.resolve();
      }, (error: any) => {
        defer.reject();
        this.$log.error(error);
      });
    }

    return defer.promise;
  }

  /**
   * Gets credit card.
   *
   * @returns {IPromise<any>}
   */
  fetchCreditCard(): ng.IPromise<any> {
    return this.fetchProfile().then(() => {
      return this.codenvyPayment.fetchAllCreditCards(this.accountId);
    }).then(() => {
      let creditCards = this.codenvyPayment.getCreditCards(this.accountId);

      if (creditCards && creditCards.length) {
        this.creditCard = creditCards[0];
      }

      this.origCreditCard = angular.copy(this.creditCard);
    }, (error: any) => {
      this.$log.error(error);
    }).finally(() => {
      this.loading = false;
    });
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
    this.loading = true;
    this.codenvyPayment.removeCreditCard(this.creditCard.accountId, this.creditCard.token).then(() => {}, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete the credit card.');
      this.$log.error(error);
    }).finally(() => {
      this.creditCard = null;
      return this.fetchCreditCard();
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
      savePromise = this.codenvyPayment.updateCreditCard(this.accountId, this.creditCard);
    } else {
      // add new card
      savePromise = this.codenvyPayment.addCreditCard(this.accountId, this.creditCard);
    }

    savePromise.then(() => {}, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete the credit card.');
      this.$log.error(error);
    }).finally(() => {
      return this.fetchCreditCard();
    });
  }

  /**
   * Cancels credit card information changes
   */
  cancelCard(): void {
    this.creditCard = angular.copy(this.origCreditCard);
  }

  /**
   * Register card info form
   *
   * @param form {ng.IFormController}
   */
  setInfoForm(form: ng.IFormController): void {
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
}
