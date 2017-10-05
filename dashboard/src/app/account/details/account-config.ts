/*
 * Copyright (c) [2015] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

import {CodenvyAccountProfile} from './profile/codenvy-account-profile.directive';
import {AccountDelete} from './account-delete.directive';
import {AccountDeleteController} from './account-delete.controller';
import {AccountUpdatePassword} from './account-update-password.directive';
import {AccountController} from './account.controller';


export class AccountConfig {

  constructor(register: che.IRegisterService) {
    register.directive('accountUpdatePassword', AccountUpdatePassword);

    register.directive('codenvyAccountProfile', CodenvyAccountProfile);

    register.controller('AccountDeleteController', AccountDeleteController);
    register.directive('accountDelete', AccountDelete);

    register.controller('AccountController', AccountController);

    // config routes
    register.app.config(($routeProvider: che.route.IRouteProvider) => {
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
