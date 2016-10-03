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
 * Controller for creating factory from a template.
 * @author Oleksii Orel
 */
export class FactoryFromTemplateCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter, codenvyAPI, cheNotification) {
    this.$filter = $filter;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;

    this.isImporting = false;
    this.factoryContent = null;

    this.editorOptions = {
      mode: 'application/json'
    };
  }

  //Gets factory template.
  getFactoryTemplate(templateName) {
    let factoryContent = this.codenvyAPI.getFactoryTemplate().getFactoryTemplate(templateName);

    if (factoryContent) {
      this.factoryContent = this.$filter('json')(factoryContent, 2);
      return;
    }

    this.isImporting = true;

    //fetch it !
    let promise = this.codenvyAPI.getFactoryTemplate().fetchFactoryTemplate(templateName);

    promise.then((factoryContent) => {
      this.isImporting = false;
      this.factoryContent = this.$filter('json')(factoryContent, 2);
    }, (error) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Fail to get factory template.');
      console.log('error', error);
    });
  }

}
