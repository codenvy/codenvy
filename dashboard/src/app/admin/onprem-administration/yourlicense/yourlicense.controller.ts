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

export class OnPremisesAdminYourLicenseCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor(imsLicenseApi, imsArtifactApi, cheNotification) {
    this.imsLicenseApi = imsLicenseApi;
    this.imsArtifactApi = imsArtifactApi;
    this.cheNotification = cheNotification;

    this.license = imsLicenseApi.getLicense();

    let artifactsList = imsArtifactApi.getInstalledArtifactsList();

    this.isLoading = true;
    artifactsList.then((artifacts) => {
      if (artifacts) {
        for (let artifact of artifacts) {
          if (artifact.artifact === 'codenvy') {
            this.installedVersion = artifact.version;
            break;
          }
        }
      }
    }, (error)=> {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Installation manager server error.');
    });

    this.checkLicense();

    this.numberOfFreeUsers = imsLicenseApi.getNumberOfFreeUsers();
  }


  /**
   * Update license state
   */
  updateLicenseState() {
    this.isLicenseExpired = !this.license.properties || this.license.properties.isExpired === 'true';
    this.licenseState = 'LICENSE';
    this.newLicense = angular.copy(this.license.key);
    this.maxUsers = this.imsLicenseApi.getNumberOfAllowedUsers();
    //change date format from 'yyyy/mm/dd' to 'mm/dd/yyyy'
    this.expirationDate = this.license.properties.EXPIRATION.replace( /(\d{4})\/(\d{2})\/(\d{2})/, "$2/$3/$1");
    this.isLoading = false;
    this.isLicenseInvalid = false;
  }


  /**
   * Check license
   */
  checkLicense() {
    if (this.license.key) {
      this.updateLicenseState();
    } else {
      this.imsLicenseApi.fetchLicense().then(() => {
        this.updateLicenseState();
      }, () => { //if no license
        this.isLoading = false;
        this.licenseState = 'NO_LICENSE';
        this.newLicense = null;
      });
    }
  }


  /**
   * Delete current  license
   */
  deleteLicense() {
    let promise = this.imsLicenseApi.deleteLicense();

    this.isLoading = true;
    promise.then(()=> {
      this.isLoading = false;
      this.cheNotification.showInfo('License successfully deleted.');
      this.licenseState = 'NO_LICENSE';
      this.newLicense = null;
    }, (error)=> {
      this.isLoading = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'License server error.');
    });
  }


  /**
   * Add new license
   */
  addLicense() {
    let promise = this.imsLicenseApi.addLicense(this.newLicense);

    this.isLoading = true;
    promise.then(()=> {
      this.isLoading = false;
      this.isLicenseInvalid = false;
      this.cheNotification.showInfo('License successfully added.');
      this.imsLicenseApi.fetchLicenseProperties().then(()=> {
        this.checkLicense();
      });
    }, (error)=> {
      this.isLoading = false;
      this.isLicenseInvalid = true;
      this.cheNotification.showError(error.data.message ? error.data.message : 'License server error.');
    });
  }

}
