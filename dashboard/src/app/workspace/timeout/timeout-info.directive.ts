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

/**
 * Defines a directive for displaying idle timeout info.
 *
 * @author Ann Shumilova
 */
export class TimeoutInfo {
  restrict: string = 'E';
  replace: boolean = false;
  templateUrl: string = 'app/workspace/timeout/timeout-info.html';

  bindToController: boolean = true;

  controller: string = 'TimeoutInfoController';
  controllerAs: string = 'timeoutInfoController';

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor () {
  }
}
