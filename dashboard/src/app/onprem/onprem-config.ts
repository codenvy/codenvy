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

import {NagMessage} from './license-messages/nag-message/nag-message.directive';
import {LicenseLimitController} from './license-messages/license-limit-dialog/license-limit-dialog.controller';
import {LicenseAgreementController} from './license-messages/license-agreement-dialog/license-agreement-dialog.controller';
import {CancelAgreementController} from './license-messages/license-agreement-dialog/cancel-agreement-dialog.controller';
import {LicenseMessagesService} from './license-messages/license-messages.service';


export class CodenvyOnpremConfig {

  constructor(register: che.IRegisterService) {
    register.directive('cdvyNagMessage', NagMessage);
    register.controller('LicenseLimitController', LicenseLimitController);
    register.controller('LicenseAgreementController', LicenseAgreementController);
    register.controller('CancelAgreementController', CancelAgreementController);
    register.service('licenseMessagesService', LicenseMessagesService);
  }
}
