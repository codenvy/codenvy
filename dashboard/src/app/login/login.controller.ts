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

export class LoginCtrl {
  /*@ngInject*/
  constructor($http, $cookies, $window, cheUser: any, $timeout, $location) {

    this.username = '';
    this.password = '';

    this.$http = $http;
    this.$cookies = $cookies;
    this.$window = $window;
    this.$timeout = $timeout;
    this.cheUser = cheUser;
    this.location = $location;

    // hide the navbar
    angular.element('#codenvynavbar').hide();
    angular.element('#codenvyfooter').hide();
  }


    submit() {
      // reset error message
      this.error = null;
      this.loginInProgress = true;

      let loginData = {'username': this.username, 'password': this.password};

      this.$http({
        url: '/api/auth/login',
        method: 'POST',
        data: loginData
      }).then((response) => {

        this.$cookies.token = response.data.value;
        this.$window.sessionStorage['codenvyToken'] = response.data.value;
        this.$cookies.refreshStatus = 'DISABLED';

        // update user
        let promise = this.cheUser.fetchUser();
        promise.then(() => this.refresh() , () => this.refresh());
      },  (response) => {
        this.loginInProgress = false;
        console.log('error on login', response);
        this.error = response.statusText;

      });
    }

  refresh() {

    // refresh the home page
    this.$location = '/';
    this.$window.location = '/';
    this.$timeout(() =>  this.$window.location.reload(), 500);

  }

}

