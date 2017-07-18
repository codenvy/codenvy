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
      this.accountId = this.codenvyTeam.getPersonalAccount() ? this.codenvyTeam.getPersonalAccount().id : null;
    }, (error: any) => {
      if (error.status === 304) {
        this.accountId = this.codenvyTeam.getPersonalAccount() ? this.codenvyTeam.getPersonalAccount().id : null;
      }
    });
  }
}
