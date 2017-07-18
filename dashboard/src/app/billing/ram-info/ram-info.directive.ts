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

/**
 * Defines a directive for displaying RAM info.
 *
 * @author Ann Shumilova
 */
export class RamInfo {
  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = 'app/billing/ram-info/ram-info.html';

  bindToController: boolean = true;

  controller: string = 'RamInfoController';
  controllerAs: string = 'ramInfoController';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (private $document: ng.IDocumentService) {
    this.scope = {
      accountId: '='
    };
  }

  link($scope: ng.IScope): void {
    // in "Get More RAM" popup
    // set focus to visible input which is RAM value
    $scope.$watch(() => { return this.$document.find('.get-more-ram-input input:visible').length; }, (visibleNumber: number) => {
      if (visibleNumber === 1) {
        this.$document.find('.get-more-ram-input input').focus();
      }
    });
  }
}
