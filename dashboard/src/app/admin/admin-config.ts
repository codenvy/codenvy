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
import {OnPremisesAdminInstallationConfig} from './onprem-administration/installation/installation-config';
import {OnPremisesAdminAvailableSoftwareCtrl} from './onprem-administration/software/software.controller';
import {AvailableSoftwarePanel} from './onprem-administration/software/software.directive';
import {OnPremisesAdminYourLicenseCtrl} from './onprem-administration/yourlicense/yourlicense.controller';
import {YourLicensePanel} from './onprem-administration/yourlicense/yourlicense.directive';

export class AdminConfig {

  constructor(register) {

    register.directive('cdvyAvailableSoftware', AvailableSoftwarePanel);
    register.directive('cdvyYourLicense', YourLicensePanel);
    register.controller('OnPremisesAdminAvailableSoftwareCtrl', OnPremisesAdminAvailableSoftwareCtrl);
    register.controller('OnPremisesAdminYourLicenseCtrl', OnPremisesAdminYourLicenseCtrl);

    // configure routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/onprem/administration', {
        title: 'Administration',
        templateUrl: 'app/admin/onprem-administration/onprem-administration.html'
      });
    });

    new AdminsUserManagementConfig(register);
    new OnPremisesAdminInstallationConfig(register);
  }
}
