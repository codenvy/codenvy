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

export class ResetServerPropsCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor(imsPropertiesApi, $q) {
    this.imsPropertiesApi = imsPropertiesApi;
    this.$q = $q;
    this.messages = [];
    let deletePromise = this.imsPropertiesApi.getAllProperties().then(resource => this._deleteProperties(resource));
    deletePromise.then(() => this._finished(), () => this._finished());
  }

  _deleteProperties(resource) {
    this.oldProperties = this._filter(resource);
    let promises = [];
    for (let property of Object.keys(this.oldProperties)) {
      let promise = this.imsPropertiesApi.deleteProperty(property).$promise
            .then(() => this.messages.push('Deleted property ' + property))
            .catch(error => this.messages.push('Property ' + property + ' not deleted: ' + JSON.stringify(error)));
      promises.push(promise);
    }
    if (promises) {
      return this.$q.all(promises);
    } else {
      return this.$q.resolve(true);
    }
  }

  _finished() {
    this.imsPropertiesApi.getAllProperties().then(resource => this.newProperties = this._filter(resource));
  }

  _filter(resource) {
    let result = {};
    for (let property of Object.keys(resource)) {
      if (property !== '$promise' && property !== '$resolved') {
        result[property] = resource[property];
      }
    }
    return result;
  }
}
