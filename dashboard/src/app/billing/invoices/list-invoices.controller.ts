/*
 *  [2015] - [2017] Codenvy, S.A.
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
