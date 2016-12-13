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
'use strict';
import IResource = angular.resource.IResource;

export interface ICreditCard {
  accountId?: string;
  number: string;
  cardholder: string;
  expirationDate: string;
  cvv?: string;
  postcode?: string;

  streetAddress: string;
  city: string;
  state: string;
  country: string;

  token?: string;
  nonce?: string;
}

/**
 * This class is handling the credit card API.
 * @author Ann Shumilova
 */
export class CodenvyPayment {
  $resource: ng.resource.IResourceService;
  $braintree: any;
  $q: ng.IQService;
  $log: ng.ILogService;

  creditCardsPerAccount: Map<string, any>;
  tokensPerAccount: Map<string, any>;
  invoicesPerAccount: Map<string, any>;

  remotePaymentAPI: any;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($resource: ng.resource.IResourceService, $braintree, $q: ng.IQService, $log: ng.ILogService) {
    // keep resource
    this.$resource = $resource;
    this.$braintree = $braintree;
    this.$q = $q;
    this.$log = $log;

    this.creditCardsPerAccount = new Map();
    this.tokensPerAccount = new Map();

    // remote call
    this.remotePaymentAPI = this.$resource('/api/creditcard/:accountId',{}, {
      getToken: {method: 'GET', url: '/api/creditcard/:accountId/token'},
      add: {method: 'POST', url: '/api/creditcard/:accountId'},
      update: {method: 'PUT', url: '/api/creditcard/:accountId/:creditCardToken'},
      remove: {method: 'DELETE', url: '/api/creditcard/:accountId/:creditCardToken'},
      get: {method: 'GET', url: '/api/creditcard/:accountId/:creditCardToken'}
    });

  }

  /**
   * Gets credit cards list
   *
   * @param accountId {string}
   * @return {ng.IPromise<any>}
   */
  fetchAllCreditCards(accountId: string): ng.IPromise<any> {
    let promise = this.remotePaymentAPI.query({accountId: accountId}).$promise;
    // check if if was OK or not
    let parsedResultPromise = promise.then((data: ICreditCard[]) => {
      this.creditCardsPerAccount.set(accountId, data);
    });
    return parsedResultPromise;
  }

  /**
   * Gets the list of credit cards by account id.
   *
   * @param accountId {string}
   * @returns {Array}
   */
  getCreditCards(accountId: string): ICreditCard[] {
    return this.creditCardsPerAccount.get(accountId);
  }

  /**
   * Gets the client token to add credit card.
   *
   * @param accountId {string}
   * @return {ng.IPromise<any>}
   */
  getClientToken(accountId: string): ng.IPromise<any> {
    let promise = this.remotePaymentAPI.getToken({accountId: accountId}).$promise;
    // check if if was OK or not
    return promise.then((data: any) => {
      this.tokensPerAccount.set(accountId, data.token);
    });
  }

  /**
   * Adds credit card data to the pointed account.
   *
   * @param accountId {String}
   * @param creditCard {ICreditCard}
   * @return {IPromise<any>}
   */
  addCreditCard(accountId: string, creditCard: ICreditCard) {
    let client,
        mainCreditCardInfo: any = {};
    mainCreditCardInfo.number = creditCard.number;
    mainCreditCardInfo.cardholderName = creditCard.cardholder;
    mainCreditCardInfo.expirationDate = creditCard.expirationDate.replace(/ /g, '');
    mainCreditCardInfo.cvv = creditCard.cvv;
    mainCreditCardInfo.billingAddress = {};
    mainCreditCardInfo.billingAddress = {postalCode: creditCard.postcode};
    let defer = this.$q.defer();

    this.getClientToken(accountId).then(() => {
      client = new this.$braintree.api.Client({
        clientToken: this.tokensPerAccount.get(accountId)
      });

      client.tokenizeCard(mainCreditCardInfo, (err: any, nonce: string) => {
        let newCreditCard: any = {nonce: nonce};
        newCreditCard.state = creditCard.state;
        newCreditCard.country = creditCard.country;
        newCreditCard.streetAddress = creditCard.streetAddress;
        newCreditCard.city = creditCard.city;

        this.remotePaymentAPI.add({accountId: accountId}, newCreditCard).$promise.then(() => {
          defer.resolve();
        }, (error: any) => {
          defer.reject(error);
        });

      });
    });
    return defer.promise;
  }

  /**
   * Update credit card billing information
   *
   * @param accountId {string}
   * @param creditCard {ICreditCard}
   * @return {IPromise<{method, url}|any|void|PromiseLike<void>|IDBRequest>}
   */
  updateCreditCard(accountId: string, creditCard: ICreditCard): ng.IPromise<any> {
    let newCreditCard = {
          streetAddress: creditCard.streetAddress,
          city: creditCard.city,
          country: creditCard.country,
          state: creditCard.state
        };

    return this.remotePaymentAPI.update({accountId: accountId, creditCardToken: creditCard.token}, newCreditCard).$promise;
  }

  /**
   * Removes credit card by it's number in the pointed account.
   *
   * @param accountId {string}
   * @param creditCardToken {string}
   * @return {angular.IPromise<Array<T>>|angular.IPromise<T>|angular.IPromise<IResourceArray<T>>}
   */
  removeCreditCard(accountId: string, creditCardToken: string): ng.IPromise<any> {
    return this.remotePaymentAPI.remove({accountId: accountId, creditCardToken: creditCardToken}).$promise;
  }

}
