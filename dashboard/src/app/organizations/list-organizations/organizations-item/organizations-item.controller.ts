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
import {CodenvyOrganization} from '../../../../components/api/codenvy-organizations.factory';
import {OrganizationsPermissionService} from '../../organizations-permission.service';
import {CodenvyPermissions} from '../../../../components/api/codenvy-permissions.factory';
import {CodenvyOrganizationActions} from '../../../../components/api/codenvy-organization-actions';

/**
 * @ngdoc controller
 * @name organizations.list.Item.controller:OrganizationsItemController
 * @description This class is handling the controller for item of organizations list
 * @author Oleksii Orel
 */
export class OrganizationsItemController {
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * User service.
   */
  private userServices: codenvy.IUserServices;
  /**
   * Organization permission service.
   */
  private organizationsPermissionService: OrganizationsPermissionService;
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Organization details (the value is set in directive attributes).
   */
  private organization: codenvy.IOrganization;
  /**
   * Callback needed to react on organizations updation (the value is set in directive attributes).
   */
  private onUpdate: Function;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, codenvyOrganization: CodenvyOrganization, confirmDialogService: any, cheNotification: any,  organizationsPermissionService: OrganizationsPermissionService, codenvyPermissions: CodenvyPermissions) {
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.codenvyOrganization = codenvyOrganization;
    this.cheNotification = cheNotification;
    this.organizationsPermissionService =  organizationsPermissionService;

    this.userServices = codenvyPermissions.getUserServices();
  }

  /**
   * returns true if current user has Delete permission
   * @returns {boolean}
   */
  hasDeletePermission(): boolean {
    if (!this.organization || (!this.organization.parent && !this.userServices.hasAdminUserService)) {
      return false;
    }
    return this.organizationsPermissionService.isUserAllowedTo(CodenvyOrganizationActions.DELETE, this.organization.id);
  }

  /**
   * Gets all sub organizations.
   */
  getAllSubOrganizations(): Array<codenvy.IOrganization> {
    let subOrganizationsTree = this.codenvyOrganization.getOrganizations().filter((organization: codenvy.IOrganization) => {
      if (!organization.parent || this.organization.id === organization.id) {
        return false;
      }
      return organization.qualifiedName.indexOf(this.organization.qualifiedName + '/') === 0;
    });

    return subOrganizationsTree;
  }

  /**
   * Redirect to factory details.
   */
  redirectToOrganizationDetails(tab: string) {
      this.$location.path('/organization/' + this.organization.qualifiedName).search(!tab ? {} : {tab: tab});
  }

  /**
   * Removes organization after confirmation.
   */
  removeOrganization(): void {
    this.confirmRemoval().then(() => {
      this.codenvyOrganization.deleteOrganization(this.organization.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        let message = 'Failed to delete organization ' + this.organization.name;
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : message);
      });
    });
  }

  /**
   * Shows dialog to confirm the current organization removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    return this.confirmDialogService.showConfirmDialog('Delete organization',
      'Would you like to delete organization \'' + this.organization.name + '\'?', 'Delete');
  }
}
