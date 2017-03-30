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

import {CodenvyAPI} from './codenvy-api.factory';
import {CodenvyLicense} from './codenvy-license.factory';
import {CodenvyPermissions} from './codenvy-permissions.factory';
import {CodenvySystem} from './codenvy-system.factory';
import {CodenvyTeam} from './codenvy-team.factory';
import {CodenvyTeamEventsManager} from './codenvy-team-events-manager.factory';
import {CodenvyResourcesDistribution} from './codenvy-resources-distribution.factory';
import {CodenvyAPIBuilder} from './builder/codenvy-api-builder.factory';
import {CodenvyHttpBackendFactory} from './test/codenvy-http-backend.factory';
import {CodenvyHttpBackendProviderFactory} from './test/codenvy-http-backend-provider.factory'
import {CodenvyPayment} from './codenvy-payment.factory';
import {CodenvyInvoices} from './codenvy-invoices.factory';
import {CodenvySubscription} from './codenvy-subscription.factory';
import {CodenvyInvite} from './codenvy-invite.factory';

export class CodenvyApiConfig {

  constructor(register) {
    register.app.constant('clientTokenPath', '/'); // is necessary for Braintree
    register.factory('codenvyPermissions', CodenvyPermissions);
    register.factory('codenvyLicense', CodenvyLicense);
    register.factory('codenvySystem', CodenvySystem);
    register.factory('codenvyTeam', CodenvyTeam);
    register.factory('codenvyTeamEventsManager', CodenvyTeamEventsManager);
    register.factory('codenvyAPI', CodenvyAPI);
    register.factory('codenvyAPIBuilder', CodenvyAPIBuilder);
    register.factory('codenvyHttpBackend', CodenvyHttpBackendFactory);
    register.factory('codenvyHttpBackendProvider', CodenvyHttpBackendProviderFactory);
    register.factory('codenvyResourcesDistribution', CodenvyResourcesDistribution);
    register.factory('codenvyPayment', CodenvyPayment);
    register.factory('codenvyInvoices', CodenvyInvoices);
    register.factory('codenvySubscription', CodenvySubscription);
    register.factory('codenvyInvite', CodenvyInvite);
  }
}
