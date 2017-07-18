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
import {ICreditCard, CodenvyPayment} from '../../../components/api/codenvy-payment.factory';

/**
 * @ngdoc controller
 * @name billing.card.info:CardInfoController
 * @description This class is handling the controller for managing credit card information.
 * @author Oleksii Kurinnyi
 */
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
