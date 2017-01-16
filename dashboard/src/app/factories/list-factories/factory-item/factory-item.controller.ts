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
 * Controller for a factory item.
 * @author Oleksii Orel
 */
export class FactoryItemCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location, codenvyFactory, cheEnvironmentRegistry, lodash) {
    this.$location = $location;
    this.codenvyFactory = codenvyFactory;
    this.cheEnvironmentRegistry = cheEnvironmentRegistry;
    this.lodash = lodash;
  }

  /**
   * Detect factory links.
   * @returns [string]
   */
  getFactoryLinks() {
    return this.codenvyFactory.detectLinks(this.factory);
  }

  /**
   * Redirect to factory details.
   */
  redirectToFactoryDetails() {
    this.$location.path('/factory/' + this.factory.id);
  }

  getMemoryLimit() {
    if (!this.factory.workspace) {
      return '-';
    }

    let defaultEnvName = this.factory.workspace.defaultEnv;
    let environment = this.factory.workspace.environments[defaultEnvName];

    let recipeType = environment.recipe.type;
    let environmentManager = this.cheEnvironmentRegistry.getEnvironmentManager(recipeType);
    let machines = environmentManager.getMachines(environment);

    let limits = this.lodash.pluck(machines, 'attributes.memoryLimitBytes');
    let total = 0;
    limits.forEach((limit) => {
      if (limit) {
        total += limit / (1024*1024);
      }
    });
    return (total > 0) ? total + ' MB' : '-';
  }
}

