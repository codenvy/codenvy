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

import {AdminsAddUserController} from './add-user/add-user.controller';
import {AdminsUserManagementCtrl} from './user-management.controller';
import {AdminUserDetailsController} from './user-details/user-details.controller';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';

export class AdminsUserManagementConfig {

  constructor(register: che.IRegisterService) {
    register.controller('AdminUserDetailsController', AdminUserDetailsController);
    register.controller('AdminsAddUserController', AdminsAddUserController);
    register.controller('AdminsUserManagementCtrl', AdminsUserManagementCtrl);


    const userDetailLocationProvider = {
      title: 'User Details',
      reloadOnSearch: false,
      templateUrl: 'app/admin/user-management/user-details/user-details.html',
      controller: 'AdminUserDetailsController',
      controllerAs: 'adminUserDetailsController',
      resolve: {
        initData: ['$q', 'cheUser', '$route', 'codenvyPermissions', ($q: ng.IQService, cheUser: any, $route: any, codenvyPermissions: CodenvyPermissions) => {
          const userId = $route.current.params.userId;
          let defer = $q.defer();
          codenvyPermissions.fetchSystemPermissions().finally(() => {
            cheUser.fetchUserId(userId).then((user: che.IUser) => {
              if (!codenvyPermissions.getUserServices().hasAdminUserService) {
                defer.reject();
              }
              defer.resolve({userId: userId, userName: user.name});
            }, (error: any) => {
              defer.reject(error);
            });
          });
          return defer.promise;
        }]
      }
    };

    // configure routes
    register.app.config(($routeProvider: ng.route.IRouteProvider) => {
      $routeProvider.accessWhen('/admin/usermanagement', {
        title: 'Users',
        templateUrl: 'app/admin/user-management/user-management.html',
        controller: 'AdminsUserManagementCtrl',
        controllerAs: 'adminsUserManagementCtrl',
        resolve: {
          check: ['$q', 'codenvyPermissions', ($q: ng.IQService, codenvyPermissions: CodenvyPermissions) => {
            let defer = $q.defer();
            codenvyPermissions.fetchSystemPermissions().finally(() => {
              if (codenvyPermissions.getUserServices().hasUserService) {
                defer.resolve();
              } else {
                defer.reject();
              }
            });
            return defer.promise;
          }]
        }
      })
        .accessWhen('/admin/userdetails/:userId', userDetailLocationProvider);
    });

  }
}
