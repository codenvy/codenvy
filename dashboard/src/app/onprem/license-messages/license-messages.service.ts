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
import {CodenvyLicense} from '../../../components/api/codenvy-license.factory';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';



const NAG_MESSAGE_ID: string = 'license-legality-message';
/**
 * This class is handling the service for the nag message
 * @author Oleksii Orel
 */
export class LicenseMessagesService {
  codenvyLicense: CodenvyLicense;
  codenvyPermissions: CodenvyPermissions;
  $compile: ng.ICompileService;
  $document: ng.IDocumentService;
  $cookies: ng.cookies.ICookiesService;
  $mdDialog: ng.material.IDialogService;

  legality: {isLegal: boolean, issues?: Array<{message: string, status: string}>};
  userServices: {hasAdminUserService: boolean};

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor(codenvyLicense: CodenvyLicense, codenvyPermissions: CodenvyPermissions, $document: ng.IDocumentService, $cookies: ng.cookies.ICookiesService, $compile: ng.ICompileService, $mdDialog: ng.material.IDialogService) {
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyLicense = codenvyLicense;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
    this.$cookies = $cookies;
    this.$compile = $compile;

    this.userServices = codenvyPermissions.getUserServices();
    this.legality = this.codenvyLicense.getLicenseLegality();
  }

  /**
   * Fetch license messages
   */
  fetchMessages(): void {
    this.codenvyLicense.fetchLicenseLegality().then(() => {
      if (!this.codenvyPermissions.getSystemPermissions()) {
        this.codenvyPermissions.fetchSystemPermissions().then(() => {
          this.checkLicenseMessage();
        });
      } else {
        this.checkLicenseMessage();
      }
    });
  }

  /**
   * Check the license Message
   */
  checkLicenseMessage(): void {
    if (!this.legality.isLegal) {
      this.showLicenseMessage();
    } else {
      this.hideLicenseMessage();
    }
  }

  /**
   * Show popup about license limit
   * @param key {string}
   * @param message {string}
   */
  showLicenseLimitPopup(key: string, message: string): void {
    this.$mdDialog.show({
      controller: 'LicenseLimitController',
      controllerAs: 'licenseLimitController',
      bindToController: true,
      clickOutsideToClose: true,
      locals: {
        key: key,
        message: message
      },
      templateUrl: 'app/onprem/license-messages/license-limit-dialog/license-limit-dialog.html'
    });
  }

  /**
   * Show license message
   * @returns {boolean} - true if successful
   */
  showLicenseMessage(): boolean {
    if (this.$document.find('#' + NAG_MESSAGE_ID).length) {
      return false;
    }
    let jqItem = angular.element('<cdvy-nag-message></cdvy-nag-message>');
    jqItem.attr('id', NAG_MESSAGE_ID);
    let jqParentElement = angular.element(this.$document.find('body'));
    let nagMessageElement = this.$compile(jqItem)(jqParentElement.scope());
    jqParentElement.addClass('license-message-indent');
    nagMessageElement.prependTo(jqParentElement);
    return true;
  }

  /**
   * Hide license message
   * @returns {boolean} - true if successful
   */
  hideLicenseMessage(): boolean {
    let jqLicenseMessageElement = this.$document.find('#' + NAG_MESSAGE_ID);
    jqLicenseMessageElement.parent().removeClass('license-message-indent');
    return jqLicenseMessageElement.remove().length > 0;
  }
}
