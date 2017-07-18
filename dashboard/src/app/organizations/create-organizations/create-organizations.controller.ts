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
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyOrganization} from '../../../components/api/codenvy-organizations.factory';

/**
 * @ngdoc controller
 * @name organizations.create.controller:CreateOrganizationController
 * @description This class is handling the controller for the new organization creation.
 * @author Oleksii Orel
 */
export class CreateOrganizationController {
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Permissions API interaction.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Notifications service.
   */
  private cheNotification: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Log service.
   */
  private $log: ng.ILogService;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Current organization's name.
   */
  private organizationName: string;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * The list of users to invite.
   */
  private members: Array<codenvy.IMember>;
  /**
   * Parent organization name.
   */
  private parentQualifiedName: string;
  /**
   * Parent organization id.
   */
  private parentOrganizationId: string;
  /**
   * List of members of parent organization.
   */
  private parentOrganizationMembers: Array<che.IUser>;

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor(codenvyOrganization: CodenvyOrganization, codenvyPermissions: CodenvyPermissions, cheUser: any, cheNotification: any,
              $location: ng.ILocationService, $q: ng.IQService, $log: ng.ILogService, $rootScope: che.IRootScopeService,
              initData: any) {
    this.codenvyOrganization = codenvyOrganization;
    this.codenvyPermissions = codenvyPermissions;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.$location = $location;
    this.$q = $q;
    this.$log = $log;
    $rootScope.showIDE = false;

    this.organizationName = '';
    this.isLoading = false;
    this.members = [];

    // injected by route provider
    this.parentQualifiedName = initData.parentQualifiedName;
    this.parentOrganizationId = initData.parentOrganizationId;
    this.parentOrganizationMembers = initData.parentOrganizationMembers;
  }

  /**
   * Check if the name is unique.
   * @param name
   * @returns {boolean}
   */
  isUniqueName(name: string): boolean {
    let organizations = this.codenvyOrganization.getOrganizations();
    let account = this.parentQualifiedName ? this.parentQualifiedName + '/' : '';
    if (!organizations.length) {
      return true;
    } else {
      for (let i = 0; i < organizations.length; i++) {
        if (organizations[i].qualifiedName === account + name) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Performs new organization creation.
   */
  createOrganization(): void {
    this.isLoading = true;
    this.codenvyOrganization.createOrganization(this.organizationName, this.parentOrganizationId).then((organization: codenvy.IOrganization) => {
      this.addPermissions(organization, this.members);
      this.codenvyOrganization.fetchOrganizations();
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to create organization ' + this.organizationName + '.';
      this.cheNotification.showError(message);
    });
  }

  /**
   * Add permissions for members in pointed organization.
   *
   * @param organization {codenvy.IOrganization} organization
   * @param members members to be added to organization
   */
  addPermissions(organization: codenvy.IOrganization, members: Array<any>) {
    let promises = [];
    members.forEach((member: codenvy.IMember) => {
      if (member.id && member.id !== this.cheUser.getUser().id) {
        let actions = this.codenvyOrganization.getActionsFromRoles(member.roles);
        let permissions = {
          instanceId: organization.id,
          userId: member.id,
          domainId: 'organization',
          actions: actions
        };

        let promise = this.codenvyPermissions.storePermissions(permissions);
        promises.push(promise);
      }
    });

    this.$q.all(promises).then(() => {
      this.isLoading = false;
      this.$location.path('/organization/' + organization.qualifiedName);
    }, (error: any) => {
      this.isLoading = false;
      let message = error.data && error.data.message ? error.data.message : 'Failed to create organization ' + this.organizationName + '.';
      this.cheNotification.showError(message);
    });
  }
}
