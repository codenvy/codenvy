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

import {AccountProfile} from './profile/account-profile.directive';
import {AccountProfileCtrl} from './profile/account-profile.controller';
import {AccountDelete} from './account-delete.directive';
import {AccountDeleteCtrl} from './account-delete.controller';
import {AccountUpdatePassword} from './account-update-password.directive';
import {AccountCtrl} from './account.controller';


export class AccountConfig {

  constructor(register) {
    register.directive('accountUpdatePassword', AccountUpdatePassword);

    register.controller('AccountProfileCtrl', AccountProfileCtrl);
    register.directive('accountProfile', AccountProfile);

    register.controller('AccountDeleteCtrl', AccountDeleteCtrl);
    register.directive('accountDelete', AccountDelete);

    register.controller('AccountCtrl', AccountCtrl);

    // config routes
    register.app.config(function ($routeProvider) {
      let locationProvider = {
        templateUrl: 'app/account/details/account.html',
        controller: 'AccountCtrl',
        controllerAs: 'accountCtrl'
      };

      $routeProvider.accessWhen('/account', locationProvider)
        .accessWhen('/account/:tabName', locationProvider);
    });
  }
}
