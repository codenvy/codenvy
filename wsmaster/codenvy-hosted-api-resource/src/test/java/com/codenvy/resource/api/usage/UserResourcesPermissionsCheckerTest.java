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
package com.codenvy.resource.api.usage;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
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

/**
 * Tests for {@link UserResourcesPermissionsChecker}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class UserResourcesPermissionsCheckerTest {
    @Mock
    private Subject subject;

    private UserResourcesPermissionsChecker permissionsChecker;

    @BeforeMethod
    public void setUp() throws Exception {
        permissionsChecker = new UserResourcesPermissionsChecker();
        EnvironmentContext.getCurrent().setSubject(subject);
    }

    @Test
    public void shouldNotThrowExceptionWhenCurrentUserIdEqualsToRequestedAccountId() throws Exception {
        //given
        when(subject.getUserId()).thenReturn("user123");

        //when
        permissionsChecker.checkResourcesVisibility("user123");
    }

    @Test(expectedExceptions = ForbiddenException.class,
          expectedExceptionsMessageRegExp = "User is not authorized to see resources information of requested account.")
    public void shouldThrowExceptionWhenCurrentUserIdDoesNotEqualToRequestedAccountId() throws Exception {
        //given
        when(subject.getUserId()).thenReturn("user321");

        //when
        permissionsChecker.checkResourcesVisibility("user123");
    }

    @Test
    public void shouldReturnPersonalAccountTypeOnGettingAccountType() throws Exception {
        //when
        final String accountType = permissionsChecker.getAccountType();

        //then
        assertEquals(accountType, UserImpl.PERSONAL_ACCOUNT);
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        EnvironmentContext.getCurrent().setSubject(null);
    }
}
