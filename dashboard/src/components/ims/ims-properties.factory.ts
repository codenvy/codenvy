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

/**
 * This class is handling server-wide properties load/store.
 */
export class ImsPropertiesApi {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($resource, $q) {
    this.$q = $q;
    // remote call
    this.remoteImsAPI = $resource('/im/storage/properties/:name', {}, {
      getAllProperties: { method: 'GET', url: '/im/storage/properties/'},
      getProperty: { method: 'GET', transformResponse: function(data) {
        // wrap answer in a content
        return {content: data};
      }},
      storeProperty: { method: 'POST' },
      putProperty: { method: 'PUT', url: '/im/storage/properties/', headers: { 'Content-Type': 'text/plain;charset=utf-8' } },
      deleteProperty: { method: 'DELETE' }
    });

    this.propertiesMap = new Map();
  }


  fetchProperty(propertyName) {
    let propertyRetrieval = this.remoteImsAPI.getProperty({ name: propertyName });
    let updatedPromise = propertyRetrieval.$promise.then((data) => {
      if (data.content) {
        this.propertiesMap.set(propertyName, data.content);
      } else {
        this.propertiesMap.set(propertyName, '');
      }
    }, () => {
      // if not found, set empty value
      this.propertiesMap.set(propertyName, '');
    });
    return updatedPromise;
  }

  /**
   * Returns the value of the property.
   * @param propertyName the name of the desired property
   * @returns a promise on the property value
   */
  getProperty(propertyName) {
    return this.propertiesMap.get(propertyName);
  }

  _handleGetError(resource, error) {
    console.log('_handleGetError error', error);
    console.log('_handleGetError resource', resource);
  }

  getAllProperties() {
    let allPropsResource = this.remoteImsAPI.getAllProperties();
    // store all properties
    return allPropsResource.$promise.then(data => this._cacheProperties(data));
  }

  _cacheProperties(data) {
    for (let property of Object.keys(data)) {
      // must ignore $resource-polluted properties
      if (property !== '$promise' && property !== '$resolved') {
        this.propertiesMap.set(property, data[property]);
      }
    }
    return data;
  }

  /**
   * Stores the given properties.
   * @param properties an object which properties will be used as key:value property pairs
   * @returns a promise (value is not defined)
   */
  storeProperties(properties) {
    return this.remoteImsAPI.storeProperty({}, properties).$promise;
  }

  storeProperty(key, value) {
    let param = {};
    param[key] = value;
    let promiseSet = this.remoteImsAPI.storeProperty({}, param).$promise;
    let updatedGet = promiseSet.then(() => {
      return this.fetchProperty(key);
    });

    return updatedGet;
  }

  deleteProperty(key) {
    let resource = this.remoteImsAPI.deleteProperty({ name: key }, {});
    resource.$promise.then(() => this.propertiesMap.delete(key));
    return resource;
  }
}
