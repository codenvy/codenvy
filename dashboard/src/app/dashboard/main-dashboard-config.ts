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

import {MainDashboardController} from './main-dashboard.controller';

export class MainDashboardConfig {

  constructor(register) {
    // controller
    register.controller('MainDashboardController', MainDashboardController);

    // config routes
    register.app.config(($routeProvider) => {
      $routeProvider.accessWhen('/', {
        title: 'Dashboard',
        templateUrl: 'app/dashboard/main-dashboard.html',
        controller: 'MainDashboardController',
        controllerAs: 'mainDashboardController'
      });
    })
    ;
  }
}


