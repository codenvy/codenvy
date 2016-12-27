#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

CHE_PRODUCT_NAME="CODENVY"
CHE_MINI_PRODUCT_NAME="codenvy"
CHE_FORMAL_PRODUCT_NAME="Codenvy"
CHE_CONTAINER_ROOT="/data"
CHE_ASSEMBLY_IN_REPO_MODULE_NAME="assembly/onpremises-ide-packaging-tomcat-codenvy-allinone"
CHE_ASSEMBLY_IN_REPO="${CHE_ASSEMBLY_IN_REPO_MODULE_NAME}/target/onpremises-ide-packaging-tomcat-codenvy-allinone-*/"
WS_AGENT_IN_REPO_MODULE_NAME="assembly/onpremises-ide-packaging-tomcat-ext-server"
WS_AGENT_IN_REPO="${WS_AGENT_IN_REPO_MODULE_NAME}/target/onpremises-ide-packaging-tomcat-ext-server-*.tar.gz"
WS_AGENT_ASSEMBLY="ws-agent.tar.gz"
TERMINAL_AGENT_IN_REPO_MODULE_NAME="assembly/onpremises-ide-packaging-zip-terminal"
TERMINAL_AGENT_IN_REPO="${TERMINAL_AGENT_IN_REPO_MODULE_NAME}/target/onpremises-ide-packaging-zip-terminal-*.tar.gz"
TERMINAL_AGENT_ASSEMBLY="websocket-terminal-linux_amd64.tar.gz"
CHE_SCRIPTS_CONTAINER_SOURCE_DIR="/repo/dockerfiles/cli/scripts"
CHE_LICENSE=true
CHE_LICENSE_URL="https://codenvy.com/legal/fair-source/"
CHE_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}_${CHE_MINI_PRODUCT_NAME}_1"
CHE_IMAGE_FULLNAME="codenvy/cli:<version>"

init_usage() {
  USAGE="
USAGE: 
  docker run -it --rm <DOCKER_PARAMETERS> ${CHE_IMAGE_FULLNAME} [COMMAND]

MANDATORY DOCKER PARAMETERS:
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}                Where user, instance, and log data saved

OPTIONAL DOCKER PARAMETERS:
  -e CODENVY_HOST=<YOUR_HOST>          IP address or hostname where ${CHE_FORMAL_PRODUCT_NAME} will serve its users
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/instance       Where instance, user, log data will be saved
  -v <LOCAL_PATH>:${CHE_CONTAINER_ROOT}/backup         Where backup files will be saved
  -v <LOCAL_PATH>:/cli                 Where the CLI trace log is saved
  -v <LOCAL_PATH>:/repo                ${CHE_FORMAL_PRODUCT_NAME} git repo to activate dev mode
  -v <LOCAL_PATH>:/sync                Where remote ws files will be copied with sync command
  -v <LOCAL_PATH>:/unison              Where unison profile for optimzing sync command resides
    
COMMANDS:
  action <action-name>                 Start action on ${CHE_FORMAL_PRODUCT_NAME} instance
  add-node                             Adds a physical node to serve workspaces intto the ${CHE_FORMAL_PRODUCT_NAME} cluster
  backup                               Backups ${CHE_FORMAL_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
  config                               Generates a ${CHE_FORMAL_PRODUCT_NAME} config from vars; run on any start / restart
  destroy                              Stops services, and deletes ${CHE_FORMAL_PRODUCT_NAME} instance data
  download                             Pulls Docker images for the current ${CHE_FORMAL_PRODUCT_NAME} version
  help                                 This message
  info                                 Displays info about ${CHE_FORMAL_PRODUCT_NAME} and the CLI
  init                                 Initializes a directory with a ${CHE_FORMAL_PRODUCT_NAME} install
  list-nodes                           Lists all physical nodes that are part of the ${CHE_FORMAL_PRODUCT_NAME} cluster
  offline                              Saves ${CHE_FORMAL_PRODUCT_NAME} Docker images into TAR files for offline install
  remove-node <ip>                     Removes the physical node from the ${CHE_FORMAL_PRODUCT_NAME} cluster
  restart                              Restart ${CHE_FORMAL_PRODUCT_NAME} services
  restore                              Restores ${CHE_FORMAL_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
  rmi                                  Removes the Docker images for <version>, forcing a repull
  ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
  start                                Starts ${CHE_FORMAL_PRODUCT_NAME} services
  stop                                 Stops ${CHE_FORMAL_PRODUCT_NAME} services
  sync <wksp-name>                     Synchronize workspace with current working directory
  test <test-name>                     Start test on ${CHE_FORMAL_PRODUCT_NAME} instance
  upgrade                              Upgrades ${CHE_FORMAL_PRODUCT_NAME} from one version to another with migrations and backups
  version                              Installed version and upgrade paths
"
}

source /scripts/base/startup.sh
start "$@"
