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

interface IAccountUpdateScope extends ng.IScope {
  newPassword: string;
  passStrength: number;
  confirmPassword: string;
  onPasswordChange: Function;
  isPasswordMatch: Function;
}

/**
 * Defines a directive for displaying update password widget.
 * @author Oleksii Orel
 */
export class AccountUpdatePassword {
  restrict = 'E';
  templateUrl = 'app/account/details/account-update-password.html';

  // scope values
  scope = {
    newPassword: '=cdvyPassword',
    changePasswordForm: '=cdvyForm'
  };

  /**
   * Keep reference to the model controller
   */
  link($scope: IAccountUpdateScope) {
    const reTests = [/[a-z]/, /[A-Z]/, /\d/, /[^a-zA-Z\d]/];

    const checkPassStrength = (pass: string): void => {
      if (!pass || !reTests || reTests.length === 0) {
        $scope.passStrength = 0;
        return;
      }
      let passStrength = pass && pass.length > 16 ? 1 : 0;
      reTests.forEach((reTest: RegExp) => {
        if (reTest.test(pass)) {
          passStrength++;
        }
      });

      $scope.passStrength = passStrength * 100 / reTests.length;
    };

    $scope.onPasswordChange = (value: string) => {
      $scope.confirmPassword = '';
      checkPassStrength(value);
    };

    $scope.isPasswordMatch = (value: string) => {
      return !value || value === $scope.newPassword;
    };
  }
}
