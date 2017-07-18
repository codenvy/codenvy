/*
 * Copyright (c) [2015] - [2017] Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
'use strict';

/**
 * This class is handling the system RAM API.
 * @author Ann Shumilova
 */
export class CodenvySystem {

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor($resource, cheWebsocket, applicationNotifications, $log) {
    this.cheWebsocket = cheWebsocket;
    this.applicationNotifications = applicationNotifications;
    this.$log = $log;

    this.SYSTEM_RAM_CHANNEL = 'system_ram_channel';

    this.LOW_RAM_TITLE = 'Low RAM';
    this.LOW_RAM_MESSAGE = 'The system is low on RAM. New workspaces will not start until more RAM is available.'

    // remote call
    this.systemAPI = $resource('/api/system/ram', {}, {
      getLimit: {method: 'GET', url: '/api/system/ram/limit'}
    });

    this.listenToSystemRamEvent();

    this.getSystemRAMLimit();
  }

  /**
   * Gets system RAM limit info.
   */
  getSystemRAMLimit() {
    let promise = this.systemAPI.getLimit().$promise;
    promise.then((info) => {
      if (info.systemRamLimitExceeded) {
        this.notification = this.applicationNotifications.addErrorNotification(this.LOW_RAM_TITLE, this.LOW_RAM_MESSAGE);
      }
    }, (error) => {
      this.$log.error('Failed to get system RAM limit: ', error);
    });
  }

  /**
   * Listen to system RAM limit channel.
   */
  listenToSystemRamEvent() {
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.SYSTEM_RAM_CHANNEL, (message) => {
      if (message.systemRamLimitExceeded) {
        this.notification = this.applicationNotifications.addErrorNotification(this.LOW_RAM_TITLE, this.LOW_RAM_MESSAGE);
      } else {
        if (this.notification) {
          this.applicationNotifications.removeNotification(this.notification);
        }
      }
    });
  }

}
