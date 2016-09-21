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


import {CodenvyUserBuilder} from './codenvy-user-builder.js';
import {CodenvyFactoryBuilder} from './codenvy-factory-builder.js';

/**
 * This class is providing the entry point for accessing the builders
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CodenvyAPIBuilder {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor () {
  }

  /***
   * The Codenvy User builder
   * @returns {CodenvyUserBuilder}
   */
  getUserBuilder() {
    return new CodenvyUserBuilder();
  }

  /***
   * The Codenvy Factory builder
   * @returns {CodenvyFactoryBuilder}
   */
  getFactoryBuilder() {
    return new CodenvyFactoryBuilder();
  }
}
