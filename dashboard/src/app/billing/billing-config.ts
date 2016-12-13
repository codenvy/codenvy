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

import {Billing} from './billing.directive';
import {BillingController} from './billing.controller';
import {CardInfo} from './card-info/card-info.directive';
import {CardInfoController} from './card-info/card-info.controller';
import {AddCreditCardController} from './card-info/add-credit-card/add-credit-card.controller';
import {AddCreditCard} from './card-info/add-credit-card/add-credit-card.directive';

export class BillingConfig {

  constructor(register: any) {
    register.controller('BillingController', BillingController);
    register.directive('billing', Billing);

    register.controller('CardInfoController', CardInfoController);
    register.directive('cardInfo', CardInfo);

    register.controller('AddCreditCardController', AddCreditCardController);
    register.directive('addCreditCard', AddCreditCard);

    // config routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      $routeProvider.accessWhen('/billing', {
        title: 'Billing',
        templateUrl: 'app/billing/billing.html',
        controller: 'BillingController',
        controllerAs: 'billingController'
      });
    });
  }
}
