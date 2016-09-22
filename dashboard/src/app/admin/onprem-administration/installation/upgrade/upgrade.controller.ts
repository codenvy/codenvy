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

export class UpgradeInstallationCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($rootScope, imsSaasAuthApi, imsUpdateApi, imsArtifactApi) {
    this.imsUpdateApi = imsUpdateApi;
    this.$rootScope = $rootScope;
    this.fetchAll = false;
    this.$rootScope.$watch(
      () => imsSaasAuthApi.promise,
      (newValue) => { this.updateSubscriptionStatus(newValue); }
    );
    // by default, false, until login and subscription check
    this.subscriptionOk = false;

    this.upgradable = false;

    this.codenvyUpgradableArtifact = undefined;

    let promise = imsArtifactApi.artifacts();

    promise.then((artifacts) => {
      this.fetchAll = true;
      this.updateUpgradable(artifacts);
    }, () => {
      this.errorFetching = true;
      this.updateUpgradable(undefined);
    });
  }

  saveSchedule() {
    // Not implemented yet
  }

  updateSubscriptionStatus(value) {
    if (value) {
      this.subscriptionOk = true;
    } else {
      this.subscriptionOk = false;
    }
    this.$rootScope.$broadcast('chePanel:disabled', {id: 'upgrade-your-installation-panel', disabled: this.isSectionDisabled()});
  }

  isSectionDisabled() {
    return !this.subscriptionOk;
  }

  updateUpgradable(artifacts) {
    // only one artifact can be installed at a time
    // search if there is one with the correct status
    if (!artifacts) {
      return;
    }
    if (artifacts.codenvy && artifacts.codenvy.toInstall) {
      artifacts.codenvy.toInstall.forEach((artifact) => {
        if ('READY_TO_INSTALL' === artifact.status) {
          this.codenvyUpgradableArtifact = artifact;
        }
      });
    }
  }

  install() {
    this.imsUpdateApi.update();
  }
}

