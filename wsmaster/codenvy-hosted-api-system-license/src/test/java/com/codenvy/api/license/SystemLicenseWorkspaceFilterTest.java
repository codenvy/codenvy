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
package com.codenvy.api.license;

import com.codenvy.api.license.server.SystemLicenseManager;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/** Test related to @SystemLicenseWorkspaceFilter class. */
@Listeners(value = MockitoTestNGListener.class)
public class SystemLicenseWorkspaceFilterTest {

    @Mock
    SystemLicenseManager licenseManager;

    @Mock
    GenericResourceMethod genericResourceMethod;

    @InjectMocks
    SystemLicenseWorkspaceFilter filter;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = "testData")
    public void shouldNotThrowForbiddenException(String  workspaceServiceMethodName, Class[] workspaceServiceMethodParameters) throws
                                                                                                                               ServerException,
                                                                                                                               ForbiddenException,
                                                                                                                               NoSuchMethodException {
        //given
        doReturn(WorkspaceService.class.getMethod(workspaceServiceMethodName, workspaceServiceMethodParameters)).when(genericResourceMethod).getMethod();
        doReturn(true).when(licenseManager).canStartWorkspace();

        //when
        filter.filter(genericResourceMethod, null);

        //then
        verify(licenseManager, never()).getMessageForLicenseCompletelyExpired();
    }

    @Test(dataProvider = "testData", expectedExceptions = ForbiddenException.class, expectedExceptionsMessageRegExp = "License expired")
    public void shouldThrowForbiddenException(String  workspaceServiceMethodName, Class[] workspaceServiceMethodParameters) throws
                                                                                                                            ServerException,
                                                                                                                            ForbiddenException,
                                                                                                                            NoSuchMethodException {
        //given
        doReturn(WorkspaceService.class.getMethod(workspaceServiceMethodName, workspaceServiceMethodParameters)).when(genericResourceMethod).getMethod();

        doReturn(false).when(licenseManager).canStartWorkspace();
        doReturn("License expired").when(licenseManager).getMessageForLicenseCompletelyExpired();

        //when
        filter.filter(genericResourceMethod, null);
    }

    @Test
    public void shouldNotFilterSomeMethods() throws ServerException, ForbiddenException, NoSuchMethodException {
        //given
        doReturn(WorkspaceService.class.getMethod("create", WorkspaceConfigDto.class, List.class, Boolean.class, String.class))
            .when(genericResourceMethod).getMethod();

        //when
        filter.filter(genericResourceMethod, null);

        //then
        verifyZeroInteractions(licenseManager);
    }

    @DataProvider
    public Object[][] testData() throws NoSuchMethodException {
        return new Object[][] {
            { "startFromConfig", new Class[] {WorkspaceConfigDto.class, Boolean.class, String.class} },
            { "startById", new Class[] {String.class, String.class, Boolean.class} }
        };
    }

}
