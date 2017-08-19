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
package com.codenvy.auth.sso.server.ticket;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.TicketManager;
import java.util.Timer;
import java.util.TimerTask;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task for removing expired tokens.
 *
 * @author Sergii Kabashniuk
 */
@Singleton
public class AccessTicketInvalidator extends TimerTask {

  private static final Logger LOG = LoggerFactory.getLogger(AccessTicketInvalidator.class);
  private final Timer timer;
  @Inject private TicketManager ticketManager;
  /** Period of time when access ticked keep valid */
  @Named("auth.sso.access_ticket_lifetime_seconds")
  @Inject
  private int ticketLifeTimeSeconds;

  public AccessTicketInvalidator() {
    this.timer = new Timer("sso-access-ticket-invalidator", true);
  }

  @Override
  public void run() {

    for (AccessTicket accessTicket : ticketManager.getAccessTickets()) {
      if (System.currentTimeMillis()
          > accessTicket.getCreationTime() + ticketLifeTimeSeconds * 1000) {
        LOG.info("Initiate user {} sso logout by timeout", accessTicket.getUserId());
        ticketManager.removeTicket(accessTicket.getAccessToken());
      }
    }
  }

  @PostConstruct
  public void startTimer() {
    //wait 1 second and run each minute
    timer.schedule(this, 1000, 60000);
  }

  @PreDestroy
  public void cancelTimer() {
    timer.cancel();
  }
}
