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
import {ICreditCard} from '../../../components/api/codenvy-payment.factory';
import {BillingService} from '../billing.service';

enum Step {
  ONE = 1,
  TWO
}

/**
 * @ngdoc controller
 * @name billing.ram:MoreRamController
 * @description This class is handling the controller for getting more RAM.
 * @author Ann Shumilova
 */
export class MoreRamController {
  /**
   * Service for displaying dialogs.
   */
  private $mdDialog: angular.material.IDialogService;
  /**
   * Angular promise service.
   */
  private $q: ng.IQService;
  /**
   * Subscription API service.
   */
  private codenvySubscription: CodenvySubscription;
  /**
   *  Billing service.
   */
  private billingService: BillingService;
  /**
   * Lodash library.
   */
  private lodash: any;
  /**
   * Notification service.
   */
  private cheNotification: any;

  /**
   * New RAM value.
   */
  private value: number;
  /**
   * Current account's id (set from outside).
   */
  private accountId: string;
  /**
   * Callback controller (set from outside).
   */
  private callbackController: any;
  /**
   * Provided free RAM. (set from outside).
   */
  private freeRAM: number;
  /**
   * Provided total RAM. (set from outside).
   */
  private totalRAM: number;
  /**
   * Price of the resources. Is retrieved from package details.
   */
  private price: number;
  /**
   * Price for the resources by portion of time. Is retrieved from package details.
   */
  private partialPrice: number;
  /**
   * Amount of resources, that are paid for. Is retrieved from package details.
   */
  private amount: string;
  /**
   * Team workspaces idle timeout. Is retrieved from package details.
   */
  private timeout: number;
  /**
   * Team workspaces idle timeout resource. Is retrieved from package details.
   */
  private timeoutResource: any;
  /**
   * The next month charge date.
   */
  private nextMonthChargeDate: Date;
  /**
   * The number of days left till the end of current month.
   */
  private leftDaysInMonth: number;
  /**
   * Minimum amount of resources, that can be bought. Is retrieved from package details.
   */
  private minValue: number;
  /**
   * Maximum amount of resources, that can be bought. Is retrieved from package details.
   */
  private maxValue: number;
  /**
   * Package with RAM type.
   */
  private ramPackage: any;
  /**
   * Loading state of the dialog.
   */
  private isLoading: boolean;
  /**
   * Steps to use them in dialog template.
   */
  private step: Object;
  /**
   * Current step of wizard.
   */
  private currentStep: number;
  /**
   * Credit card data.
   */
  private creditCard: ICreditCard;

  private resourceLimits: che.resource.ICheResourceLimits;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($mdDialog: angular.material.IDialogService, $q: ng.IQService, codenvySubscription: CodenvySubscription,
               lodash: any, cheNotification: any, billingService: BillingService, resourcesService: che.service.IResourcesService) {
    this.$q = $q;
    this.$mdDialog = $mdDialog;
    this.codenvySubscription = codenvySubscription;
    this.lodash = lodash;
    this.cheNotification = cheNotification;
    this.billingService = billingService;
    this.isLoading = true;
    this.step = Step;
    this.currentStep = Step.ONE;
    this.resourceLimits = resourcesService.getResourceLimits();

    this.calcDateBasedValues();

    this.getPackages();
    this.fetchCreditCard();
  }

  /**
   * Calculate the needed values based on current date: next month charge date
   * and the number of days left in current month.
   */
  calcDateBasedValues(): void {
    let now = new Date();
    this.nextMonthChargeDate = (now.getMonth() === 11) ? new Date(now.getFullYear() + 1, 0, 1) : new Date(now.getFullYear(), now.getMonth() + 1, 1);
    this.leftDaysInMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0).getDate() - now.getDate() + 1;
  }



  /**
   * Fetches the list of packages.
   */
  getPackages(): void {
    this.isLoading = true;
    this.codenvySubscription.fetchPackages().then(() => {
      this.isLoading = false;
      this.processPackages(this.codenvySubscription.getPackages());
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.processPackages(this.codenvySubscription.getPackages());
      }
    });
  }

  /**
   * Processes packages to get RAM resources details.
   *
   * @param packages list of packages
   */
  processPackages(packages: Array<any>): void {
    this.ramPackage = this.lodash.find(packages, (pack: any) => {
      return pack.type === this.resourceLimits.RAM;
    });

    if (!this.ramPackage) {
      return;
    }

    let ramResource = this.lodash.find(this.ramPackage.resources, (resource: any) => {
      return resource.type === this.resourceLimits.RAM;
    });

    this.timeoutResource = this.lodash.find(this.ramPackage.resources, (resource: any) => {
      return resource.type === this.resourceLimits.TIMEOUT;
    });

    if (!ramResource) {
      return;
    }

    this.price = ramResource.fullPrice;
    this.partialPrice = ramResource.partialPrice;
    this.amount = ramResource.amount / 1024 + 'GB';
    this.minValue = ramResource.minAmount / 1024;
    let paidRAM = this.totalRAM - this.freeRAM;
    this.maxValue = ramResource.maxAmount / 1024 - paidRAM;
    this.value = angular.copy(this.minValue);
    this.timeout = this.timeoutResource ? this.timeoutResource.amount / 60 : 4;
  }

  /**
   * Calculate the cost of the request per month.
   *
   * @returns {number} the request's cost per month
   */
  calcRequestMonthlyCost(): number {
    return this.price * this.value;
  }

  /**
   * Calculate the price that will be charged based on left days in month and chosen amount of resources.
   *
   * @returns {number} charged amount
   */
  calcChargedAmount(): number {
    return this.partialPrice * this.value * this.leftDaysInMonth;
  }

  /**
   * Calculate the price that will be charged based chosen amount of resources next month.
   *
   * @returns {number} charged amount
   */
  calcNextMonthChargeAmount(): number {
    return this.price * (this.value + this.totalRAM - this.freeRAM);
  }

  /**
   * Hides the dialog.
   */
  hide() {
    this.$mdDialog.hide();
  }

  /**
   * Requests more RAM based on subscription state.
   */
  getMoreRAM(): void {
    if (!this.creditCard && this.currentStep === Step.ONE) {
      this.currentStep = Step.TWO;
      return;
    }

    this.isLoading = true;

    let savePromise;
    if (!this.creditCard.token) {
      savePromise = this.saveCard();
    } else {
      let defer = this.$q.defer();
      savePromise = defer.promise;
      defer.resolve();
    }

    savePromise.then(() => {
      return this.codenvySubscription.fetchActiveSubscription(this.accountId).finally(() => {
        this.processSubscription(this.codenvySubscription.getActiveSubscription(this.accountId));
      });
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Process active subscription if exists or creates new one,
   *
   * @param subscription
   */
  processSubscription(subscription: any): void {
    let ramValue = this.value * 1024;

    let promise;
    // check subscription exists:
    if (subscription) {
      let packages = angular.copy(subscription.packages);

      // try to update RAM package:
      let ramPackage = this.lodash.find(packages, (pckg: any) => {
        return pckg.templateId === this.ramPackage.id;
      });

      if (ramPackage) {
        let ramResource = this.lodash.find(ramPackage.resources, (resource: any) => {
          return resource.type === this.resourceLimits.RAM;
        });
        // check RAM resource was defined:
        if (ramResource) {
          ramResource.amount += ramValue;
        } else { // process no RAM resource:
          ramPackage.resources.push(this.prepareRAMResource(ramValue));
        }
      } else { // process no RAM package:
        let resources = [this.prepareRAMResource(ramValue)];
        packages.push({resources: resources});
      }
      promise = this.codenvySubscription.updateSubscription(this.accountId, packages);
    } else { // process no active subscription:
      let packages = [];
      let resources = [this.prepareRAMResource(ramValue), this.prepareTimeoutResource()];
      packages.push({resources: resources, templateId: this.ramPackage.id});
      promise = this.codenvySubscription.createSubscription(this.accountId, packages);
    }

    promise.then(() => {
      this.isLoading = false;
      this.callbackController.onRAMChanged();
      this.hide();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to add more RAM to account.');
    });
  }

  /**
   * Returns RAM resource based on provided RAM amount.
   *
   * @param value RAM amount
   * @returns any ram resource
   */
  prepareRAMResource(value: number): any {
    return {amount: value, unit: 'mb', type: this.resourceLimits.RAM};
  }

  /**
   * Returns timeout resource.
   *
   * @returns any timeout resource
   */
  prepareTimeoutResource(): any {
    return {amount: this.timeoutResource.amount, unit: this.timeoutResource.unit, type: this.resourceLimits.TIMEOUT};
  }

  /**
   * Gets credit card.
   *
   * @return {ng.IPromise<any>}
   */
  fetchCreditCard(): ng.IPromise<any> {
    return this.billingService.fetchCreditCard(this.accountId).then((creditCard: ICreditCard) => {
      this.creditCard = creditCard;
    });
  }

  /**
   * Adds new credit card or updates an existing one.
   */
  saveCard(): ng.IPromise<any> {
    this.isLoading = true;

    return this.billingService.addCreditCard(this.accountId, this.creditCard).then(() => {
      return this.fetchCreditCard();
    }, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to save the credit card.');
    }).finally(() => {
      this.isLoading = false;
    });
  }
}
