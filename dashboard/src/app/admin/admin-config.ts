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

import {AdminsUserManagementConfig} from './user-management/user-management-config';
import {OnPremisesAdminLicenseController} from './onprem-administration/license/license.controller';
import {License} from './onprem-administration/license/license.directive';

export class AdminConfig {

  constructor(register: che.IRegisterService) {

    register.directive('cdvyLicense', License);
    register.controller('OnPremisesAdminLicenseController', OnPremisesAdminLicenseController);

    // configure routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      $routeProvider.accessWhen('/onprem/administration', {
        title: 'Administration',
        templateUrl: 'app/admin/onprem-administration/onprem-administration.html'
      });
    });

    new AdminsUserManagementConfig(register);
  }
}
