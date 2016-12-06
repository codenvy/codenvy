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

interface ICodenvyLicenseResource<T> extends ng.resource.IResourceClass<T> {
  getLicense: any;
  setLicense: any;
  getLegality: any;
  getProperties: any;
}

interface ICodenvyLicenseIssue {
  message: string;
  status: string;
}

interface ILicenseLegality {
  isLegal: boolean;
  issues?: Array<ICodenvyLicenseIssue>;
}

/**
 * This class is handling the license API retrieval
 * @author Oleksii Orel
 */
export class CodenvyLicense {
  $q: ng.IQService;
  $compile: ng.ICompileService;
  $document: ng.IDocumentService;
  $resource: ng.resource.IResourceService;
  numberOfFreeUsers: number;
  remoteLicenseAPI: ICodenvyLicenseResource<any>;
  currentLicense: {
    key: string,
    properties: any
  };
  licenseLegality: ILicenseLegality;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, $document: ng.IDocumentService, $compile: ng.ICompileService) {
    // keep resource
    this.$document = $document;
    this.$resource = $resource;
    this.$compile = $compile;
    this.$q = $q;

    // remote call
    this.remoteLicenseAPI = <ICodenvyLicenseResource<any>>this.$resource('/api/license/system', {}, {
      getLicense: {
        method: 'GET', url: '/api/license/system', responseType: 'text', transformResponse: (data: Object) => {
          return {key: data};
        }
      },
      setLicense: {
        method: 'POST', url: '/api/license/system', isArray: false,
        headers: {
          'Content-Type': 'text/plain'
        }
      },
      getProperties: {method: 'GET', url: '/api/license/system/properties'},
      getLegality: {method: 'GET', url: '/api/license/system/legality'}
    });

    // default number of free users
    this.numberOfFreeUsers = 3;

    // default license
    this.currentLicense = {
      key: null,
      properties: null
    };

    // default license legality
    this.licenseLegality = {
      isLegal: true,
      issues: []
    };
  }

  /**
   * Gets the number of free users
   * @returns {Number}
   */
  getNumberOfFreeUsers(): number {
    return this.numberOfFreeUsers;
  }

  /**
   * Gets the number of allowed users
   * @returns {Number}
   */
  getNumberOfAllowedUsers(): number {
    // if no license
    if (!this.currentLicense.properties) {
      return this.numberOfFreeUsers;
    }
    // if valid license
    return parseInt(this.currentLicense.properties.USERS, 10) | 0;
  }

  /**
   * Gets the current license properties
   * @returns {{key: string, properties: any}}
   */
  getLicense(): {key: string, properties: any} {
    return this.currentLicense;
  }

  /**
   * Delete current license.
   * @returns {*} the promise
   */
  deleteLicense(): ng.IPromise<any> {
    let promise: ng.IPromise<any> = this.remoteLicenseAPI.delete().$promise;

    // check if was OK or not
    promise.then(() => {
      this.currentLicense.key = null;
      this.currentLicense.properties = null;
      this.fetchLicenseLegality();
    });

    return promise;
  }

  /**
   * Ask for loading the users license (key and properties)
   * @returns {*} the promise
   */
  fetchLicense(): ng.IPromise<any> {
    let deferred = this.$q.defer();

    this.fetchLicenseKey().then(() => {
      this.fetchLicenseProperties().then(() => {
        deferred.resolve(this.currentLicense);
      }, (error: any) => {
        deferred.reject(error);
      });
    }, (error: any) => {
      deferred.reject(error);
    });

    return deferred.promise;
  }

  /**
   * Ask for loading the users license key in asynchronous way
   * If there are no changes, it's not updated
   * @returns {*} the promise
   */
  fetchLicenseKey(): ng.IPromise<any> {
    let deferred = this.$q.defer();

    let promise = this.remoteLicenseAPI.getLicense().$promise;

    // check if was OK or not
    promise.then((license: {key: string}) => {
      this.currentLicense.key = license.key;
      deferred.resolve(license.key);
    }, (error: any) => {
      if (error.status === 304) {
        deferred.resolve(this.currentLicense.key);
      } else {
        deferred.reject(error);
      }
    });

    return deferred.promise;
  }

  /**
   * Ask for loading the users license properties in asynchronous way
   * If there are no changes, it's not updated
   * @returns {*} the promise
   */
  fetchLicenseProperties(): ng.IPromise<any> {
    let deferred = this.$q.defer();

    let promise = this.remoteLicenseAPI.getProperties().$promise;
    // check if was OK or not
    promise.then((properties: any) => {
      // update current license properties
      this.currentLicense.properties = properties;
      deferred.resolve(properties);
    }, (error: any) => {
      if (error.status === 304) {
        deferred.resolve(this.currentLicense.properties);
      } else {
        deferred.reject(error);
      }
    });

    return deferred.promise;
  }

  /**
   * Add license.
   * @param licenseKey
   * @returns {*} the promise
   */
  addLicense(licenseKey: string): ng.IPromise<any> {
    let promise = this.remoteLicenseAPI.setLicense(licenseKey).$promise;

    // check if was OK or not
    promise.then(() => {
      // update current license
      this.currentLicense.key = licenseKey;
      this.fetchLicenseLegality();
    });

    return promise;
  }

  /**
   * Ask for the users license legality in asynchronous way
   * @returns {*} the promise
   */
  fetchLicenseLegality(): ng.IPromise<any> {
    let deferred: ng.IDeferred<any> = this.$q.defer();

    let promise = this.remoteLicenseAPI.getLegality().$promise;

    // check if was OK or not
    promise.then((licenseLegality: ILicenseLegality) => {
      this.licenseLegality.isLegal = licenseLegality.isLegal;
      this.licenseLegality.issues = licenseLegality.issues ? licenseLegality.issues : [];
      deferred.resolve(this.licenseLegality);
    }, (error: any) => {
      if (error.status === 304) {
        deferred.resolve(this.licenseLegality);
      } else {
        deferred.reject(error);
      }
    });

    return deferred.promise;
  }

  /**
   * Gets the users license legality
   * @returns {ILicenseLegality}
   */
  getLicenseLegality(): ILicenseLegality {
    return this.licenseLegality;
  }
}
