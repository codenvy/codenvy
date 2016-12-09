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

import com.codenvy.api.license.model.Constants;
import com.codenvy.api.license.server.jpa.JpaCodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
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

import static com.codenvy.api.license.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.model.Constants.License.PRODUCT_LICENSE;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Anatolii Bazko
 */
@Listeners(TckListener.class)
@Test(suiteName = "CodenvyLicenseDaoTck")
public class JpaCodenvyLicenseActionDaoTest {
    private CodenvyLicenseActionImpl codenvyLicenseActions[];

    @Inject
    private TckRepository<CodenvyLicenseActionImpl> codenvyLicenseRepository;
    @Inject
    private EventService                            eventService;
    @Inject
    private JpaCodenvyLicenseActionDao              codenvyLicenseDao;

    @BeforeMethod
    public void setUp() throws Exception {
        codenvyLicenseActions = new CodenvyLicenseActionImpl[] {new CodenvyLicenseActionImpl(Constants.License.FAIR_SOURCE_LICENSE,
                                                                                             ACCEPTED,
                                                                                             System.currentTimeMillis(),
                                                                                             null,
                                                                                             ImmutableMap.of("prop1", "value1")),
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

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowExceptionIfLicenseActionAlreadyExists() throws Exception {
        codenvyLicenseDao.store(new CodenvyLicenseActionImpl(Constants.License.FAIR_SOURCE_LICENSE,
                                                             ACCEPTED,
                                                             System.currentTimeMillis(),
                                                             null,
                                                             ImmutableMap.of()));
    }

    @Test
    public void shouldFindRecordByTypeAndAction() throws Exception {
        CodenvyLicenseActionImpl codenvyLicenseAction =
                codenvyLicenseDao.getByLicenseAndAction(Constants.License.FAIR_SOURCE_LICENSE, ACCEPTED);

        assertNotNull(codenvyLicenseAction);
        assertNotNull(codenvyLicenseAction.getAttributes().isEmpty());
        assertEquals(codenvyLicenseAction.getAttributes().get("prop1"), "value1");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfActionNotFound() throws Exception {
        codenvyLicenseDao.getByLicenseAndAction(Constants.License.FAIR_SOURCE_LICENSE, Constants.Action.EXPIRED);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldRemoveCodenvyLicenseAction() throws Exception {
        codenvyLicenseDao.remove(PRODUCT_LICENSE, ACCEPTED);

        codenvyLicenseDao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED);
    }

    @AfterMethod
    public void cleanUp() throws Exception {
        codenvyLicenseRepository.removeAll();
    }
}
