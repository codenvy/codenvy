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
package com.codenvy.api.dao.ldap;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

@Listeners(value = {MockitoTestNGListener.class})
public class UserDaoTest extends BaseTest {

    @Mock
    UserProfileDao profileDao;
    @Mock
    PreferenceDao  preferenceDao;

    UserDaoImpl               userDao;
    InitialLdapContextFactory factory;
    UserAttributesMapper      mapper;
    User[]                    users;

    @BeforeMethod
    public void setUp() throws Exception {
        factory = spy(new InitialLdapContextFactory(embeddedLdapServer.getUrl(),
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null,
                                                    null));
        mapper = spy(new UserAttributesMapper());
        userDao = new UserDaoImpl(profileDao,
                                  preferenceDao,
                                  factory,
                                  "dc=codenvy;dc=com",
                                  "uid",
                                  "cn",
                                  mapper,
                                  new EventService());

        users = new User[]{
                new User().withId("1")
                          .withEmail("user1@mail.com")
                          .withName("user1")
                          .withPassword("secret")
                          .withAliases(singletonList("user1@mail.com")),
                new User().withId("2")
                          .withName("user2")
                          .withEmail("user2@mail.com")
                          .withPassword("secret")
                          .withAliases(singletonList("user2@mail.com")),
                new User().withId("3")
                          .withName("user3")
                          .withEmail("user3@mail.com")
                          .withPassword("secret")
                          .withAliases(singletonList("user3@mail.com"))
        };
        for (User user : users) {
            userDao.create(user);
        }
    }

    @Test
    public void shouldAuthenticateUserUsingAliases() throws Exception {
        assertEquals(userDao.authenticate(users[0].getAliases().get(0), users[0].getPassword()), users[0].getId());
    }

    @Test
    public void shouldAuthenticateUserUsingName() throws Exception {
        assertEquals(userDao.authenticate(users[0].getName(), users[0].getPassword()), users[0].getId());
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldNotAuthenticateUserWithWrongPassword() throws Exception {
        userDao.authenticate(users[0].getName(), "invalid");
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldNotAuthenticateUserWithEmptyAlias() throws Exception {
        userDao.authenticate("", "valid");
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldNotAuthenticateUserWithEmptyPassword() throws Exception {
        userDao.authenticate(users[0].getName(), "");
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldNotAuthenticateUserWithNullAlias() throws Exception {
        userDao.authenticate(null, "valid");
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldNotAuthenticateUserWithNullPassword() throws Exception {
        userDao.authenticate(users[0].getName(), null);
    }

    @Test(expectedExceptions = UnauthorizedException.class)
    public void shouldThrowNotFoundExceptionWhenAuthenticatingNotExistingUser() throws Exception {
        userDao.authenticate("not_found", "secret");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenAuthenticatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException("message"));

        userDao.authenticate(users[0].getName(), users[0].getPassword());
    }

    @Test
    public void shouldCreateUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123_name")
                                       .withEmail("user123@mail.com")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getById("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getName());
        assertEquals(result.getEmail(), newUser.getEmail());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test
    public void shouldUseUserNameAsEmailAndAliasWhenCreatingUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123_name")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getById("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getName());
        assertEquals(result.getEmail(), newUser.getName());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test
    public void shouldUseUserIdWhenUserNameIsNullWhenCreatingUser() throws Exception {
        final User newUser = new User().withId("user123")
                                       .withName("user123")
                                       .withPassword("password");

        userDao.create(newUser);

        final User result = userDao.getByAlias("user123");
        assertEquals(result.getId(), newUser.getId());
        assertEquals(result.getName(), newUser.getId());
        assertEquals(result.getEmail(), newUser.getEmail());
        assertEquals(result.getAliases(), singletonList(newUser.getEmail()));
        assertNull(result.getPassword());
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable create new user .*. User already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedId() throws Exception {
        userDao.create(doClone(users[0]).withEmail("example@mail.com")
                                        .withName("new_name")
                                        .withAliases(singletonList("example@mail.com"))
                                        .withPassword("new password"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with alias .* already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedAlias() throws Exception {
        final User copy = doClone(users[0]).withName("new_name")
                                           .withId("new_id")
                                           .withEmail("example@mail.com")
                                           .withPassword("new_secret");
        copy.getAliases().add("example@mail.com");

        userDao.create(copy);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with name .* already exists")
    public void shouldThrowConflictExceptionWhenCreatingUserWithReservedName() throws Exception {
        userDao.create(doClone(users[0]).withId("new_id")
                                        .withEmail("example@mail.com")
                                        .withAliases(singletonList("example@mail.com"))
                                        .withPassword("new password"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable create new user .*. User already exists")
    public void shouldWrapAnyNameAlreadyBoundExceptionWithConflictExceptionWhenCreatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NameAlreadyBoundException());

        userDao.create(users[0]);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "User with email .* already exists")
    public void shouldThrowConflictExceptionWhenCreatingTwoUsersWithSameEmails() throws Exception {
        userDao.create(new User().withId("id_1")
                                 .withEmail("example@mail.com")
                                 .withPassword("password"));
        userDao.create(new User().withId("id_2")
                                 .withEmail("example@mail.com")
                                 .withPassword("password"));

    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenCreatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.create(users[0]);
    }

    @Test
    public void shouldUpdateUser() throws Exception {
        final User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setName("new_name");
        copy.setPassword("new_secret");
        copy.setAliases(singletonList("example@mail.com"));

        userDao.update(copy);

        final User updated = userDao.getById(copy.getId());
        assertEquals(updated.getId(), copy.getId());
        assertEquals(updated.getName(), copy.getName());
        assertEquals(updated.getEmail(), copy.getEmail());
        assertEquals(updated.getPassword(), null);
        assertEquals(updated.getAliases(), copy.getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUpdatingNotExistingUser() throws Exception {
        userDao.update(doClone(users[0]).withId("invalid")
                                        .withName("new-name")
                                        .withEmail("new_email@mail.com")
                                        .withPassword("new secret"));
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable update user .*, alias .* is already in use")
    public void shouldThrowConflictExceptionWhenUpdatingUserWithAliasWhichIsReserved() throws Exception {
        final User copy = doClone(users[0]);
        copy.setEmail("example@mail.com");
        copy.setPassword("new_secret");
        final String conflictAlias = users[1].getAliases().get(0);
        copy.getAliases().add("example@mail.com");
        copy.getAliases().add(conflictAlias);

        userDao.update(copy);
    }

    @Test(expectedExceptions = ConflictException.class,
          expectedExceptionsMessageRegExp = "Unable update user .*, name .* is already in use")
    public void shouldThrowConflictExceptionWhenUpdatingUserWithNameWhichIsReserved() throws Exception {
        userDao.update(doClone(users[0]).withEmail("new-email@mail.com")
                                        .withPassword("new secret")
                                        .withName(users[1].getName()));
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenUpdatingUser() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.update(users[0]);
    }

    @Test
    public void shouldBeAbleToGetUserByAlias() throws Exception {
        final User user = userDao.getByAlias(users[2].getAliases().get(0));

        assertEquals(user.getId(), users[2].getId());
        assertEquals(user.getEmail(), users[2].getEmail());
        assertEquals(user.getName(), users[2].getName());
        assertEquals(user.getPassword(), null);
        assertEquals(user.getAliases(), users[2].getAliases());
    }

    @Test
    public void shouldBeAbleToGetUserByName() throws Exception {
        final User user = userDao.getByName(users[2].getName());

        assertEquals(user.getId(), users[2].getId());
        assertEquals(user.getEmail(), users[2].getEmail());
        assertEquals(user.getName(), users[2].getName());
        assertEquals(user.getPassword(), null);
        assertEquals(user.getAliases(), users[2].getAliases());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUserWithGivenAliasDoesNotExist() throws Exception {
        userDao.getByAlias("invalid");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenGettingUserByAlias() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.getByAlias("valid");
    }

    @Test
    public void shouldBeAbleToGetUserById() throws Exception {
        final User user = userDao.getById(users[1].getId());

        assertEquals(user.getId(), users[1].getId());
        assertEquals(user.getEmail(), users[1].getEmail());
        assertEquals(user.getName(), users[1].getName());
        assertEquals(user.getPassword(), null);
        assertEquals(user.getAliases(), users[1].getAliases());
    }

    @Test
    public void shouldRenameEntityWhenItIsNotFoundWithNewDn() throws Exception {
        final Attributes attributes = mapper.toAttributes(new User().withId("user123")
                                                                    .withName("user123")
                                                                    .withPassword("password"));
        InitialLdapContext context = factory.createContext();
        context.createSubcontext("cn=user123,dc=codenvy;dc=com", attributes);

        userDao.getById("user123");

        try {
            context.getAttributes("cn=user123,dc=codenvy;dc=com");
            fail("Should rename entity");
        } catch (NameNotFoundException ignored) {
            //it okay
        }
        assertNotNull(context.getAttributes("uid=user123,dc=codenvy;dc=com"));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUserWithGivenIdDoesNotExist() throws Exception {
        userDao.getById("invalid");
    }

    @Test(expectedExceptions = ServerException.class)
    public void shouldWrapAnyNamingExceptionWithServerExceptionWhenGettingUserById() throws Exception {
        when(factory.createContext()).thenThrow(new NamingException());

        userDao.getById("valid");
    }

    @Test
    public void shouldRemoveUserAndAllDependentEntries() throws Exception {
        //given
        //prepare user
        final User testUser = users[0];

        //when
        userDao.remove(testUser.getId());

        //then
        try {
            userDao.getById(testUser.getId());
            fail("User was not removed");
        } catch (NotFoundException ignored) {
            //user was removed successfully
        }
        verify(profileDao).remove(testUser.getId());
        verify(preferenceDao).remove(testUser.getId());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenUserDoesNotExist() throws Exception {
        userDao.remove("invalid");
    }

    private User doClone(User other) {
        return new User().withId(other.getId())
                         .withName(other.getName())
                         .withEmail(other.getEmail())
                         .withPassword(other.getPassword())
                         .withAliases(new ArrayList<>(other.getAliases()));
    }
}
