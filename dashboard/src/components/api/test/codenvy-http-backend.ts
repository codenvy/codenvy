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
import {CodenvyAPIBuilder} from '../builder/codenvy-api-builder.factory';


/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CodenvyHttpBackend {
  private httpBackend: ng.IHttpBackendService;
  private defaultBranding: any;
  private teamsMap: Map<string, any>;
  private permissionsMap: Map<string, Array<codenvy.IPermissions>>;
  private resourcesMap: Map<string, Map<string, any>>;

  /**
   * Constructor to use
   */
  constructor($httpBackend: ng.IHttpBackendService, codenvyAPIBuilder: CodenvyAPIBuilder) {
    this.httpBackend = $httpBackend;

    this.defaultBranding = {};

    this.teamsMap = new Map();

    this.permissionsMap = new Map();

    this.resourcesMap = new Map();

    this.httpBackend.when('OPTIONS', '/api/').respond({});

    this.httpBackend.when('GET', '/api/user/settings').respond({});

    // change password
    this.httpBackend.when('POST', '/api/user/password').respond(() => {
      return [200, {success: true, errors: []}];
    });

    // create new user
    this.httpBackend.when('POST', '/api/user').respond(() => {
      return [200, {success: true, errors: []}];
    });
    // admin role - false
    this.httpBackend.when('GET', '/api/user/inrole?role=admin&scope=system&scopeId=').respond(false);
    // user role - true
    this.httpBackend.when('GET', '/api/user/inrole?role=user&scope=system&scopeId=').respond(true);
    // branding
    this.httpBackend.when('GET', 'assets/branding/product.json').respond(this.defaultBranding);
    this.httpBackend.when('GET', /\/_app\/compilation-mappings(\?.*$)?/).respond(200, '');
    // settings
    this.httpBackend.when('GET', '/api/workspace/settings').respond({});
  }

  /**
   * Gets the internal http backend used
   * @returns {CheHttpBackend.httpBackend|*}
   */
  getHttpBackend() {
    return this.httpBackend;
  }

  /**
   * Setup Backend for teams
   */
  teamsBackendSetup() {
    let allTeams = [];

    let teamsKeys = this.teamsMap.keys();
    for (let key of teamsKeys) {
      let team = this.teamsMap.get(key);
      this.httpBackend.when('GET', '/api/organization/' + team.id).respond(team);
      this.httpBackend.when('DELETE', '/api/organization/' + team.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      allTeams.push(team);
    }

    this.httpBackend.when('GET', /\/api\/organization(\?.*$)?/).respond(allTeams);
  }

  /**
   * Add the given team to teamsMap
   * @param team
   */
  addTeamById(team) {
    this.teamsMap.set(team.id, team);
  }

  /**
   * Setup Backend for permissions.
   */
  permissionsBackendSetup(): void {
    const keys = this.permissionsMap.keys();
    for (let domainInstanceKey of keys) {
      const permissionsList = this.permissionsMap.get(domainInstanceKey);
      const {domainId, instanceId} = permissionsList[0];

      this.httpBackend.when('GET', `/api/permissions/${domainId}/all?instance=${instanceId}`).respond(permissionsList);
    }
  }

  /**
   * Add permission to a permissions map
   *
   * @param {codenvy.IPermissions} permissions
   */
  addPermissions(permissions: codenvy.IPermissions): void {
    let domainInstanceKey = permissions.domainId + '|' + permissions.instanceId;

    if (this.permissionsMap.has(domainInstanceKey)) {
      this.permissionsMap.get(domainInstanceKey).push(permissions);
    } else {
      this.permissionsMap.set(domainInstanceKey, [permissions]);
    }
  }

  /**
   * Setup Backend for resources.
   */
  resourcesBackendSetup(): void {
    const keys = this.resourcesMap.keys();
    for (let organizationId of keys) {
      const organizationResourcesMap = this.resourcesMap.get(organizationId);

      // distributed
      if (organizationResourcesMap.has('distributed')) {
        const resources = organizationResourcesMap.get('distributed');
        this.httpBackend.when('GET', `/api/organization/resource/${organizationId}/cap`).respond(resources);
      }

      // total
      if (organizationResourcesMap.has('total')) {
        const resources = organizationResourcesMap.get('total');
        this.httpBackend.when('GET', `/api/resource/${organizationId}`).respond(resources);
      }
    }
  }

  /**
   * Add resource to a resources map
   *
   * @param {string} organizationId organization ID
   * @param {string} scope total, used or available
   * @param {any} resource
   */
  addResource(organizationId: string, scope: string, resource: any): void {
    if (!this.resourcesMap.has(organizationId)) {
      this.resourcesMap.set(organizationId, new Map());
    }

    const organizationResourcesMap = this.resourcesMap.get(organizationId);
    if (organizationResourcesMap.has(scope)) {
      organizationResourcesMap.get(scope).push(resource);
    } else {
      organizationResourcesMap.set(scope, [resource]);
    }
  }

}

