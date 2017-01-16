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
 * Defines a directive for factory item in list.
 * @author Oleksii Orel
 */
export class CodenvyFactoryItem {

  /**
   * Default constructor.
   */
  constructor() {
    this.restrict = 'E';

    this.templateUrl = 'app/factories/list-factories/factory-item/factory-item.html';
    this.replace = false;

    this.controller = 'FactoryItemCtrl';
    this.controllerAs = 'factoryItemCtrl';

    this.bindToController = true;

    // we require ngModel as we want to use it inside our directive
    this.require = ['ngModel'];

    // scope values
    this.scope = {
      factory: '=cdvyFactory',
      isChecked: '=cdvyChecked',
      isSelectable: '=cdvyIsSelectable',
      isSelect: '=?ngModel',
      onCheckboxClick: '&?cdvyOnCheckboxClick'
    };

  }

}
