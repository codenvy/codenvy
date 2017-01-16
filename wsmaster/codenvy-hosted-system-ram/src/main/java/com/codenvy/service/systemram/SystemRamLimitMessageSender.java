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
package com.codenvy.service.systemram;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;

import org.eclipse.che.commons.schedule.ScheduleDelay;
import com.codenvy.service.systemram.dto.SystemRamLimitDto;

import org.everrest.websockets.WSConnectionContext;
import org.everrest.websockets.message.ChannelBroadcastMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.websocket.EncodeException;
import java.io.IOException;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Sends web-socket messages about system RAM limit status to all users.
 *
 * @author Igor Vinokur
 */
@Singleton
public class SystemRamLimitMessageSender {

    private final static Logger LOG = LoggerFactory.getLogger(SystemRamLimitMessageSender.class);

    private final SystemRamInfoProvider systemRamInfoProvider;

    private boolean systemRamLimitExceedMessageSent;

    @Inject
    public SystemRamLimitMessageSender(SystemRamInfoProvider systemRamInfoProvider) {
        this.systemRamInfoProvider = systemRamInfoProvider;
    }

    private void sendMessage(boolean isSystemRamLimitExceeded) {
        final ChannelBroadcastMessage bm = new ChannelBroadcastMessage();
        bm.setChannel("system_ram_channel");
        bm.setBody(newDto(SystemRamLimitDto.class).withSystemRamLimitExceeded(isSystemRamLimitExceeded).toString());
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
            boolean isSystemRamLimitExceeded = systemRamInfoProvider.getSystemRamInfo().isSystemRamLimitExceeded();
            if (systemRamLimitExceedMessageSent != isSystemRamLimitExceeded) {
                sendMessage(isSystemRamLimitExceeded);
                systemRamLimitExceedMessageSent = isSystemRamLimitExceeded;
            }
        } catch (Exception exception) {
            LOG.error(exception.getMessage(), exception);
        }
    }
}
