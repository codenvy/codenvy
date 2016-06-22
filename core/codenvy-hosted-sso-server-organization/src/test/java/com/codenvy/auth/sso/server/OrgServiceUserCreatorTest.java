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
package com.codenvy.auth.sso.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;


/**
 * @author Mihail Kuznyetsov
 */
@Listeners(MockitoTestNGListener.class)
public class OrgServiceUserCreatorTest {
    @Mock
    UserManager manager;

    @Mock
    UserProfileDao profileDao;

    @Mock
    PreferenceDao preferenceDao;

    @Mock
    User createdUser;

    OrgServiceUserCreator creator;

    @BeforeMethod
    public void setUp() throws Exception {
        creator = new OrgServiceUserCreator(manager, profileDao, preferenceDao, true);
        when(createdUser.getId()).thenReturn("id123");
        doReturn(createdUser).when(manager).getByName(anyString());
    }

    @Test
    public void shouldCreateUser() throws Exception {
        doThrow(NotFoundException.class).when(manager).getByAlias(anyObject());

        creator.createUser("user@codenvy.com", "test", "John", "Doe");

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(manager).create(user.capture(), eq(false));
        assertTrue(user.getValue().getName().equals("test"));
    }

    @Test
    public void shouldCreateUserWithGeneratedNameOnConflict() throws Exception {
        doThrow(NotFoundException.class).when(manager).getByAlias(anyObject());
        doAnswer(invocation -> {
            for (Object arg : invocation.getArguments()) {
                if (arg instanceof User && ((User)arg).getName().equals("reserved")) {
                    throw new ConflictException("User name is reserved");
                }
            }
            return null;
        }).when(manager).create(anyObject(), anyBoolean());

        creator.createUser("user@codenvy.com", "reserved", "John", "Doe");

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(manager, times(2)).create(user.capture(), eq(false));
        assertTrue(user.getValue().getName().startsWith("reserved"));
        assertFalse(user.getValue().getName().equals("reserved"));
    }

}
