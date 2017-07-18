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

