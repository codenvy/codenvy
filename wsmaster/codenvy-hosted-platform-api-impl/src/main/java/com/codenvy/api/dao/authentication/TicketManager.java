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
package com.codenvy.api.dao.authentication;

import java.util.Set;

/**
 * Manager to persist access tickets for SSO login process
 *
 * <p>
 *
 * @author Andrey Parfonov
 * @author Sergey Kabashniuk
 */
public interface TicketManager {

  /**
   * Add access ticket
   *
   * @param accessTicket ticket to add
   */
  void putAccessTicket(AccessTicket accessTicket);

  /**
   * Get access ticket from manager by its token
   *
   * @param accessToken unique token of access ticket
   * @return access ticket
   */
  AccessTicket getAccessTicket(String accessToken);

  /**
   * Remove access ticket from manager.
   *
   * @param accessToken unique token of ticket to remove
   * @return removed instance of <code>AccessTicket</code>
   */
  AccessTicket removeTicket(String accessToken);

  /**
   * Get all access tickets.
   *
   * @return set of access tickets
   */
  Set<AccessTicket> getAccessTickets();
}
