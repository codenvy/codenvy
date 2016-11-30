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
package com.codenvy.api.workspace;

import org.eclipse.che.api.environment.server.EnvironmentParser;
import org.eclipse.che.plugin.docker.compose.yaml.ComposeEnvironmentParser;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link EnvironmentRamCalculator}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentRamCalculatorTest {
    private EnvironmentRamCalculator environmentRamCalculator;

    @BeforeMethod
    public void setUp() throws Exception {
        environmentRamCalculator = new EnvironmentRamCalculator(new EnvironmentParser(singletonMap("compose",
                                                                                                   new ComposeEnvironmentParser(null))),
                                                                2048);
    }

    @Test
    public void shouldCalculateRamOfEnvironmentWithMultipleMachines() throws Exception {
        long ram = environmentRamCalculator.calculate(TestObjects.createEnvironment("1gb", "512mb"));

        assertEquals(ram, 1536L);
    }

    @Test
    public void shouldUseDefaultMachineRamWhenCalculatingRamOfEnvironmentWithMultipleMachinesIncludingMachineWithoutLimits()
            throws Exception {

        long ram = environmentRamCalculator.calculate(TestObjects.createEnvironment("1gb", "512mb", null));

        assertEquals(ram, 3584L);
    }
}
