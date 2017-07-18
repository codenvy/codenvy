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
