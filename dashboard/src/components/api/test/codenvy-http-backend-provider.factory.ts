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

