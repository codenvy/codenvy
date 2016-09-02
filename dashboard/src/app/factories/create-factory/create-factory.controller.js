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
 * Controller for a create factory.
 * @author Oleksii Orel
 * @author Florent Benoit
 */
export class CreateFactoryCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location, cheAPI, $log, codenvyAPI, cheNotification, $scope, $filter, lodash, $document) {
    this.$location = $location;
    this.cheAPI = cheAPI;
    this.$log = $log;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;
    this.$filter = $filter;
    this.lodash = lodash;
    this.$document = $document;

    this.isLoading = false;
    this.isImporting = false;

    this.stackRecipeMode = 'current-recipe';

    this.factoryContent = null;

    $scope.$watch('createFactoryCtrl.factoryObject', () => {
      this.factoryContent = this.$filter('json')(angular.fromJson(this.factoryObject));
    }, true);

    $scope.$watch('createFactoryCtrl.gitLocation', (newValue) => {
      // update underlying model
      // Updating first project item
      if (!this.factoryObject) {
        //fetch it !
        let templateName = 'git';
        let promise = this.codenvyAPI.getFactoryTemplate().fetchFactoryTemplate(templateName);

        promise.then(() => {
          let factoryContent = this.codenvyAPI.getFactoryTemplate().getFactoryTemplate(templateName);
          this.factoryObject = angular.fromJson(factoryContent);
          this.updateGitProjectLocation(newValue);
        });
      } else {
        this.updateGitProjectLocation(newValue);
      }

    }, true);

    this.form;
  }

  setForm(form) {
    this.form = form;
  }

  isFormInvalid() {
    return this.form.$invalid;
  }

  /**
   * Update the source project location for git
   * @param location the new location
   */
  updateGitProjectLocation(location) {
    let project = this.factoryObject.workspace.projects[0];
    project.source.type = 'git';
    project.source.location = location;
  }

  /**
   * Create a new factory by factory content
   * @param factoryContent
   */
  createFactoryByContent(factoryContent) {
    if (!factoryContent) {
      return;
    }

    // try to set factory name
    try {
      let factoryObject = angular.fromJson(factoryContent);
      factoryObject.name = this.name;
      factoryContent = angular.toJson(factoryObject);
    } catch (e) {
      this.$log.error(e);
    }

    this.isImporting = true;

    let promise = this.codenvyAPI.getFactory().createFactoryByContent(factoryContent);

    promise.then((factory) => {
      this.isImporting = false;

      this.lodash.find(factory.links, (link) => {
        if (link.rel === 'accept' || link.rel === 'accept-named') {
          this.factoryLink = link.href;
        }
      });

      var parser = this.$document[0].createElement('a');
      parser.href = this.factoryLink;
      this.factoryId = factory.id;
      this.factoryBadgeUrl = parser.protocol + '//' + parser.hostname + '/factory/resources/codenvy-contribute.svg';

      this.markdown = '[![Contribute](' + this.factoryBadgeUrl + ')](' + this.factoryLink + ')';
    }, (error) => {
      this.isImporting = false;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Create factory failed.');
      this.$log.error(error);
    }).then(() => {
      this.finishFlow();
    });
  }

  /*
   * Flow of creating a factory is finished, we can redirect to details of factory
   */
  finishFlow() {
    this.$location.path('/factory/' + this.factoryId);
  }

}
