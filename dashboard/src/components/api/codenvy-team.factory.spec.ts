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
 * Test of the Codenvy Team API
 */
describe('CodenvyTeam', () => {

  /**
   * User Factory for the test
   */
  let factory;

  /**
   * Codenvy User API
   */
  let userFactory;

  /**
   * che API builder.
   */
  let cheAPIBuilder;

  /**
   * codenvy API builder.
   */
  let codenvyAPIBuilder;

  /**
   * Backend for handling http operations
   */
  let httpBackend;

  /**
   * Codenvy backend
   */
  let codenvyBackend;

  /**
   *  setup module
   */
  beforeEach(angular.mock.module('codenvyDashboard'));

  /**
   * Inject factory and http backend
   */
  beforeEach(inject((codenvyTeam, cheUser, _cheAPIBuilder_, _codenvyAPIBuilder_, codenvyHttpBackend) => {
    factory = codenvyTeam;
    userFactory = cheUser;
    cheAPIBuilder = _cheAPIBuilder_;
    codenvyAPIBuilder = _codenvyAPIBuilder_;
    codenvyBackend = codenvyHttpBackend;
    httpBackend = codenvyHttpBackend.getHttpBackend();
  }));

  /**
   * Check assertion after the test
   */
  afterEach(() => {
    httpBackend.verifyNoOutstandingRequest();
    httpBackend.verifyNoOutstandingExpectation();
  });

  /**
   * Check than we are able to fetch team data
   */
  describe('Fetch team method', () => {
    let testUser, testTeam;

    beforeEach(() => {
      /* user setup */

      // setup tests objects
      let userId = 'idTestUser';
      let email = 'eclipseCodenvy@eclipse.org';

      testUser = cheAPIBuilder.getUserBuilder().withId(userId).withEmail(email).build();

      // providing request
      // add test user on Http backend
      codenvyBackend.setDefaultUser(testUser);

      // setup backend for users
      codenvyBackend.usersBackendSetup();

      /* team setup */

      // setup tests objects
      let teamId = 'idTestTeam';
      let teamName = 'testTeam';

      let testTeam = codenvyAPIBuilder.getTeamBuilder().withId(teamId).withName(teamName).build();

      // add test team on Http backend
      codenvyBackend.addTeamById(testTeam);

      // setup backend for teams
      codenvyBackend.teamsBackendSetup();

      /* fulfil all requests */
      httpBackend.flush();
    });

    it('should reject promise if user\'s request failed', () => {
      let errorMessage = 'user request failed',
          callbacks = {
            testResolve: () => { },
            testReject: (error: any) => {
              expect(error.data.message).toEqual(errorMessage);
            }
          };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      factory.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject)
        .finally();

      // change response to make request fail
      httpBackend.expect('GET', '/api/user').respond(404, {message: errorMessage});

      httpBackend.flush();

      expect(callbacks.testResolve).not.toHaveBeenCalled();
      expect(callbacks.testReject).toHaveBeenCalled();
    });

    it('should reject promise if team\'s request failed', () => {
      let errorMessage = 'teams request failed',
          callbacks = {
            testResolve: () => { },
            testReject: (error: any) => {
              expect(error.data.message).toEqual(errorMessage);
            }
          };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      // change response to make request fail
      httpBackend.expectGET('/api/organization').respond(404, {message: errorMessage});

      factory.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject)
        .finally();

      httpBackend.flush();

      expect(callbacks.testResolve).not.toHaveBeenCalled();
      expect(callbacks.testReject).toHaveBeenCalled();
    });

    it('should resolve promise', () => {
      let errorMessage = 'user request failed',
          callbacks = {
            testResolve: () => { },
            testReject: (error: any) => {
              expect(error.data.message).toEqual(errorMessage);
            }
          };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      factory.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject)
        .finally();

      httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve promise if user\'s request status code equals 304', () => {
      let errorMessage = 'user request failed',
          callbacks = {
            testResolve: () => { },
            testReject: (error: any) => {
              expect(error.data.message).toEqual(errorMessage);
            }
          };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      factory.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject)
        .finally();

      // change response
      httpBackend.expect('GET', '/api/user').respond(304, {});

      httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

    it('should resolve promise if team\'s request status code equals 304', () => {
      let errorMessage = 'teams request failed',
          callbacks = {
            testResolve: () => { },
            testReject: (error: any) => {
              expect(error.data.message).toEqual(errorMessage);
            }
          };

      // create spies
      spyOn(callbacks, 'testResolve');
      spyOn(callbacks, 'testReject');

      factory.fetchTeams()
        .then(callbacks.testResolve)
        .catch(callbacks.testReject)
        .finally();

      // change response
      httpBackend.expect('GET', '/api/organization').respond(304, {});

      httpBackend.flush();

      expect(callbacks.testResolve).toHaveBeenCalled();
      expect(callbacks.testReject).not.toHaveBeenCalled();
    });

  });

});
