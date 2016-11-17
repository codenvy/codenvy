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

export class CodenvyNavBarCtrl {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav, $scope, $location, $route, userDashboardConfig, cheAPI, codenvyAPI, $rootScope, $http, $window, $cookies, $resource) {
    this.mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.codenvyAPI = codenvyAPI;
    this.codenvyUser = codenvyAPI.getUser();
    this.codenvyPermissions = codenvyAPI.getPermissions();
    this.links = [{href: '#/create-workspace', name: 'New Workspace'}];
    this.$rootScope = $rootScope;
    this.$window = $window;
    this.$resource = $resource;
    this.$cookies = $cookies;
    this.logoutAPI = this.$resource('/api/auth/logout', {});

    this.userServices = this.codenvyPermissions.getUserServices();
    if (!this.codenvyPermissions.getSystemPermissions()) {
      this.codenvyPermissions.fetchSystemPermissions();
    }

    this.displayLoginItem = userDashboardConfig.developmentMode;
    let promiseService = this.cheAPI.getService().fetchServices();
    promiseService.then(() => {
      this.isFactoryServiceAvailable = cheAPI.getService().isServiceAvailable(codenvyAPI.getFactory().getFactoryServicePath());
    });

    let promiseAdminService = this.cheAPI.getAdminService().fetchServices();
    promiseAdminService.then(() => {
      this.isAdminServiceAvailable = cheAPI.getAdminService().isAdminServiceAvailable();
      this.isAdminPluginServiceAvailable = cheAPI.getAdminService().isServiceAvailable(cheAPI.getAdminPlugins().getPluginsServicePath());
    });

    this.profile = cheAPI.getProfile().getProfile();
    if (this.profile.attributes) {
      this.email = this.profile.attributes.email;
    } else {
      this.profile.$promise.then(() => {
        this.email = this.profile.attributes.email ? this.profile.attributes.email : 'N/A ';
      }, () => {
        this.email = 'N/A ';
      });
    }
    this.onpremAdminExpanded = true;

    this.menuItemUrl = {
      login: '/site/login',
      dashboard: '#/',
      workspaces: '#/workspaces',
      stacks: '#/stacks',
      factories: '#/factories',
      administration: '#/onprem/administration',
      usermanagement: '#/admin/usermanagement',

      // subsection
      plugins: '#/admin/plugins'
    };

    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    // update branding
    let assetPrefix = 'assets/branding/';
    $http.get(assetPrefix + 'product.json').then((data) => {
      if (data.data.navbarButton) {
        this.$rootScope.branding.navbarButton = {
          title: data.data.navbarButton.title,
          tooltip: data.data.navbarButton.tooltip,
          link: data.data.navbarButton.link
        };
        this.accountItems.splice(2, 0, {
          name: data.data.navbarButton.title,
          url: data.data.navbarButton.link
        });
      }
    });

    cheAPI.cheWorkspace.fetchWorkspaces();

    // account dropdown items
    this.accountItems = [
      {
        name: 'Profile & Account',
        url: '#/account'
      }, {
        name: 'Administration',
        url: '#/administration'
      }, {
        name: 'Logout',
        onclick: () => {
          this.logout();
        }
      }
    ];
  }

  reload() {
    this.$route.reload();
  }

  /**
   * Toggle the left menu
   */
  toggleLeftMenu() {
    this.mdSidenav('left').toggle();
  }

  getWorkspacesNumber() {
    return this.cheAPI.cheWorkspace.getWorkspaces().length;
  }

  getFactoriesNumber() {
    let pagesInfo = this.codenvyAPI.codenvyFactory.getPagesInfo();
    return pagesInfo && pagesInfo.count ? pagesInfo.count : this.codenvyAPI.codenvyFactory.factoriesById.size;
  }

  openLinkInNewTab(url) {
    this.$window.open(url, '_blank');
  }

  /**
   * Logout current user
   */
  logout() {
    let data = {token: this.$cookies['session-access-key']};
    let promise = this.logoutAPI.save(data).$promise;
    promise.then(() => {
      this.$rootScope.showIDE = false;
      this.$window.location.href = this.menuItemUrl.login;
    });
  }
}
