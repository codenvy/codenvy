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


/**
 * This class is providing a builder for Team
 * @author Oleksii Kurinnyi
 */
export class CodenvyTeamBuilder {
  private team: any;

  /**
   * Default constructor.
   */
  constructor() {
    this.team = {};
  }


  /**
   * Sets the name of the team
   * @param name the name to use
   * @returns {CodenvyTeamBuilder}
   */
  withName(name) {
    this.team.name = name;
    return this;
  }

  /**
   * Sets the id of the team
   * @param id the id to use
   * @returns {CodenvyTeamBuilder}
   */
  withId(id) {
    this.team.id = id;
    return this;
  }

  /**
   * Build the team
   * @return {CodenvyTeamBuilder}
   */
  build() {
    return this.team;
  }

}
