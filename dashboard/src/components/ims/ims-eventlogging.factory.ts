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
 * This class is handling the interface with Installation Manager Server (IMS) SaaS logging events API.
 */
export class ImsEventLoggingApi {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource) {

    // remote call
    this.remoteImsAPI = $resource('/im', {}, {
      logSaasAnalyticsEvent: { method: 'POST', url: '/im/event' }
    });
  }

  /**
   * Returns a promise for the "logged event" state.
   */
  logSaasAnalyticsEvent(event) {
    return this.remoteImsAPI.logSaasAnalyticsEvent(event).$promise;
  }

  /**
   * Returns a promise for the "logged 'CDEC_FIRST_LOGIN' event" state.
   */
  logSaasCdecFirstLoginEvent() {
    let event = { type: 'CDEC_FIRST_LOGIN', parameters: {}};
    return this.logSaasAnalyticsEvent(event);
  }
}
