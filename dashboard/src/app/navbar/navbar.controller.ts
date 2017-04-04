/*
 *  [2015] - [2017] Codenvy, S.A.
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
import {CodenvyAPI} from '../../components/api/codenvy-api.factory';
import {CodenvyPermissions} from '../../components/api/codenvy-permissions.factory';

interface IUserServices {
  hasUserService: boolean;
  hasUserProfileService: boolean;
  hasAdminUserService: boolean;
  hasInstallationManagerService: boolean;
  hasLicenseService: boolean;
}

export class CodenvyNavBarController {
  menuItemUrl = {
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
  // account dropdown items
  accountItems = [
    {
      name: 'Profile & Account',
      url: '#/account'
    },
    {
      name: 'Administration',
      url: '#/administration'
    }, {
      name: 'Logout',
      onclick: () => {
        this.logout();
      }
    }
  ];
  links = [{
    href: '#/create-workspace',
    name: 'New Workspace'
  }];
  displayLoginItem: boolean;
  onpremAdminExpanded: boolean;
  isAdminServiceAvailable: boolean;
  isFactoryServiceAvailable: boolean;
  isBillingServiceAvailable: boolean;
  isAdminPluginServiceAvailable: boolean;

  private $scope: ng.IScope;
  private $window: ng.IWindowService;
  private $location: ng.ILocationService;
  private $route: ng.route.IRouteService;
  private $rootScope: ng.IRootScopeService;
  private $cookies: ng.cookies.ICookiesService;
  private $resource: ng.resource.IResourceService;
  private $mdSidenav: ng.material.ISidenavService;
  private userServices: IUserServices;
  private codenvyAPI: CodenvyAPI;
  private cheFactory: any;
  private codenvyPermissions: CodenvyPermissions;
  private cheAPI: any;
  private profile: any;
  private logoutAPI: any;
  private email: string;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($mdSidenav: ng.material.ISidenavService, $scope: ng.IScope, $location: ng.ILocationService,
              $route: ng.route.IRouteService, userDashboardConfig: any, cheAPI: any, codenvyAPI: CodenvyAPI,
              $rootScope: ng.IRootScopeService, $http: ng.IHttpService, $window: ng.IWindowService,
              $cookies: ng.cookies.ICookiesService, $resource: ng.resource.IResourceService) {
    this.$mdSidenav = $mdSidenav;
    this.$scope = $scope;
    this.$location = $location;
    this.$route = $route;
    this.cheAPI = cheAPI;
    this.codenvyAPI = codenvyAPI;
    this.cheFactory = cheAPI.getFactory();
    this.codenvyPermissions = codenvyAPI.getPermissions();
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
      this.isFactoryServiceAvailable = cheAPI.getService().isServiceAvailable(this.cheFactory.getFactoryServicePath());
      let isBillingServiceAvailable = cheAPI.getService().isServiceAvailable(codenvyAPI.getPayment().getPaymentServicePath());
      if (isBillingServiceAvailable) {
        this.accountItems.splice(1, 0, {
          name: 'Billing',
          url: '#/billing'
        });
      }
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


    // highlight navbar menu item
    $scope.$on('$locationChangeStart', () => {
      let path = '#' + $location.path();
      $scope.$broadcast('navbar-selected:set', path);
    });

    // update branding
    let assetPrefix = 'assets/branding/';
    $http.get(assetPrefix + 'product.json').then((data: any) => {
      if (data.data.navbarButton) {
        (this.$rootScope as any).branding.navbarButton = {
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
  }

  reload(): void {
    this.$route.reload();
  }

  /**
   * Toggle the left menu
   */
  toggleLeftMenu(): void {
    this.$mdSidenav('left').toggle();
  }

  getWorkspacesNumber(): number {
    return this.cheAPI.cheWorkspace.getWorkspaces().length;
  }

  getFactoriesNumber(): number {
    let pagesInfo = this.cheFactory.getPagesInfo();
    return pagesInfo && pagesInfo.count ? pagesInfo.count : this.cheFactory.factoriesById.size;
  }

  openLinkInNewTab(url: string): void {
    this.$window.open(url, '_blank');
  }

  /**
   * Logout current user
   */
  logout(): void {
    let data = {token: this.$cookies['session-access-key']};
    let promise = this.logoutAPI.save(data).$promise;
    promise.then(() => {
      (this.$rootScope as any).showIDE = false;
      this.$window.location.href = this.menuItemUrl.login;
      this.$cookies.remove('LICENSE_EXPIRED');
    });
  }
}
