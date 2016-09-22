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

import dictionary from './dictionary';

/**
 * This class is handling the interface with Installation Manager Server (IMS) update API.
 */
export class ImsNodesApi {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($http) {

    // remote call
    this.remoteImsAPI = {
      addNode: (node) => $http.post('/im/node', { params: {nodeName: node}}),
      deleteNode: (node) => $http.delete('/im/node', { params: {nodeName: node}}),
      getCodenvyOnPremConfig: () => $http.get('/im/nodes')
    };
  }

  /**
   * Add an (existing and running) node to installation.
   * @param node the fully qualified node name
   */
  addNode(node) {
    let nodeDesc = { nodeName: node };
    return this.remoteImsAPI.addNode(nodeDesc, {});
  }

  /**
   * Remove a node from installation.
   * @param node the fully qualified node name
   */
  deleteNode(node) {
    let nodeDesc = { nodeName: node };
    return this.remoteImsAPI.deleteNode(nodeDesc, {});
  }

  /**
   * Returns Codenvy on-premise installation node list.
   * @returns a promise on an object hostKey => { nodeType, hostname }
   */
  listNodes() {
    let serverPromise = this.remoteImsAPI.getCodenvyOnPremConfig();
    return serverPromise.then(response => this.unwrapNodes(response.data));
  }

  unwrapNodes(nodes) {
    let result = {};
    for (let key in nodes) {
      if (nodes.hasOwnProperty(key)) {
        let nodeHostname = nodes[key];
        let nodeData = dictionary.nodes.get(key);
        if (nodeData) {
          if (nodeData.unwrap) {
            this.unwrapNodeSet(result, key, nodeData.type, nodeHostname); // Actually, here nodeHostname is not a hostname but an array
          } else {
            result[key] = { type: nodeData.type, hostname: nodeHostname };
          }
        } else {
          result[key] = { type: 'Unknown Type', hostname: nodeHostname };
        }
      }
    }
    return result;
  }

  unwrapNodeSet(result, key, type, nodeArray) {
    var i = 0;
    for (let hostname of nodeArray) {
      result[`${key}_${i}`] = { type: type, hostname: hostname };
      i++;
    }
  }
}
