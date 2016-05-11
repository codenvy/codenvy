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
  constructor($mdSidenav, $scope, $location, $route, userDashboardConfig, cheAPI, codenvyAPI, onBoarding, imsArtifactApi, $rootScope, $http) {
    this.mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.codenvyAPI = codenvyAPI;
    this.onBoarding = onBoarding;
    this.imsArtifactApi = imsArtifactApi;
    this.cheUser = cheAPI.getUser();
    this.links = [{href: '#/create-workspace', name: 'New Workspace'}];
    this.$rootScope = $rootScope;

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

    this.cheUser.fetchUser();

    this.ims = this.imsArtifactApi.getIms();

    this.menuItemUrl = {
      login: '#/login',
      dashboard: '#/',
      projects: '#/projects',
      workspaces: '#/workspaces',
      factories: '#/factories',
      administration: '#/onprem/administration',
      usermanagement: '#/admin/usermanagement',

      // subsection
      plugins: '#/admin/plugins',

      // subsection
      account: '#/account',
      team: '#/team'
    };

    // clear highlighting of menu item from navbar
    // if route is not part of navbar
    // or restore highlighting otherwise
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path(),
        match = Object.keys(this.menuItemUrl).some(item => this.menuItemUrl[item] === path);
      if (match) {
        $scope.$broadcast('navbar-selected:restore', path);
      }
      else {
        $scope.$broadcast('navbar-selected:clear');
      }
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
      }
    });

    cheAPI.cheWorkspace.fetchWorkspaces();
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

  userIsAdmin() {
    return this.cheUser.isAdmin();
  }

  isUser() {
    return this.cheUser.isUser();
  }

  getWorkspacesNumber() {
    return this.cheAPI.cheWorkspace.getWorkspaces().length;
  }
}
