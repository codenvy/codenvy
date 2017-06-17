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

import {AccountProfile} from './profile/account-profile.directive';
import {AccountDelete} from './account-delete.directive';
import {AccountDeleteController} from './account-delete.controller';
import {AccountUpdatePassword} from './account-update-password.directive';
import {AccountController} from './account.controller';


export class AccountConfig {

  constructor(register: che.IRegisterService) {
    register.directive('accountUpdatePassword', AccountUpdatePassword);

    register.directive('accountProfile', AccountProfile);

    register.controller('AccountDeleteController', AccountDeleteController);
    register.directive('accountDelete', AccountDelete);

    register.controller('AccountController', AccountController);

    // config routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      let locationProvider = {
        title: 'Account',
        templateUrl: 'app/account/details/account.html',
        controller: 'AccountController',
        controllerAs: 'accountController'
      };

      $routeProvider.accessWhen('/account', locationProvider)
        .accessWhen('/account/:tabName', locationProvider);
    });
  }
}
