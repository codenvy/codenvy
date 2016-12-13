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
import {CodenvyLicense} from '../../../../components/api/codenvy-license.factory';

/**
 * Defines a directive for displaying the nag message.
 * @author Oleksii Orel
 */
export class NagMessage {
  replace: boolean;
  bindToController: boolean;
  restrict: string;
  controller: string;
  controllerAs: string;
  templateUrl: string;
  numberOfFreeUsers: number;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyLicense: CodenvyLicense) {
    this.restrict = 'E';
    this.replace = true;

    this.numberOfFreeUsers = codenvyLicense.getNumberOfFreeUsers();
  }

  /**
   * Template for the nag message
   * @returns {string} the template
   */
  template() {
    return '<div class="license-message">' +
      'No valid license detected. Codenvy is free for ' + this.numberOfFreeUsers + ' users.&nbsp;' +
      '<che-link ng-href="https://codenvy.com/legal/fair-source/" che-link-text="Please upgrade" che-no-padding="true" che-new-window></che-link>' +
      '.&nbsp;Your mother would approve!</div>';
  }
}
