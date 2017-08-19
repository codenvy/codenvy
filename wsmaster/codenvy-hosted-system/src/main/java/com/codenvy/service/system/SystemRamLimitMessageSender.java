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

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.codenvy.service.system.dto.SystemRamLimitDto;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.io.IOException;
import javax.inject.Singleton;
import javax.websocket.EncodeException;
import org.eclipse.che.commons.schedule.ScheduleDelay;
import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends web-socket messages about system RAM limit status to all users.
 *
 * @author Igor Vinokur
 */
@Singleton
public class SystemRamLimitMessageSender {

  private static final Logger LOG = LoggerFactory.getLogger(SystemRamLimitMessageSender.class);

  private final SystemRamInfoProvider systemRamInfoProvider;

  private boolean systemRamLimitExceedMessageSent;

  @Inject
  public SystemRamLimitMessageSender(SystemRamInfoProvider systemRamInfoProvider) {
    this.systemRamInfoProvider = systemRamInfoProvider;
  }

  private void sendMessage(boolean isSystemRamLimitExceeded) {
    final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
    bm.setChannel("system_ram_channel");
    bm.setBody(
        newDto(SystemRamLimitDto.class)
            .withSystemRamLimitExceeded(isSystemRamLimitExceeded)
            .toString());
    try {
      WSConnectionContext.sendMessage(bm);
    } catch (EncodeException | IOException e) {
      LOG.error("An error occurred while sending web-socket message", e);
    }
  }

  @VisibleForTesting
  @ScheduleDelay(initialDelay = 60, delayParameterName = "system.ram.limit_check_period_sec")
  synchronized void checkRamLimitAndSendMessageIfNeeded() {
    try {
      boolean isSystemRamLimitExceeded =
          systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded();
      if (systemRamLimitExceedMessageSent != isSystemRamLimitExceeded) {
        sendMessage(isSystemRamLimitExceeded);
        systemRamLimitExceedMessageSent = isSystemRamLimitExceeded;
      }
    } catch (Exception exception) {
      LOG.error(exception.getMessage(), exception);
    }
  }
}
