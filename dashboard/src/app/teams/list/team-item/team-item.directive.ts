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

/**
 * Defines a directive for team item in list.
 *
 * @author Ann Shumilova
 */
export class TeamItem {
  restrict: string = 'E';
  templateUrl: string = 'app/teams/list/team-item/team-item.html';
  replace: boolean = false;

  controller: string = 'TeamItemController';
  controllerAs: string = 'teamItemController';
  bindToController: boolean = true;
  require: Array<string> = ['ngModel'];
  scope: {
    [propName: string]: string
  };

  constructor() {
    this.scope = {
      team: '=team',
      members: '=',
      ramCap: '=',
      isChecked: '=cdvyChecked',
      isSelect: '=?ngModel',
      onCheckboxClick: '&?cdvyOnCheckboxClick',
      onUpdate: '&?onUpdate'
    };
  }
}
