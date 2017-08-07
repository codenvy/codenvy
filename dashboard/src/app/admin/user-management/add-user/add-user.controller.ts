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
import {AdminsUserManagementCtrl} from '../user-management.controller';
import {CodenvyOrganization} from '../../../../components/api/codenvy-organizations.factory';
import {CodenvyPermissions} from '../../../../components/api/codenvy-permissions.factory';
import {CodenvyOrganizationRoles} from '../../../../components/api/codenvy-organization-roles';

/**
 * This class is handling the controller for the add user
 * @author Oleksii Orel
 */
export class AdminsAddUserController {
  private $mdDialog: ng.material.IDialogService;
  private lodash: _.LoDashStatic;
  private cheNotification: any;
  private cheUser: any;
  private callbackController: AdminsUserManagementCtrl;
  private newUserName: string;
  private newUserEmail: string;
  private newUserPassword: string;
  private organizations: Array<string>;
  private organization: string;
  private codenvyOrganization: CodenvyOrganization;
  private codenvyPermissions: CodenvyPermissions;

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($mdDialog: ng.material.IDialogService, cheUser: any, cheNotification: any, lodash: _.LoDashStatic,
              codenvyOrganization: CodenvyOrganization, codenvyPermissions: CodenvyPermissions) {
    this.$mdDialog = $mdDialog;
    this.lodash = lodash;
    this.cheUser = cheUser;
    this.cheNotification = cheNotification;
    this.codenvyOrganization = codenvyOrganization;
    this.codenvyPermissions = codenvyPermissions;

    this.organizations = [];

    this.codenvyOrganization.fetchOrganizations().then(() => {
      let organizations = this.codenvyOrganization.getOrganizations();
      let rootOrganizations = organizations.filter((organization: any) => {
        return !organization.parent;
      });
      this.organizations = lodash.pluck(rootOrganizations, 'name');
      if (this.organizations.length > 0) {
        this.organization = this.organizations[0];
      }
    });
  }

  /**
   * Callback of the cancel button of the dialog.
   */
  abort(): void {
    this.$mdDialog.hide();
  }

  /**
   * Callback of the add button of the dialog(create new user).
   */
  createUser(): void {
    let promise = this.cheUser.createUser(this.newUserName, this.newUserEmail, this.newUserPassword);

    promise.then((data: any) => {
      if (this.organization) {
        this.addUserToOrganization(data.id);
      } else {
        this.finish();
      }
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to create user.');
    });
  }

  /**
   * Finish user creation.
   */
  private finish(): void {
    this.$mdDialog.hide();
    this.callbackController.updateUsers();
    this.cheNotification.showInfo('User successfully created.');
  }

  /**
   * Adds user to choosen organization.
   *
   * @param userId
   */
  private addUserToOrganization(userId: string): void {
    let organizations = this.codenvyOrganization.getOrganizations();
    let organization = this.lodash.find(organizations, (organization: any) => {
      return organization.name === this.organization;
    });

    let actions = CodenvyOrganizationRoles.MEMBER.actions;
    let permissions = {
      instanceId: organization.id,
      userId: userId,
      domainId: 'organization',
      actions: actions
    };
    this.codenvyPermissions.storePermissions(permissions).then(() => {
      this.finish();
    }, (error: any) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Failed to add user to organization' + this.organization + '.');
    });
  }
}
