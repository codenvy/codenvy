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


import {CreateFactoryCtrl} from '../create-factory/create-factory.controller';

import {FactoryFromWorkspaceCtrl} from '../create-factory/workspaces-tab/factory-from-workpsace.controller.js';
import {FactoryFromWorkspace} from '../create-factory/workspaces-tab/factory-from-workspace.directive.js';
import {FactoryFromFileCtrl} from '../create-factory/config-file-tab/factory-from-file.controller';
import {FactoryFromFile} from '../create-factory/config-file-tab/factory-from-file.directive';
import {FactoryFromTemplateCtrl} from '../create-factory/template-tab/factory-from-template.controller';
import {FactoryFromTemplate} from '../create-factory/template-tab/factory-from-template.directive';
import {FactoryActionBoxController} from './action/factory-action-box.controller';
import {FactoryActionBox} from './action/factory-action-box.directive';
import {FactoryActionDialogEditController} from './action/factory-action-edit.controller';
import {FactoryCommandController} from './command/factory-command.controller';
import {FactoryCommand} from './command/factory-command.directive';
import {FactoryCommandDialogEditController} from './command/factory-command-edit.controller';
import {CreateFactoryGitController} from './git/create-factory-git.controller';
import {CreateFactoryGit} from './git/create-factory-git.directive';

export class CreateFactoryConfig {

  constructor(register) {

    register.controller('CreateFactoryCtrl', CreateFactoryCtrl);

    register.controller('FactoryFromWorkspaceCtrl', FactoryFromWorkspaceCtrl);
    register.directive('cdvyFactoryFromWorkspace', FactoryFromWorkspace);

    register.controller('FactoryFromFileCtrl', FactoryFromFileCtrl);
    register.directive('cdvyFactoryFromFile', FactoryFromFile);

    register.controller('FactoryFromTemplateCtrl', FactoryFromTemplateCtrl);
    register.directive('cdvyFactoryFromTemplate', FactoryFromTemplate);

    register.controller('FactoryActionBoxController', FactoryActionBoxController);
    register.directive('cdvyFactoryActionBox', FactoryActionBox);

    register.controller('FactoryCommandController', FactoryCommandController);
    register.directive('cdvyFactoryCommand', FactoryCommand);

    register.controller('CreateFactoryGitController', CreateFactoryGitController);
    register.directive('cdvyCreateFactoryGit', CreateFactoryGit);

    register.controller('FactoryActionDialogEditController', FactoryActionDialogEditController);
    register.controller('FactoryCommandDialogEditController', FactoryCommandDialogEditController);



    // config routes
    register.app.config(function ($routeProvider) {
      $routeProvider.accessWhen('/factories/create-factory', {
        title: 'New Factory',
        templateUrl: 'app/factories/create-factory/create-factory.html',
        controller: 'CreateFactoryCtrl',
        controllerAs: 'createFactoryCtrl'
      });

    });

  }
}
