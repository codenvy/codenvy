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
import {CodenvyTeam} from '../codenvy-team.factory';


/**
 * Defines a directive for checking whether team name already exists.
 *
 * @author Ann Shumilova
 */
export class UniqueTeamNameValidator {

  /**
   * Team interection API.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Promises service.
   */
  private $q: ng.IQService;
  private restrict: string;
  private require: string;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor (codenvyTeam: CodenvyTeam, $q: ng.IQService) {
    this.codenvyTeam = codenvyTeam;
    this.$q = $q;
    this.restrict = 'A';
    this.require = 'ngModel';
  }

  /**
   * Check that the name of team is unique
   */
  link($scope: ng.IScope, element: ng.IAugmentedJQuery, attributes: ng.IAttributes, ngModel: any) {

    // validate only input element
    if ('input' === element[0].localName) {

      ngModel.$asyncValidators.uniqueTeamName = (modelValue: any) => {

        // parent scope ?
        var scopingTest = $scope.$parent;
        if (!scopingTest) {
          scopingTest = $scope;
        }
        var deferred = this.$q.defer();
        let currentTeamName = scopingTest.$eval(attributes.uniqueTeamName),
          parentAccount = scopingTest.$eval(attributes.parentAccount),
          teams = this.codenvyTeam.getTeams();

        if (teams.length) {
          for (let i = 0; i < teams.length; i++) {
            if (teams[i].qualifiedName === parentAccount + '/' + currentTeamName) {
              continue;
            }
            if (teams[i].qualifiedName === parentAccount + '/' + modelValue) {
              deferred.reject(false);
            }
          }
          deferred.resolve(true);
        } else {
          deferred.resolve(true);
        }
        return deferred.promise;
      };
    }
  }
}
