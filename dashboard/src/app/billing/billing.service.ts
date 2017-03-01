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

import {CodenvyPayment, ICreditCard} from '../../components/api/codenvy-payment.factory';

export class BillingService {
  $log: ng.ILogService;
  $q: ng.IQService;
  cheAPI: any;
  codenvyPayment: CodenvyPayment;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($q: ng.IQService, $log: ng.ILogService,  cheAPI: any,  codenvyPayment: CodenvyPayment) {
    this.$log = $log;
    this.$q = $q;
    this.cheAPI = cheAPI;
    this.codenvyPayment = codenvyPayment;
  }

  /**
   * Fetches credit card.
   *
   * @param {string} accountId
   *
   * @returns {IPromise<any>}
   */
  fetchCreditCard(accountId: string): ng.IPromise<any> {
    let defer = this.$q.defer();

    this.codenvyPayment.fetchAllCreditCards(accountId).then(() => {
      let creditCard = this.getCreditCard(accountId);
      defer.resolve(creditCard);
    }, (error: any) => {
      if (error.status === 304) {
        let creditCard = this.getCreditCard(accountId);
        defer.resolve(creditCard);
      } else {
        this.$log.error(error);
        defer.reject(error);
      }
    });

    return defer.promise;
  }

  /**
   * Returns a credit card
   *
   * @param {string} accountId
   * @return {ICreditCard}
   */
  getCreditCard(accountId: string): ICreditCard {
    let creditCards = this.codenvyPayment.getCreditCards(accountId) || [];
    return creditCards[0] || null;
  }

  /**
   * Deletes existing credit card.
   *
   * @param {string} accountId
   * @param {string} token
   *
   * @return {ng.IPromise<any>}
   */
  removeCreditCard(accountId: string, token: string): ng.IPromise<any> {
    return this.codenvyPayment.removeCreditCard(accountId, token).then(() => {
      angular.noop();
    }, (error: any) => {
      this.$log.error(error);
    });
  }

  /**
   * Adds new credit card.
   *
   * @param {string} accountId
   * @param {ICreditCard} creditCard
   *
   * @return {ng.IPromise<any>}
   */
  addCreditCard(accountId: string, creditCard: ICreditCard): ng.IPromise<any> {
    return this.codenvyPayment.addCreditCard(accountId, creditCard);
  }

  /**
   * Updates an existing credit card.
   *
   * @param {string} accountId
   * @param {ICreditCard} creditCard
   *
   * @return {ng.IPromise<any>}
   */
  updateCreditCard(accountId: string, creditCard: ICreditCard): ng.IPromise<any> {
    return this.codenvyPayment.updateCreditCard(accountId, creditCard);
  }

}
