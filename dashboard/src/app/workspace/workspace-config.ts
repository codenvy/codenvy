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

import {ShareWorkspaceController} from './share-workspace/share-workspace.controller';
import {ShareWorkspace} from './share-workspace/share-workspace.directive';

import {TimeoutInfoController} from './timeout/timeout-info.controller';
import {TimeoutInfo} from './timeout/timeout-info.directive';

import {UserItem} from './share-workspace/user-item/user-item.directive';
import {UserItemController} from './share-workspace/user-item/user-item.controller';
import {AddDeveloperController} from './share-workspace/add-developers/add-developers.controller';
import {AddMemberController} from './share-workspace/add-members/add-members.controller';

export class WorkspaceConfig {

  constructor(register: che.IRegisterService) {
    register.controller('ShareWorkspaceController', ShareWorkspaceController);
    register.directive('shareWorkspace', ShareWorkspace);

    register.controller('UserItemController', UserItemController);
    register.directive('userItem', UserItem);

    register.controller('AddDeveloperController', AddDeveloperController);
    register.controller('AddMemberController', AddMemberController);

    register.controller('TimeoutInfoController', TimeoutInfoController);
    register.directive('timeoutInfo', TimeoutInfo);
  }
}
