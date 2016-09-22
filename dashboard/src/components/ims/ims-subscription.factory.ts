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

/**
 * This class is handling the interface with Installation Manager Server (IMS) part of the API that relates to subscriptions.
 */
export class ImsSubscriptionApi {

  /**
   * Default constructor for the subscription API.
   * @ngInject for Dependency injection
   */
  constructor($resource, $rootScope, imsSaasAuthApi) {

    // remote call
    this.remoteImsAPI = $resource('/im/subscription', {}, {
      addTrialSubscription: { method: 'POST', url: '/im/subscription' },
      checkSubscription: { method: 'GET', url: '/im/subscription' }
    });

    $rootScope.$watch(
      () => imsSaasAuthApi.promise,
      (newValue, oldValue) => this._loginChanged(newValue, oldValue)
    );
  }

  /**
   * Checks if the user logged on SaaS has an active on-prem subscription.
   */
  checkOnPremisesSubscription() {
    let serverPromise = this.remoteImsAPI.checkSubscription().$promise;
    this.promise =  serverPromise.then(response => this._gotSubscription(response)).catch(response => this._failedSubscription(response));
    return this.promise;
  }

  _gotSubscription(response) {
    return response;
  }

  _failedSubscription(response) {
    switch (response.status) {
      case 404:// Subscription not found
        return { state: 'NO_SUBSCRIPTION' };
      case 403:// SaaS User is not authenticated or authentication token is expired
      case 500:// server error
        throw response.status;
      default:// unspecified error
        throw response.status;
    }
  }

  _loginChanged(newValue, oldValue) {
    if (newValue && newValue !== oldValue) {
      this.checkOnPremisesSubscription();
    }
  }

  isActive(subscription) {
    return (subscription && subscription.state === 'ACTIVE');
  }
}
