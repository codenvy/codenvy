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
import {IInvoice, CodenvyInvoices} from '../../../components/api/codenvy-invoices.factory';

/**
 * @ngdoc controller
 * @name billing.invoices.list:ListInvoicesController
 * @description This class is handling the controller for managing invoices.
 * @author Oleksii Kurinnyi
 */
export class ListInvoicesController {
  codenvyInvoices: CodenvyInvoices;
  cheNotification: any
  $filter: any;
  lodash: _.LoDashStatic

  invoices: Array<IInvoice>;
  accountId: string;
  isLoading: boolean;
  filter: any;

  /**
   * @ngInject for Dependency injection
   */
  constructor (codenvyInvoices: CodenvyInvoices, cheNotification: any, $filter: any, lodash: _.LoDashStatic) {
    this.codenvyInvoices = codenvyInvoices;
    this.cheNotification = cheNotification;
    this.filter = {creationDate: ''};
    this.$filter = $filter;
    this.lodash = lodash;

    this.isLoading = true;
    this.codenvyInvoices.fetchInvoices(this.accountId).then(() => {
      this.invoices = this.codenvyInvoices.getInvoices(this.accountId);
      this.formatInvoices();
      this.isLoading = false;
    }, (error: any) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.invoices = this.codenvyInvoices.getInvoices(this.accountId);
        this.formatInvoices();
      } else {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to load invoices.');
      }
    });
  }

  /**
   * Formats the invoices creation date.
   */
  formatInvoices(): void {
    this.invoices.forEach((invoice: any) => {
      invoice.creationDate = this.$filter('date')(new Date(invoice.creationDate), 'dd-MMM-yyyy');
      invoice.preview = this.lodash.find(invoice.links, (link: any) => {
        return link.produces === 'text/html' && link.rel === 'get invoice';
      });
    });
  }
}
