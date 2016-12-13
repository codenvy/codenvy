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
import {CodenvyFactory} from './codenvy-factory.factory';
import {CodenvyFactoryTemplate} from './codenvy-factory-template.factory';
import {CodenvyUser} from './codenvy-user.factory';
import {CodenvyPermissions} from './codenvy-permissions.factory';
import {CodenvySystem} from './codenvy-system.factory';
import {CodenvyPayment} from './codenvy-payment.factory';
import {CodenvyLicense} from "./codenvy-license.factory";


/**
 * This class is providing the entry point for accessing to Codenvy API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CodenvyAPI {
  codenvyFactory: CodenvyFactory;
  codenvyFactoryTemplate: CodenvyFactoryTemplate;
  codenvyUser: CodenvyUser;
  codenvyPermissions: CodenvyPermissions;
  codenvySystem: CodenvySystem;
  codenvyLicense: CodenvyLicense;
  codenvyPayment: CodenvyPayment;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyFactory, codenvyFactoryTemplate, codenvyUser, codenvyPermissions, codenvySystem, codenvyLicense, codenvyPayment) {
    this.codenvyFactory = codenvyFactory;
    this.codenvyFactoryTemplate = codenvyFactoryTemplate;
    this.codenvyUser = codenvyUser;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvySystem = codenvySystem;
    this.codenvyLicense = codenvyLicense;
    this.codenvyPayment = codenvyPayment;
  }

  /**
   * The Codenvy Payment API
   * @returns {CodenvyPayment|*}
   */
  getPayment(): CodenvyPayment {
    return this.codenvyPayment;
  }

  /**
   * The Codenvy Factory API
   * @returns {CodenvyFactory|*}
   */
  getFactory(): CodenvyFactory {
    return this.codenvyFactory;
  }

  /**
   * The Codenvy Factory Template API
   * @returns {CodenvyFactoryTemplate|*}
   */
  getFactoryTemplate(): CodenvyFactoryTemplate {
    return this.codenvyFactoryTemplate;
  }

  /**
   * The Codenvy License API interaction service.
   *
   * @returns {CodenvyLicense}
   */
  getLicense(): CodenvyLicense {
    return this.codenvyLicense;
  }

  /**
   * The Codenvy User API
   * @returns {CodenvyUser|*}
   */
  getUser(): CodenvyUser {
    return this.codenvyUser;
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
}
