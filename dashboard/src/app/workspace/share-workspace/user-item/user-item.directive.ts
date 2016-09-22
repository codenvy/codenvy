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
 * Defines a directive for user item in permissions list.
 *
 * @author Ann Shumilova
 */
export class UserItem {

  /**
   * Default constructor.
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/workspace/share-workspace/user-item/user-item.html';
    this.replace = false;

    this.controller = 'UserItemController';
    this.controllerAs = 'userItemController';

    this.bindToController = true;

    // scope values
    this.scope = {
      user: '=codenvyUser',
      callback: '=callback'
    };
  }
}
