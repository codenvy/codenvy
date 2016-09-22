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

export class OnPremisesAdminAvailableSoftwareCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor(imsArtifactApi, $interval) {
    'ngInject';

    this.imsArtifactApi = imsArtifactApi;
    this.$interval = $interval;
    let artifactsPromise = this.imsArtifactApi.artifacts();
    artifactsPromise.then(result => { console.log('got artifacts data:', result); this.artifacts = result; });
    artifactsPromise.catch(error => { console.log('artifacts list failed' , error); this.artifacts = undefined; });

    this.currentDownloadedArtifact = {
      inProgress: false,
      name: '',
      version: '',
      percent: 0
    };

    function internalFunction(object, id) {
      object.checkDownloadID(id);
    }

    // do we have a download in progress ?
    let promise = this.imsArtifactApi.getCurrentDownloadID();
    promise.then(() => {
      promise.then((data) => {
        this.promiseInterval = this.$interval(() => {internalFunction(this, data.id);}, 2000);
      });
    });
  }


  downloadArtifact(artifact, artifactAvailableVersion) {
    function internalFunction(object, id) {
      object.checkDownloadID(id);
    }

    let promise = this.imsArtifactApi.downloadArtifact(artifact.name, artifactAvailableVersion);
    promise.then((data) => {
      this.promiseInterval = this.$interval(() => {internalFunction(this, data.id);}, 2000);
    });

  }

  checkDownloadID(id) {
    let promise = this.imsArtifactApi.getInfoDownloadID(id);
    promise.then(data => {
      // check artifact being downloaded
      let artifact = data.artifacts[0];
      let artifactName = artifact.name;
      let artifactVersion = artifact.version;
      let percent = data.percents;
      let status = data.status;

      this.currentDownloadedArtifact.name = artifactName;
      this.currentDownloadedArtifact.version = artifactVersion;
      this.currentDownloadedArtifact.percent = percent;

      if (status === 'DOWNLOADED') {
        this.$interval.cancel(this.promiseInterval);
        this.currentDownloadedArtifact.inProgress = false;
        let artifactsPromise = this.imsArtifactApi.artifacts();
        // update artifacts
        artifactsPromise.then(result => {
          this.artifacts = result;
        });

      }

      this.currentDownloadedArtifact.inProgress = true;
    });

      promise.catch(() => {
      // remove any timeout
      if (this.promiseInterval) {
        this.$interval.cancel(this.promiseInterval);
        this.currentDownloadedArtifact.inProgress = false;
      }
    });
  }

  artifactDisplayName(artifactId) {
    return this.imsArtifactApi.getArtifactDisplayName(artifactId);
  }

  artifactDescription(artifactId) {
    return this.imsArtifactApi.getArtifactDescription(artifactId);
  }

  releaseNoteUrl(artifactId, version) {
    return this.imsArtifactApi.getArtifactReleaseNotesUrl(artifactId, version);
  }
}
