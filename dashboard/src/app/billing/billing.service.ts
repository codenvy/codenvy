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
