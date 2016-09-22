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

import {AdminsAddUserCtrl} from './add-user/add-user.controller';
import {AdminsUserManagementCtrl} from './user-management.controller';

export class AdminsUserManagementConfig {

  constructor(register) {
    register.controller('AdminsAddUserCtrl', AdminsAddUserCtrl);
    register.controller('AdminsUserManagementCtrl', AdminsUserManagementCtrl);

    // configure routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/admin/usermanagement', {
        title: 'Users',
        templateUrl: 'app/admin/user-management/user-management.html',
        controller: 'AdminsUserManagementCtrl',
        controllerAs: 'adminsUserManagementCtrl',
        resolve: {
          check: ['$q', 'codenvyPermissions', ($q, codenvyPermissions) => {
            let defer = $q.defer();
            if (codenvyPermissions.getUserServices().hasUserService) {
              defer.resolve();
            } else {
              defer.reject();
            }
            return defer.promise;
          }]
        }
      });
    });

  }
}
