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

help_cmd_add-node() {
  text "\n"
  text "USAGE: ${CHE_IMAGE_FULLNAME} add-node\n"
  text "\n"
  text "Displays the process for adding a new workspace node to the ${CHE_MINI_PRODUCT_NAME} cluster"
  text "\n"
}

pre_cmd_add-node() {
  true
}

# Prints command that should be executed on a node to add it to swarm cluster
cmd_add-node() {
  info "add-node" "1. For the node you want to add, verify that Docker is installed."
  info "add-node" "2. Collect the externally accessible IP address or DNS of the node."
  info "add-node" "3. Grab your Codenvy admin user name and password."
  info "add-node" "4. SSH into the remote node and execute:"
  printf "                             ${YELLOW}bash <(curl -sSL http://${CODENVY_HOST}/api/nodes/script) --user <admin-user> --password <admin-pass> --ip <node-ip>${NC}"
  echo ""
  info "add-node" "5. The node will configure itself and update the Codenvy cluster."
}
