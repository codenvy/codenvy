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
 * This class is providing the entry point for accessing to Codenvy API
 * It handles workspaces, projects, etc.
 * @author Florent Benoit
 */
export class CodenvyAPI {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(codenvyFactory, codenvyFactoryTemplate, codenvyUser) {
    this.codenvyFactory = codenvyFactory;
    this.codenvyFactoryTemplate = codenvyFactoryTemplate;
    this.codenvyUser = codenvyUser;

  }

  /**
   * The Codenvy Factory API
   * @returns {codenvyFactory|*}
   */
  getFactory() {
    return this.codenvyFactory;
  }

  /**
   * The Codenvy Factory Template API
   * @returns {CodenvyFactoryTemplate|*}
   */
  getFactoryTemplate() {
    return this.codenvyFactoryTemplate;
  }

  /**
   * The Codenvy User API
   * @returns {CodenvyAPI.codenvyUser|*}
   */
  getUser() {
    return this.codenvyUser;
  }
}
