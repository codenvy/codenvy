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

import org.eclipse.che.api.system.server.SystemManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link SystemRamLimitMessageSender}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class SystemRamLimitMessageSenderTest {

    @Mock
    private SystemRamInfoProvider systemRamInfoProvider;
    @Mock
    private SystemRamInfo systemRamInfo;
    @Mock
    private SystemManager systemManager;
    @InjectMocks
    private SystemRamLimitMessageSender systemRamLimitMessageSender;

    private Field systemRamLimitExceedMessageSent;

    @BeforeMethod
    public void setup() throws Exception{
        systemRamLimitExceedMessageSent = systemRamLimitMessageSender.getClass().getDeclaredField("systemRamLimitExceedMessageSent");
        systemRamLimitExceedMessageSent.setAccessible(true);

        when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(systemRamInfo);
    }

    @Test
    public void shouldSetSystemRamLimitExceedMessageSentFlagToFalseWhenRamLimitNotExceedMessageSent() throws Exception{
        //given
        systemRamLimitExceedMessageSent.set(systemRamLimitMessageSender, true);
        when(systemRamInfo.isSystemRamLimitExceeded()).thenReturn(false);

        //when
        systemRamLimitMessageSender.checkRamLimitAndSendMessageIfNeeded();

        //then
        assertFalse((boolean)systemRamLimitExceedMessageSent.get(systemRamLimitMessageSender));
    }

    @Test
    public void shouldSetSystemRamLimitExceedMessageSentFlagToTrueWhenRamLimitExceedMessageSent() throws Exception{
        //given
        systemRamLimitExceedMessageSent.set(systemRamLimitMessageSender, false);
        when(systemRamInfo.isSystemRamLimitExceeded()).thenReturn(true);

        //when
        systemRamLimitMessageSender.checkRamLimitAndSendMessageIfNeeded();

        //then
        assertTrue((boolean)systemRamLimitExceedMessageSent.get(systemRamLimitMessageSender));
    }
}
