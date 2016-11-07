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

init_host_arch() {
  GLOBAL_HOST_ARCH=${GLOBAL_HOST_ARCH:=$(docker version --format \{\{.Client\}\} | cut -d" " -f5)}
}

init_name_map() {
  GLOBAL_NAME_MAP=${GLOBAL_NAME_MAP:=$(docker_exec info | grep "Name:" | cut -d" " -f2)}
}

init_host_ip() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_exec run --net host --rm codenvy/che-ip:nightly)}
}

init_uname() {
  GLOBAL_UNAME=${GLOBAL_UNAME:=$(docker_exec run --rm alpine sh -c "uname -r")}
}

init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  NC='\033[0m'

  # Which che CLI version to run?
  DEFAULT_CHE_CLI_VERSION="latest"
  CHE_CLI_VERSION=${CHE_CLI_VERSION:-${DEFAULT_CHE_CLI_VERSION}}

  DEFAULT_CHE_PRODUCT_NAME="CODENVY"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="codenvy"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

  # Turns on stack trace
  DEFAULT_CHE_CLI_DEBUG="false"
  CHE_CLI_DEBUG=${CHE_CLI_DEBUG:-${DEFAULT_CHE_CLI_DEBUG}}

  # Activates console output
  DEFAULT_CHE_CLI_INFO="true"
  CHE_CLI_INFO=${CHE_CLI_INFO:-${DEFAULT_CHE_CLI_INFO}}

  # Activates console warnings
  DEFAULT_CHE_CLI_WARN="true"
  CHE_CLI_WARN=${CHE_CLI_WARN:-${DEFAULT_CHE_CLI_WARN}}

  # Activates console output
  DEFAULT_CHE_CLI_LOG="true"
  CHE_CLI_LOG=${CHE_CLI_LOG:-${DEFAULT_CHE_CLI_LOG}}

  # Initialize CLI folder
  CLI_DIR=~/."${CHE_MINI_PRODUCT_NAME}"/cli
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  # Initialize logging into a log file
  DEFAULT_CHE_CLI_LOGS_FOLDER="${CLI_DIR}"
  CHE_CLI_LOGS_FOLDER="${CHE_CLI_LOGS_FOLDER:-${DEFAULT_CHE_CLI_LOGS_FOLDER}}"

  # Ensure logs folder exists
  LOGS="${CHE_CLI_LOGS_FOLDER}/cli.log"
  mkdir -p "${CHE_CLI_LOGS_FOLDER}"
  # Rename existing log file by adding .old suffix
  if [[ -f "${LOGS}" ]]; then
    mv -f "${LOGS}" "${LOGS}.old"
  fi
  # Log date of CLI execution
  log "$(date)"

  USAGE="
Usage: ${CHE_MINI_PRODUCT_NAME} [COMMAND]
    help                                 This message
    version                              Installed version and upgrade paths
    init [--pull|--force|--offline]      Initializes a directory with a ${CHE_MINI_PRODUCT_NAME} configuration
    start [--pull|--force|--offline]     Starts ${CHE_MINI_PRODUCT_NAME} services
    stop                                 Stops ${CHE_MINI_PRODUCT_NAME} services
    restart [--pull|--force]             Restart ${CHE_MINI_PRODUCT_NAME} services
    destroy [--quiet]                    Stops services, and deletes ${CHE_MINI_PRODUCT_NAME} instance data
    rmi [--quiet]                        Removes the Docker images for CODENVY_VERSION, forcing a repull
    config                               Generates a ${CHE_MINI_PRODUCT_NAME} config from vars; run on any start / restart
    add-node                             Adds a physical node to serve workspaces intto the ${CHE_MINI_PRODUCT_NAME} cluster
    remove-node <ip>                     Removes the physical node from the ${CHE_MINI_PRODUCT_NAME} cluster
    upgrade                              Upgrades Codenvy from one version to another with migrations and backups
    download [--pull|--force|--offline]  Pulls Docker images for the current Codenvy version
    backup [--quiet|--skip-data] Backups ${CHE_MINI_PRODUCT_NAME} configuration and data to CODENVY_BACKUP_FOLDER
    restore [--quiet]                    Restores ${CHE_MINI_PRODUCT_NAME} configuration and data from CODENVY_BACKUP_FOLDER
    offline                              Saves ${CHE_MINI_PRODUCT_NAME} Docker images into TAR files for offline install
    info [ --all                         Run all debugging tests
           --debug                       Displays system information
           --network ]                   Test connectivity between ${CHE_MINI_PRODUCT_NAME} sub-systems

Variables:
    CODENVY_VERSION                       Version to install for 'codenvy init'
    CODENVY_CONFIG                        Where the Codenvy config, CLI and variables are located
    CODENVY_INSTANCE                      Where ${CHE_MINI_PRODUCT_NAME} data, database, logs, are saved
    CODENVY_DEVELOPMENT_MODE              If 'on', then mounts host source folders into Docker images
    CODENVY_DEVELOPMENT_REPO              Location of host git repository that contains source code to be mounted
    CODENVY_CLI_VERSION                   Version of CLI to run
    CODENVY_UTILITY_VERSION               Version of ${CHE_MINI_PRODUCT_NAME} launcher, mount, dev, action to run
    CODENVY_BACKUP_FOLDER                 Location where backups files of installation are stored. Default = pwd
"
}

# Sends arguments as a text to CLI log file
# Usage:
#   log <argument> [other arguments]
log() {
  if is_log; then
    echo "$@" >> "${LOGS}"
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
    PRINT_COMMAND="($CHE_MINI_PRODUCT_NAME $1):"
    PRINT_STATEMENT=$2
  fi
  if is_info; then
    printf "${GREEN}INFO:${NC} %s %s\n" \
              "${PRINT_COMMAND}" \
              "${PRINT_STATEMENT}"
  fi
  log $(printf "INFO: %s %s\n" \
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

has_docker() {
  hash docker 2>/dev/null && return 0 || return 1
}

has_docker_compose() {
  hash docker-compose 2>/dev/null && return 0 || return 1 
}

has_curl() {
  hash curl 2>/dev/null && return 0 || return 1
}

check_docker() {
  if ! has_docker; then
    error "Docker not found. Get it at https://docs.docker.com/engine/installation/."
    return 1;
  fi

  if ! docker ps >> "${LOGS}" 2>&1; then
    error "Docker issues - 'docker ps' fails."
    return 1;
  fi
}

check_docker_compose() {
  if ! has_docker_compose; then
    error "Error - Docker Compose not found. Get it at https://docs.docker.com/compose/install/."
    return 2;
  fi

  COMPOSE_VERSION=$(docker-compose -version | cut -d' ' -f3 | sed 's/,//')
  COMPOSE_VERSION=${COMPOSE_VERSION:-1}

  FIRST=$(echo ${COMPOSE_VERSION:0:1})
  SECOND=$(echo ${COMPOSE_VERSION:2:1})

  # Docker compose needs to be greater than or equal to 1.8.1
  if [[ ${FIRST} -lt 1 ]] ||
     [[ ${SECOND} -lt 8 ]]; then
      output=$(docker-compose -version)
      error "Error - Docker Compose 1.8+ required:"
      error "Docker compose output: ${output}"
      return 2;
  fi
}

grab_offline_images(){
  # If you are using codenvy in offline mode, images must be loaded here
  # This is the point where we know that docker is working, but before we run any utilities
  # that require docker.
  if [ ! -z ${2+x} ]; then
    if [ "${2}" == "--offline" ]; then
      info "init" "Importing ${CHE_MINI_PRODUCT_NAME} Docker images from tars..."

      if [ ! -d offline ]; then
        info "init" "You requested offline loading of images, but could not find 'offline/'"
        return 2;
      fi

      IFS=$'\n'
      for file in "offline"/*.tar 
      do
        if ! $(docker load < "offline"/"${file##*/}" > /dev/null); then
          error "Failed to restore ${CHE_MINI_PRODUCT_NAME} Docker images"
          return 2;
        fi
        info "init" "Loading ${file##*/}..."
      done
    fi
  fi
}

grab_initial_images() {
  # Prep script by getting default image
  if [ "$(docker images -q alpine 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image alpine:latest"
    log "docker pull alpine >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull alpine >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image alpine unavailable. Not on dockerhub or built locally."
      return 1;
    fi
  fi

  if [ "$(docker images -q appropriate/curl 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image curl:latest"
    log "docker pull appropriate/curl >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull appropriate/curl >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image appropriate/curl unavailable. Not on dockerhub or built locally."
      return 1;
    fi
  fi

  if [ "$(docker images -q codenvy/che-ip:nightly 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image eclipse/che-ip:nightly"
    log "docker pull codenvy/che-ip:nightly >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull codenvy/che-ip:nightly >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image codenvy/che-ip:nightly unavailable. Not on dockerhub or built locally."
      return 1;
    fi
  fi

  if [ "$(docker images -q eclipse/che-version 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image eclipse/che-version"
    log "docker pull eclipse/che-version >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull codenvy/version >> "${LOGS}" 2>&1 || TEST=$? 
    if [ "$TEST" = "1" ]; then
      error "Image codenvy/version not found on dockerhub or locally."
      return 1;
    fi
  fi
}

check_volume_mount() {
  docker_exec run --rm -v $(pwd):/copy alpine sh -c "echo 'test' > /copy/test"
  if [[ ! -f $(pwd)/test ]]; then
    error "Docker installed, but unable to volume mount files from your host."
    error "Have you enabled Docker to allow mounting host directories?"
    return 1;
  fi
  docker_exec run --rm -v $(pwd):/copy alpine sh -c "rm -rf /copy/test"
}


has_docker_for_windows_client(){
  debug $FUNCNAME
  init_host_arch
  if [ "${GLOBAL_HOST_ARCH}" = "windows" ]; then
    return 0
  else
    return 1
  fi
}

docker_exec() {
  debug $FUNCNAME
  if has_docker_for_windows_client; then
    log "MSYS_NO_PATHCONV=1 docker.exe \"$@\""
    MSYS_NO_PATHCONV=1 docker.exe "$@"
  else
    log "$(which docker) \$@\""
    "$(which docker)" "$@"
  fi
}

curl() {
  if ! has_curl; then
    log "docker run --rm --net=host appropriate/curl \"$@\""
    docker run --rm --net=host appropriate/curl "$@"
  else
    log "$(which curl) \"$@\""
    $(which curl) "$@"
  fi
}

update_cli() {
  info "cli" "Downloading cli-$CHE_CLI_VERSION"

  # If the che.sh is running from within the Che source repo, then 
  # copy cli.sh from the repo to ~/.che and then return.
  if [[ $(get_script_source_dir) != ~/."${CHE_MINI_PRODUCT_NAME}"/cli ]]; then
    log "cp -rf $(get_script_source_dir)/cli.sh ~/.\"${CHE_MINI_PRODUCT_NAME}\"/cli/cli-$CHE_CLI_VERSION.sh"
    cp -rf $(get_script_source_dir)/cli.sh ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-$CHE_CLI_VERSION.sh
    return
  fi

  # We are downloading the CLI from the hosted repository.
  # We will download a version that is tagged from GitHub.
  if [[ "${CHE_CLI_VERSION}" = "latest" ]] || \
     [[ "${CHE_CLI_VERSION}" = "nightly" ]] || \
     [[ ${CHE_CLI_VERSION:0:1} == "4" ]]; then
    GITHUB_VERSION=master
  else
    GITHUB_VERSION=$CHE_CLI_VERSION
  fi

  # We are downloading the CLI from the core repository.
  URL=https://raw.githubusercontent.com/${CHE_MINI_PRODUCT_NAME}/${CHE_MINI_PRODUCT_NAME}/$GITHUB_VERSION/cli.sh

  if ! curl --output /dev/null --silent --head --fail "$URL"; then
    error "CLI download error. Bad network or version."
    return 1;
  else
    log "curl -sL $URL > ~/.\"${CHE_MINI_PRODUCT_NAME}\"/cli/cli-$CHE_CLI_VERSION.sh"
    curl -sL $URL > ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-$CHE_CLI_VERSION.sh
  fi
}

get_script_source_dir() {
  SOURCE="${BASH_SOURCE[0]}"
  while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
    DIR="$( cd -P '$( dirname \"$SOURCE\" )' && pwd )"
    SOURCE="$(readlink '$SOURCE')"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
  done
  echo "$( cd -P "$( dirname "$SOURCE" )" && pwd )"
}

init() {
  init_logging

  if [[ $# == 0 ]]; then
    usage;
  fi

  check_docker
  check_docker_compose
  grab_offline_images
  grab_initial_images
  check_volume_mount

  # Test to see if we have cli_funcs
  if [[ ! -f ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-${CHE_CLI_VERSION}.sh ]] ||
     [[ $(get_script_source_dir)/cli.sh -nt ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-${CHE_CLI_VERSION}.sh ]]; then
    update_cli
  fi

  log "source ~/.\"${CHE_MINI_PRODUCT_NAME}\"/cli/cli-${CHE_CLI_VERSION}.sh"
  source ~/."${CHE_MINI_PRODUCT_NAME}"/cli/cli-${CHE_CLI_VERSION}.sh
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -e
set -u

# Initialize the self-updating CLI - this is a common code between Che & Codenvy.
init "$@"

# Begin product-specific CLI calls
info "cli" "Loading cli..."
cli_init "$@"
cli_parse "$@"
cli_cli "$@"
