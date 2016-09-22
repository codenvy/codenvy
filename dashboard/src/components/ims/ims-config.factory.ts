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
 * This class is handling the interface with Installation Manager Server (IMS) configuration.
 */
export class ImsConfigApi {

  /**
   * Default constructorfor the config API.
   * @ngInject for Dependency injection
   */
  constructor($resource) {

    // remote call
    this.remoteImsAPI = $resource('/im/codenvy/properties', {}, {
      getIMConfig: { method: 'GET' },
      getIMConfigProperty: { method: 'GET', url: '/im/codenvy/properties/:key' },
      setIMConfig: { method: 'PUT' }
    });
  }

  /**
   * Returns Installation Manager Server configuration
   */
  getIMConfig() {
    return this.remoteImsAPI.getIMConfig();
  }

  getIMConfigProperty(propertyName) {
    let param = { key: propertyName };
    return this.remoteImsAPI.getIMConfigProperty(param);
  }

  /**
   * Add the given values to the codenvy configuration.
   * @param it an iterator for [key, value] arrays
   */
  setIMConfig(it) {
    let payload = {};
    for (let [key, value] of it) {
      payload[key] = value;
    }
    return this.remoteImsAPI.setIMConfig(payload);
  }
}
