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
import {CodenvyTeam} from '../../components/api/codenvy-team.factory';

/**
 * @ngdoc controller
 * @name dashboard.controller:DashboardController
 * @description This class is handling the controller for dashboard page.
 * @author Ann Shumilova
 */
export class MainDashboardController {
  codenvyTeam: CodenvyTeam;
  accountId: string;

  /**
   * @ngInject for Dependency injection
   */
  constructor ($rootScope: che.IRootScopeService, codenvyTeam: CodenvyTeam) {
    this.codenvyTeam = codenvyTeam;

    $rootScope.showIDE = false;

    this.accountId = '';

    this.fetchAccountId();
  }

  /**
   * Fetches account ID.
   *
   * @return {IPromise<any>}
   */
  fetchAccountId(): ng.IPromise<any> {
    return this.codenvyTeam.fetchTeams().then(() => {
      this.accountId = this.codenvyTeam.getPersonalAccount().id;
    }, (error: any) => {
      if (error.status === 304) {
        this.accountId = this.codenvyTeam.getPersonalAccount().id;
      }
    });
  }
}
