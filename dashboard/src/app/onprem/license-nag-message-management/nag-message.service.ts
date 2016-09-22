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
 * This class is handling the service for the nag message
 * @author Oleksii Orel
 */
export class NagMessageService {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($document, $compile) {
    this.$document = $document;
    this.$compile = $compile;

    this.nagMessageId = 'codenvy-nag-message';
  }

  /**
   * Create a license nag message element
   */
  createLicenseMessage() {
    if (this.nagMessageElement) {
      return;
    }
    // create nag message element
    let jqItem = angular.element('<cdvy-nag-message ng-hide="showIDE"></cdvy-nag-message>');
    jqItem.attr('id', this.nagMessageId);
    // compile
    this.nagMessageElement = this.$compile(jqItem)(angular.element(this.$document.find('body')[0]).scope());
  }

  /**
   * Show license message
   * @returns {boolean} - true if successful
   */
  showLicenseMessage() {
    if (this.$document[0].getElementById(this.nagMessageId) || !this.nagMessageElement) {
      return false;
    }
    //the parent of the new element
    let jqParentElement = angular.element(this.$document.find('body'));

    this.nagMessageElement.prependTo(jqParentElement);
    jqParentElement.addClass('license-message-indent');

    return true;
  }

  /**
   * Hide license message
   * @returns {boolean} - true if successful
   */
  hideLicenseMessage() {
    let findElement = this.$document[0].getElementById(this.nagMessageId);
    if (findElement) {
      findElement.remove();
      //the parent of the new element
      let jqParentElement = angular.element(this.$document.find('body'));
      jqParentElement.removeClass('license-message-indent');
      return true;
    }
    return false;
  }

}
