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


import {FactoryDetailsConfig} from './factory-details/factory-details-config';
import {CreateFactoryConfig} from './create-factory/create-factory-config';
import {LastFactoriesConfig} from './last-factories/last-factories-config';
import {ListFactoriesCtrl} from './list-factories/list-factories.controller';
import {FactoryItemCtrl} from './list-factories/factory-item/factory-item.controller';
import {CodenvyFactoryItem} from './list-factories/factory-item/factory-item.directive';
import {LoadFactoryCtrl} from './load-factory/load-factory.controller';
import {LoadFactoryService} from './load-factory/load-factory.service';

export class FactoryConfig {

  constructor(register) {
    register.controller('ListFactoriesCtrl', ListFactoriesCtrl);

    register.controller('FactoryItemCtrl', FactoryItemCtrl);
    register.directive('cdvyFactoryItem', CodenvyFactoryItem);

    register.controller('LoadFactoryCtrl', LoadFactoryCtrl);
    register.service('loadFactoryService', LoadFactoryService);

    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/factories', {
        title: 'Factories',
        templateUrl: 'app/factories/list-factories/list-factories.html',
        controller: 'ListFactoriesCtrl',
        controllerAs: 'listFactoriesCtrl'
      })
        .accessWhen('/load-factory', {
          title: 'Load Factory',
          templateUrl: 'app/factories/load-factory/load-factory.html',
          controller: 'LoadFactoryCtrl',
          controllerAs: 'loadFactoryCtrl'
        })
      .accessWhen('/load-factory/:id', {
        title: 'Load Factory',
        templateUrl: 'app/factories/load-factory/load-factory.html',
        controller: 'LoadFactoryCtrl',
        controllerAs: 'loadFactoryCtrl'
      });

    });

    // config files
    new FactoryDetailsConfig(register);
    new CreateFactoryConfig(register);
    new LastFactoriesConfig(register);

  }
}

