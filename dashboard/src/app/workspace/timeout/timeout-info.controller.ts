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
import {CodenvySubscription} from '../../../components/api/codenvy-subscription.factory';

/**
 * Controller for timeout information widget.
 *
 * @author Ann Shumilova
 */
export class TimeoutInfoController {
  /**
   * Subscription API service.
   */
  codenvySubscription: CodenvySubscription;
  cheTeam: che.api.ICheTeam;
  cheUser: any;
  $mdDialog: ng.material.IDialogService;
  cheResourcesDistribution: che.api.ICheResourcesDistribution;
  resourceLimits: che.resource.ICheResourceLimits;
  lodash: any;

  team: any;
  totalRAM: number;
  usedRAM: number;
  freeRAM: number;
  timeoutValue: string;
  timeout: string;
  accountId: string;
  canBuy: boolean;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: ng.material.IDialogService, $route: ng.route.IRouteService, cheTeam: che.api.ICheTeam,
               cheResourcesDistribution: che.api.ICheResourcesDistribution, cheUser: any,
               codenvySubscription: CodenvySubscription, lodash: any, resourcesService: che.service.IResourcesService) {
    this.$mdDialog = $mdDialog;
    this.cheTeam = cheTeam;
    this.cheResourcesDistribution = cheResourcesDistribution;
    this.codenvySubscription = codenvySubscription;
    this.cheUser = cheUser;
    this.lodash = lodash;
    this.resourceLimits = resourcesService.getResourceLimits();

    this.fetchTeamDetails($route.current.params.namespace);
    this.getPackages();
  }

  /**
   * Fetches the team's details by it's name.
   *
   * @param name {string}
   *
   */
  fetchTeamDetails(name: string): void {
    this.team  = this.cheTeam.getTeamByName(name);
    if (!this.team) {
      this.cheTeam.fetchTeamByName(name).then((team: any) => {
        this.team = team;
        this.fetchTimeoutValue(this.team.id);
      }, (error: any) => {
        if (error.status === 304) {
          this.team = this.cheTeam.getTeamByName(name);
          this.fetchTimeoutValue(this.team.id);
        } else if (error.status === 404 && !this.cheTeam.getPersonalAccount() && this.cheUser.getUser().name === name) {
          this.fetchTimeoutValue(this.cheUser.getUser().id);
        }
      });
    } else {
      this.fetchTimeoutValue(this.team.id);
    }
  }

  /**
   * Fetches available resources to process timeout by provided id.
   *
   * @param id id of the instance to fetch available resources
   */
  fetchTimeoutValue(id: string): void {
    this.cheResourcesDistribution.fetchAvailableOrganizationResources(id).then(() => {
      this.processTimeoutValue(this.cheResourcesDistribution.getAvailableOrganizationResources(id));
    }, (error: any) => {
      if (error.status === 304) {
        this.processTimeoutValue(this.cheResourcesDistribution.getAvailableOrganizationResources(id));
      }
    });
  }

  /**
   * Process resources to find timeout resource's value.
   *
   * @param resources {Array<any>}
   */
  processTimeoutValue(resources: Array<any>): void {
    if (!resources || resources.length === 0) {
      return;
    }

    let timeout = this.lodash.find(resources, (resource: any) => {
      return resource.type === this.resourceLimits.TIMEOUT;
    });

    this.canBuy = (this.cheTeam.getPersonalAccount() && this.team && this.cheTeam.getPersonalAccount().id === this.team.id);

    this.timeoutValue =  timeout ? (timeout.amount < 60 ? (timeout.amount + ' minute') : (timeout.amount / 60 + ' hour')) : '';
  }

  /**
   * Fetches the list of packages.
   */
  getPackages(): void {
    this.codenvySubscription.fetchPackages().then(() => {
      this.processPackages(this.codenvySubscription.getPackages());
    }, (error: any) => {
      if (error.status === 304) {
        this.processPackages(this.codenvySubscription.getPackages());
      } else {
        this.timeout = null;
      }
    });
  }

  /**
   * Processes packages to get RAM resources details.
   *
   * @param packages list of packages
   */
  processPackages(packages: Array<any>): void {
    let ramPackage = this.lodash.find(packages, (pack: any) => {
      return pack.type === this.resourceLimits.RAM;
    });

    if (!ramPackage) {
      this.timeout = '4 hour';
      return;
    }

    let timeoutResource = this.lodash.find(ramPackage.resources, (resource: any) => {
      return resource.type === this.resourceLimits.TIMEOUT;
    });

    this.timeout = timeoutResource ? (timeoutResource.amount < 60 ? (timeoutResource.amount + ' minute') : (timeoutResource.amount / 60 + ' hour')) : '4 hour';
  }

  /**
   * Retrieves RAM information.
   */
  getRamInfo() {
    this.accountId = this.team.parent || this.team.id;

    this.codenvySubscription.fetchLicense(this.accountId).then(() => {
      this.processLicense(this.codenvySubscription.getLicense(this.accountId));
    }, (error: any) => {
      if (error.status === 304) {
        this.processLicense(this.codenvySubscription.getLicense(this.accountId));
      }
    });
  }

  /**
   * Processes license, retrieves free resources info.
   *
   * @param license
   */
  processLicense(license: any): void {
    let details = license.resourcesDetails;
    let freeResources = this.lodash.find(details, (resource: any) => {
      return resource.providerId === 'free';
    });

    if (!freeResources) {
      this.freeRAM = 0;
    } else {
      this.freeRAM = this.getRamValue(freeResources.resources);
    }

    this.totalRAM = this.getRamValue(license.totalResources);

    this.cheResourcesDistribution.fetchAvailableOrganizationResources(this.accountId).then(() => {
      let resources = this.cheResourcesDistribution.getAvailableOrganizationResources(this.accountId);
      this.usedRAM = this.totalRAM - this.getRamValue(resources);
      this.getMoreRAM();
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.cheResourcesDistribution.getAvailableOrganizationResources(this.accountId);
        this.usedRAM = this.totalRAM - this.getRamValue(resources);
        this.getMoreRAM();
      }
    });

  }

  /**
   *
   * @param resources
   */
  getRamValue(resources: Array<any>): number {
    if (!resources || resources.length === 0) {
      return 0;
    }

    let ram = this.lodash.find(resources, (resource: any) => {
      return resource.type === this.resourceLimits.RAM;
    });
    return ram ? (ram.amount / 1024) : 0;
  }

  /**
   * Shows popup.
   */
  getMoreRAM(): void {
    this.$mdDialog.show({
      controller: 'MoreRamController',
      controllerAs: 'moreRamController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        accountId: this.accountId,
        totalRAM: this.totalRAM,
        usedRAM: this.usedRAM,
        freeRAM: this.freeRAM,
        callbackController: this
      },
      templateUrl: 'app/billing/ram-info/more-ram-dialog.html'
    });
  }

  /**
   * Handler for RAM changed event.
   */
  onRAMChanged(): void {
    this.fetchTimeoutValue(this.team.id);
  }
}
