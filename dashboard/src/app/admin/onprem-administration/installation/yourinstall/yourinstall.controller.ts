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

export class YourInstallationCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor(imsNodesApi, imsArtifactApi, $q) {
    this.customerName = '<Customer Name>';
    this.downloadedVersions = [];
    this.imsArtifactApi = imsArtifactApi;
    this.imsNodesApi = imsNodesApi;
    this.$q = $q;

    this.isLoading = false;
    this.initArtifacts();
  }

  initArtifacts() {
    this.isLoading = true;
    let first = this.imsNodesApi.listNodes().then((nodes) => { this.nodeList = nodes; });
    let second = this.imsArtifactApi.getInstalledArtifactsList().then(result => this.updateInstalledVersion(result));
    let third = this.imsArtifactApi.getDownloadedArtifactsList().then(result => this.updateDownloadedVersion(result));
    let allPromise = this.$q.all([first, second, third]);
    allPromise.then(() => {
        this.isLoading = false;
    }, () => {
        this.isLoading = false;
    });

  }

  updateInstalledVersion(resource) {
    if (resource) {
      for (let artifact of resource) {
        if (artifact.artifact === 'codenvy') {
          this.installedVersion = artifact.version;
          return;
        }
      }
    }
    this.installedVersion = undefined;
  }

  removeDownloadedVersion(downloadedVersion) {
    // delete
    let deletePromise = this.imsArtifactApi.deleteDownloadedArtifact('codenvy', downloadedVersion);

    // remove existing versions
    deletePromise.then(() => {this.imsArtifactApi.getDownloadedArtifactsList().then(result => this.updateDownloadedVersion(result));});

  }


  updateDownloadedVersion(resource) {
    this.downloadedVersions.length = 0;
    if (resource) {
      for (let artifact of resource) {
        if (artifact.artifact === 'codenvy' && artifact.status !== 'INSTALLED') {
          this.downloadedVersions.push(artifact.version);
        }
      }
      return;
    }
    // else, clear all values
    this.downloadedVersions.length = 0;
  }
}
