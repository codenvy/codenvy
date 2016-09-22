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
 * This class is handling the interface with Installation Manager Server (IMS) SaaS login API.
 */
export class ImsSaasAuthApi {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource) {

    // remote call
    this.remoteImsAPI = $resource('/im', {}, {
      logOnSaas: { method: 'POST', url: '/im/login' }
    });

  }

  /**
   * Returns a promise for the "logged-in" state.
   */
  logOnSaas(username, password) {
    // try to refresh if user is not yet logged in
    if (!this.promise || this.canRetry) {
      this.requestLogin(username, password);
    }
    return this.promise;
  }

  requestLogin(username, password) {
    let credentials = { username: username, password: password };
    let saasAuth = this.remoteImsAPI.logOnSaas(credentials);
    this.promise = saasAuth.$promise;
    this.canRetry = false;

    // If login failed, reset promise for next try
    this.promise.catch(() => { this.canRetry = true; });
  }

  resetLogin() {
    this.promise = undefined;
  }

  isAuthFailedError(error) {
    return (error && error.status === 400);
  }
}
