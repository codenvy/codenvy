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
 * Defines a directive for creating a credit card component.
 * @author Oleksii Kurinnyi
 */
export class AddCreditCard {
  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = '/app/billing/card-info/add-credit-card/add-credit-card.html';

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
  constructor () {
    this.scope = {
      creditCard: '='
    };
  }

  link(): void {
    let card = new Card({
      // a selector or DOM element for the form where users will
      // be entering their information
      form: '#addCreditCardForm', // *required*
      // a selector or DOM element for the container
      // where you want the card to appear
      container: '.card-wrapper', // *required*

      // bind inputs which are visible on desktop
      formSelectors: {
        numberInput: 'input[name="deskcardNumber"]', // optional — default input[name="number"]
        expiryInput: 'input[name="deskexpires"]', // optional — default input[name="expiry"]
        cvcInput: 'input[name="deskcvv"]', // optional — default input[name="cvc"]
        nameInput: 'input[name="deskcardholder"]' // optional - defaults input[name="name"]
      },

      // width: 200, // optional — default 350px
      formatting: true, // optional - default true

      // strings for translation - optional
      messages: {
        validDate: 'valid\ndate', // optional - default 'valid\nthru'
        monthYear: 'mm/yyyy' // optional - default 'month/year'
      },

      // default placeholders for rendered fields - optional
      placeholders: {
        number: '•••• •••• •••• ••••',
        name: 'Full Name',
        expiry: '••/••',
        cvc: '•••'
      }
    });

  }
}
