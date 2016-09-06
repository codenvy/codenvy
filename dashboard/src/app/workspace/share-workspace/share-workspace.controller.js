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
  constructor(cheWorkspace, codenvyUser, codenvyPermissions, cheNotification, $mdDialog, $document, $mdConstant, $route, $q, lodash) {
    "ngInject";

    this.cheWorkspace = cheWorkspace;
    this.codenvyUser = codenvyUser;
    this.codenvyPermissions = codenvyPermissions;
    this.cheNotification = cheNotification;
    this.$mdDialog = $mdDialog;
    this.$document = $document;
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
    //Filtered entered emails - users that are not registered
    this.notExistingUsers = [];

    this.isLoading = false;
    //Temp solution with defined actions, better to provide list of available for user to choose:
    this.actions = ['read', 'use', 'run', 'configure'];
    this.workspaceDomain = 'workspace';
    this.recipeDomain = 'recipe';

    this.namespace = $route.current.params.namespace;
    this.workspaceName = $route.current.params.workspaceName;

    this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
    if (this.workspace) {
      this.refreshWorkspacePermissions();
    } else {
      this.isLoading = true;
      let promise = this.cheWorkspace.fetchWorkspaces();

      promise.then(() => {
        this.isLoading = false;
        this.workspace = this.cheWorkspace.getWorkspaceByName(this.namespace, this.workspaceName);
        this.refreshWorkspacePermissions();
      }, (error) => {
        this.isLoading = false;
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to update workspace data.');
      });
    }

    this.userOrderBy = 'email';
    this.userFilter = {email: ''};
    this.usersSelectedStatus = {};
    this.isNoSelected = true;
    this.isAllSelected = false;
    this.isBulkChecked = false;
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
      this.notExistingUsers.length = 0;

      this.refreshWorkspacePermissions();
    }, (error) => {
      this.isLoading = false;
      this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to share workspace.');
    });

    return permissionPromises;
  }

  /**
   * Check all users in list
   */
  selectAllUsers() {
    this.users.forEach((user) => {
      this.usersSelectedStatus[user.id] = true;
    });
  }

  /**
   * Uncheck all users in list
   */
  deselectAllUsers() {
    this.users.forEach((user) => {
      this.usersSelectedStatus[user.id] = false;
    });
  }

  /**
   * Change bulk selection value
   */
  changeBulkSelection() {
    if (this.isBulkChecked) {
      this.deselectAllUsers();
      this.isBulkChecked = false;
    } else {
      this.selectAllUsers();
      this.isBulkChecked = true;
    }
    this.updateSelectedStatus();
  }

  /**
   * Update users selected status
   */
  updateSelectedStatus() {
    this.isNoSelected = true;
    this.isAllSelected = true;

    this.users.forEach((user) => {
      if (this.usersSelectedStatus[user.id]) {
        this.isNoSelected = false;
      } else {
        this.isAllSelected = false;
      }
    });

    if (this.isNoSelected) {
      this.isBulkChecked = false;
      return;
    }

    if (this.isAllSelected) {
      this.isBulkChecked = true;
    }
  }

  /**
   * Removes all selected users permissions for current workspace
   */
  deleteSelectedWorkspaceMembers() {
    let usersSelectedStatusKeys = Object.keys(this.usersSelectedStatus);
    let checkedUsers = [];

    if (!usersSelectedStatusKeys.length) {
      this.cheNotification.showError('No such workspace members.');
      return;
    }

    this.users.forEach((user) => {
      usersSelectedStatusKeys.forEach((key) => {
        if (user.id === key && this.usersSelectedStatus[key] === true) {
          checkedUsers.push(user);
        }
      });
    });

    let queueLength = checkedUsers.length;
    if (!queueLength) {
      this.cheNotification.showError('No such workspace member.');
      return;
    }

    let confirmationPromise = this.showDeleteConfirmation(queueLength);
    confirmationPromise.then(() => {
      let numberToDelete = queueLength;
      let isError = false;
      let deleteUserPromises = [];
      let currentUserEmail;
      checkedUsers.forEach((user) => {
        currentUserEmail = user.email;
        this.usersSelectedStatus[user.id] = false;
        let promise = this.codenvyPermissions.removeWorkspacePermissions(user.permissions.instance, user.id);
        promise.then(() => {
          queueLength--;
        }, (error) => {
          isError = true;
          this.$log.error('Cannot delete permissions: ', error);
        });
        deleteUserPromises.push(promise);
      });

      this.$q.all(deleteUserPromises).finally(() => {
        this.refreshWorkspacePermissions();
        if (isError) {
          this.cheNotification.showError('Delete failed.');
        } else {
          if (numberToDelete === 1) {
            this.cheNotification.showInfo(currentUserEmail + 'has been removed.');
          } else {
            this.cheNotification.showInfo('Selected numbers have been removed.');
          }
        }
      });
    });
  }

  /**
   * Show confirmation popup before delete
   * @param numberToDelete
   * @returns {*}
   */
  showDeleteConfirmation(numberToDelete) {
    let confirmTitle = 'Would you like to delete ';
    if (numberToDelete > 1) {
      confirmTitle += 'these ' + numberToDelete + ' members?';
    } else {
      confirmTitle += 'this selected member?';
    }
    let confirm = this.$mdDialog.confirm()
      .title(confirmTitle)
      .ariaLabel('Remove members')
      .ok('Delete!')
      .cancel('Cancel')
      .clickOutsideToClose(true);

    return this.$mdDialog.show(confirm);
  }


  /**
   * Add new workspace member. Show the dialog
   * @param  event - the $event
   */
  showAddMembersDialog(event) {
    let parentEl = angular.element(this.$document.body);

    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AddMemberController',
      controllerAs: 'addMemberController',
      locals: {callbackController: this},
      parent: parentEl,
      templateUrl: 'app/workspace/share-workspace/add-members/add-members.html'
    });
  }

  /**
   * Returns the list of the not registered users emails as string.
   *
   * @returns {*} string with wrong emails coma separated
   */
  getNotExistingEmails() {
    return this.notExistingUsers.join(', ');
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
      this.notExistingUsers.push(email);
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

    this.lodash.remove(this.notExistingUsers, (data) => {
      return email === data;
    });
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

    let defaultEnvironment = environments[defaultEnvName];

    let devMachine = this.lodash.find(defaultEnvironment.machines, (machine) => {
      return machine.agents.indexOf('org.eclipse.che.ws-agent') >= 0;
    });

    //TODO check after environment model changes completed
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
