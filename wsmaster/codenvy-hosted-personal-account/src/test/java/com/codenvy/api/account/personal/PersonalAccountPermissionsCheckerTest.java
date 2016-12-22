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
package com.codenvy.api.account.personal;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(MockitoTestNGListener.class)
public class PersonalAccountPermissionsCheckerTest {
    private static String userId = "userok";
    @Mock
    private Subject subject;

    private PersonalAccountPermissionsChecker permissionsChecker;

    @BeforeMethod
    public void setUp() {
        when(subject.getUserId()).thenReturn(userId);
        EnvironmentContext.getCurrent().setSubject(subject);

        permissionsChecker = new PersonalAccountPermissionsChecker();
    }

    @AfterMethod
    public void cleanUp() {
        EnvironmentContext.getCurrent().setSubject(null);
    }

    @Test
    public void shouldNotThrowExceptionWhenUserIdFromSubjectEqualsToSpecifiedAccountId() throws Exception {
        permissionsChecker.checkPermissions(userId, null);
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to use specified account")
    public void shouldThrowForbiddenExceptionWhenUserIdFromSubjectDoesNotEqualToSpecifiedAccountId() throws Exception {
        permissionsChecker.checkPermissions("anotherUserId", null);
    }

    @Test
    public void shouldReturnPersonalAccountType() throws Exception {
        //when
        final String accountType = permissionsChecker.getAccountType();

        //then
        assertEquals(accountType, OnpremisesUserManager.PERSONAL_ACCOUNT);
    }
}
