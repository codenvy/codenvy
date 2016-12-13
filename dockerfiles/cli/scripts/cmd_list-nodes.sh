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

# Shows list of the docker nodes in the swarm cluster
cmd_list-nodes() {
  ## TODO use
  ## - config to get all configured nodes
  ## - instance to get all registered nodes
  ## - call to swarm api to get all enabled nodes
  ## Print output that marks all the nodes in accordance to it state

  if container_exist_by_name $CHE_SERVER_CONTAINER_NAME; then
    CURRENT_CHE_SERVER_CONTAINER_ID=$(get_server_container_id $CHE_SERVER_CONTAINER_NAME)
    if container_is_running ${CURRENT_CHE_SERVER_CONTAINER_ID} && \
       server_is_booted ${CURRENT_CHE_SERVER_CONTAINER_ID}; then
       info "list-nodes" $(cat "${CHE_CONTAINER_INSTANCE}/config/swarm/node_list")
       return
    fi
  fi

  info "list-nodes" "The system is not running."
}
