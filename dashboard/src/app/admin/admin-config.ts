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

import {OnPremisesAdminLicenseController} from './onprem-administration/license/license.controller';
import {AdminsUserManagementConfig} from './user-management/user-management-config';
import {License} from './onprem-administration/license/license.directive';

export class AdminConfig {

  constructor(register: che.IRegisterService) {

    register.directive('cdvyLicense', License);
    register.controller('OnPremisesAdminLicenseController', OnPremisesAdminLicenseController);

    // configure routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      ($routeProvider as any).accessWhen('/onprem/administration', {
        title: 'Administration',
        templateUrl: 'app/admin/onprem-administration/onprem-administration.html'
      });
    });

    new AdminsUserManagementConfig(register);
  }
}
