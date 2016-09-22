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
 * Defines a directive for displaying update password widget.
 * @author Oleksii Orel
 */
export class AccountUpdatePassword {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor() {
    this.restrict = 'E';
    this.replace = false;
    this.templateUrl = 'app/account/details/account-update-password.html';

    // scope values
    this.scope = {
      password: '=cdvyPassword',
      resetPassword: '=cdvyResetPassword'
    };

  }

  /**
   * Keep reference to the model controller
   */
  link($scope) {
    $scope.$watch('changePasswordForm.$pristine', () => {
      $scope.$watch('newPassword', (newVal) => {
        if (!$scope.changePasswordForm || $scope.changePasswordForm.$invalid || ($scope.confirmPassword !== newVal)) {
          $scope.password = null;
          return;
        }
        $scope.password = newVal;
      });
      $scope.$watch('confirmPassword', (newVal) => {
        if (!$scope.changePasswordForm || $scope.changePasswordForm.newPassword.$invalid || ($scope.newPassword !== newVal)) {
          $scope.password = null;
          return;
        }
        $scope.password = newVal;
      });
      $scope.$watch('resetPassword', (newVal) => {
        if (!newVal) {
          return;
        }
        $scope.newPassword = '';
        $scope.confirmPassword = '';
        $scope.resetPassword = false;
      });
    });

  }

}
