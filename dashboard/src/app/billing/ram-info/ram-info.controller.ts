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
import {CodenvyResourceLimits} from './../../../components/api/codenvy-resource-limits';

/**
 * @ngdoc controller
 * @name billing.ram:RamInfoController
 * @description This class is handling the controller for displaying RAM info.
 * @author Ann Shumilova
 */
export class RamInfoController {
  /**
   * Subscription API service.
   */
  codenvySubscription: CodenvySubscription;
  $mdDialog: ng.material.IDialogService;
  codenvyResourcesDistribution: CodenvyResourcesDistribution;
  lodash: any;
  /**
   * Current account id, comes from external component.
   */
  accountId: string;
  totalRAM: number;
  usedRAM: number;
  freeRAM: number;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: ng.material.IDialogService, codenvyResourcesDistribution: CodenvyResourcesDistribution,
               codenvySubscription: CodenvySubscription, lodash: any) {
    this.$mdDialog = $mdDialog;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;
    this.codenvySubscription = codenvySubscription;
    this.lodash = lodash;

    this.getRamInfo();
  }

  getRamInfo() {
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
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.codenvyResourcesDistribution.getAvailableTeamResources(this.accountId);
        this.usedRAM = this.totalRAM - this.getRamValue(resources);
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
   * Shows popup
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

  onRAMChanged(): void {
    this.getRamInfo();
  }
}
