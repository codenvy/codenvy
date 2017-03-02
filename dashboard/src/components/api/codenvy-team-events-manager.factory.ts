/*
 *  [2015] - [2017] Codenvy, S.A.
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
import {CodenvyUser} from './codenvy-user.factory';

enum TEAM_EVENTS {MEMBER_ADDED, MEMBER_REMOVED, ORGANIZATION_REMOVED, ORGANIZATION_RENAMED}


/**
 * This class is handling the notifications per each team.
 *
 * @author Ann Shumilova
 */
export class CodenvyTeamEventsManager {
  codenvyUser: CodenvyUser;
  $log: ng.ILogService;
  cheWebsocket: any;
  applicationNotifications: any;
  TEAM_CHANNEL: string = 'organization:';
  TEAM_MEMBER_CHANNEL: string = 'organization:member:';
  subscribers: Array<string>;
  renameHandlers: Array<Function>;
  newTeamHandlers: Array<Function>;
  deleteHandlers: Array<Function>;

  /**
   * Default constructor that is using resource
   * @ngInject for Dependency injection
   */
  constructor(cheWebsocket: any, applicationNotifications: any, $log: ng.ILogService, codenvyUser: CodenvyUser) {
    this.codenvyUser = codenvyUser;
    this.cheWebsocket = cheWebsocket;
    this.applicationNotifications = applicationNotifications;
    this.$log = $log;
    this.subscribers = [];
    this.renameHandlers = [];
    this.deleteHandlers = [];
    this.newTeamHandlers = [];
    this.fetchUser();
  }

  /**
   * Subscribe team changing events.
   *
   * @param teamId team id to subscribe on events
   */
  subscribeTeamNotifications(teamId: string) {
    if (this.subscribers.indexOf(teamId) >= 0) {
      return;
    }
    this.subscribers.push(teamId);
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.TEAM_CHANNEL + teamId, (message: any) => {
      switch (TEAM_EVENTS[message.type]) {
        case TEAM_EVENTS.ORGANIZATION_RENAMED:
          this.processRenameTeam(message);
          break;
        case TEAM_EVENTS.ORGANIZATION_REMOVED:
          this.processDeleteTeam(message);
          break;
        default:
          break;
      }
    });
  }

  fetchUser(): void {
    this.codenvyUser.fetchUser().then(() => {
      this.subscribeTeamMemberNotifications();
    }, (error: any) => {
      if (error.status === 304) {
        this.subscribeTeamMemberNotifications();
      }
    });
  }

  /**
   * Subscribe team member changing events.
   */
  subscribeTeamMemberNotifications(): void {
    let id = this.codenvyUser.getUser().id;
    let bus = this.cheWebsocket.getBus();
    bus.subscribe(this.TEAM_MEMBER_CHANNEL + id, (message: any) => {
      switch (TEAM_EVENTS[message.type]) {
        case TEAM_EVENTS.MEMBER_ADDED:
          this.processAddedToTeam(message);
          break;
        case TEAM_EVENTS.MEMBER_REMOVED:
          this.processDeleteMember(message);
          break;
        default:
          break;
      }
    });
  }

  /**
   * Unsubscribe team changing events.
   *
   * @param teamId
   */
  unSubscribeTeamNotifications(teamId: string) {
    let bus = this.cheWebsocket.getBus();
    bus.unsubscribe(this.TEAM_CHANNEL + teamId);
  }

  /**
   * Adds rename handler.
   *
   * @param handler rename handler function
   */
  addRenameHandler(handler: Function): void {
    this.renameHandlers.push(handler);
  }

  /**
   * Removes rename handler.
   *
   * @param handler handler to remove
   */
  removeRenameHandler(handler: Function): void {
    this.renameHandlers.splice(this.renameHandlers.indexOf(handler), 1);
  }

  /**
   * Adds delete handler.
   *
   * @param handler delete handler function
   */
  addDeleteHandler(handler: Function): void {
    this.deleteHandlers.push(handler);
  }

  /**
   * Removes delete handler.
   *
   * @param handler delete handler to remove
   */
  removeDeleteHandler(handler: Function): void {
    this.deleteHandlers.splice(this.deleteHandlers.indexOf(handler), 1);
  }

  /**
   * Adds new team handler.
   *
   * @param handler new team handler function
   */
  addNewTeamHandler(handler: Function): void {
    this.newTeamHandlers.push(handler);
  }

  /**
   * Process team renamed event.
   *
   * @param info
   */
  processRenameTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      //TODO
    } else {
      let title = 'Team renamed';
      let content = 'Team \"' + info.oldName + '\" has been renamed to \"' + info.newName + '\" by ' + info.initiator;
      this.applicationNotifications.addInfoNotification(title, content);

      this.renameHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process team renamed event.
   *
   * @param info
   */
  processAddedToTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      //TODO
    } else {
      let title = 'You were added to team';
      let content = info.initiator + ' added you to team called \"' + info.organization.qualifiedName +'\".'
      this.applicationNotifications.addInfoNotification(title, content);

      this.newTeamHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process team deleted event.
   *
   * @param info
   */
  processDeleteTeam(info: any): void {
    let isCurrentUser = this.isCurrentUser(info.initiator);
    if (isCurrentUser) {
      //TODO
    } else {
      let title = 'Team deleted';
      let content = 'Team \"' + info.organization.qualifiedName + '\" has been deleted by ' + info.initiator;
      this.applicationNotifications.addInfoNotification(title, content);

      this.unSubscribeTeamNotifications(info.organization.id);

      this.deleteHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Process member deleted event.
   *
   * @param info
   */
  processDeleteMember(info: any): void {
    let isCurrentUserInitiator = this.isCurrentUser(info.initiator);
    if (isCurrentUserInitiator) {
      //TODO
    } else {
      let title = 'You have been removed from team';
      let content = info.initiator + ' removed you from team called \"' + info.organization.qualifiedName +'\".';
      this.applicationNotifications.addInfoNotification(title, content);

      this.deleteHandlers.forEach((handler: Function) => {
        handler(info);
      });
    }
  }

  /**
   * Checks current user is the performer of the action, that causes team changes.
   *
   * @param name
   * @returns {boolean}
   */
  isCurrentUser(name: string): boolean {
    return name === this.codenvyUser.getUser().name;
  }
}
