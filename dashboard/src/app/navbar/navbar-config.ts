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

import {CodenvyNavBarController} from './navbar.controller';
import {CodenvyNavBar} from './navbar.directive';

export class CodenvyNavbarConfig {

  constructor(register) {
    register.controller('CodenvyNavBarController', CodenvyNavBarController);
    register.directive('codenvyNavBar', CodenvyNavBar);
  }
}
