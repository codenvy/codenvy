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

import {CodenvyTeamRoles} from './codenvy-team-roles';
import {CodenvyTeamEventsManager} from './codenvy-team-events-manager.factory';
import {CodenvyOrganization} from './codenvy-organizations.factory';
import {CodenvyResourcesDistribution} from './codenvy-resources-distribution.factory';
import {CodenvyResourceLimits} from './codenvy-resource-limits';

interface ITeamsResource<T> extends ng.resource.IResourceClass<T> {
  findTeam(data: { teamName: string }): ng.resource.IResource<T>;
}

/**
 * This class is handling the interactions with Team management API.
 *
 * @author Ann Shumilova
 */
export class CodenvyTeam {
  /**
   * Angular Resource service.
   */
  private $resource: ng.resource.IResourceService;
  private $q: ng.IQService;
  private lodash: any;
  /**
   * Teams map by team's id.
   */
  private teamsMap: Map<string, any> = new Map();
  /**
   * Teams map by team's name.
   */
  private teamsByNameMap: Map<string, any> = new Map();
  /**
   * Array of teams.
   */
  private teams: any = [];
  /**
   * The registry for managing available namespaces.
   */
  private cheNamespaceRegistry: any;
  /**
   * The user API.
   */
  private cheUser : any;
  /**
   * The Codenvy Team notifications.
   */
  private teamEventsManager: CodenvyTeamEventsManager;
  /**
   * User's personal account.
   */
  private personalAccount: any;
  /**
   * Client for requesting Team API.
   */
  private remoteTeamAPI: ITeamsResource<any>;
  /**
   * Deferred object which will be resolved when teams are fetched
   */
  private fetchTeamsDefer: ng.IDeferred<any>;
  /**
   * The Codenvy Organization Service.
   */
  private codenvyOrganization: CodenvyOrganization;
  /**
   * The Codenvy resources API.
   */
  private codenvyResourcesDistribution: CodenvyResourcesDistribution;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource: ng.resource.IResourceService, $q: ng.IQService, lodash: any, cheNamespaceRegistry: any, cheUser: any,
              codenvyOrganization: CodenvyOrganization, codenvyTeamEventsManager: CodenvyTeamEventsManager, codenvyResourcesDistribution: CodenvyResourcesDistribution) {
    this.$resource = $resource;
    this.$q = $q;
    this.lodash = lodash;
    this.cheNamespaceRegistry = cheNamespaceRegistry;
    this.cheUser = cheUser;
    this.teamEventsManager = codenvyTeamEventsManager;
    this.codenvyOrganization = codenvyOrganization;
    this.codenvyResourcesDistribution = codenvyResourcesDistribution;

    this.remoteTeamAPI = <ITeamsResource<any>>$resource('/api/organization', {}, {
      findTeam: {method: 'GET', url: '/api/organization/find?name=:teamName'}
    });

    this.fetchTeamsDefer = this.$q.defer();
    const fetchTeamsPromise = this.fetchTeamsDefer.promise;
    this.cheNamespaceRegistry.setFetchPromise(fetchTeamsPromise);

    codenvyTeamEventsManager.addRenameHandler(() => {
      this.fetchTeams();
    });

    codenvyTeamEventsManager.addDeleteHandler(() => {
      this.fetchTeams();
    });

    codenvyTeamEventsManager.addNewTeamHandler(() => {
      this.fetchTeams();
    });
  }

  /**
   * Request the list of available teams.
   *
   * @returns {ng.IPromise<any>}
   */
  fetchTeams(): ng.IPromise<any> {
    let defer = this.$q.defer();

    let userPromise = this.cheUser.fetchUser();

    let teamsPromise = userPromise.then(() => {
      return this.codenvyOrganization.fetchOrganizations();
    }, (error: any) => {
      if (error.status === 304) {
        return this.codenvyOrganization.getOrganizations();
      }
      return this.$q.reject(error);
    });

    teamsPromise.then((teams: any[]) => {
      this.processTeams(teams, this.cheUser.getUser());
      defer.resolve();
    }, (error: any) => {
      if (error.status === 304) {
        defer.resolve();
      } else {
        defer.reject(error);
      }
    });

    return defer.promise.then(() => {
      this.fetchTeamsDefer.resolve();
    }, (error: any) => {
      this.fetchTeamsDefer.reject();
      return this.$q.reject(error);
    });
  }

  /**
   * Process teams to retrieve personal account (name of the organization === current user's name) and
   * teams (organization with parent).
   *
   * @param organizations {codenvy.IOrganization}
   * @param user {codenvy.IUser}
   */
  processTeams(organizations: Array<codenvy.IOrganization>, user: any): void {
    this.teamsMap = new Map();
    this.teams = [];
    this.cheNamespaceRegistry.getNamespaces().length = 0;

    let name = user.name;
    // detection personal account (organization which name equals to current user's name):
    this.personalAccount = this.lodash.find(organizations, (organization: codenvy.IOrganization) => {
      return organization.qualifiedName === name;
    });

    if (this.personalAccount) {
      // display personal account as "personal" on UI, namespace(id) stays the same for API interactions:
      this.cheNamespaceRegistry.getNamespaces().push({id: this.personalAccount.qualifiedName, label: 'personal', location: '/billing'});
      this.cheNamespaceRegistry.setCaption('Team');
    } else {
      this.cheNamespaceRegistry.setCaption('Organization');
      // todo add back, when API is ready: this.cheNamespaceRegistry.setEmptyMessage('You are not member of any organization and not able to create workspace. Please, contact your administrator.');
      this.processOrganizationInfoRetriever(organizations);
    }

    organizations.forEach((organization: codenvy.IOrganization) => {
      this.teamsMap.set(organization.id, organization);
      // team has to have parent (root organizations are skipped):
      if (organization.parent) {
        this.teams.push(organization);
        this.teamEventsManager.subscribeTeamNotifications(organization.id);
      }

      if (this.personalAccount) {
        if (organization.id !== this.personalAccount.id) {
          this.cheNamespaceRegistry.getNamespaces().push({
            id: organization.qualifiedName,
            label: organization.qualifiedName,
            location: '/team/' + organization.qualifiedName
          });
        }
      } else {
        this.cheNamespaceRegistry.getNamespaces().push({id: organization.qualifiedName, label: organization.qualifiedName, location: '/organization/' + organization.qualifiedName});
      }
    });
  }

  /**
   * Process organization information retriever.
   *
   * @param organizations
   */
  processOrganizationInfoRetriever(organizations: Array<codenvy.IOrganization>) {
    this.cheNamespaceRegistry.setGetAdditionalInfo((namespaceId: string) => {
      let organization = this.lodash.find(organizations, (organization: codenvy.IOrganization) => {
        return organization.qualifiedName === namespaceId;
      });

      if (!organization) {
        return null;
      }

      return this.codenvyResourcesDistribution.fetchAvailableOrganizationResources(organization.id).then(() => {
        let resource = this.codenvyResourcesDistribution.getOrganizationAvailableResourceByType(organization.id, CodenvyResourceLimits.RAM);
        return resource ? 'Available RAM: ' + (resource.amount / 1024) + 'GB' : null;
      });
    });
  }

  /**
   * Return current user's personal account.
   *
   * @returns {any} personal account
   */
  getPersonalAccount(): any {
    return this.personalAccount;
  }

  /**
   * Returns the array of teams.
   *
   * @returns {Array<any>} the array of teams
   */
  getTeams(): Array<any> {
    return this.teams;
  }

  /**
   * Requests team by it's id.
   *
   * @param id {string} the team's Id
   * @returns {ng.IPromise<any>} result promise
   */
  fetchTeamById(id: string): ng.IPromise<any> {
    let promise = this.codenvyOrganization.fetchOrganizationById(id);
    let resultPromise = promise.then((organization: codenvy.IOrganization) => {
      this.teamsMap.set(id, organization);
      return organization;
    }, (error: any) => {
      if (error.status === 304) {
        return this.teamsMap.get(id);
      }
      return this.$q.reject();
    });

    return resultPromise;
  }

  /**
   * Requests team by it's name.
   *
   * @param name the team's name
   * @returns {ng.IPromise<any>} result promise
   */
  fetchTeamByName(name: string): ng.IPromise<any> {
    let promise = this.remoteTeamAPI.findTeam({'teamName' : name}).$promise;
    let resultPromise = promise.then((team: any) => {
    this.teamsByNameMap.set(team.qualifiedName, team);
    return team;
    }, (error: any) => {
      if (error.status === 304) {
        return this.getTeamByName(name);
      }
      return this.$q.reject(error);
    });

    return resultPromise;
  }

  /**
   * Returns team by it's name.
   *
   * @param name team's name
   * @returns {any} team or <code>null</code> if not found
   */
  getTeamByName(name: string): any {
    if (this.personalAccount && this.personalAccount.qualifiedName === name) {
      return this.personalAccount;
    }

    if (this.teamsByNameMap.has(name)) {
      return this.teamsByNameMap.get(name);
    }

    for (let i = 0; i < this.teams.length; i++) {
      if (this.teams[i].qualifiedName === name) {
        return this.teams[i];
      }
    }

    return null;
  }

  /**
   * Returns team by it's id.
   *
   * @param id {string} team's id
   * @returns {any} team or <code>null</code> if not found
   */
  getTeamById(id: string): any {
    return this.teamsMap.get(id);
  }

  /**
   * Creates new team with pointed name.
   *
   * @param name the name of the team to be created
   * @returns {ng.IPromise<any>} result promise
   */
  createTeam(name: string): ng.IPromise<any> {
    return this.codenvyOrganization.createOrganization(name, this.personalAccount.id);
  }

  /**
   * Delete team by pointed id.
   *
   * @param id team's id to be deleted
   * @returns {ng.IPromise<any>} result promise
   */
  deleteTeam(id: string): ng.IPromise<any> {
    return this.codenvyOrganization.deleteOrganization(id);
  }

  /**
   * Update team's info.
   *
   * @param team the team info to be updated
   * @returns {ng.IPromise<any>} result promise
   */
  updateTeam(team: any): ng.IPromise<any> {
    return this.codenvyOrganization.updateOrganization(team);
  }

  /**
   * Forms the list of roles based on the list of actions
   *
   * @param actions array of actions
   * @returns {Array<any>} array of roles
   */
  getRolesFromActions(actions: Array<string>): Array<any> {
    let roles = [];
    let teamRoles = CodenvyTeamRoles.getValues();
    teamRoles.forEach((role: any) => {
      if (this.lodash.difference(role.actions, actions).length === 0) {
        roles.push(role);
      }
    });

    // avoid roles intake (filter if any role's action is subset of any other):
    roles = this.lodash.filter(roles, (role: any) => {
      return !this._checkIsSubset(role, roles);
    });

    return roles;
  }

  /**
   * Checks the actions in provided role to be part (subset) of any other role's actions.
   *
   * @param role role to be checked
   * @param roles list of roles
   * @returns {boolean} <code>true</code> if subset
   * @private
   */
  _checkIsSubset(role: any, roles: Array<any>): boolean {
    let isSubset = false;
    for (let i = 0; i < roles.length; i++) {
      let r = roles[i];
      // checks provided role's action is subset of any other role's actions in the roles list:
      if (role.actions.length === this.lodash.intersection(role.actions, r.actions).length && role.actions.length !== r.actions.length) {
        return true;
      }
    }

    return isSubset;
  }

  /**
   * Forms the list actions based on the list of roles.
   *
   * @param roles array of roles
   * @returns {Array<string>} actions array
   */
  getActionsFromRoles(roles: Array<any>): Array<string> {
    let actions = [];
    roles.forEach((role: any) => {
      actions = actions.concat(role.actions);
    });

    return actions;
  }

  getTeamDisplayName(team: any): string {
    let teamNames = this.lodash.pluck(this.teams, 'name');
    let size = this.lodash.pull(teamNames, team.name).length;

    return (this.teams.length - size) > 1 ? team.qualifiedName : team.name;
  }
}
