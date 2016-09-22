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
 * @ngdoc directive
 * @name account.profile.directive:accountProfile
 * @restrict E
 * @element
 *
 * @description
 * <account-profile></account-profile>` for displaying account profile.
 *
 * @usage
 *   <account-profile></account-profile>
 *
 * @author Florent Benoit
 */
export class AccountProfile {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.templateUrl = 'app/account/details/profile/account-profile.html';
    this.replace = false;

    this.controller = 'AccountProfileController';
    this.controllerAs = 'accountProfileController';

    this.bindToController = true;

    this.scope = true;

  }

}
