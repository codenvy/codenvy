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


import {CodenvyHttpBackend} from './codenvy-http-backend';


/**
 * This class is providing helper methods for simulating a fake HTTP backend simulating
 * @author Florent Benoit
 * @author Oleksii Orel
 */
export class CodenvyHttpBackendFactory extends CodenvyHttpBackend {

  /**
   * Default constructor
   * @ngInject for Dependency injection
   */
  constructor($httpBackend, codenvyAPIBuilder) {
    super($httpBackend, codenvyAPIBuilder);
  }

}

