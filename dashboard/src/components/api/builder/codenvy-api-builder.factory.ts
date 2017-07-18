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


import {CodenvyTeamBuilder} from './codenvy-team-builder';
import {CodenvyOrganizationsBuilder} from './codenvy-organizations-builder';
import {CodenvyPermissionsBuilder} from './codenvy-permissions-builder';
import {CodenvyResourceBuilder} from './codenvy-resource-builder';

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
   * The Codenvy Team builder
   * @returns {CodenvyTeamBuilder}
   */
  getTeamBuilder() {
    return new CodenvyTeamBuilder();
  }

  /**
   * The Codenvy Organizations builder
   *
   * @return {CodenvyOrganizationsBuilder}
   */
  getOrganizationsBuilder() {
    return new CodenvyOrganizationsBuilder();
  }

  /**
   * The Codenvy Permissions builder
   *
   * @return {CodenvyPermissionsBuilder}
   */
  getPermissionsBuilder() {
    return new CodenvyPermissionsBuilder();
  }

  /**
   * The Codenvy Resources builder
   *
   * @return {CodenvyResourceBuilder}
   */
  getResourceBuilder() {
    return new CodenvyResourceBuilder();
  }
}
