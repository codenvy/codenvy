/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.im.update;

import org.eclipse.che.commons.subject.Subject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

public class UserManagerTest {

    public static final String ANY_NAME = "any_name";

    private UserManager spyUserManager;

    @BeforeMethod
    public void setup() {
        spyUserManager = spy(new UserManager());
    }

    @Test(dataProvider = "dataForTestIsAnonymous")
    public void testIsAnonymous(Subject currentSubject, boolean expected) throws Exception {
        when(spyUserManager.getCurrentUser()).thenReturn(currentSubject);
        assertEquals("Current user is " + currentSubject, spyUserManager.isAnonymous(), expected);
    }

    @DataProvider
    public Object[][] dataForTestIsAnonymous() {
        return new Object[][]{
            {null, true},

            {when(mock(Subject.class).getUserName())
                 .thenReturn(UserManager.ANONYMOUS_USER_NAME)
                 .getMock(),
             true},

            {when(mock(Subject.class).getUserName())
                 .thenReturn(ANY_NAME)
                 .getMock(),
             false},
        };
    }
}
