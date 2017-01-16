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

/**
 * Controller for a factory details.
 * @author Florent Benoit
 */
export class FactoryDetailsCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($route, codenvyAPI, cheNotification) {
    'ngInject';

    this.codenvyAPI = codenvyAPI;
    let factoryId = $route.current.params.id;

    this.factory = codenvyAPI.getFactory().getFactoryById(factoryId);

    codenvyAPI.getFactory().fetchFactoryById(factoryId).then((factory) => {
      this.factory = factory;
    }, (error) => {
      cheNotification.showError(error.data.message ? error.data.message : 'Get factory failed.');
      console.log('error', error);
    });
  }

  /**
   * Returns the factory url based on id.
   * @returns {link.href|*} link value
   */
  getFactoryIdUrl() {
    if (!this.factory) {
      return null;
    }
    return this.codenvyAPI.getFactory().getFactoryIdUrl(this.factory);
  }
}

