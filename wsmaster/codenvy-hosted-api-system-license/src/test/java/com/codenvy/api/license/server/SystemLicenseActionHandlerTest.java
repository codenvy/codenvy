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

import com.codenvy.api.license.SystemLicense;
import com.codenvy.api.license.server.dao.SystemLicenseActionDao;
import com.codenvy.api.license.server.model.impl.SystemLicenseActionImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;

import static com.codenvy.api.license.shared.model.Constants.Action.ACCEPTED;
import static com.codenvy.api.license.shared.model.Constants.Action.ADDED;
import static com.codenvy.api.license.shared.model.Constants.Action.EXPIRED;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.FAIR_SOURCE_LICENSE;
import static com.codenvy.api.license.shared.model.Constants.PaidLicense.PRODUCT_LICENSE;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class SystemLicenseActionHandlerTest {

    private static final String LICENSE_ID = "id";

    @Mock
    private SystemLicenseActionDao     dao;
    @Mock
    private SystemLicenseManager       systemLicenseManager;
    @Mock
    private SystemLicense              systemLicense;
    @Mock
    private SystemLicenseActionImpl    codenvyLicenseAction;
    @Mock
    private UserManager                userManager;
    @InjectMocks
    private SystemLicenseActionHandler systemLicenseActionHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        final EnvironmentContext ctx = new EnvironmentContext();
        ctx.setSubject(new SubjectImpl("test-user-name", "test-user-id", "test-token", false));
        EnvironmentContext.setCurrent(ctx);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@user");
        when(userManager.getById("test-user-id")).thenReturn(user);
        when(systemLicense.getLicenseId()).thenReturn(LICENSE_ID);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        EnvironmentContext.reset();
    }

    @Test
    public void shouldAddFairSourceLicenseAcceptedRecord() throws Exception {
        systemLicenseActionHandler.onCodenvyFairSourceLicenseAccepted();

        ArgumentCaptor<SystemLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(SystemLicenseActionImpl.class);
        verify(dao).insert(actionCaptor.capture());
        SystemLicenseActionImpl value = actionCaptor.getValue();
        assertEquals(value.getLicenseType(), FAIR_SOURCE_LICENSE);
        assertEquals(value.getActionType(), ACCEPTED);
        assertEquals(value.getAttributes(), Collections.emptyMap());
        assertNull(value.getLicenseId());
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionIfDaoThrowConflictException() throws Exception {
        doThrow(new ConflictException("conflict")).when(dao).insert(any(SystemLicenseActionImpl.class));

        systemLicenseActionHandler.onCodenvyFairSourceLicenseAccepted();
    }

    @Test
    public void shouldAddProductLicenseExpiredRecord() throws Exception {
        // given
        doThrow(NotFoundException.class).when(dao).getByLicenseIdAndAction(LICENSE_ID, EXPIRED);

        // when
        systemLicenseActionHandler.onProductLicenseDeleted(systemLicense);

        // then
        ArgumentCaptor<SystemLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(SystemLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        SystemLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), EXPIRED);
        assertEquals(expireAction.getLicenseId(), LICENSE_ID);
    }

    @Test
    public void shouldNotUpsertProductLicenseExpiredRecordForExistedRecord() throws Exception {
        // when
        systemLicenseActionHandler.onProductLicenseDeleted(systemLicense);

        // then
        verify(dao, never()).upsert(any());
    }

    @Test
    public void ifProductLicenseStoredShouldAddRecord() throws Exception {
        when(dao.getByLicenseTypeAndAction(PRODUCT_LICENSE, ADDED)).thenThrow(new NotFoundException("Not found"));

        systemLicenseActionHandler.onProductLicenseStored(systemLicense);

        ArgumentCaptor<SystemLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(SystemLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        SystemLicenseActionImpl acceptAction = actionCaptor.getValue();
        assertEquals(acceptAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(acceptAction.getActionType(), ADDED);
        assertEquals(acceptAction.getLicenseId(), LICENSE_ID);
        assertEquals(acceptAction.getAttributes().get("email"), "test@user");
    }

    @Test
    public void ifSameProductLicenseStoredShouldUpsertAddedLicenseRecord() throws Exception {
        when(codenvyLicenseAction.getLicenseId()).thenReturn(LICENSE_ID);
        when(dao.getByLicenseTypeAndAction(PRODUCT_LICENSE, ADDED)).thenReturn(codenvyLicenseAction);

        systemLicenseActionHandler.onProductLicenseStored(systemLicense);

        verify(dao).upsert(any(SystemLicenseActionImpl.class));
        verify(dao, never()).insert(any(SystemLicenseActionImpl.class));
    }

    @Test
    public void ifNewProductLicenseStoredShouldRemoveOldAndAddNewRecord() throws Exception {
        when(codenvyLicenseAction.getLicenseId()).thenReturn("old id");
        when(dao.getByLicenseTypeAndAction(PRODUCT_LICENSE, ADDED)).thenReturn(codenvyLicenseAction);

        systemLicenseActionHandler.onProductLicenseStored(systemLicense);

        ArgumentCaptor<SystemLicenseActionImpl> actionCaptor = ArgumentCaptor.forClass(SystemLicenseActionImpl.class);
        verify(dao).upsert(actionCaptor.capture());
        SystemLicenseActionImpl expireAction = actionCaptor.getValue();
        assertEquals(expireAction.getLicenseType(), PRODUCT_LICENSE);
        assertEquals(expireAction.getActionType(), ADDED);
        assertEquals(expireAction.getLicenseId(), LICENSE_ID);
    }
}
