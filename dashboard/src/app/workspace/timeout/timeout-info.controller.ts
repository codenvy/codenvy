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
  }

  /**
   * Fetches the team's details by it's name.
   */
  fetchTeamDetails(name): void {
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
    this.timeoutValue =  timeout ? (timeout.amount < 60 ? (timeout.amount + ' minutes') : (timeout.amount / 60 + ' hours')) : '';
  }

  /**
   * Retrieves RAM information.
   */
  getRamInfo() {
    let accountId = this.team.parent || this.team.id;

    this.codenvySubscription.fetchLicense(accountId).then(() => {
      this.processLicense(this.codenvySubscription.getLicense(accountId));
    }, (error: any) => {
      if (error.status === 304) {
        this.processLicense(this.codenvySubscription.getLicense(accountId));
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

    this.codenvyResourcesDistribution.fetchUsedTeamResources(this.team.id).then(() => {
      let resources = this.codenvyResourcesDistribution.getUsedTeamResources(this.team.id);
      this.usedRAM = this.getRamValue(resources);
      this.getMoreRAM();
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.codenvyResourcesDistribution.getUsedTeamResources(this.team.id);
        this.usedRAM = this.getRamValue(resources);
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
    let accountId = this.team.parent || this.team.id;
    this.$mdDialog.show({
      controller: 'MoreRamController',
      controllerAs: 'moreRamController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        accountId: accountId,
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
