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

export class AccountCtrl {

  /**
   * Controller for a account details
   * @ngInject for Dependency injection
   * @author Oleksii Orel
   */
  constructor($routeParams, $location, cheAPI, cheNotification, $rootScope, $timeout, $scope) {
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    this.profile = this.cheAPI.getProfile().getProfile();

    this.profileAttributes = {};

    //copy the profile attribute if it exist
    if (this.profile.attributes) {
      this.profileAttributes = angular.copy(this.profile.attributes);
    } else {
      this.profile.$promise.then(() => {
        this.profileAttributes = angular.copy(this.profile.attributes);
      });
    }

    //search the selected tab
    let routeParams = $routeParams.tabName;
    if (!routeParams) {
      this.selectedTabIndex = 0;
    } else {
      switch (routeParams) {
        case 'profile':
          this.selectedTabIndex = 0;
          break;
        case 'security':
          this.selectedTabIndex = 1;
          break;
        default:
          $location.path('/account');
      }
    }

    this.resetPasswordForm = false;

    $rootScope.showIDE = false;

    this.timeoutPromise;
    $scope.$watch(() => {return this.profileAttributes}, () => {
      if (!$scope.profileInformationForm || $scope.profileInformationForm.$invalid) {
        return;
      }
      $timeout.cancel(this.timeoutPromise);

      this.timeoutPromise = $timeout(() => {
        this.setProfileAttributes();
      }, 500);
    }, true);
    $scope.$on('$destroy', () => {
      if (this.timeoutPromise) {
        $timeout.cancel(this.timeoutPromise);
      }
    });
  }

  /**
   * Check if profile attributes have changed
   * @returns {boolean}
   */
  isAttributesChanged() {
    return !angular.equals(this.profile.attributes, this.profileAttributes);
  }

  /**
   * Set profile attributes
   */
  setProfileAttributes() {

    if (this.isAttributesChanged()) {
      let promise = this.cheAPI.getProfile().setAttributes(this.profileAttributes);

      promise.then(() => {
        this.cheNotification.showInfo('Profile successfully updated.');
        this.profile.attributes = angular.copy(this.profileAttributes);
      }, (error) => {
        if (error.status === 304) {
          this.profile.attributes = angular.copy(this.profileAttributes);
        } else {
          this.profileAttributes = angular.copy(this.profile.attributes);
          this.cheNotification.showError(error.data.message ? error.data.message : 'Profile update failed.');
          console.log('error', error);
        }
      });

    }
  }

  /**
   * Set new password
   */
  setPassword(password) {
    if (!password) {
      return;
    }

    let promise = this.cheAPI.getUser().setPassword(password);

    promise.then(() => {
      this.cheNotification.showInfo('Password successfully updated.');
      this.resetPasswordForm = true;
    }, (error) => {
      this.cheNotification.showError(error.data.message ? error.data.message : 'Password updated failed.');
      console.log('error', error);
    });
  }

}
