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
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CodenvyHttpBackend {

  /**
   * Constructor to use
   */
  constructor($httpBackend, codenvyAPIBuilder) {
    this.httpBackend = $httpBackend;

    this.defaultBranding = {};

    this.defaultUser = {};
    this.userIdMap = new Map();
    this.userEmailMap = new Map();

    this.factoriesMap = new Map();

    this.pageMaxItem = 5;
    this.pageSkipCount = 0;

    this.httpBackend.when('OPTIONS', '/api/').respond({});

    // change password
    this.httpBackend.when('POST', '/api/user/password').respond(() => {
      return [200, {success: true, errors: []}];
    });

    // create new user
    this.httpBackend.when('POST', '/api/user').respond(() => {
      return [200, {success: true, errors: []}];
    });
    //license legality - true
    this.httpBackend.when('GET', '/api/license/system/legality').respond({isLegal: true});

    // admin role - false
    this.httpBackend.when('GET', '/api/user/inrole?role=admin&scope=system&scopeId=').respond(false);
    // user role - true
    this.httpBackend.when('GET', '/api/user/inrole?role=user&scope=system&scopeId=').respond(true);
    // branding
    this.httpBackend.when('GET', 'assets/branding/product.json').respond(this.defaultBranding);
  }


  /**
   * Setup Backend for factories
   */
  factoriesBackendSetup() {
    let allFactories = [];
    let pageFactories = [];

    let factoriesKeys = this.factoriesMap.keys();
    for (let key of factoriesKeys) {
      let factory = this.factoriesMap.get(key);
      this.httpBackend.when('GET', '/api/factory/' + factory.id).respond(factory);
      this.httpBackend.when('DELETE', '/api/factory/' + factory.id).respond(() => {
        return [200, {success: true, errors: []}];
      });
      allFactories.push(factory);
    }

    if (this.defaultUser) {
      this.httpBackend.when('GET', '/api/user').respond(this.defaultUser);

      if (allFactories.length >  this.pageSkipCount) {
        if(allFactories.length > this.pageSkipCount + this.pageMaxItem) {
          pageFactories = allFactories.slice(this.pageSkipCount, this.pageSkipCount + this.pageMaxItem);
        } else {
          pageFactories = allFactories.slice(this.pageSkipCount);
        }
      }
      this.httpBackend.when('GET', '/api/factory/find?creator.userId=' + this.defaultUser.id + '&maxItems=' + this.pageMaxItem + '&skipCount=' + this.pageSkipCount).respond(pageFactories);
    }
  }

  /**
   * Setup all users
   */
  usersBackendSetup() {
    this.httpBackend.when('GET', '/api/user').respond(this.defaultUser);

    let userIdKeys = this.userIdMap.keys();
    for (let key of userIdKeys) {
      this.httpBackend.when('GET', '/api/user/' + key).respond(this.userIdMap.get(key));
    }

    let userEmailKeys = this.userEmailMap.keys();
    for (let key of userEmailKeys) {
      this.httpBackend.when('GET', '/api/user/find?email=' + key).respond(this.userEmailMap.get(key));
    }
  }

  /**
   * Add the given factory
   * @param factory
   */
  addUserFactory(factory) {
    this.factoriesMap.set(factory.id, factory);
  }

  /**
   * Sets max objects on response
   * @param pageMaxItem
   */
  setPageMaxItem(pageMaxItem) {
    this.pageMaxItem = pageMaxItem;
  }

  /**
   * Sets skip count of values
   * @param pageSkipCount
   */
  setPageSkipCount(pageSkipCount) {
    this.pageSkipCount = pageSkipCount;
  }

  /**
   * Add the given user
   * @param user
   */
  setDefaultUser(user) {
    this.defaultUser = user;
  }

  /**
   * Add the given user to userIdMap
   * @param user
   */
  addUserById(user) {
    this.userIdMap.set(user.id, user);
  }

  /**
   * Add the given user to userEmailMap
   * @param user
   */
  addUserEmail(user) {
    this.userEmailMap.set(user.email, user);
  }

  /**
   * Gets the internal http backend used
   * @returns {CheHttpBackend.httpBackend|*}
   */
  getHttpBackend() {
    return this.httpBackend;
  }
}

