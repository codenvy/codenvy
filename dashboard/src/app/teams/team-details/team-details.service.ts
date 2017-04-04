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
import {CodenvyTeam} from '../../../components/api/codenvy-team.factory';

/**
 * This class is fetch and handling the data for team details
 *
 * @author Oleksii Orel
 */
export class TeamDetailsService {

  /**
   * Promises service.
   */
  private $q: ng.IQService;
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * User API interaction.
   */
  private cheUser: any;
  /**
   * Route service.
   */
  private $route: ng.route.IRouteService;
  /**
   * Current team (comes from directive's scope).
   */
  private team: any;
  /**
   * Current team's owner (comes from directive's scope).
   */
  private owner: any;


  /**
   * @ngInject for Dependency injection
   */
  constructor($q: ng.IQService, cheUser: any, codenvyTeam: CodenvyTeam, $route: ng.route.IRouteService) {
    this.$q = $q;
    this.codenvyTeam = codenvyTeam;
    this.cheUser = cheUser;
    this.$route = $route;
  }

  /**
   * Fetches the team's details by it's name.
   * @param teamName {string}
   *
   * @return {ng.IPromise<any>}
   */
  fetchTeamDetailsByName(teamName: string): ng.IPromise<any> {
    if (!teamName) {
      return;
    }
    let deferred = this.$q.defer();
    this.codenvyTeam.fetchTeamByName(teamName).then((team: any) => {
      this.team = team;
      deferred.resolve(team);
    }, (error: any) => {
      this.team = null;
      deferred.reject(error);
    });

    return deferred.promise;
  }

  /**
   * Fetches the team's owner by team's name.
   * @param teamName {string}
   *
   * @return {ng.IPromise<any>}
   */
  fetchOwnerByTeamName(teamName: string): ng.IPromise<any> {
    let deferred = this.$q.defer();
    let parts = teamName.split('/');
    let accountName = (parts && parts.length > 0) ? parts[0] : '';

    this.cheUser.fetchUserByName(accountName).then((owner: any) => {
      this.owner = owner;
      deferred.resolve(owner);
    }, (error: any) => {
      this.owner = null;
      deferred.reject(error);
    });

    return deferred.promise;
  }

  /**
   * Gets the team.
   *
   * @return {any}
   */
  getTeam(): any {
    return this.team;
  }

  /**
   * Gets the owner.
   *
   * @return {any}
   */
  getOwner(): any {
    return this.owner;
  }
}
