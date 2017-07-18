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
 * Defines a directive for creating a invoices page.
 *
 * @author Ann Shumilova
 */
export class ListInvoices {
  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = 'app/billing/invoices/list-invoices.html';

  bindToController: boolean = true;

  controller: string = 'ListInvoicesController';
  controllerAs: string = 'listInvoicesController';

  scope: {
    [propName: string]: string
  };

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.scope = {
      accountId: '='
    };
  }
}
