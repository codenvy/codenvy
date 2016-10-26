/*
 *  [2015] - [2016] Codenvy, S.A.
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
