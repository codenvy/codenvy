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
export class CodenvyHttpBackendProviderFactory {

  /**
   * Build a new Codenvy backend based on the given http backend.
   * @param $httpBackend the backend on which to add calls
   * @returns {CodenvyHttpBackend} the new instance
   */
  buildBackend($httpBackend, codenvyAPIBuilder) {

    // first, add pass through
    $httpBackend.whenGET(new RegExp('components.*')).passThrough();
    $httpBackend.whenGET(new RegExp('^app.*')).passThrough();


    // return instance
    return new CodenvyHttpBackend($httpBackend, codenvyAPIBuilder);
  }


}

