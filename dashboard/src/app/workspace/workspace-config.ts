/*
 *  [2015] - [2017] Codenvy, S.A.
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

import {ShareWorkspaceController} from './share-workspace/share-workspace.controller';
import {ShareWorkspace} from './share-workspace/share-workspace.directive';

import {UserItem} from './share-workspace/user-item/user-item.directive';
import {UserItemController} from './share-workspace/user-item/user-item.controller';
import {AddMemberController} from './share-workspace/add-members/add-members.controller';

export class WorkspaceConfig {

  constructor(register) {
    register.controller('ShareWorkspaceController', ShareWorkspaceController);
    register.directive('shareWorkspace', ShareWorkspace);

    register.controller('UserItemController', UserItemController);
    register.directive('userItem', UserItem);

    register.controller('AddMemberController', AddMemberController);
  }
}
