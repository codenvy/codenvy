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

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 8/23/16.
 */
@org.testng.annotations.Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = "SystemPermissionsDaoTck")
public class SystemPermissionsDaoTest {

    @Inject
    private JpaSystemPermissionsDao dao;

    UserImpl[] users;

    SystemPermissionsImpl[] systemPermissionses;

    @Inject
    private TckRepository<UserImpl>   userRepository;
    @Inject
    private TckRepository<SystemPermissionsImpl> systemRepository;

    @BeforeMethod
    public void setupEntities() throws Exception {
        systemPermissionses = new SystemPermissionsImpl[]{new SystemPermissionsImpl("user1", Arrays.asList("read", "use", "run")),
                                                          new SystemPermissionsImpl("user2", Arrays.asList("read", "use")),
        };

        users = new UserImpl[]{new UserImpl("user1", "user1@com.com", "usr1"),
                               new UserImpl("user2", "user2@com.com", "usr2")};


        userRepository.createAll(Arrays.asList(users));

        systemRepository.createAll(Arrays.asList(systemPermissionses));
    }



    @AfterMethod
    public void cleanup() throws Exception {
        systemRepository.removeAll();
        userRepository.removeAll();
    }



    @Test(expectedExceptions = ServerException.class)
    public void shouldThrowServerExceptionOnGetByInstance() throws Exception {
        dao.getByInstance("instanceq123");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowExceptionWhenGetPermissionsUserIdArgumentIsNull() throws Exception {
        dao.get(null, "instance");
    }

    @Test
    public void shouldBeAbleToGetPermissions() throws Exception {

        final SystemPermissionsImpl result1 = dao.get(systemPermissionses[0].getUserId(), systemPermissionses[0].getInstanceId());
        final SystemPermissionsImpl result2 = dao.get(systemPermissionses[1].getUserId(), systemPermissionses[1].getInstanceId());

        assertEquals(result1, systemPermissionses[0]);
        assertEquals(result2, systemPermissionses[1]);
    }


    public static class TestDomain extends AbstractPermissionsDomain<SystemPermissionsImpl> {
        public TestDomain() {
            super("system", Arrays.asList("read", "write", "use"));
        }

        @Override
        protected SystemPermissionsImpl doCreateInstance(String userId, String instanceId, List allowedActions) {
            return null;
        }
    }

}
