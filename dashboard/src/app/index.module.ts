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

import {BillingConfig} from './billing/billing-config';
import {CodenvyNavbarConfig} from './navbar/navbar-config';
import {Register} from './utils/register';
import {CodenvyComponentsConfig} from '../components/components-config';
import {LoginCtrl} from './login/login.controller';
import {AccountConfig} from './account/details/account-config';
import {WorkspaceConfig} from './workspace/workspace-config';
import {MainDashboardConfig} from './dashboard/main-dashboard-config';

let initModule = angular.module('codenvyDashboard', ['ngAnimate', 'ngCookies', 'ngTouch', 'ngSanitize', 'braintree-angular', 'gavruk.card',
  'ngResource', 'ngRoute', 'ui.codemirror', 'ui.gravatar', 'userDashboard', 'ngMessages']);

// add a global resolve flag on all routes (user needs to be resolved first)
initModule.config(['$routeProvider', ($routeProvider) => {
  $routeProvider.accessWhen = (path, route) => {
    route.resolve || (route.resolve = {});
    route.resolve.app = ['cheBranding', '$q', 'chePreferences', (cheBranding, $q, chePreferences) => {
      let deferred = $q.defer();
      if (chePreferences.getPreferences() || DEV) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error) => {
          deferred.reject(error);
        });
      }

      return deferred.promise;
    }];

    return $routeProvider.when(path, route);
  };

  $routeProvider.accessOtherWise = (route) => {
    route.resolve || (route.resolve = {});
    route.resolve.app = ['$q', 'chePreferences', ($q, chePreferences) => {
      let deferred = $q.defer();
      if (chePreferences.getPreferences() || DEV) {
        deferred.resolve();
      } else {
        chePreferences.fetchPreferences().then(() => {
          deferred.resolve();
        }, (error) => {
          deferred.reject(error);
        });
      }

      return deferred.promise;
    }];
    return $routeProvider.otherwise(route);
  };

}]);

// Development mode is set to TRUE
// the build assembly (pom.xml) will replace this mode by false when building the application
// so distribution will have development mode turned off
var DEV = true;

// and setup controllers
initModule.controller('LoginCtrl', LoginCtrl);


// config routes
initModule.config(['$routeProvider', ($routeProvider) => {
  $routeProvider
    .accessWhen('/login', {
      title: 'Login',
      templateUrl: 'app/login/login.html',
      controller: 'LoginCtrl',
      controllerAs: 'loginCtrl'
    })
    .accessOtherWise({
      redirectTo: '/workspaces'
    });
}]);

// add tasks to run
initModule.run(['$rootScope', '$routeParams', 'cheUIElementsInjectorService', 'workspaceDetailsService',
  ($rootScope, $routeParams, cheUIElementsInjectorService, workspaceDetailsService) => {

    $rootScope.$on('$routeChangeSuccess', (event, next) => {
      if (next.$$route.title && angular.isFunction(next.$$route.title)) {
        $rootScope.currentPage = next.$$route.title($routeParams);
      } else {
        $rootScope.currentPage = next.$$route.title || 'Dashboard';
      }
    });

    workspaceDetailsService.addSection('Idle timeout', 'The time period during which the workspace may be not used, before being stopped.', '<timeout-info></timeout-info>');
  }]);

// add interceptors
initModule.factory('AuthInterceptor', ($window, $cookies, $q, $location, $log) => {
  return {
    request: (config) => {
      //remove prefix url
      if (config.url.indexOf('https://codenvy.com/api') === 0) {
        config.url = config.url.substring('https://codenvy.com'.length);
      }

      let authHeader = config.headers['Authorization'];

      // do not add token on auth login
      if (config.url.indexOf('/api/auth/login') === -1 && config.url.indexOf('api/') !== -1 && $window.sessionStorage['codenvyToken'] && (!authHeader || authHeader.length === 0)) {
        config.headers['Authorization'] = $window.sessionStorage['codenvyToken'];
      }
      return config || $q.when(config);
    },
    response: (response) => {
      return response || $q.when(response);
    },
    responseError: (rejection) => {
      // handle only api call
      if (rejection.config) {
        if (rejection.config.url.indexOf('localhost') > 0 || rejection.config.url.startsWith('/api/user') > 0) {
          if (rejection.status === 401 || rejection.status === 403) {
            $log.info('Redirect to login page.');
            $location.path('/login');

          }
        }
      }
      return $q.reject(rejection);
    }
  };
});

initModule.constant('userDashboardConfig', {
  developmentMode: DEV
});

// This can not be moved to separate factory class, because it is not fits into
// model how Angular works with them. When we override request and responseError
// functions, they are called in another context, without creating new class instance,
// and "this" became undefined.
// See http://stackoverflow.com/questions/30978743/how-can-this-be-undefined-in-the-constructor-of-an-angular-config-class
initModule.factory('AddMachineTokenToUrlInterceptor', ($injector, $q) => {
  var tokens = {};

  function requestToken(workspaceId) {

    let promise = $injector.get('$http').get('/api/machine/token/' + workspaceId);

    return promise.then((resp) => {
      tokens[workspaceId] = resp.data.machineToken;
      return tokens[workspaceId];
    }, (error) => {
      if (error.status === 304) {
        return tokens[workspaceId];
      }
    });
  }

  function getWorkspaceId(url) {
    let workspaceId;
    // in case of injection 'cheWorkspace' we will get an error with 'circular dependency found' message,
    // so to avoid this we need to use injector.get() directly.
    $injector.get('cheWorkspace').getWorkspaceAgents().forEach((value, key) => {
      if (url.startsWith(value.workspaceAgentData.path)) {
        workspaceId = key;
      }
    });
    return workspaceId;
  }

  return {
    request: (config) => {
      let workspaceId = getWorkspaceId(config.url);
      if (!workspaceId) {
        return config || $q.when(config);
      }

      return $q.when(tokens[workspaceId] || requestToken(workspaceId))
        .then((token) => {
          config.headers['Authorization'] = token;
          return config;
        });
    },

    responseError: (rejection) => {
      let workspaceId = getWorkspaceId(rejection.config.url);
      if (rejection && workspaceId && (rejection.status === 401 || rejection.status === 503)) {
        delete tokens[workspaceId];
      }
      return $q.reject(rejection);
    }
  };
});

// prevents CSRF(see https://en.wikipedia.org/wiki/Cross-site_request_forgery)
// using additional token header, see
// https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CSRF_Prevention_Filter_for_REST_APIs
initModule.factory('CsrfPreventionInterceptor', ($injector, $q) => {
  const CSRF_TOKEN_HEADER_NAME = 'X-CSRF-Token';
  let csrfToken = '';

  function isModifyingMethod(method: string): boolean {
    return method === 'POST' || method === 'PUT' || method === 'DELETE';
  };

  function isMachineRequest(url: string): boolean {
    let agents = $injector.get('cheWorkspace').getWorkspaceAgents();
    for (let agent of agents.values()) {
      if (url.startsWith(agent.workspaceAgentData.path)) {
        return true;
      }
    }
    return false;
  };

  function requestCSRFToken() {
    // request X-CSRF-Token through any GET requests, which requires user to be logged-in
    // and is not used by dashboard to avoid 304:
    let promise = $injector.get('$http').get('/api/user/settings');

    return promise.then((response: any) => {
      let respCsrfToken = response.headers(CSRF_TOKEN_HEADER_NAME);
      if (respCsrfToken) {
        csrfToken = respCsrfToken;
      }
      return respCsrfToken;
    }, (error: any) => {
      if (error.status === 304) {
        return csrfToken;
      }
    });
  }

  return {
    request: (config: any) => {
      // no need to add X-CSRF-Token to machine requests
      if (isMachineRequest(config.url)) {
        return config;
      }

      // the X-CSRF-Token=Fetch if the request method is 'GET' and token is not fetched yet
      if (config.method === 'GET' && !csrfToken) {
        config.headers[CSRF_TOKEN_HEADER_NAME] = 'Fetch';
        return config;
      }

      // the X-CSRF-Token=0ABCD(actual token) if request modifies server state and token is fetched
      if (isModifyingMethod(config.method)) {
        return $q.when((csrfToken) || requestCSRFToken()).then((token: string) => {
            config.headers[CSRF_TOKEN_HEADER_NAME] = token;
            return config;
          });
      }

      return config;
    },

    // gets fetched X-CSRF-Token and caches it
    response: (response: any) => {
      var respCsrfToken = response.headers(CSRF_TOKEN_HEADER_NAME);
      if (respCsrfToken && response.config.method === 'GET') {
        csrfToken = respCsrfToken;
      }
      return response;
    }
  };
});

initModule.config(['$routeProvider', '$locationProvider', '$httpProvider', ($routeProvider, $locationProvider, $httpProvider) => {
  $httpProvider.interceptors.push('AddMachineTokenToUrlInterceptor');
  $httpProvider.interceptors.push('CsrfPreventionInterceptor');
  if (DEV) {
    console.log('adding auth interceptor');
    $httpProvider.interceptors.push('AuthInterceptor');
  }
}]);

angular.module('ui.gravatar').config(['gravatarServiceProvider', (gravatarServiceProvider) => {
  gravatarServiceProvider.defaults = {
    size: 43,
    default: 'mm'  // mystery man as default for missing avatars
  };

  // Use https endpoint
  gravatarServiceProvider.secure = true;

}
]);

var instanceRegister = new Register(initModule);
new MainDashboardConfig(instanceRegister);
new BillingConfig(instanceRegister);
new CodenvyNavbarConfig(instanceRegister);
new CodenvyComponentsConfig(instanceRegister);
new AccountConfig(instanceRegister);
new WorkspaceConfig(instanceRegister);
