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
import {CodenvySubscription} from '../../../components/api/codenvy-subscription.factory';
import {CodenvyResourcesDistribution} from './../../../components/api/codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';

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
  codenvyTeam: CodenvyTeam;
  $mdDialog: ng.material.IDialogService;
  codenvyResourcesDistribution: CodenvyResourcesDistribution;
  lodash: any;

  team: any;
  totalRAM: number;
  usedRAM: number;
  freeRAM: number;
  timeoutValue: string;
  timeout: string;
  accountId: string;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: ng.material.IDialogService, $route: ng.route.IRouteService, codenvyTeam: CodenvyTeam,
               codenvyResourcesDistribution: CodenvyResourcesDistribution,
               codenvySubscription: CodenvySubscription, lodash: any) {
    this.$mdDialog = $mdDialog;
    this.codenvyTeam = codenvyTeam;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.codenvySubscription = codenvySubscription;
    this.lodash = lodash;

    this.fetchTeamDetails($route.current.params.namespace);
    this.getPackages();
  }

  /**
   * Fetches the team's details by it's name.
   */
  fetchTeamDetails(name: string): void {
    this.team  = this.codenvyTeam.getTeamByName(name);
    if (!this.team) {
      this.codenvyTeam.fetchTeamByName(name).then((team: any) => {
        this.team = team;
        this.fetchTimeoutValue();
      }, (error: any) => {
        if (error.status === 304) {
          this.team = this.codenvyTeam.getTeamByName(name);
          this.fetchTimeoutValue();
        }
      });
    } else {
      this.fetchTimeoutValue();
    }
  }

  /**
   * Fetches team's available resources to process timeout.
   */
  fetchTimeoutValue(): void {
    this.codenvyResourcesDistribution.fetchAvailableTeamResources(this.team.id).then(() => {
      this.processTimeoutValue(this.codenvyResourcesDistribution.getAvailableTeamResources(this.team.id));
    }, (error: any) => {
      if (error.status === 304) {
        this.processTimeoutValue(this.codenvyResourcesDistribution.getAvailableTeamResources(this.team.id));
      }
    });
  }

  /**
   * Process resources to find timeout resource's value.
   *
   * @param resources
   */
  processTimeoutValue(resources: Array<any>): void {
    if (!resources || resources.length === 0) {
      return;
    }

    let timeout = this.lodash.find(resources, (resource: any) => {
      return resource.type === CodenvyResourceLimits.TIMEOUT;
    });
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
      return pack.type === CodenvyResourceLimits.RAM;
    });

    if (!ramPackage) {
      this.timeout = '4 hour';
      return;
    }

    let timeoutResource = this.lodash.find(ramPackage.resources, (resource: any) => {
      return resource.type === CodenvyResourceLimits.TIMEOUT;
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

    this.codenvyResourcesDistribution.fetchAvailableTeamResources(this.accountId).then(() => {
      let resources = this.codenvyResourcesDistribution.getAvailableTeamResources(this.accountId);
      this.usedRAM = this.totalRAM - this.getRamValue(resources);
      this.getMoreRAM();
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.codenvyResourcesDistribution.getAvailableTeamResources(this.accountId);
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
      return resource.type === CodenvyResourceLimits.RAM;
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
    this.fetchTimeoutValue();
  }
}
