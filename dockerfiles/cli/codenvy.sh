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

init_host_ip() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host eclipse/che-ip:nightly)}
}

init_constants() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  YELLOW='\033[38;5;220m'
  NC='\033[0m'

  DEFAULT_CHE_PRODUCT_NAME="CODENVY"
  CHE_PRODUCT_NAME=${CHE_PRODUCT_NAME:-${DEFAULT_CHE_PRODUCT_NAME}}

  # Name used in CLI statements
  DEFAULT_CHE_MINI_PRODUCT_NAME="codenvy"
  CHE_MINI_PRODUCT_NAME=${CHE_MINI_PRODUCT_NAME:-${DEFAULT_CHE_MINI_PRODUCT_NAME}}

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

  USAGE="
Usage: docker run -it --rm 
                  -v /var/run/docker.sock:/var/run/docker.sock
                  -v <host-path-for-codenvy-data>:/codenvy
                  ${CHE_MINI_PRODUCT_NAME}/cli:<version> [COMMAND]

    help                                 This message
    version                              Installed version and upgrade paths
    init [--pull|--force|--offline]      Initializes a directory with a ${CHE_MINI_PRODUCT_NAME} configuration
    start [--pull|--force|--offline]     Starts ${CHE_MINI_PRODUCT_NAME} services
    stop                                 Stops ${CHE_MINI_PRODUCT_NAME} services
    restart [--pull|--force]             Restart ${CHE_MINI_PRODUCT_NAME} services
    destroy [--quiet]                    Stops services, and deletes ${CHE_MINI_PRODUCT_NAME} instance data
    rmi [--quiet]                        Removes the Docker images for <version>, forcing a repull
    config                               Generates a ${CHE_MINI_PRODUCT_NAME} config from vars; run on any start / restart
    add-node                             Adds a physical node to serve workspaces intto the ${CHE_MINI_PRODUCT_NAME} cluster
    remove-node <ip>                     Removes the physical node from the ${CHE_MINI_PRODUCT_NAME} cluster
    upgrade                              Upgrades Codenvy from one version to another with migrations and backups
    download [--pull|--force|--offline]  Pulls Docker images for the current Codenvy version
    backup [--quiet|--skip-data] Backups ${CHE_MINI_PRODUCT_NAME} configuration and data to /codenvy/backup volume mount
    restore [--quiet]                    Restores ${CHE_MINI_PRODUCT_NAME} configuration and data from /codenvy/backup mount
    offline                              Saves ${CHE_MINI_PRODUCT_NAME} Docker images into TAR files for offline install
    info [ --all                         Run all debugging tests
           --debug                       Displays system information
           --network ]                   Test connectivity between ${CHE_MINI_PRODUCT_NAME} sub-systems

Variables:
    CODENVY_HOST                         IP address or hostname where ${CHE_MINI_PRODUCT_NAME} will serve its users
    CLI_DEBUG                            Default=false.Prints stack trace during execution
    CLI_INFO                             Default=true. Prints out INFO messages to standard out
    CLI_WARN                             Default=true. Prints WARN messages to standard out
    CLI_LOG                              Default=true. Prints messages to cli.log file
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

has_compose() {
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

  DOCKER_VERSION=($(docker version |  grep  "Version:" | sed 's/Version://'))

  MAJOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:0:1})
  MINOR_VERSION_ID=$(echo ${DOCKER_VERSION[0]:2:2})

  # Docker needs to be greater than or equal to 1.11
  if [[ ${MAJOR_VERSION_ID} -lt 1 ]] ||
     [[ ${MINOR_VERSION_ID} -lt 11 ]]; then
       error "Error - Docker engine 1.11+ required."
       return 2;
  fi

  # If DOCKER_HOST is not set, then it should bind mounted
  if [ -z "${DOCKER_HOST+x}" ]; then
      if ! docker ps > /dev/null 2>&1; then
        printf "${GREEN}INFO:${NC} Welcome to Codenvy!\n"
        printf "${GREEN}INFO:${NC} \n"
        printf "${GREEN}INFO:${NC} We did not detect a valid DOCKER_HOST.\n"
        printf "${GREEN}INFO:${NC} \n"
        printf "${GREEN}INFO:${NC} Rerun the CLI:\n"
        printf "${GREEN}INFO:${NC}   docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock \n"
        printf "${GREEN}INFO:${NC}                       -v <local-path>:/codenvy \n"
        printf "${GREEN}INFO:${NC}                            codenvy/cli $@\n"
        return 2;
      fi
  fi

  # Detect version so that we can provide better error warnings
  DEFAULT_CODENVY_VERSION="latest"
  CODENVY_IMAGE_NAME=$(docker inspect --format='{{.Config.Image}}' $(get_this_container_id))
  CODENVY_IMAGE_VERSION=$(echo "${CODENVY_IMAGE_NAME}" | cut -d : -f2 -s)

  if [ "${CODENVY_IMAGE_VERSION}" = "" ]; then
    CODENVY_VERSION=$DEFAULT_CODENVY_VERSION
  else
    CODENVY_VERSION=$CODENVY_IMAGE_VERSION
  fi  
}
  
check_mounts() {
  DATA_MOUNT=$(get_container_bind_folder)
  CONFIG_MOUNT=$(get_container_config_folder)
  INSTANCE_MOUNT=$(get_container_instance_folder)
  BACKUP_MOUNT=$(get_container_backup_folder)
  REPO_MOUNT=$(get_container_repo_folder)
   
  TRIAD=""
  if [[ "${CONFIG_MOUNT}" != "not set" ]] && \
     [[ "${INSTANCE_MOUNT}" != "not set" ]] && \
     [[ "${BACKUP_MOUNT}" != "not set" ]]; then
     TRIAD="set"
  fi

  if [[ "${DATA_MOUNT}" != "not set" ]]; then
    DEFAULT_CODENVY_CONFIG="${DATA_MOUNT}"/config
    DEFAULT_CODENVY_INSTANCE="${DATA_MOUNT}"/instance
    DEFAULT_CODENVY_BACKUP="${DATA_MOUNT}"/backup
  elif [[ "${DATA_MOUNT}" = "not set" ]] && [[ "$TRIAD" = "set" ]]; then  
    DEFAULT_CODENVY_CONFIG="${CONFIG_MOUNT}"
    DEFAULT_CODENVY_INSTANCE="${INSTANCE_MOUNT}"
    DEFAULT_CODENVY_BACKUP="${BACKUP_MOUNT}"
  else
    printf "${GREEN}INFO:${NC} Welcome to Codenvy!\n"
    printf "${GREEN}INFO:${NC} \n"
    printf "${GREEN}INFO:${NC} We did not detect a host mounted data directory.\n"
    printf "${GREEN}INFO:${NC} \n"
    printf "${GREEN}INFO:${NC} Rerun with a single path:\n"
    printf "${GREEN}INFO:${NC}   docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock\n"
    printf "${GREEN}INFO:${NC}                       -v <local-path>:/codenvy\n"
    printf "${GREEN}INFO:${NC}                          codenvy/cli:${CODENVY_VERSION} $@\n"
    printf "${GREEN}INFO:${NC} \n"
    printf "${GREEN}INFO:${NC} \n"
    printf "${GREEN}INFO:${NC} Or rerun with paths for config, instance, and backup (all required):\n"
    printf "${GREEN}INFO:${NC}   docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock\n"
    printf "${GREEN}INFO:${NC}                       -v <local-config-path>:/codenvy/config\n"
    printf "${GREEN}INFO:${NC}                       -v <local-instance-path>:/codenvy/instance\n"
    printf "${GREEN}INFO:${NC}                       -v <local-backup-path>:/codenvy/backup\n"
    printf "${GREEN}INFO:${NC}                          codenvy/cli:${CODENVY_VERSION} $@\n"
    return 2;
  fi

  # if CONFIG_MOUNT && INSTANCE_MOUNT both set, then use those values.
  #   Set offline to CONFIG_MOUNT
  CODENVY_HOST_CONFIG=${CODENVY_CONFIG:-${DEFAULT_CODENVY_CONFIG}}
  CODENVY_CONTAINER_CONFIG="/codenvy/config"

  CODENVY_HOST_INSTANCE=${CODENVY_INSTANCE:-${DEFAULT_CODENVY_INSTANCE}}
  CODENVY_CONTAINER_INSTANCE="/codenvy/instance"

  CODENVY_HOST_BACKUP=${CODENVY_BACKUP:-${DEFAULT_CODENVY_BACKUP}}
  CODENVY_CONTAINER_BACKUP="/codenvy/backup"

  ### DEV MODE VARIABLES
  CODENVY_DEVELOPMENT_MODE="off"
  if [[ "${REPO_MOUNT}" != "not set" ]]; then 
    CODENVY_DEVELOPMENT_MODE="on"
    CODENVY_HOST_DEVELOPMENT_REPO="${REPO_MOUNT}"
    CODENVY_CONTAINER_DEVELOPMENT_REPO="/repo"

    DEFAULT_CODENVY_DEVELOPMENT_TOMCAT="assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone"
    CODENVY_DEVELOPMENT_TOMCAT="${CODENVY_HOST_INSTANCE}/dev"

    if [[ ! -d "${CODENVY_CONTAINER_DEVELOPMENT_REPO}"  ]] || [[ ! -d "${CODENVY_CONTAINER_DEVELOPMENT_REPO}/assembly" ]]; then
      printf "${GREEN}INFO:${NC} Welcome to Codenvy!\n"
      printf "${GREEN}INFO:${NC} \n"
      printf "${GREEN}INFO:${NC} You volume mounted :/repo, but we did not detect a valid Codenvy source repo.\n"
      printf "${GREEN}INFO:${NC} \n"
      printf "${GREEN}INFO:${NC} Rerun with a single path:\n"
      printf "${GREEN}INFO:${NC}   docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock\n"
      printf "${GREEN}INFO:${NC}                       -v <local-path>:/codenvy\n"
      printf "${GREEN}INFO:${NC}                       -v <local-repo>:/repo\n"
      printf "${GREEN}INFO:${NC}                          codenvy/cli:${CODENVY_VERSION} $@\n"
      printf "${GREEN}INFO:${NC} \n"
      printf "${GREEN}INFO:${NC} \n"
      printf "${GREEN}INFO:${NC} Or rerun with paths for config, instance, and backup (all required):\n"
      printf "${GREEN}INFO:${NC}   docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock\n"
      printf "${GREEN}INFO:${NC}                       -v <local-config-path>:/codenvy/config\n"
      printf "${GREEN}INFO:${NC}                       -v <local-instance-path>:/codenvy/instance\n"
      printf "${GREEN}INFO:${NC}                       -v <local-backup-path>:/codenvy/backup\n"
      printf "${GREEN}INFO:${NC}                       -v <local-repo>:/repo\n"
      printf "${GREEN}INFO:${NC}                          codenvy/cli:${CODENVY_VERSION} $@\n"
      return 2
    fi
    if [[ ! -d $(echo "${CODENVY_CONTAINER_DEVELOPMENT_REPO}"/"${DEFAULT_CODENVY_DEVELOPMENT_TOMCAT}"-*/) ]]; then
      printf "${GREEN}INFO:${NC} Welcome to Codenvy!\n"
      printf "${GREEN}INFO:${NC} \n"
      printf "${GREEN}INFO:${NC} You volume mounted a valid Codenvy repo to :/repo, but we could not find a Tomcat assembly.\n"
      printf "${GREEN}INFO:${NC} Have you built /assembly/onpremises-ide-packaging-tomcat-codenvy-allinone?\n"
      return 2
    fi
  fi
}

init_logging() {
  # Initialize CLI folder
  CLI_DIR="${CODENVY_CONTAINER_INSTANCE}/logs/cli"
  test -d "${CLI_DIR}" || mkdir -p "${CLI_DIR}"

  # Ensure logs folder exists
  LOGS="${CLI_DIR}/cli.log"

  # Log date of CLI execution
  log "$(date)"
}


get_container_bind_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder ":/codenvy" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}

get_container_config_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder ":/codenvy/config" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}

get_container_instance_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder ":/codenvy/instance" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}

get_container_backup_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder ":/codenvy/backup" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}

get_container_repo_folder() {
  THIS_CONTAINER_ID=$(get_this_container_id)
  FOLDER=$(get_container_host_bind_folder ":/repo" $THIS_CONTAINER_ID)
  echo "${FOLDER:=not set}"
}

get_this_container_id() {
  hostname
}

get_container_host_bind_folder() {
  # BINDS in the format of var/run/docker.sock:/var/run/docker.sock <path>:/codenvy  
  BINDS=$(docker inspect --format="{{.HostConfig.Binds}}" "${2}" | cut -d '[' -f 2 | cut -d ']' -f 1)
  
  # Remove /var/run/docker.sock:/var/run/docker.sock
  VALUE=${BINDS/\/var\/run\/docker\.sock\:\/var\/run\/docker\.sock/}

  # Remove leading and trailing spaces
  VALUE2=$(echo "${VALUE}" | xargs)

  # Remove $1 from the end
# VALUE3=${VALUE2%$1}

  # What is left is the mount path
#  echo $VALUE3

  MOUNT=""
  IFS=$' '
  for SINGLE_BIND in $VALUE2; do
    case $SINGLE_BIND in
      *$1)
        MOUNT="${MOUNT} ${SINGLE_BIND}"
        echo "${MOUNT}" | cut -f1 -d":" | xargs
      ;;
      *)
        if [[ ${SINGLE_BIND} != *":"* ]]; then
          MOUNT="${MOUNT} ${SINGLE_BIND}"
        else
          MOUNT=""
        fi
      ;;
    esac
  done
}

docker_run() {
  debug $FUNCNAME
  # Setup options for connecting to docker host
  if [ -z "${DOCKER_HOST+x}" ]; then
      DOCKER_HOST="/var/run/docker.sock"
  fi

  if [ -S "$DOCKER_HOST" ]; then
    docker run --rm -v $DOCKER_HOST:$DOCKER_HOST \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  else
    docker run --rm -e DOCKER_HOST -e DOCKER_TLS_VERIFY -e DOCKER_CERT_PATH \
                    -v $HOME:$HOME \
                    -w "$(pwd)" "$@"
  fi
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

curl() {
  if ! has_curl; then
    log "docker run --rm --net=host appropriate/curl \"$@\""
    docker run --rm --net=host appropriate/curl "$@"
  else
    log "$(which curl) \"$@\""
    $(which curl) "$@"
  fi
}

init() {
  init_constants

  if [[ $# == 0 ]]; then
    usage;
  fi

  # Make sure Docker is working and we have /var/run/docker.sock mounted or valid DOCKER_HOST
  check_docker "$@"

  # Only verify mounts after Docker is confirmed to be working.
  check_mounts "$@"

  # Only initialize after mounts have been established so we can write cli.log out to a mount folder
  init_logging "$@"

  if [[ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]]; then
     # Use the CLI that is inside the repository.  
     source /repo/dockerfiles/cli/cli.sh
  else
     # Use the CLI that is inside the container.  
    source /cli/cli.sh
  fi
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
cli_cli "$@"
