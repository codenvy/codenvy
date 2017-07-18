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
import {CodenvyOrganization} from '../../../components/api/codenvy-organizations.factory';
import {CodenvyResourceLimits} from '../../../components/api/codenvy-resource-limits';
import {CodenvyPermissions} from '../../../components/api/codenvy-permissions.factory';
import {CodenvyResourcesDistribution} from '../../../components/api/codenvy-resources-distribution.factory';
import {OrganizationsPermissionService} from '../organizations-permission.service';
import {CodenvyOrganizationActions} from '../../../components/api/codenvy-organization-actions';


/**
 * @ngdoc controller
 * @name organizations.list.controller:ListOrganizationsController
 * @description This class is handling the controller for listing the organizations
 * @author Oleksii Orel
 */
export class ListOrganizationsController {
  /**
   * Organization API interaction.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Permissions service.
   */
  private codenvyPermissions: CodenvyPermissions;
  /**
   * Resources distribution service.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;
  /**
   * Organization permission service.
   */
  private organizationsPermissionService: OrganizationsPermissionService;
  /**
   * List of organizations.
   */
  private organizations: Array<any>;
  /**
   * Map of organization members.
   */
  private organizationMembers: Map<string, number>;
  /**
   * Map of organization total resources.
   */
  private organizationTotalResources: Map<string, any>;
  /**
   * Map of organization available resources.
   */
  private organizationAvailableResources: Map<string, any>;
  /**
   * Loading state of the page.
   */
  private isLoading: boolean;
  /**
   * On update function.
   */
  private onUpdate: Function;

  /**
   * Parent organization name.
   */
  private parentName: string;
  /**
   * Parent organization id.
   */
  private parentId: string;
  /**
   * Organization order by.
   */
  private organizationOrderBy: string;
  /**
   * Organization filter.
   */
  private organizationFilter: {name: string};
  /**
   * User services.
   */
  private userServices: codenvy.IUserServices;
  /**
   * Selection and filtration helper.
   */
  private cheListHelper: che.widget.ICheListHelper;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, $scope: ng.IScope, codenvyPermissions: CodenvyPermissions, codenvyResourcesDistribution: CodenvyResourcesDistribution, codenvyOrganization: CodenvyOrganization, cheNotification: any, confirmDialogService: any, $route: ng.route.IRouteService, organizationsPermissionService: OrganizationsPermissionService, cheListHelperFactory: che.widget.ICheListHelperFactory) {
    this.$q = $q;
    this.cheNotification = cheNotification;
    this.codenvyPermissions = codenvyPermissions;
    this.codenvyOrganization = codenvyOrganization;
    this.confirmDialogService = confirmDialogService;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;

    this.parentName = $route.current.params.organizationName;
    this.organizationOrderBy = 'name';
    this.organizationFilter = {name: ''};

    const helperId = 'list-organizations';
    this.cheListHelper = cheListHelperFactory.getHelper(helperId);
    $scope.$on('$destroy', () => {
      cheListHelperFactory.removeHelper(helperId);
    });

    this.userServices = this.codenvyPermissions.getUserServices();
    this.organizationsPermissionService = organizationsPermissionService;

    $scope.$watch(() => {
      return this.organizations;
    }, (newValue: Array<any>, oldValue: Array<any>) => {
      if (newValue && !angular.equals(newValue, oldValue)) {
        this.processOrganizations();
      }
    });
    this.processOrganizations();
  }

  /**
   * Callback when name is changed.
   *
   * @param str {string} a string to filter organizations.
   */
  onSearchChanged(str: string): void {
    this.organizationFilter.name = str;
    this.cheListHelper.applyFilter('name', this.organizationFilter);
  }

  /**
   * Returns true if user has manage permission.
   *
   * @returns {boolean}
   */
  hasManagePermission(): boolean {
    if (this.parentId) {
      return this.organizationsPermissionService.isUserAllowedTo(CodenvyOrganizationActions.MANAGE_SUB_ORGANIZATION, this.parentId);
    }
    return this.userServices.hasAdminUserService;
  }

  /**
   * Process organization - retrieving additional data.
   */
  processOrganizations(): void {
    if (angular.isUndefined(this.organizations)) {
      return;
    }
    if (this.parentName) {
      const parentOrganization = this.codenvyOrganization.getOrganizationByName(this.parentName);
      this.parentId = parentOrganization ? parentOrganization.id : null;
    }
    if (this.organizations && this.organizations.length) {
      this.organizationMembers = new Map();
      this.organizationTotalResources = new Map();
      this.organizationAvailableResources = new Map();
      const promises = [];
      this.isLoading = true;
      this.organizations.forEach((organization: codenvy.IOrganization) => {
        const promiseMembers = this.codenvyPermissions.fetchOrganizationPermissions(organization.id).then(() => {
          this.organizationMembers.set(organization.id, this.codenvyPermissions.getOrganizationPermissions(organization.id).length);
        });
        promises.push(promiseMembers);
        let promiseTotalResource = this.codenvyResourcesDistribution.fetchTotalOrganizationResources(organization.id).then(() => {
          this.processTotalResource(organization.id);
        });
        promises.push(promiseTotalResource);

        let promiseAvailableResource = this.codenvyResourcesDistribution.fetchAvailableOrganizationResources(organization.id).then(() => {
          this.processAvailableResource(organization.id);
        });
        promises.push(promiseAvailableResource);
      });
      this.$q.all(promises).finally(() => {
        this.isLoading = false;
        this.cheListHelper.setList(this.organizations, 'id');
      });
    } else {
      this.cheListHelper.setList(this.organizations, 'id');
    }
  }

  /**
   * Process total organization resources.
   *
   * @param organizationId organization's id
   */
  processTotalResource(organizationId: string): void {
    let ramLimit = this.codenvyResourcesDistribution.getOrganizationTotalResourceByType(organizationId, CodenvyResourceLimits.RAM);
    this.organizationTotalResources.set(organizationId, ramLimit ? ramLimit.amount : undefined);
  }

  /**
   * Process available organization resources.
   *
   * @param organizationId organization's id
   */
  processAvailableResource(organizationId: string): void {
    let ramLimit = this.codenvyResourcesDistribution.getOrganizationAvailableResourceByType(organizationId, CodenvyResourceLimits.RAM);
    this.organizationAvailableResources.set(organizationId, ramLimit ? ramLimit.amount : undefined);
  }

  /**
   * Returns the number of organization's members.
   *
   * @param organizationId organization's id
   * @returns {any} number of organization members to display
   */
  getMembersCount(organizationId: string): any {
    if (this.organizationMembers && this.organizationMembers.size > 0) {
      return this.organizationMembers.get(organizationId) || '-';
    }
    return '-';
  }

  /**
   * Returns the total RAM of the organization.
   *
   * @param organizationId organization's id
   * @returns {any}
   */
  getTotalRAM(organizationId: string): any {
    if (this.organizationTotalResources && this.organizationTotalResources.size > 0) {
      let ram = this.organizationTotalResources.get(organizationId);
      return (ram && ram !== -1) ? (ram / 1024) : null;
    }
    return null;
  }

  /**
   * Returns the available RAM of the organization.
   *
   * @param organizationId organization's id
   * @returns {any}
   */
  getAvailableRAM(organizationId: string): any {
    if (this.organizationAvailableResources && this.organizationAvailableResources.size > 0) {
      let ram = this.organizationAvailableResources.get(organizationId);
      return (ram && ram !== -1) ? (ram / 1024) : null;
    }
    return null;
  }

  /**
   * Delete all selected organizations.
   */
  deleteSelectedOrganizations(): void {
    const selectedOrganizations = this.cheListHelper.getSelectedItems(),
          selectedOrganizationIds = selectedOrganizations.map((organization: codenvy.IOrganization) => {
            return organization.id;
          });

    if (!selectedOrganizationIds.length) {
      this.cheNotification.showError('No such organization.');
      return;
    }

    const confirmationPromise = this._showDeleteOrganizationConfirmation(selectedOrganizationIds.length);
    confirmationPromise.then(() => {
      let promises = [];

      selectedOrganizationIds.forEach((organizationId: string) => {
        this.cheListHelper.itemsSelectionStatus[organizationId] = false;

        let promise = this.codenvyOrganization.deleteOrganization(organizationId).catch((error: any) => {
          let errorMessage = 'Failed to delete organization ' + organizationId + '.';
          this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : errorMessage);
        });

        promises.push(promise);
      });

      this.$q.all(promises).finally(() => {
        if (typeof this.onUpdate !== 'undefined') {
          this.onUpdate();
        }
      });
    });
  }

  /**
   * Show confirmation popup before organization deletion.
   *
   * @param numberToDelete number of organization to be deleted
   * @returns {ng.IPromise<any>}
   */
  _showDeleteOrganizationConfirmation(numberToDelete: number): ng.IPromise<any> {
    let content = 'Would you like to delete ';
    if (numberToDelete > 1) {
      content += 'these ' + numberToDelete + ' organizations?';
    } else {
      content += 'this selected organization?';
    }

    return this.confirmDialogService.showConfirmDialog('Delete organizations', content, 'Delete');
  }

}
