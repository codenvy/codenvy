#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

# Removes node from swarm cluster configuration and restarts swarm container
cmd_remove-node() {
  if [ $# -eq 0 ]; then
    error "No IP provided"
    return 1
  fi

  NODE_IP=${1}
  NODES=$(grep "^CODENVY_SWARM_NODES=" "${CHE_CONFIG}/${CHE_MINI_PRODUCT_NAME}.env" | sed "s/.*=//")
  NODES_ARRAY=(${NODES//,/ })
  REMOVE_NODE="CODENVY_SWARM_NODES="
  for NODE in "${NODES_ARRAY[@]}"; do
    if [ "${NODE/$NODE_IP}" = "$NODE" ] ; then
      REMOVE_NODE+=${NODE}","
    fi
  done

  # remove trailing coma
  REMOVE_NODE=$(echo "${REMOVE_NODE}" | sed 's/\(.*\),/\1/g')

  ## TODO added "" for OSX - find portable solution
  sed -i'.bak' -e "s|^CODENVY_SWARM_NODES=.*|${REMOVE_NODE}|" "${CHE_CONFIG}/${CHE_MINI_PRODUCT_NAME}.env"

  # TODO: Restart should not be needed.  Find way to deregister nodes.
  cmd_restart
}
