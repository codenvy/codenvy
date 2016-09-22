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

import {ImsArtifactApi} from './ims-artifact.factory';
import {ImsConfigApi} from './ims-config.factory';
import {ImsSaasAuthApi} from './ims-saasauth.factory';
import {ImsSubscriptionApi} from './ims-subscription.factory';
import {ImsUpdateApi} from './ims-update.factory';
import {ImsNodesApi} from './ims-nodes.factory';
import {ImsPropertiesApi} from './ims-properties.factory';
import {ImsEventLoggingApi} from './ims-eventlogging.factory';
import {ImsLicenseApi} from './ims-license.factory';

export class ImsApiConfig {

  constructor(register) {
    register.factory('imsArtifactApi', ImsArtifactApi);
    register.factory('imsConfigApi', ImsConfigApi);

    register.factory('imsSaasAuthApi', ImsSaasAuthApi);

    register.factory('imsSubscriptionApi', ImsSubscriptionApi);

    register.factory('imsUpdateApi', ImsUpdateApi);

    register.factory('imsNodesApi', ImsNodesApi);

    register.factory('imsPropertiesApi', ImsPropertiesApi);

    register.factory('imsEventLoggingApi', ImsEventLoggingApi);

    register.factory('imsLicenseApi', ImsLicenseApi);
  }
}
