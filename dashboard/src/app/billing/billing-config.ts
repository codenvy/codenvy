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

import {BillingController} from './billing.controller';
import {BillingService} from './billing.service';
import {CardInfo} from './card-info/card-info.directive';
import {CardInfoController} from './card-info/card-info.controller';
import {AddCreditCardController} from './card-info/add-credit-card/add-credit-card.controller';
import {AddCreditCard} from './card-info/add-credit-card/add-credit-card.directive';
import {ListInvoices} from './invoices/list-invoices.directive';
import {ListInvoicesController} from './invoices/list-invoices.controller';
import {RamInfo} from './ram-info/ram-info.directive';
import {RamInfoController} from './ram-info/ram-info.controller';
import {MoreRamController} from './ram-info/more-ram.controller';
import {ErrorPopupController} from './error-popup/error-popup.controller';

export class BillingConfig {

  constructor(register: any) {
    register.controller('BillingController', BillingController);
    register.service('billingService', BillingService);

    register.controller('CardInfoController', CardInfoController);
    register.directive('cardInfo', CardInfo);

    register.controller('AddCreditCardController', AddCreditCardController);
    register.directive('addCreditCard', AddCreditCard);

    register.controller('ListInvoicesController', ListInvoicesController);
    register.directive('listInvoices', ListInvoices);

    register.controller('RamInfoController', RamInfoController);
    register.directive('ramInfo', RamInfo);

    register.controller('MoreRamController', MoreRamController);
    register.controller('ErrorPopupController', ErrorPopupController);

    // config routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      $routeProvider.accessWhen('/billing', {
        title: 'Billing',
        templateUrl: 'app/billing/billing.html',
        controller: 'BillingController',
        controllerAs: 'billingController',
        resolve: {
          check: ['$q', 'cheService', function ($q, cheService) {
            var defer = $q.defer();
            cheService.fetchServices().then(() => {
              if (cheService.isServiceAvailable('creditcard')) {
                defer.resolve();
              } else {
                defer.reject();
              }
            });
            return defer.promise;
          }]
        }
      });
    });
  }
}
