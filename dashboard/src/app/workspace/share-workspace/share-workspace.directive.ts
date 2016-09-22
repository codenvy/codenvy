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
 * @ngdoc directive
 * @name workspaces.details.directive:shareWorkspace
 * @restrict E
 * @element
 *
 * @description
 * <share-workspace></share-workspace> for managing sharing the workspace.
 *
 * @usage
 *   <share-workspace></share-workspace>
 *
 * @author Ann Shumilova
 */
export class ShareWorkspace {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
    this.restrict = 'E';
    this.templateUrl = 'app/workspace/share-workspace/share-workspace.html';

    this.controller = 'ShareWorkspaceController';
    this.controllerAs = 'shareWorkspaceController';
    this.bindToController = true;
  }
}
