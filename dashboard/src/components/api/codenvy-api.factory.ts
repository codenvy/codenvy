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
import {CodenvyPermissions} from './codenvy-permissions.factory';
import {CodenvySystem} from './codenvy-system.factory';
import {CodenvyTeam} from './codenvy-team.factory';
import {CodenvyOrganization} from './codenvy-organizations.factory';
import {CodenvyPayment} from './codenvy-payment.factory';
import {CodenvyInvoices} from './codenvy-invoices.factory';


/**
 * This class is providing the entry point for accessing to Codenvy API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CodenvyAPI {
  private codenvyPermissions: CodenvyPermissions;
  private codenvySystem: CodenvySystem;
  private codenvyTeam: CodenvyTeam;
  private codenvyOrganization: CodenvyOrganization;
  private codenvyPayment: CodenvyPayment;
  private codenvyInvoices: CodenvyInvoices;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyPermissions, codenvySystem, codenvyTeam, codenvyOrganization: CodenvyOrganization, codenvyPayment, codenvyInvoices) {
    this.codenvyPermissions = codenvyPermissions;
    this.codenvySystem = codenvySystem;
    this.codenvyTeam = codenvyTeam;
    this.codenvyOrganization = codenvyOrganization;
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
   * The Codenvy Permissions API
   * @returns {CodenvyPermissions|*}
   */
  getPermissions(): CodenvyPermissions {
    return this.codenvyPermissions;
  }

  /**
   * The Codenvy System API
   * @returns {CodenvySystem|*}
   */
  getSystem(): CodenvySystem {
    return this.codenvySystem;
  }

  getTeam(): CodenvyTeam {
    return this.codenvyTeam;
  }

  getOrganization(): CodenvyOrganization {
    return this.codenvyOrganization;
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
