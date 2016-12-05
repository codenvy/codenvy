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
package com.codenvy.api.license.server.spi.tck;

import com.codenvy.api.license.server.jpa.JpaCodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.api.license.shared.model.Constants;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.License.PRODUCT_LICENSE;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatolii Bazko
 */
@Listeners(TckListener.class)
@Test(suiteName = "CodenvyLicenseActionDaoTck")
public class JpaCodenvyLicenseActionDaoTest {
    private CodenvyLicenseActionImpl codenvyLicenseActions[];

    @Inject
    private TckRepository<CodenvyLicenseActionImpl> codenvyLicenseRepository;
    @Inject
    private EventService                            eventService;
    @Inject
    private JpaCodenvyLicenseActionDao              dao;

    @BeforeMethod
    public void setUp() throws Exception {
        codenvyLicenseActions = new CodenvyLicenseActionImpl[] {new CodenvyLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                                                             ACCEPTED,
                                                                                             System.currentTimeMillis(),
                                                                                             null,
                                                                                             ImmutableMap.of("prop1", "value1",
                                                                                                             "prop2", "value2",
                                                                                                             "prop3", "value2")),
                                                                new CodenvyLicenseActionImpl(PRODUCT_LICENSE,
                                                                                             ACCEPTED,
                                                                                             System.currentTimeMillis(),
                                                                                             "licenseQualifier1",
                                                                                             ImmutableMap.of()),
                                                                new CodenvyLicenseActionImpl(PRODUCT_LICENSE,
                                                                                             Constants.Action.EXPIRED,
                                                                                             System.currentTimeMillis(),
                                                                                             "licenseQualifier1",
                                                                                             ImmutableMap.of())};
        codenvyLicenseRepository.createAll(asList(codenvyLicenseActions));
    }


    @AfterMethod
    public void cleanUp() throws Exception {
        codenvyLicenseRepository.removeAll();
    }

    @Test
    public void shouldInsertNewRecord() throws Exception {
        dao.insert(new CodenvyLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                EXPIRED,
                                                System.currentTimeMillis(),
                                                null,
                                                ImmutableMap.of("prop1", "value1")));

        CodenvyLicenseActionImpl action = dao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, EXPIRED);
        assertNotNull(action);
    }


    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionOnInsertIfRecordExists() throws Exception {
        dao.insert(new CodenvyLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                ACCEPTED,
                                                System.currentTimeMillis(),
                                                null,
                                                ImmutableMap.of()));
    }

    @Test
    public void shouldFindRecordByLicenseAndAction() throws Exception {
        CodenvyLicenseActionImpl codenvyLicenseAction = dao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);

        assertNotNull(codenvyLicenseAction);
        assertEquals(codenvyLicenseAction.getAttributes().size(), 3);
        assertEquals(codenvyLicenseAction.getAttributes().get("prop1"), "value1");
        assertEquals(codenvyLicenseAction.getAttributes().get("prop2"), "value2");
        assertEquals(codenvyLicenseAction.getAttributes().get("prop3"), "value2");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfRecordAbsent() throws Exception {
        dao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, Constants.Action.EXPIRED);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldRemoveRecord() throws Exception {
        assertNotNull(dao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED));

        dao.remove(PRODUCT_LICENSE, ACCEPTED);

        dao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED);
    }

    @Test
    public void shouldNotThrowExceptionOnRemoveIfRecordAbsent() throws Exception {
        dao.remove(PRODUCT_LICENSE, ACCEPTED);
        dao.remove(PRODUCT_LICENSE, ACCEPTED);
    }

    @Test
    public void shouldUpdaterRecord() throws Exception {
        CodenvyLicenseActionImpl action = dao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
        assertEquals(action.getAttributes().get("prop1"), "value1");

        dao.upsert(new CodenvyLicenseActionImpl(FAIR_SOURCE_LICENSE,
                                                ACCEPTED,
                                                System.currentTimeMillis(),
                                                null,
                                                ImmutableMap.of("prop2", "value2")));

        action = dao.getByLicenseAndAction(FAIR_SOURCE_LICENSE, ACCEPTED);
        assertEquals(action.getAttributes().size(), 1);
        assertEquals(action.getAttributes().get("prop2"), "value2");
    }
}
