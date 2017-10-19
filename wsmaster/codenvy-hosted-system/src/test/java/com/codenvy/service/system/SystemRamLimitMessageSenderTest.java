/*
 * Copyright (c) [2012] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package com.codenvy.service.system;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Field;
import org.eclipse.che.api.system.server.SystemManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link SystemRamLimitMessageSender}
 *
 * @author Igor Vinokur
 */
@Listeners(MockitoTestNGListener.class)
public class SystemRamLimitMessageSenderTest {

  @Mock private SystemRamInfoProvider systemRamInfoProvider;
  @Mock private SystemRamInfo systemRamInfo;
  @Mock private SystemManager systemManager;
  @InjectMocks private SystemRamLimitMessageSender systemRamLimitMessageSender;

  private Field systemRamLimitExceedMessageSent;

  @BeforeMethod
  public void setup() throws Exception {
    systemRamLimitExceedMessageSent =
        systemRamLimitMessageSender.getClass().getDeclaredField("systemRamLimitExceedMessageSent");
    systemRamLimitExceedMessageSent.setAccessible(true);

    when(systemRamInfoProvider.getSystemRamInfo()).thenReturn(systemRamInfo);
  }

  @Test
  public void shouldSetSystemRamLimitExceedMessageSentFlagToFalseWhenRamLimitNotExceedMessageSent()
      throws Exception {
    // given
    systemRamLimitExceedMessageSent.set(systemRamLimitMessageSender, true);
    when(systemRamInfo.isSystemRamLimitExceeded()).thenReturn(false);

    // when
    systemRamLimitMessageSender.checkRamLimitAndSendMessageIfNeeded();

    // then
    assertFalse((boolean) systemRamLimitExceedMessageSent.get(systemRamLimitMessageSender));
  }

  @Test
  public void shouldSetSystemRamLimitExceedMessageSentFlagToTrueWhenRamLimitExceedMessageSent()
      throws Exception {
    // given
    systemRamLimitExceedMessageSent.set(systemRamLimitMessageSender, false);
    when(systemRamInfo.isSystemRamLimitExceeded()).thenReturn(true);

    // when
    systemRamLimitMessageSender.checkRamLimitAndSendMessageIfNeeded();

    // then
    assertTrue((boolean) systemRamLimitExceedMessageSent.get(systemRamLimitMessageSender));
  }
}
