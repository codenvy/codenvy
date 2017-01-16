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
 * @ngdoc controller
 * @name factories.controller:LastFactoriesController
 * @description This class is handling the controller of the last factories to display in the dashboard
 * @author Oleksii Orel
 */
export class LastFactoriesController {


  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(codenvyAPI) {

    this.factories = codenvyAPI.getFactory().getPageFactories();

    //TODO we should change to modificationDate after model's change
    this.factoriesOrderBy = '-creator.created';

    this.maxItems = 5;

    //TODO add OrderBy to condition in fetch API
    let promise = codenvyAPI.getFactory().fetchFactories(this.maxItems, 0);

    this.isLoading = true;
    promise.finally(() => {
      this.isLoading = false;
      this.updateFactories();
    });
  }

  /**
   * Update factories array
   */
  updateFactories() {
    this.factories = codenvyAPI.getFactory().getPageFactories();
  }

  getFactories() {
    return this.factories;
  }

}
