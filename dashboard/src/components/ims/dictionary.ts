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

var ArtifactDictionary = {
  codenvy: {
    display: 'Codenvy',
    description: 'The Codenvy On-Prem binary that runs both single-node and multi-node.',
    releaseNotes: 'http://docs.codenvy.com/user/saas-release-notes/'
  },
  'installation-manager-cli': {
    display: 'Codenvy Installer',
    description: 'Detects new versions of Codenvy software, schedules software downloads, and executes the installation / update process for new software.',
    releaseNotes: 'http://docs.codenvy.com/onpremises/cdec-onpremises-installation-manager-release-notes/'
  }
};

var NodeDictionary = new Map();
NodeDictionary.set('puppet_master_host_name', { type: 'Puppet Master' });
NodeDictionary.set('builder_host_name', { type: 'Main Builder' });
NodeDictionary.set('additional_builders', { type: 'Additional Builder', unwrap: true });
NodeDictionary.set('runner_host_name', { type: 'Main Runner' });
NodeDictionary.set('additional_runners', { type: 'Additional Runner', unwrap: true });
NodeDictionary.set('analytics_host_name', { type: 'Analytics service' });
NodeDictionary.set('data_host_name', { type: 'Data service' });
NodeDictionary.set('api_host_name', { type: 'Api service' });
NodeDictionary.set('site_host_name', { type: 'Site' });
NodeDictionary.set('datasource_host_name', { type: 'Datasource service' });
NodeDictionary.set('host_url', { type: 'All-in-one single node' });

export default { artifacts: ArtifactDictionary, nodes: NodeDictionary };

