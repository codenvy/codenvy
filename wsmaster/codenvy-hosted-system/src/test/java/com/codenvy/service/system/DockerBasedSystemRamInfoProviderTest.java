/*
 *  [2012] - [2017] Codenvy, S.A.
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
package com.codenvy.service.system;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorProvider;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link DockerBasedSystemRamInfoProvider}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class DockerBasedSystemRamInfoProviderTest {

    @Mock
    private SystemInfo              systemInfo;
    @Mock
    private DockerConnectorProvider dockerConnectorProvider;
    @Mock
    private DockerConnector         dockerConnector;

    private SystemRamInfoProvider systemRamInfoProvider;

    @BeforeMethod
    private void setup() throws Exception{
        when(dockerConnectorProvider.get()).thenReturn(dockerConnector);
        when(dockerConnector.getSystemInfo()).thenReturn(systemInfo);

        systemRamInfoProvider = new DockerBasedSystemRamInfoProvider(dockerConnectorProvider);
    }

    @Test
    public void shouldReturnRamNotExceededStatusWhenSystemRamLimitIsNotExceeded() throws Exception {
        when(systemInfo.getDriverStatus()).thenReturn(new String[][] {{" └ Reserved Memory", "0 B / 3 GiB"}});

        assertFalse(systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded());
    }

    @Test
    public void shouldReturnRamExceededStatusWhenSystemRamLimitIsExceeded() throws Exception {
        when(systemInfo.getDriverStatus()).thenReturn(new String[][]{{" └ Reserved Memory", "800 MiB / 1 GiB"},
                                                                     {" └ Reserved Memory", "0.99 GiB / 1 GiB"},
                                                                     {" └ Reserved Memory", "0.95 GiB / 1 GiB"}});

        assertTrue(systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded());
    }

    @Test
    public void shouldReturnRamValues() throws Exception {
        when(systemInfo.getDriverStatus()).thenReturn(new String[][] {{" └ Reserved Memory", "1 GiB / 3 GiB"}});

        SystemRamInfo systemRamInfo = systemRamInfoProvider.getSystemRamInfo();

        assertEquals(systemRamInfo.getSystemRamUsed(), 1024 * 1024 * 1024);
        assertEquals(systemRamInfo.getSystemRamTotal(), 3L * 1024 * 1024 * 1024 );
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "An error occurred while getting system RAM info.")
    public void shouldThrowExceptionIfFailedToRecognizeDockerSystemInfo() throws Exception {
        when(systemInfo.getDriverStatus()).thenReturn(new String[][] {{"Unrecognized value", "Unrecognized value"}});

        systemRamInfoProvider.getSystemRamInfo();
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "An error occurred while getting system RAM info.")
    public void shouldThrowExceptionIfFailedToRecognizeDockerSystemRamValues() throws Exception {
        when(systemInfo.getDriverStatus()).thenReturn(new String[][] {{" └ Reserved Memory", "Unrecognized value"}});

        systemRamInfoProvider.getSystemRamInfo();
    }

    @Test(expectedExceptions = ServerException.class,
          expectedExceptionsMessageRegExp = "An error occurred while getting system RAM info.")
    public void shouldThrowExceptionIfDockerSystemRamValuesArrayFromResponseIsNull() throws Exception {
        systemRamInfoProvider.getSystemRamInfo();
    }
}
