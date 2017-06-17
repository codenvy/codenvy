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

interface IAccountScope extends ng.IScope {
  profileInformationForm: ng.IFormController;
}

export class AccountCtrl {

  private cheAPI: any;
  private cheNotification: any;
  private profile: che.IProfile;
  private selectedTabIndex: number;
  private profileAttributes: che.IProfileAttributes;
  private newPassword: string;

  private isLoading: boolean;

  /**
   * Controller for a account details
   * @ngInject for Dependency injection
   * @author Oleksii Orel
   */
  constructor($routeParams: ng.route.IRouteParamsService, $location: ng.ILocationService, cheAPI: any, cheNotification: any, $timeout: ng.ITimeoutService, $scope: IAccountScope) {
    this.cheAPI = cheAPI;
    this.cheNotification = cheNotification;

    this.profile = this.cheAPI.getProfile().getProfile();

    this.profileAttributes = {};

    // copy the profile attribute if it exist
    if (this.profile.attributes) {
      this.profileAttributes = angular.copy(this.profile.attributes);
    } else {
      this.profile.$promise.then(() => {
        this.profileAttributes = angular.copy(this.profile.attributes);
      });
    }

    // search the selected tab
    let routeParams = (<any>$routeParams).tabName;
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

    let timeoutPromise: ng.IPromise<any>;
    $scope.$watch(() => {
      return this.profileAttributes;
    }, () => {
      const form: ng.IFormController = $scope.profileInformationForm;
      if (!form || form.$invalid) {
        return;
      }
      if (angular.isDefined(timeoutPromise)) {
        $timeout.cancel(timeoutPromise);
      }

      timeoutPromise = $timeout(() => {
        this.setProfileAttributes();
      }, 500);
    }, true);
    $scope.$on('$destroy', () => {
      if (angular.isDefined(timeoutPromise)) {
        $timeout.cancel(timeoutPromise);
      }
    });
  }

  /**
   * Check if profile attributes have changed
   * @returns {boolean}
   */
  isAttributesChanged(): boolean {
    return !angular.equals(this.profile.attributes, this.profileAttributes);
  }

  /**
   * Set profile attributes
   */
  setProfileAttributes(): void {
    if (!this.isAttributesChanged()) {
      return;
    }
    const promise = this.cheAPI.getProfile().setAttributes(this.profileAttributes);

    this.isLoading = true;
    promise.then(() => {
      this.cheNotification.showInfo('Profile successfully updated.');
      this.profile.attributes = angular.copy(this.profileAttributes);
    }, (error: any) => {
      if (error.status === 304) {
        this.profile.attributes = angular.copy(this.profileAttributes);
      } else {
        this.profileAttributes = angular.copy(this.profile.attributes);
        this.cheNotification.showError(error.data.message ? error.data.message : 'Profile update failed.');
        console.log('error', error);
      }
    }).finally(() => {
      this.isLoading = false;
    });
  }

  /**
   * Sets new password
   */
  setPassword(): void {
    if (!this.newPassword) {
      return;
    }
    const promise = this.cheAPI.getUser().setPassword(this.newPassword);

    this.isLoading = true;
    promise.then(() => {
      this.cheNotification.showInfo('Password successfully updated.');
      this.newPassword = '';
    }, (error: any) => {
      this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Password updated failed.');
    }).finally(() => {
      this.isLoading = false;
    });
  }

}
