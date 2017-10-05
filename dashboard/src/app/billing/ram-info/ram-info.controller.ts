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
  cheResourcesDistribution: che.api.ICheResourcesDistribution;
  resourceLimits: che.resource.ICheResourceLimits;
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
  constructor ($mdDialog: ng.material.IDialogService, cheResourcesDistribution: che.api.ICheResourcesDistribution,
               codenvySubscription: CodenvySubscription, lodash: any, resourcesService: che.service.IResourcesService) {
    this.$mdDialog = $mdDialog;
    this.cheResourcesDistribution = cheResourcesDistribution;
    this.codenvySubscription = codenvySubscription;
    this.lodash = lodash;
    this.resourceLimits = resourcesService.getResourceLimits();
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

    this.cheResourcesDistribution.fetchAvailableOrganizationResources(this.accountId).then(() => {
      let resources = this.cheResourcesDistribution.getAvailableOrganizationResources(this.accountId);
      this.usedRAM = this.totalRAM - this.getRamValue(resources);
    }, (error: any) => {
      if (error.status === 304) {
        let resources = this.cheResourcesDistribution.getAvailableOrganizationResources(this.accountId);
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
      return resource.type === this.resourceLimits.RAM;
    });
    return (ram && ram.amount !== -1) ? (ram.amount / 1024) : ram.amount;
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
