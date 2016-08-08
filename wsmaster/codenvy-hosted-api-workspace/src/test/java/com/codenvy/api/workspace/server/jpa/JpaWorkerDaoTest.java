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
package com.codenvy.api.workspace.server.jpa;


import com.codenvy.api.workspace.server.model.impl.WorkerImpl;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.eclipse.che.api.workspace.server.event.BeforeWorkspaceRemovedEvent;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import java.util.Arrays;

import static org.testng.Assert.assertTrue;

/**
 * JPA-specific (non-TCK compliant) tests of {@link JpaWorkerDao}
 * @author Max Shaposhnik
 */
public class JpaWorkerDaoTest {

    private EntityManager manager;

    private JpaWorkerDao workerDao;

    WorkerImpl[] workers;

    @AfterMethod
    private void cleanup() {
        manager.getEntityManagerFactory().close();
    }

    @BeforeTest
    public void setUp() throws Exception {
        workers = new WorkerImpl[]{new WorkerImpl("ws1", "user1", Arrays.asList("read", "use", "run")),
                                   new WorkerImpl("ws1", "user2", Arrays.asList("read", "use")),
                                   new WorkerImpl("ws2", "user1", Arrays.asList("read", "run")),
                                   new WorkerImpl("ws2", "user2", Arrays.asList("read", "use", "run", "configure"))};

        Injector injector = Guice.createInjector(new WorkerTckModule());
        manager = injector.getInstance(EntityManager.class);
        workerDao  = injector.getInstance(JpaWorkerDao.class);

        manager.getTransaction().begin();
        for (WorkerImpl worker : workers) {
            manager.persist(worker);
        }
        manager.getTransaction().commit();
        manager.clear();
    }

    @Test
    public void shouldRemoveWorkersWhenWorkspaceIsRemoved() throws Exception {
        WorkspaceConfigImpl wsConfig = new WorkspaceConfigImpl();
        wsConfig.setName("ws1");
        BeforeWorkspaceRemovedEvent event =  new BeforeWorkspaceRemovedEvent(new WorkspaceImpl("id1", "ns", wsConfig));
        new JpaWorkerDao.RemoveWorkersBeforeWorkspaceRemovedEventSubscriber().onEvent(event);
        assertTrue(workerDao.getWorkers("ws1").isEmpty());
    }

}
