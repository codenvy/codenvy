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

interface ICreditCardElement extends ng.IAugmentedJQuery {
  card: Function;
}

/**
 * Defines a directive for creating a credit card component.
 * @author Oleksii Kurinnyi
 */
export class AddCreditCard {
  $timeout: ng.ITimeoutService;

  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = 'app/billing/card-info/add-credit-card/add-credit-card.html';

  bindToController: boolean = true;

  controller: string = 'AddCreditCardController';
  controllerAs: string = 'addCreditCardController';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor ($timeout: ng.ITimeoutService) {
    this.$timeout = $timeout;

    this.scope = {
      creditCard: '='
    };
  }

  link($scope: ng.IScope, $element: ICreditCardElement): void {
    ($element.find('.addCreditCardForm') as ICreditCardElement).card({
      // a selector or jQuery object for the container
      // where you want the card to appear
      container: '.card-wrapper', // *required*
      numberInput: 'input[name="deskcardNumber"]', // optional — default input[name="number"]
      expiryInput: 'input[name="deskexpires"]', // optional — default input[name="expiry"]
      cvcInput: 'input[name="deskcvv"]', // optional — default input[name="cvc"]
      nameInput: 'input[name="deskcardholder"]', // optional - defaults input[name="name"]

      // width: 200, // optional — default 350px
      formatting: true // optional - default true
    });

    let deregistrationFn = $scope.$watch(() => { return $element.find('input[name="deskcardNumber"]').is(':visible'); }, (visible) => {
      if (visible) {
        deregistrationFn();
        this.$timeout(() => {
          $element.find('input[name="deskcardNumber"]').focus();
        }, 100);
      }
    });
  }

}
