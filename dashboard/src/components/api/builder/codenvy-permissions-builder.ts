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
 * This class is providing a builder for Permissions
 *
 * @autor Oleksii Kurinnyi
 */
export class CodenvyPermissionsBuilder {

  private permissions: codenvy.IPermissions;

  /**
   * Default constructor
   */
  constructor() {
    this.permissions = {
      actions: [],
      domainId: '',
      instanceId: '',
      userId: ''
    };
  }

  /**
   * Sets actions of the permissions
   *
   * @param {string[]} id user ID
   * @return {CodenvyPermissionsBuilder}
   */
  withActions(actions: string[]): CodenvyPermissionsBuilder {
    this.permissions.actions = actions;
    return this;
  }
  /**
   * Sets the user ID of the permissions
   *
   * @param {string} id user ID
   * @return {CodenvyPermissionsBuilder}
   */
  withUserId(id: string): CodenvyPermissionsBuilder {
    this.permissions.userId = id;
    return this;
  }

  /**
   * Sets the instance ID of the permissions
   *
   * @param {string} id instance ID
   * @return {CodenvyPermissionsBuilder}
   */
  withInstanceId(id: string): CodenvyPermissionsBuilder {
    this.permissions.instanceId = id;
    return this;
  }

  /**
   * Sets the domain ID of the permissions
   *
   * @param {string} id domain ID
   * @return {CodenvyPermissionsBuilder}
   */
  withDomainId(id: string): CodenvyPermissionsBuilder {
    this.permissions.domainId = id;
    return this;
  }

  /**
   * Build the permissions
   *
   * @return {codenvy.IOrganization}
   */
  build(): codenvy.IPermissions {
    return this.permissions;
  }

}
