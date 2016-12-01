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
package com.codenvy.api.license.server;

import com.codenvy.api.license.CodenvyLicense;
import com.codenvy.api.license.server.dao.CodenvyLicenseActionDao;
import com.codenvy.api.license.server.model.impl.CodenvyLicenseActionImpl;
import com.codenvy.api.license.server.model.impl.FairSourceLicenseAcceptanceImpl;
import com.codenvy.api.license.shared.model.Constants;
import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.License.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.License.PRODUCT_LICENSE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CodenvyLicenseActionHandlerTest {

    private static final String LICENSE_QUALIFIER = "id";

    @Mock
    private CodenvyLicenseActionDao     dao;
    @Mock
    private CodenvyLicenseManager       codenvyLicenseManager;
    @Mock
    private CodenvyLicense              codenvyLicense;
    @Mock
    private CodenvyLicenseActionImpl    codenvyLicenseAction;
    @InjectMocks
    private CodenvyLicenseActionHandler codenvyLicenseActionHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        when(codenvyLicense.getLicenseId()).thenReturn(LICENSE_QUALIFIER);
    }

    @Test
    public void shouldAddFairSourceLicenseAcceptedRecord() throws Exception {
        FairSourceLicenseAcceptanceImpl fairSourceLicenseAcceptance = new FairSourceLicenseAcceptanceImpl("fn", "ln", "em@codenvy.com");

        codenvyLicenseActionHandler.onCodenvyFairSourceLicenseAccepted(fairSourceLicenseAcceptance);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(dao).insert(actionCaptor.capture());
        CodenvyLicenseActionImpl value = actionCaptor.getValue();
        assertEquals(value.getLicenseType(), FAIR_SOURCE_LICENSE);
        assertEquals(value.getActionType(), ACCEPTED);
        assertEquals(value.getAttributes(), ImmutableMap.of("firstName", "fn", "lastName", "ln", "email", "em@codenvy.com"));
        assertNull(value.getLicenseQualifier());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionIfDaoThrowConflictException() throws Exception {
        FairSourceLicenseAcceptanceImpl fairSourceLicenseAcceptance = new FairSourceLicenseAcceptanceImpl("fn", "ln", "em@codenvy.com");
        doThrow(new ConflictException("conflict")).when(dao).insert(any(CodenvyLicenseActionImpl.class));

        codenvyLicenseActionHandler.onCodenvyFairSourceLicenseAccepted(fairSourceLicenseAcceptance);
    }

    @Test
    public void shouldAddProductLicenseExpiredRecord() throws Exception {
        codenvyLicenseActionHandler.onProductLicenseDeleted(codenvyLicense);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        CodenvyLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), EXPIRED);
        assertEquals(expireAction.getLicenseQualifier(), LICENSE_QUALIFIER);
    }

    @Test
    public void ifProductLicenseStoredShouldAddRecord() throws Exception {
        when(dao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED)).thenThrow(new NotFoundException("Not found"));

        codenvyLicenseActionHandler.onProductLicenseStored(codenvyLicense);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        CodenvyLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), ACCEPTED);
        assertEquals(expireAction.getLicenseQualifier(), LICENSE_QUALIFIER);
    }

    @Test
    public void ifSameProductLicenseStoredShouldNotAddRecord() throws Exception {
        when(codenvyLicenseAction.getLicenseQualifier()).thenReturn(LICENSE_QUALIFIER);
        when(dao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED)).thenReturn(codenvyLicenseAction);

        codenvyLicenseActionHandler.onProductLicenseStored(codenvyLicense);

        verify(dao, never()).upsert(any(CodenvyLicenseActionImpl.class));
        verify(dao, never()).insert(any(CodenvyLicenseActionImpl.class));
        verify(dao, never()).remove(any(Constants.License.class), any(Constants.Action.class));
    }

    @Test
    public void ifNewProductLicenseStoredShouldRemoveOldAndAddNewRecord() throws Exception {
        when(codenvyLicenseAction.getLicenseQualifier()).thenReturn("old qualifier");
        when(dao.getByLicenseAndAction(PRODUCT_LICENSE, ACCEPTED)).thenReturn(codenvyLicenseAction);

        codenvyLicenseActionHandler.onProductLicenseStored(codenvyLicense);

        verify(dao).remove(PRODUCT_LICENSE, EXPIRED);

        ArgumentCaptor<CodenvyLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(CodenvyLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        CodenvyLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), ACCEPTED);
        assertEquals(expireAction.getLicenseQualifier(), LICENSE_QUALIFIER);
    }
}
