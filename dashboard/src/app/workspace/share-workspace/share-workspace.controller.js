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
 * Controller for workspace sharing with other users.
 *
 * @ngdoc controller
 * @name workspace.details.controller:ShareWorkspaceController
 * @description This class is handling the controller sharing workspace
 * @author Ann Shumilova
 */
export class ShareWorkspaceController {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWorkspace, codenvyUser, codenvyPermissions, cheNotification, $mdConstant, $route, $q, lodash) {
    "ngInject";

    this.cheWorkspace = cheWorkspace;
    this.codenvyUser = codenvyUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheNotification = cheNotification;
    this.$q = $q;
    this.lodash = lodash;

    //Email values separators:
    this.separators = [$mdConstant.KEY_CODE.ENTER, $mdConstant.KEY_CODE.COMMA, $mdConstant.KEY_CODE.SPACE];
    //Users that have permissions in current workspace:
    this.users = [];
    //Entered emails to share workspace with:
    this.emails = [];
    //Filtered entered emails - users that really are registered
    this.existingUsers = new Map();

    this.isLoading = false;
    //Temp solution with defined actions, better to provide list of available for user to choose:
    this.actions = ['read', 'use', 'run', 'configure'];
    this.workspaceDomain = 'workspace';
    this.recipeDomain = 'recipe';

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;

    this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    this.refreshWorkspacePermissions();
  }

  /**
   * Refresh the workspace permissions list.
   */
  refreshWorkspacePermissions() {
    this.isLoading = true;
    this.noPermissionsError = true;

    this.codenvyPermissions.fetchWorkspacePermissions(this.workspace.id).then(() => {
      this.isLoading = false;
      this.formUserList();
    }, (error) => {
      this.isLoading = false;
      if (error.status === 304) {
        this.formUserList();
      } else if (error.status === 403) {
        this.noPermissionsError = false;
      }
      else {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve workspace permissions.');
      }
    });
  }

  /**
   * Combines permissions and users data in one list.
   */
  formUserList() {
    let permissions = this.codenvyPermissions.getWorkspacePermissions(this.workspace.id);
    this.users = [];

    permissions.forEach((permission) => {
      let userId = permission.user;
      let user = this.codenvyUser.getUserFromId(userId);
      if (user) {
        this.formUserItem(user, permission);
      } else {
        this.codenvyUser.fetchUserId(userId).then(() => {
          this.formUserItem(this.codenvyUser.getUserFromId(userId), permission);
        });
      }
    });
  }

  /**
   * Forms item to display with permissions and user data.
   *
   * @param user user data
   * @param permissions permissions data
   */
  formUserItem(user, permissions) {
    let userItem = angular.copy(user);
    userItem.permissions = permissions;
    this.users.push(userItem);
  }

  shareWorkspace() {
    let permissionPromises = [];
    this.recipeId = this.getRecipeId();

    this.existingUsers.forEach((user) => {
      permissionPromises.push(this.storeWorkspacePermissions(user));
      if (this.recipeId) {
        permissionPromises.push(this.storeRecipePermissions(user));
      }
    });

    this.$q.all(permissionPromises).then(() => {
      //Clear share input data:
      this.emails.length = 0;
      this.existingUsers.clear();

      this.refreshWorkspacePermissions();
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to share workspace.');
    });
  }

  /**
   * Stores user premissions of the current workspace.
   *
   * @param user user data
   * @returns {*} promise with store permissions request
   */
  storeWorkspacePermissions(user) {
    let permission = {};
    permission.user = user.id;
    permission.domain = this.workspaceDomain;
    permission.instance = this.workspace.id;
    permission.actions = this.actions;

    return this.codenvyPermissions.storePermissions(permission);
  }

  /**
   * Strores user permissions for current workspace's recipe.
   *
   * @param user user data
   * @returns {*} promise with store permissions request
   */
  storeRecipePermissions(user) {
    let permission = {};
    permission.user = user.id;
    permission.domain = this.recipeDomain;
    permission.instance = this.recipeId;
    permission.actions = ['read'];

    return this.codenvyPermissions.storePermissions(permission);
  }

  /**
   * Removes user permissions for current workspace
   *
   * @param user user
   */
  removePermissions(user) {
    this.isLoading = true;
    this.codenvyPermissions.removeWorkspacePermissions(user.permissions.instance, user.id).then(() => {
      this.refreshWorkspacePermissions();
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to remove user ' + user.email + ' permissions.');
    });
  }

  /**
   * Handle user email adding.
   *
   * @param email user's email
   * @returns {*}
   */
  handleUserAdding(email) {
    //Prevents mensioning same users twice:
    if (this.existingUsers.has(email)) {
      return null;
    }

    //Displays user name instead of email
    if (this.codenvyUser.getUserByAlias(email)) {
      let user = this.codenvyUser.getUserByAlias(email);
      this.existingUsers.set(email, user);
      return email;
    }

    let findUser = this.codenvyUser.fetchUserByAlias(email).then(() => {
      let user = this.codenvyUser.getUserByAlias(email);
      this.existingUsers.set(email, user);
    }, (error) => {
      //do nothing
    });

    return email;
  }

  /**
   * Removes removed email from the list of registered users.
   *
   * @param email email to remove
   */
  onRemoveEmail(email) {
    this.existingUsers.delete(email);
  }

  /**
   * Returns user's name by email
   *
   * @param email user email
   * @returns {user.name|*} user's name
   */
  getUserName(email) {
    let user = this.codenvyUser.getUserByAlias(email);
    return user ? user.name : email;
  }

  /**
   * Checks user exists in the system (is reqistered).
   *
   * @param email user's email to be checked
   * @returns {boolean}
   */
  isUserExists(email) {
    return (this.codenvyUser.getUserByAlias(email) !== undefined);
  }

  /**
   * Detects recipe id from it's location.
   *
   * (Format of the url : http://{host}/api/recipe/{recipe id}/script).
   *
   * @returns {*|string}
   */
  getRecipeId() {
    let workspace = this.cheWorkspace.getWorkspaceById(this.workspace.id);
    let environments = workspace.config.environments;
    let defaultEnvName = workspace.config.defaultEnv;

    let defaultEnvironment = this.lodash.find(environments, (environment) => {
        return environment.name === defaultEnvName;
    });

    let devMachine = this.lodash.find(defaultEnvironment.machineConfigs, (config) => {
      return config.dev;
    });

    let recipeLocation = devMachine.source.location;
    if (!recipeLocation || recipeLocation.length === 0) {
      return null;
    }

    let recipePath = '/recipe/';
    let start = recipeLocation.indexOf(recipePath);
    let end = recipeLocation.indexOf('/script', start);

    let recipeId = (start > 0 && end > 0) ? recipeLocation.substring(start + recipePath.length, end) : null;
    return recipeId;
  }
}
