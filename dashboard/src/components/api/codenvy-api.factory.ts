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
import {CodenvySystem} from './codenvy-system.factory';
import {CodenvyPayment} from './codenvy-payment.factory';
import {CodenvyInvoices} from './codenvy-invoices.factory';


/**
 * This class is providing the entry point for accessing to Codenvy API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CodenvyAPI {
  private codenvySystem: CodenvySystem;
  private codenvyPayment: CodenvyPayment;
  private codenvyInvoices: CodenvyInvoices;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvySystem, codenvyPayment, codenvyInvoices) {
    this.codenvySystem = codenvySystem;
    this.codenvyPayment = codenvyPayment;
    this.codenvyInvoices = codenvyInvoices;
  }

  /**
   * The Codenvy Payment API
   * @returns {CodenvyPayment|*}
   */
  getPayment(): CodenvyPayment {
    return this.codenvyPayment;
  }

  /**
   * The Codenvy System API
   * @returns {CodenvySystem|*}
   */
  getSystem(): CodenvySystem {
    return this.codenvySystem;
  }

  /**
   * The Codenvy Invoices API.
   *
   * @returns {CodenvyInvoices}
   */
  getInvoices(): CodenvyInvoices {
    return this.codenvyInvoices;
  }
}
