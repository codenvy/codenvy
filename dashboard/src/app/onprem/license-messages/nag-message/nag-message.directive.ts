/*
 * Copyright (c) [2015] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
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

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyLicense: CodenvyLicense) {
    this.restrict = 'E';
    this.replace = true;

    this.scope = {
      message: '@?cheMessage'
    };

    this.numberOfFreeUsers = codenvyLicense.getNumberOfFreeUsers();
  }

  /**
   * Template for the nag message
   * @returns {string} the template
   */
  template($element: ng.IAugmentedJQuery, attrs: any) {
    let message;
    if (attrs.cheMessage) {
      message = attrs.cheMessage;
    } else {
      message = 'No valid license detected. Codenvy is free for ' + this.numberOfFreeUsers + ' users.&nbsp;' +
        '<che-link ng-href="https://codenvy.com/legal/fair-source/" che-link-text="Please upgrade" che-no-padding="true" che-new-window></che-link>' +
        '.&nbsp;Your mother would approve!';
    }

    return '<div class="license-message">' +
        message +
      '</div>';
  }
}
