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

import {CodenvyAPI} from './codenvy-api.factory';
import {CodenvyUser} from './codenvy-user.factory';
import {CodenvyFactory} from './codenvy-factory.factory';
import {CodenvyFactoryTemplate} from './codenvy-factory-template.factory';

export class CodenvyApiConfig {

  constructor(register) {
    register.factory('codenvyUser', CodenvyUser);
    register.app.constant('clientTokenPath', '/');//is necessary for Braintree
    register.factory('codenvyFactory', CodenvyFactory);
    register.factory('codenvyFactoryTemplate', CodenvyFactoryTemplate);
    register.factory('codenvyAPI', CodenvyAPI);

  }
}
