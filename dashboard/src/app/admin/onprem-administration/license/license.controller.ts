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
import {LicenseMessagesService} from '../../../onprem/license-messages/license-messages.service';
import {CodenvyLicense} from '../../../../components/api/codenvy-license.factory';


/**
 * @ngdoc controller
 * @description This class is handling the controller for 'license' section.
 * @author Oleksii Orel
 */
export class OnPremisesAdminLicenseController {
  licenseMessagesService: LicenseMessagesService;
  codenvyLicense: CodenvyLicense;
  isLoading: boolean;
  isLicenseInvalid: boolean;
  isLicenseExpired: boolean;
  maxUsers: number;
  numberOfFreeUsers: number;
  licenseState: string;
  expirationDate: string;
  license: any;
  newLicense: any;
  cheNotification: any;


  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor(codenvyLicense: CodenvyLicense, cheNotification: any, licenseMessagesService: LicenseMessagesService) {
    this.codenvyLicense = codenvyLicense;
    this.cheNotification = cheNotification;
    this.licenseMessagesService = licenseMessagesService;

    this.license = codenvyLicense.getLicense();
    this.numberOfFreeUsers = codenvyLicense.getNumberOfFreeUsers();

    this.isLoading = true;
    this.checkLicense();
  }

  /**
   * Update license state
   */
  updateLicenseState(): void {
    this.isLicenseExpired = !this.license.properties || this.license.properties.isExpired === 'true';
    this.licenseState = 'LICENSE';
    this.newLicense = angular.copy(this.license.key);
    this.maxUsers = this.codenvyLicense.getNumberOfAllowedUsers();
    // change date format from 'yyyy/mm/dd' to 'mm/dd/yyyy'
    this.expirationDate = this.license.properties.EXPIRATION.replace(/(\d{4})\/(\d{2})\/(\d{2})/, '$2/$3/$1');
    this.isLoading = false;
    this.isLicenseInvalid = false;
  }

  /**
   * Check license
   */
  checkLicense(): void {
    if (this.license.key) {
      this.updateLicenseState();
    } else {
      this.codenvyLicense.fetchLicense().then(() => {
        this.updateLicenseState();
      }, () => { // if no license
        this.isLoading = false;
        this.licenseState = 'NO_LICENSE';
        this.newLicense = null;
      });
    }
  }

  /**
   * Delete current  license
   */
  deleteLicense(): void {
    this.isLoading = true;
    let promise = this.codenvyLicense.deleteLicense();

    promise.then(() => {
      this.isLoading = false;
      this.cheNotification.showInfo('License successfully deleted.');
      this.licenseState = 'NO_LICENSE';
      this.newLicense = null;
      this.licenseMessagesService.fetchMessages();
    }, (error: any) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'License server error.');
    });
  }

  /**
   * Add new license
   */
  addLicense(): void {
    this.isLoading = true;
    let promise = this.codenvyLicense.addLicense(this.newLicense);

    promise.then(() => {
      this.isLoading = false;
      this.isLicenseInvalid = false;
      this.cheNotification.showInfo('License successfully added.');
      this.licenseMessagesService.fetchMessages();
      this.codenvyLicense.fetchLicenseProperties().then(() => {
        this.checkLicense();
      });
    }, (error: any) => {
      this.isLoading = false;
      this.isLicenseInvalid = true;
      this.cheNotification.showError(error.data.message ? error.data.message : 'License server error.');
    });
  }
}
