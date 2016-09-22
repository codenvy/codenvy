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
 * Controller for creating factory from a workspace.
 * @author Oleksii Orel
 * @author Michail Kuznyetsov
 */
export class FactoryFromWorkspaceCtrl {

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($filter, cheAPI, codenvyAPI, cheNotification) {
    this.$filter = $filter;
    this.cheAPI = cheAPI;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;

    this.workspaces = cheAPI.getWorkspace().getWorkspaces();
    this.workspacesById = cheAPI.getWorkspace().getWorkspacesById();

    this.filtersWorkspaceSelected = {};

    this.workspaceFilter = {config:{name: ''}};

    this.isLoading = true;

    // fetch workspaces when initializing
    let promise = cheAPI.getWorkspace().fetchWorkspaces();

    promise.then(() => {
        this.isLoading = false;
        this.updateData();
      },
      (error) => {
        this.isLoading = false;
        if (error.status === 304) {
          this.updateData();
        }
      });

  }

  updateData() {
    this.setAllFiltersWorkspaces(true);
  }

  /**
   * Get factory content from workspace
   * @param workspace is selected workspace
   */
  getFactoryContentFromWorkspace(workspace) {
    let factoryContent = this.codenvyAPI.getFactory().getFactoryContentFromWorkspace(workspace);
    if (factoryContent) {
      this.factoryContent = this.$filter('json')(factoryContent, 2);
      return;
    }

    this.isImporting = true;

    let promise = this.codenvyAPI.getFactory().fetchFactoryContentFromWorkspace(workspace);

    promise.then((factoryContent) => {
      this.isImporting = false;
      this.factoryContent = this.$filter('json')(factoryContent, 2);
    }, (error) => {
      this.isImporting = false;
      this.factoryContent = null;
      this.cheNotification.showError(error.data.message ? error.data.message : 'Get factory configuration failed.');
      console.log('error', error);
    });
  }

  /**
   * Set all workspaces in the filters of workspaces
   * @param isChecked is setting value
   */
  setAllFiltersWorkspaces(isChecked) {
    this.workspaces.forEach((workspace) => {
      this.filtersWorkspaceSelected[workspace.id] = isChecked;
    });
  }

  /**
   * Get the workspace name by ID
   * @param workspaceId
   * @returns {String} workspace name
   */
  getWorkspaceName(workspaceId) {
    let workspace = this.workspacesById.get(workspaceId);
    if (workspace && workspace.config.name) {
      return workspace.config.name;
    }
    return '';
  }
}
