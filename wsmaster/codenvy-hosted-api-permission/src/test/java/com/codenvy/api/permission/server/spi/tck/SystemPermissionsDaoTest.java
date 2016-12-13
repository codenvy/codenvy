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
package com.codenvy.api.permission.server.spi.tck;

import com.codenvy.api.permission.server.AbstractPermissionsDomain;
import com.codenvy.api.permission.server.jpa.JpaSystemPermissionsDao;
import com.codenvy.api.permission.server.model.impl.SystemPermissionsImpl;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author Max Shaposhnik
 */
@Listeners(TckListener.class)
@Test(suiteName = "SystemPermissionsDaoTck")
public class SystemPermissionsDaoTest {

    @Inject
    private JpaSystemPermissionsDao dao;

    private UserImpl[] users;

    private SystemPermissionsImpl[] systemPermissions;

    @Inject
    private TckRepository<UserImpl>              userRepository;
    @Inject
    private TckRepository<SystemPermissionsImpl> systemRepository;

    @BeforeMethod
    public void setupEntities() throws Exception {
        systemPermissions = new SystemPermissionsImpl[] {new SystemPermissionsImpl("user1", asList("read", "use", "run")),
                                                         new SystemPermissionsImpl("user2", asList("read", "use")),
                                                         new SystemPermissionsImpl("user3", asList("read", "use"))
        };

        users = new UserImpl[] {new UserImpl("user1", "user1@com.com", "usr1"),
                                new UserImpl("user2", "user2@com.com", "usr2"),
                                new UserImpl("user3", "user3@com.com", "usr3")};


        userRepository.createAll(asList(users));
        systemRepository.createAll(Stream.of(systemPermissions)
                                         .map(SystemPermissionsImpl::new)
                                         .collect(Collectors.toList()));
    }

    @AfterMethod
    public void cleanup() throws Exception {
        systemRepository.removeAll();
        userRepository.removeAll();
    }

    @Test
    public void shouldReturnAllPermissionsWhenGetByInstance() throws Exception {
        final Page<SystemPermissionsImpl> permissionsPage = dao.getByInstance(null, 1, 1);
        final List<SystemPermissionsImpl> permissions = permissionsPage.getItems();

        assertEquals(permissionsPage.getTotalItemsCount(), 3);
        assertEquals(permissionsPage.getItemsCount(), 1);
        assertTrue(permissions.contains(systemPermissions[0])
                   ^ permissions.contains(systemPermissions[1])
                   ^ permissions.contains(systemPermissions[2]));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
        dao.get(null, "instance");
    }

    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {
        final SystemPermissionsImpl result1 = dao.get(systemPermissions[0].getUserId(), systemPermissions[0].getInstanceId());
        final SystemPermissionsImpl result2 = dao.get(systemPermissions[1].getUserId(), systemPermissions[1].getInstanceId());

        assertEquals(result1, systemPermissions[0]);
        assertEquals(result2, systemPermissions[1]);
    }

    public static class TestDomain extends AbstractPermissionsDomain<SystemPermissionsImpl> {
        public TestDomain() {
            super("system", asList("read", "write", "use"));
        }

        @Override
        protected SystemPermissionsImpl doCreateInstance(String userId, String instanceId, List allowedActions) {
            return null;
        }
    }
}
