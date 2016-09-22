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
 * This class is providing a builder for factory
 * @author Oleksii Orel
 */
export class CodenvyFactoryBuilder {

  /**
   * Default constructor.
   */
  constructor() {
    this.factory = {};
    this.factory.creator = {};
  }

  /**
   * Sets the creator email
   * @param email
   * @returns {CodenvyFactoryBuilder}
   */
  withCreatorEmail(email) {
    this.factory.creator.email = email;
    return this;
  }

  /**
   * Sets the creator name
   * @param name
   * @returns {CodenvyFactoryBuilder}
   */
  withCreatorName(name) {
    this.factory.creator.name = name;
    return this;
  }

  /**
   * Sets the id of the factory
   * @param id
   * @returns {CodenvyFactoryBuilder}
   */
  withId(id) {
    this.factory.id = id;
    return this;
  }

  /**
   * Sets the name of the factory
   * @param name
   * @returns {CodenvyFactoryBuilder}
   */
  withName(name) {
    this.factory.name = name;
    return this;
  }

  /**
   * Sets the workspace of the factory
   * @param workspace
   * @returns {CodenvyFactoryBuilder}
   */
  withWorkspace(workspace) {
    this.factory.workspace = workspace;
    return this;
  }

  /**
   * Build the factory
   * @returns {CodenvyFactoryBuilder|*}
   */
  build() {
    return this.factory;
  }

}
