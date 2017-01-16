/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.im.managers;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/** @author Dmytro Nochevnov */
public class TestPropertyNotFoundException {

    @Test
    public void testPropertyNotFoundException() {
        PropertyNotFoundException exception = PropertyNotFoundException.from("x");
        assertEquals(exception.toString(), "com.codenvy.im.managers.PropertyNotFoundException: Property 'x' not found");
    }
}
