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
package com.codenvy.api.workspace;

import com.codenvy.resource.api.usage.tracker.EnvironmentRamCalculator;

import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link EnvironmentRamCalculator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentRamCalculatorTest {
    private static final long MEGABYTES_TO_BYTES_MULTIPLIER = 1024L * 1024L;

    @Mock
    private EnvironmentParser environmentParser;
    @Mock
    private Environment       environment;

    private EnvironmentRamCalculator environmentRamCalculator;

    @BeforeMethod
    public void setUp() throws Exception {
        environmentRamCalculator = new EnvironmentRamCalculator(environmentParser,
                                                                2048);
    }

    @Test
    public void shouldCalculateRamOfEnvironmentWithMultipleMachines() throws Exception {
        Map<String, CheServiceImpl> services = new HashMap<>();
        services.put("service1", new CheServiceImpl().withMemLimit(1024 * MEGABYTES_TO_BYTES_MULTIPLIER));
        services.put("service2", new CheServiceImpl().withMemLimit(512 * MEGABYTES_TO_BYTES_MULTIPLIER));

        when(environmentParser.parse(anyObject())).thenReturn(new CheServicesEnvironmentImpl().withServices(services));

        long ram = environmentRamCalculator.calculate(environment);

        assertEquals(ram, 1536L);
    }

    @Test
    public void shouldUseDefaultMachineRamWhenCalculatingRamOfEnvironmentWithMultipleMachinesIncludingMachineWithoutLimit()
            throws Exception {
        Map<String, CheServiceImpl> services = new HashMap<>();
        services.put("service1", new CheServiceImpl().withMemLimit(null));

        when(environmentParser.parse(anyObject())).thenReturn(new CheServicesEnvironmentImpl().withServices(services));

        long ram = environmentRamCalculator.calculate(environment);

        assertEquals(ram, 2048L);
    }

    @Test
    public void shouldUseDefaultMachineRamWhenCalculatingRamOfEnvironmentIncludingMachineWithZeroLimit()
            throws Exception {
        Map<String, CheServiceImpl> services = new HashMap<>();
        services.put("service2", new CheServiceImpl().withMemLimit(0L));

        when(environmentParser.parse(anyObject())).thenReturn(new CheServicesEnvironmentImpl().withServices(services));

        long ram = environmentRamCalculator.calculate(environment);

        assertEquals(ram, 2048L);
    }
}
