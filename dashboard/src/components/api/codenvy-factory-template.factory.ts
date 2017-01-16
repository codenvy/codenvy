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

/**
 * This class is handling the factory template retrieval
 * It sets to the Map factory templates
 * @author Oleksii Orel
 */
export class CodenvyFactoryTemplate {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, $q) {
    // keep resource
    this.$resource = $resource;
    this.$q = $q;

    // factory templates map
    this.factoryTemplatesByName = new Map();

    // remote call
    this.remoteFactoryTemplateAPI = this.$resource('https://dockerfiles.codenvycorp.com/templates-4.8/factory/:fileName');
  }

  /**
   * Ask for loading the factory template in asynchronous way
   * If there are no changes, it's not updated
   * @param templateName the template name
   * @returns {*} the promise
   */
  fetchFactoryTemplate(templateName) {
    var deferred = this.$q.defer();

    let templateFileName = templateName + '.json';

    let promise = this.remoteFactoryTemplateAPI.get({fileName: templateFileName}).$promise;

    promise.then((factoryTemplateContent) => {
      //update factory template map
      this.factoryTemplatesByName.set(templateName, factoryTemplateContent);
      deferred.resolve(factoryTemplateContent);
    }, (error) => {
      if (error.status === 304) {
        let findFactoryTemplateContent = this.factoryTemplatesByName.get(templateName);
        deferred.resolve(findFactoryTemplateContent);
      } else {
        deferred.reject(error);
      }
    });
    return deferred.promise;
  }

  /**
   * Gets factory template by template name
   * @param templateName the template name
   * @returns factory template content
   */
  getFactoryTemplate(templateName) {
    return this.factoryTemplatesByName.get(templateName);
  }

}
