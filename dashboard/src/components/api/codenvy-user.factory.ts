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
 * This class is handling the user API retrieval
 * @author Oleksii Orel
 */
export class CodenvyUser {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($q, $resource, $cookies, codenvyLicense, codenvyPermissions) {
    this.$q = $q;
    this.$resource = $resource;
    this.$cookies = $cookies;
    this.codenvyLicense = codenvyLicense;
    this.codenvyPermissions = codenvyPermissions;

    // remote call
    this.remoteUserAPI = this.$resource('/api/user', {}, {
      findByID: {method: 'GET', url: '/api/user/:userId'},
      findByAlias: {method: 'GET', url: '/api/user/find?email=:alias'},
      inRole: {method: 'GET', url: '/api/user/inrole?role=:role&scope=:scope&scopeId=:scopeId'},
      setPassword: {
        method: 'POST', url: '/api/user/password', isArray: false,
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
        }
      },
      createUser: {method: 'POST', url: '/api/user'},
      getUsers: {
        method: 'GET',
        url: '/api/admin/user?maxItems=:maxItems&skipCount=:skipCount',
        isArray: false,
        responseType: 'json',
        transformResponse: (data, headersGetter) => {
          return this._getPageFromResponse(data, headersGetter('link'));
        }
      },
      removeUserById: {method: 'DELETE', url: '/api/user/:userId'}
    });

    this.logoutAPI = this.$resource('/api/auth/logout', {});

    // users by ID
    this.useridMap = new Map();

    // users by alias
    this.userAliasMap = new Map();

    // all users by ID
    this.usersMap = new Map();

    // page users by relative link
    this.userPagesMap = new Map();

    //pages info
    this.pageInfo = {};

    //Current user has to be for sure fetched:
    this.fetchUser();
  }

  /**
   * Create new user
   * @param name - new user name
   * @param email - new user e-mail
   * @param password - new user password
   * @returns {*}
   */
  createUser(name, email, password) {
    let data = {
      password: password,
      name: name
    };

    if (email) {
      data.email = email;
    }

    let promise = this.remoteUserAPI.createUser(data).$promise;

    // check if was OK or not
    promise.then((user) => {
      //update users map
      this.usersMap.set(user.id, user);//add user
      this.codenvyLicense.fetchLicenseLegality();//fetch license legality
    });

    return promise;
  }

  _getPageFromResponse(data, headersLink) {
    let links = new Map();
    if (!headersLink) {
      return {users: data};
    }
    let pattern = new RegExp('<([^>]+?)>.+?rel="([^"]+?)"', 'g');
    let result;
    while (result = pattern.exec(headersLink)) { //look for pattern
      links.set(result[2], result[1]);//add link
    }
    return {
      users: data,
      links: links
    };
  }

  _getPageParamByLink(pageLink) {
    let lastPageParamMap = new Map();
    let pattern = new RegExp('([_\\w]+)=([\\w]+)', 'g');
    let result;
    while (result = pattern.exec(pageLink)) {
      lastPageParamMap.set(result[1], result[2]);
    }

    let skipCount = lastPageParamMap.get('skipCount');
    let maxItems = lastPageParamMap.get('maxItems');
    if (!maxItems || maxItems === 0) {
      return null;
    }
    return {
      maxItems: maxItems,
      skipCount: skipCount ? skipCount : 0
    };
  }

  _updateCurrentPage() {
    let pageData = this.userPagesMap.get(this.pageInfo.currentPageNumber);
    if (!pageData) {
      return;
    }
    this.usersMap.clear();
    if (!pageData.users) {
      return;
    }
    pageData.users.forEach((user) => {
      this.usersMap.set(user.id, user);//add user
    });
  }

  _updateCurrentPageUsers(users) {
    this.usersMap.clear();
    if (!users) {
      return;
    }
    users.forEach((user) => {
      this.usersMap.set(user.id, user);//add user
    });
  }

  /**
   * Update user page links by relative direction ('first', 'prev', 'next', 'last')
   */
  _updatePagesData(data) {
    if (!data.links) {
      return;
    }
    let firstPageLink = data.links.get('first');
    if (firstPageLink) {
      let firstPageData = {link: firstPageLink};
      if (this.pageInfo.currentPageNumber === 1) {
        firstPageData.users = data.users;
      }
      if (!this.userPagesMap.get(1) || firstPageData.users) {
        this.userPagesMap.set(1, firstPageData);
      }
    }
    let lastPageLink = data.links.get('last');
    if (lastPageLink) {
      let pageParam = this._getPageParamByLink(lastPageLink);
      this.pageInfo.countOfPages = pageParam.skipCount / pageParam.maxItems + 1;
      this.pageInfo.count = pageParam.skipCount;
      let lastPageData = {link: lastPageLink};
      if (this.pageInfo.currentPageNumber === this.pageInfo.countOfPages) {
        lastPageData.users = data.users;
      }
      if (!this.userPagesMap.get(this.pageInfo.countOfPages) || lastPageData.users) {
        this.userPagesMap.set(this.pageInfo.countOfPages, lastPageData);
      }
    }
    let prevPageLink = data.links.get('prev');
    let prevPageNumber = this.pageInfo.currentPageNumber - 1;
    if (prevPageNumber > 0 && prevPageLink) {
      let prevPageData = {link: prevPageLink};
      if (!this.userPagesMap.get(prevPageNumber)) {
        this.userPagesMap.set(prevPageNumber, prevPageData);
      }
    }
    let nextPageLink = data.links.get('next');
    let nextPageNumber = this.pageInfo.currentPageNumber + 1;
    if (nextPageNumber) {
      let lastPageData = {link: nextPageLink};
      if (!this.userPagesMap.get(nextPageNumber)) {
        this.userPagesMap.set(nextPageNumber, lastPageData);
      }
    }
  }

  /**
   * Gets the pageInfo
   * @returns {Object}
   */
  getPagesInfo() {
    return this.pageInfo;
  }

  /**
   * Ask for loading the users in asynchronous way
   * If there are no changes, it's not updated
   * @param maxItems - the max number of items to return
   * @param skipCount - the number of items to skip
   * @returns {*} the promise
   */
  fetchUsers(maxItems, skipCount) {
    let promise = this.remoteUserAPI.getUsers({maxItems: maxItems, skipCount: skipCount}).$promise;

    promise.then((data) => {
      this.pageInfo.currentPageNumber = skipCount / maxItems + 1;
      this._updateCurrentPageUsers(data.users);
      this._updatePagesData(data);
    });

    return promise;
  }

  /**
   * Ask for loading the users page in asynchronous way
   * If there are no changes, it's not updated
   * @param pageKey - the key of page ('first', 'prev', 'next', 'last'  or '1', '2', '3' ...)
   * @returns {*} the promise
   */
  fetchUsersPage(pageKey) {
    let deferred = this.$q.defer();
    let pageNumber;
    if ('first' === pageKey) {
      pageNumber = 1;
    } else if ('prev' === pageKey) {
      pageNumber = this.pageInfo.currentPageNumber - 1;
    } else if ('next' === pageKey) {
      pageNumber = this.pageInfo.currentPageNumber + 1;
    } else if ('last' === pageKey) {
      pageNumber = this.pageInfo.countOfPages;
    } else {
      pageNumber = parseInt(pageKey, 10);
    }
    if (pageNumber < 1) {
      pageNumber = 1;
    } else if (pageNumber > this.pageInfo.countOfPages) {
      pageNumber = this.pageInfo.countOfPages;
    }
    let pageData = this.userPagesMap.get(pageNumber);
    if (pageData.link) {
      this.pageInfo.currentPageNumber = pageNumber;
      let promise = this.remoteUserAPI.getUsers(this._getPageParamByLink(pageData.link)).$promise;
      promise.then((data) => {
        this._updatePagesData(data);
        pageData.users = data.users;
        this._updateCurrentPage();
        deferred.resolve(data);
      }, (error) => {
        if (error && error.status === 304) {
          this._updateCurrentPage();
        }
        deferred.reject(error);
      });
    } else {
      deferred.reject({data: {message: 'Error. No necessary link.'}});
    }
    return deferred.promise;
  }

  /**
   * Gets the users
   * @returns {Map}
   */
  getUsersMap() {
    return this.usersMap;
  }

  /**
   * Performs user deleting by the given user ID.
   * @param userId the user id
   * @returns {*} the promise
   */
  deleteUserById(userId) {
    let promise = this.remoteUserAPI.removeUserById({userId: userId}).$promise;

    // check if was OK or not
    promise.then(() => {
      //update users map
      this.usersMap.delete(userId);//remove user
      this.codenvyLicense.fetchLicenseLegality();//fetch license legality
    });

    return promise;
  }

  /**
   * Performs current user deletion.
   * @returns {*} the promise
   */
  deleteCurrentUser() {
    let userId = this.user.id;
    let promise = this.remoteUserAPI.removeUserById({userId: userId}).$promise;
    return promise;
  }

  /**
   * Performs logout of current user.
   * @returns {y.ResourceClass.prototype.$promise|*|$promise|T.$promise|S.$promise}
   */
  logout() {
    let data = {token: this.$cookies['session-access-key']};
    let promise = this.logoutAPI.save(data).$promise;
    return promise;
  }

  /**
   * Gets the user
   * @return user
   */
  getUser() {
    return this.user;
  }

  /**
   * Fetch the user
   */
  fetchUser() {
    let promise = this.remoteUserAPI.get().$promise;
    // check if if was OK or not
    promise.then((user) => {
      this.user = user;
      });

    return promise;
  }

  fetchUserId(userId) {
    let promise = this.remoteUserAPI.findByID({userId: userId}).$promise;
    let parsedResultPromise = promise.then((user) => {
      this.useridMap.set(userId, user);
    });

    return parsedResultPromise;
  }

  getUserFromId(userId) {
    return this.useridMap.get(userId);
  }

  fetchUserByAlias(alias) {
    let promise = this.remoteUserAPI.findByAlias({alias: alias}).$promise;
    let parsedResultPromise = promise.then((user) => {
      this.useridMap.set(user.id, user);
      this.userAliasMap.set(alias, user);
    });

    return parsedResultPromise;
  }

  getUserByAlias(userAlias) {
    return this.userAliasMap.get(userAlias);
  }

  setPassword(password) {
    return this.remoteUserAPI.setPassword('password=' + password).$promise;
  }
}
