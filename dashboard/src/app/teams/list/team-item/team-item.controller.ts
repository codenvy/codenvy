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
import {CodenvyTeam} from '../../../../components/api/codenvy-team.factory';

/**
 * Controller for a team item.
 *
 * @author Ann Shumilova
 */
export class TeamItemController {
  /**
   * Service for displaying dialogs.
   */
  private confirmDialogService: any;
  /**
   * Location service.
   */
  private $location: ng.ILocationService;
  /**
   * Team API interaction.
   */
  private codenvyTeam: CodenvyTeam;
  /**
   * Service for displaying notifications.
   */
  private cheNotification: any;
  /**
   * Team details (the value is set in directive attributes).
   */
  private team: any;
  /**
   * Callback needed to react on teams updation (the value is set in directive attributes).
   */
  private onUpdate: Function;

  /**
   * Default constructor that is using resource injection
   * @ngInject for Dependency injection
   */
  constructor($location: ng.ILocationService, codenvyTeam: CodenvyTeam, confirmDialogService: any, cheNotification: any) {
    this.$location = $location;
    this.confirmDialogService = confirmDialogService;
    this.codenvyTeam = codenvyTeam;
    this.cheNotification = cheNotification;
  }

  /**
   * Redirect to team details.
   */
  redirectToTeamDetails(page: string) {
    this.$location.path('/team/' + this.team.qualifiedName + (page ? '/page/' + page : ''));
  }

  /**
   * Removes team after confirmation.
   */
  removeTeam(): void {
    this.confirmRemoval().then(() => {
      this.codenvyTeam.deleteTeam(this.team.id).then(() => {
        this.onUpdate();
      }, (error: any) => {
        this.cheNotification.showError(error && error.data && error.data.message ? error.data.message : 'Failed to delete team ' + this.team.name);
      });
    });
  }

  /**
   * Get team display name.
   *
   * @param team
   * @returns {string}
   */
  getTeamDisplayName(team): string {
    return this.codenvyTeam.getTeamDisplayName(team);
  }

  /**
   * Shows dialog to confirm the current team removal.
   *
   * @returns {angular.IPromise<any>}
   */
  confirmRemoval(): ng.IPromise<any> {
    let promise = this.confirmDialogService.showConfirmDialog('Delete team',
      'Would you like to delete team \'' + this.team.name + '\'?', 'Delete');
    return promise;
  }
}

