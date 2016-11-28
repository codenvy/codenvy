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

init_constants() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  BOLD='\033[1m'
  UNDERLINE='\033[4m'
  NC='\033[0m'
  LOG_INITIALIZED=false

  DEFAULT_CHE_PRODUCT_NAME="CODENVY"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="codenvy"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

  DEFAULT_CHE_FORMAL_PRODUCT_NAME="Codenvy"
  CHE_FORMAL_PRODUCT_NAME=${CHE_FORMAL_PRODUCT_NAME:-${DEFAULT_CHE_FORMAL_PRODUCT_NAME}}

  # Path to root folder inside the container
  DEFAULT_CHE_CONTAINER_ROOT="/${CHE_MINI_PRODUCT_NAME}"
  CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT:-${DEFAULT_CHE_CONTAINER_ROOT}}

  # Turns on stack trace
  DEFAULT_CHE_CLI_DEBUG="false"
  CHE_CLI_DEBUG=${CLI_DEBUG:-${DEFAULT_CHE_CLI_DEBUG}}

  # Activates console output
  DEFAULT_CHE_CLI_INFO="true"
  CHE_CLI_INFO=${CLI_INFO:-${DEFAULT_CHE_CLI_INFO}}

  # Activates console warnings
  DEFAULT_CHE_CLI_WARN="true"
  CHE_CLI_WARN=${CLI_WARN:-${DEFAULT_CHE_CLI_WARN}}

  # Activates console output
  DEFAULT_CHE_CLI_LOG="true"
  CHE_CLI_LOG=${CLI_LOG:-${DEFAULT_CHE_CLI_LOG}}
}

init_usage() {
  USAGE="
Usage: docker run -it --rm
                  -v /var/run/docker.sock:/var/run/docker.sock
                  -v <LOCAL_DATA_PATH>:${CHE_CONTAINER_ROOT}
                  ${CODENVY_IMAGE_NAME} [COMMAND]

    help                                 This message
    version                              Installed version and upgrade paths
    init                                 Initializes a directory with a ${CHE_FORMAL_PRODUCT_NAME} install
         [--no-force                         Default - uses cached local Docker images
          --pull                             Checks for newer images from DockerHub
          --force                            Removes all images and re-pulls all images from DockerHub
          --offline                          Uses images saved to disk from the offline command
          --accept-license                   Auto accepts the ${CHE_FORMAL_PRODUCT_NAME} license during installation
          --reinit]                          Reinstalls using existing $CHE_MINI_PRODUCT_NAME.env configuration
    start [--pull | --force | --offline] Starts ${CHE_FORMAL_PRODUCT_NAME} services
    stop                                 Stops ${CHE_FORMAL_PRODUCT_NAME} services
    restart [--pull | --force]           Restart ${CHE_FORMAL_PRODUCT_NAME} services
    destroy                              Stops services, and deletes ${CHE_FORMAL_PRODUCT_NAME} instance data
            [--quiet                         Does not ask for confirmation before destroying instance data
             --cli]                          If :/cli is mounted, will destroy the cli.log
    rmi [--quiet]                        Removes the Docker images for <version>, forcing a repull
    config                               Generates a ${CHE_FORMAL_PRODUCT_NAME} config from vars; run on any start / restart
    add-node                             Adds a physical node to serve workspaces intto the ${CHE_FORMAL_PRODUCT_NAME} cluster
    remove-node <ip>                     Removes the physical node from the ${CHE_FORMAL_PRODUCT_NAME} cluster
    upgrade                              Upgrades ${CHE_FORMAL_PRODUCT_NAME} from one version to another with migrations and backups
    download [--pull|--force|--offline]  Pulls Docker images for the current ${CHE_FORMAL_PRODUCT_NAME} version
    backup [--quiet | --skip-data]       Backups $${CHE_FORMAL_PRODUCT_NAME} configuration and data to ${CHE_CONTAINER_ROOT}/backup volume mount
    restore [--quiet]                    Restores ${CHE_FORMAL_PRODUCT_NAME} configuration and data from ${CHE_CONTAINER_ROOT}/backup mount
    offline                              Saves ${CHE_FORMAL_PRODUCT_NAME} Docker images into TAR files for offline install
    info                                 Displays info about ${CHE_FORMAL_PRODUCT_NAME} and the CLI
         [ --all                             Run all debugging tests
           --debug                           Displays system information
           --network]                        Test connectivity between ${CHE_FORMAL_PRODUCT_NAME} sub-systems
    ssh <wksp-name> [machine-name]       SSH to a workspace if SSH agent enabled
    mount <wksp-name>                    Synchronize workspace with current working directory
    action <action-name> [--help]        Start action on ${CHE_FORMAL_PRODUCT_NAME} instance
    test <test-name> [--help]            Start test on ${CHE_FORMAL_PRODUCT_NAME} instance

Variables:
    CODENVY_HOST                         IP address or hostname where ${CHE_FORMAL_PRODUCT_NAME} will serve its users
    CLI_DEBUG                            Default=false. Prints stack trace during execution
    CLI_INFO                             Default=true. Prints out INFO messages to standard out
    CLI_WARN                             Default=true. Prints WARN messages to standard out
    CLI_LOG                              Default=true. Prints messages to cli.log file
"
}

# Sends arguments as a text to CLI log file
# Usage:
#   log <argument> [other arguments]
log() {
  if [[ "$LOG_INITIALIZED"  = "true" ]]; then
    if is_log; then
      echo "$@" >> "${LOGS}"
    fi 
  fi
}

usage () {
  debug $FUNCNAME
  printf "%s" "${USAGE}"
  return 1;
}

warning() {
  if is_warning; then
    printf  "${YELLOW}WARN:${NC} %s\n" "${1}"
  fi
  log $(printf "WARN: %s\n" "${1}")
}

info() {
  if [ -z ${2+x} ]; then
    PRINT_COMMAND=""
    PRINT_STATEMENT=$1
  else
    PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1): "
    PRINT_STATEMENT=$2
  fi
  if is_info; then
    printf "${GREEN}INFO:${NC} %b%b\n" \
              "${PRINT_COMMAND}" \
              "${PRINT_STATEMENT}"
  fi
  log $(printf "INFO: %b %b\n" \
        "${PRINT_COMMAND}" \
        "${PRINT_STATEMENT}")
}

debug() {
  if is_debug; then
    printf  "\n${BLUE}DEBUG:${NC} %s" "${1}"
  fi
  log $(printf "\nDEBUG: %s" "${1}")
}

error() {
  printf  "${RED}ERROR:${NC} %s\n" "${1}"
  log $(printf  "ERROR: %s\n" "${1}")
}

# Prints message without changes
# Usage: has the same syntax as printf command
text() {
  printf "$@"
  log $(printf "$@")
}

## TODO use that for all native calls to improve logging for support purposes
# Executes command with 'eval' command.
# Also logs what is being executed and stdout/stderr
# Usage:
#   cli_eval <command to execute>
# Examples:
#   cli_eval "$(which curl) http://localhost:80/api/"
cli_eval() {
  log "$@"
  tmpfile=$(mktemp)
  if eval "$@" &>"${tmpfile}"; then
    # Execution succeeded
    cat "${tmpfile}" >> "${LOGS}"
    cat "${tmpfile}"
    rm "${tmpfile}"
  else
    # Execution failed
    cat "${tmpfile}" >> "${LOGS}"
    cat "${tmpfile}"
    rm "${tmpfile}"
    fail
  fi
}

# Executes command with 'eval' command and suppress stdout/stderr.
# Also logs what is being executed and stdout+stderr
# Usage:
#   cli_silent_eval <command to execute>
# Examples:
#   cli_silent_eval "$(which curl) http://localhost:80/api/"
cli_silent_eval() {
  log "$@"
  eval "$@" >> "${LOGS}" 2>&1
}

is_log() {
  if [ "${CHE_CLI_LOG}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_warning() {
  if [ "${CHE_CLI_WARN}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_info() {
  if [ "${CHE_CLI_INFO}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

is_debug() {
  if [ "${CHE_CLI_DEBUG}" = "true" ]; then
    return 0
  else
    return 1
  fi
}

check_docker() {
  if ! has_docker; then
    error "Docker not found. Get it at https://docs.docker.com/engine/installation/."
    return 1;
  fi

  # If DOCKER_HOST is not set, then it should bind mounted
  if [ -z "${DOCKER_HOST+x}" ]; then
    if ! docker ps > /dev/null 2>&1; then
      info "Welcome to ${CHE_FORMAL_PRODUCT_NAME}!"
      info ""
      info "$CHE_FORMAL_PRODUCT_NAME commands require additional parameters:"
      info "  Mounting 'docker.sock', which let's us access Docker"
      info ""
      info "Syntax:"
      info "  docker run -it --rm ${BOLD} -v /var/run/docker.sock:/var/run/docker.sock${NC}"
      info "                  $CHE_MINI_PRODUCT_NAME/cli $*"
      return 2;
    fi
  fi

  DOCKER_VERSION=($(docker version |  grep  "Version:" | sed 's/Version://'))

  MAJOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:0:1})
  MINOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:2:2})

  # Docker needs to be greater than or equal to 1.11
  if [[ ${MAJOR_VERSION_ID} -lt 1 ]] ||
     [[ ${MINOR_VERSION_ID} -lt 11 ]]; then
       error "Error - Docker engine 1.11+ required."
       return 2;
  fi

  # Detect version so that we can provide better error warnings
  DEFAULT_CODENVY_VERSION=$(cat "/version/latest.ver")
  CODENVY_IMAGE_NAME=$(docker inspect --format='{{.Config.Image}}' $(get_this_container_id))
  CODENVY_IMAGE_VERSION=$(echo "${CODENVY_IMAGE_NAME}" | cut -d : -f2 -s)

  if [[ "${CODENVY_IMAGE_VERSION}" = "" ]] ||
     [[ "${CODENVY_IMAGE_VERSION}" = "latest" ]]; then
     warning "You are using CLI image version 'latest' which is set to '$DEFAULT_CODENVY_VERSION'."
    CODENVY_IMAGE_VERSION=$DEFAULT_CODENVY_VERSION
  else
    CODENVY_IMAGE_VERSION=$CODENVY_IMAGE_VERSION
  fi

  CODENVY_VERSION=$CODENVY_IMAGE_VERSION
}
  
check_mounts() {

  # Verify that we can write to the host file system from the container
  check_host_volume_mount

  DATA_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}")
  INSTANCE_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/instance")
  BACKUP_MOUNT=$(get_container_folder ":${CHE_CONTAINER_ROOT}/backup")
  REPO_MOUNT=$(get_container_folder ":/repo")
  CLI_MOUNT=$(get_container_folder ":/cli")
  SYNC_MOUNT=$(get_container_folder ":/sync")
  UNISON_PROFILE_MOUNT=$(get_container_folder ":/unison")
   
  if [[ "${DATA_MOUNT}" = "not set" ]]; then
    info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
    info ""
    info "We need some information before we can start ${CHE_FORMAL_PRODUCT_NAME}."
    info ""
    info "$CHE_FORMAL_PRODUCT_NAME commands require additional parameters:"
    info "  1: Mounting 'docker.sock', which let's us access Docker"
    info "  2: A local path where ${CHE_FORMAL_PRODUCT_NAME} will save user data"
    info ""
    info "Simplest syntax:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                         ${CODENVY_IMAGE_NAME} $*"
    info ""
    info ""
    info "Or run with overrides for instance and/or backup:"
    info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
    info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
    info "                      -v <YOUR_INSTANCE_PATH>:${CHE_CONTAINER_ROOT}/instance"
    info "                      -v <YOUR_BACKUP_PATH>:${CHE_CONTAINER_ROOT}/backup"
    info "                         ${CODENVY_IMAGE_NAME} $*"
    return 2;
  fi

  DEFAULT_CODENVY_CONFIG="${DATA_MOUNT}"
  DEFAULT_CODENVY_INSTANCE="${DATA_MOUNT}"/instance
  DEFAULT_CODENVY_BACKUP="${DATA_MOUNT}"/backup

  if [[ "${INSTANCE_MOUNT}" != "not set" ]]; then
    DEFAULT_CODENVY_INSTANCE="${INSTANCE_MOUNT}"
  fi

  if [[ "${BACKUP_MOUNT}" != "not set" ]]; then
    DEFAULT_CODENVY_BACKUP="${BACKUP_MOUNT}"
  fi

  #   Set offline to CONFIG_MOUNT
  CODENVY_HOST_CONFIG=${CODENVY_CONFIG:-${DEFAULT_CODENVY_CONFIG}}
  CODENVY_CONTAINER_CONFIG="${CHE_CONTAINER_ROOT}"

  CODENVY_HOST_INSTANCE=${CODENVY_INSTANCE:-${DEFAULT_CODENVY_INSTANCE}}
  CODENVY_CONTAINER_INSTANCE="${CHE_CONTAINER_ROOT}/instance"

  CODENVY_HOST_BACKUP=${CODENVY_BACKUP:-${DEFAULT_CODENVY_BACKUP}}
  CODENVY_CONTAINER_BACKUP="${CHE_CONTAINER_ROOT}/backup"

  ### DEV MODE VARIABLES
  CODENVY_DEVELOPMENT_MODE="off"
  if [[ "${REPO_MOUNT}" != "not set" ]]; then 
    CODENVY_DEVELOPMENT_MODE="on"
    CODENVY_HOST_DEVELOPMENT_REPO="${REPO_MOUNT}"
    CODENVY_CONTAINER_DEVELOPMENT_REPO="/repo"

    DEFAULT_CODENVY_DEVELOPMENT_TOMCAT="assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone"
    CODENVY_DEVELOPMENT_TOMCAT="${CODENVY_HOST_INSTANCE}/dev"

    if [[ ! -d "${CODENVY_CONTAINER_DEVELOPMENT_REPO}"  ]] || [[ ! -d "${CODENVY_CONTAINER_DEVELOPMENT_REPO}/assembly" ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted ':/repo', but we did not detect a valid ${CHE_FORMAL_PRODUCT_NAME} source repo."
      info ""
      info "Volume mounting ':/repo' activate dev mode, using assembly and CLI files from $CHE_FORMAL_PRODUCT_NAME repo."
      info ""
      info "Please check the path you mounted to verify that is a valid $CHE_FORMAL_PRODUCT_NAME git repository."
      info ""
      info "Simplest syntax::"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
      info "                      -v <YOUR_${CHE_PRODUCT_NAME}_REPO>:/repo"
      info "                         ${CODENVY_IMAGE_NAME} $*"
      info ""
      info ""
      info "Or run with overrides for instance, and backup (all required):"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <YOUR_LOCAL_PATH>:${CHE_CONTAINER_ROOT}"
      info "                      -v <YOUR_INSTANCE_PATH>:${CHE_CONTAINER_ROOT}/instance"
      info "                      -v <YOUR_BACKUP_PATH>:${CHE_CONTAINER_ROOT}/backup"
      info "                      -v <YOUR_${CHE_PRODUCT_NAME}_REPO>:/repo"
      info "                         ${CODENVY_IMAGE_NAME} $*"
      return 2
    fi
    if [[ ! -d $(echo "${CODENVY_CONTAINER_DEVELOPMENT_REPO}"/"${DEFAULT_CODENVY_DEVELOPMENT_TOMCAT}"-*/) ]]; then
      info "Welcome to $CHE_FORMAL_PRODUCT_NAME!"
      info ""
      info "You volume mounted a valid $CHE_FORMAL_PRODUCT_NAME repo to ':/repo', but we could not find a Tomcat assembly."
      info "Have you built /assembly/onpremises-ide-packaging-tomcat-codenvy-allinone with 'mvn clean install'?"
      return 2
    fi
  fi
}

init_logging() {
  # Initialize CLI folder
  CLI_DIR="/cli"
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  # Ensure logs folder exists
  LOGS="${CLI_DIR}/cli.log"
  LOG_INITIALIZED=true

  # Log date of CLI execution
  log "$(date)"
}

docker_compose() {
  debug $FUNCNAME

  if has_compose; then
    docker-compose "$@"
  else
    docker_run -v "${CODENVY_HOST_INSTANCE}":"${CODENVY_CONTAINER_INSTANCE}" \
                  docker/compose:1.8.1 "$@"
  fi
}
init() {
  init_constants

  SCRIPTS_BASE_CONTAINER_SOURCE_DIR="/scripts/base"
  # add helper scripts
  for COMMAND_FILE in "${SCRIPTS_BASE_CONTAINER_SOURCE_DIR}"/*.sh
  do
    source "${COMMAND_FILE}"
  done

  # Make sure Docker is working and we have /var/run/docker.sock mounted or valid DOCKER_HOST
  check_docker "$@"

  init_usage
  if [[ $# == 0 ]]; then
    usage;
  fi

  # Only verify mounts after Docker is confirmed to be working.
  check_mounts "$@"

  # Only initialize after mounts have been established so we can write cli.log out to a mount folder
  init_logging "$@"

  SCRIPTS_CONTAINER_SOURCE_DIR=""
  if [[ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]]; then
     # Use the CLI that is inside the repository.
     SCRIPTS_CONTAINER_SOURCE_DIR="/repo/dockerfiles/cli"  
  else
     # Use the CLI that is inside the container.  
     SCRIPTS_CONTAINER_SOURCE_DIR="/scripts"  
  fi

  # Primary source directory
  source "${SCRIPTS_CONTAINER_SOURCE_DIR}"/cli.sh
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

# Bootstrap enough stuff to load /cli/cli.sh
init "$@"

# Begin product-specific CLI calls
info "cli" "Loading cli..."
cli_init "$@"
cli_parse "$@"
