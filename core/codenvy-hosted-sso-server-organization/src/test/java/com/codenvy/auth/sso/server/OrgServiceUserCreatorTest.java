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
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    public void shouldCreateUser() throws Exception {
        doThrow(NotFoundException.class).when(manager).getByAlias(anyObject());

        new OrgServiceUserCreator(manager, profileDao, preferenceDao, true).createUser("user@codenvy.com", "test", "John", "Doe");

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

        new OrgServiceUserCreator(manager, profileDao, preferenceDao, true).createUser("user@codenvy.com", "reserved", "John", "Doe");

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(manager, times(2)).create(user.capture(), eq(false));
        assertTrue(user.getValue().getName().startsWith("reserved"));
        assertFalse(user.getValue().getName().equals("reserved"));
    }

}
