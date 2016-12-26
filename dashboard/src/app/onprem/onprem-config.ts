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
