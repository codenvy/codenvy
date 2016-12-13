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
import {ICreditCard, CodenvyPayment} from '../../../components/api/codenvy-payment.factory';

export class CardInfoController {
  codenvyPayment: CodenvyPayment;

  creditCard: ICreditCard;
  countries: {
    name: string,
    code: string
  };

  creditCardOnChange: Function;
  creditCardOnDelete: Function;

  /**
   * @ngInject for Dependency injection
   */
  constructor (codenvyPayment: CodenvyPayment, jsonCountries: string) {
    this.codenvyPayment = codenvyPayment;

    this.countries = angular.fromJson(jsonCountries);
  }

  /**
   * Callback when card or billing information has been changed.
   *
   * @param isFormValid {Boolean} true if cardInfoForm is valid
   */
  infoChanged(isFormValid: boolean): void {
    this.creditCardOnChange({creditCard: this.creditCard});
  }

  deleteCard(): void {
    this.creditCardOnDelete();
  }
}
