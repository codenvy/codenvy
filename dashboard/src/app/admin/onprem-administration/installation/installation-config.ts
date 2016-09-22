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

import {AutomaticUpdatesCtrl} from './automatic/automatic.controller';
import {AutomaticUpdatesPanel} from './automatic/automatic.directive';
import {OnPremConfigurationCtrl} from './configuration/configuration.controller';
import {ConfigurationPanel} from './configuration/configuration.directive';
import {UpgradeInstallationCtrl} from './upgrade/upgrade.controller';
import {UpgradeInstallationPanel} from './upgrade/upgrade.directive';
import {YourInstallationCtrl} from './yourinstall/yourinstall.controller';
import {YourInstallationPanel} from './yourinstall/yourinstall.directive';

export class OnPremisesAdminInstallationConfig {

  constructor(register) {

    register.controller('OnPremConfigurationCtrl', OnPremConfigurationCtrl);
    register.controller('UpgradeInstallationCtrl', UpgradeInstallationCtrl);
    register.controller('AutomaticUpdatesCtrl', AutomaticUpdatesCtrl);
    register.controller('YourInstallationCtrl', YourInstallationCtrl);

    register.directive('cdvyOnpremAutoUpdates', AutomaticUpdatesPanel);
    register.directive('cdvyOnpremInstallConfig', ConfigurationPanel);
    register.directive('cdvyOnpremUpgradeInstall', UpgradeInstallationPanel);
    register.directive('cdvyOnpremYourInstall', YourInstallationPanel);
  }
}
