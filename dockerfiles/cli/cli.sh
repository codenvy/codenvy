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

cli_init() {

  grab_offline_images
  grab_initial_images
  check_host_volume_mount

  DEFAULT_CODENVY_CLI_ACTION="help"
  CODENVY_CLI_ACTION=${CODENVY_CLI_ACTION:-${DEFAULT_CODENVY_CLI_ACTION}}

  init_host_ip
  DEFAULT_CODENVY_HOST=$GLOBAL_HOST_IP
  CODENVY_HOST=${CODENVY_HOST:-${DEFAULT_CODENVY_HOST}}

  if [[ "${CODENVY_HOST}" = "" ]]; then
      info "Welcome to Codenvy!"
      info ""
      info "We did not auto-detect a valid HOST or IP address."
      info "Pass CODENVY_HOST with your hostname or IP address."
      info ""
      info "Rerun the CLI:"
      info "  docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock"
      info "                      -v <local-path>:/codenvy"
      info "                      -e CODENVY_HOST=<your-ip-or-host>"
      info "                         codenvy/cli:${CODENVY_VERSION} $@"
      return 2;
  fi

  CODENVY_VERSION_FILE="codenvy.ver"
  CODENVY_ENVIRONMENT_FILE="codenvy.env"
  CODENVY_COMPOSE_FILE="docker-compose-container.yml"
  CODENVY_SERVER_CONTAINER_NAME="codenvy_codenvy_1"
  CODENVY_CONFIG_BACKUP_FILE_NAME="codenvy_config_backup.tar"
  CODENVY_INSTANCE_BACKUP_FILE_NAME="codenvy_instance_backup.tar"
  DOCKER_CONTAINER_NAME_PREFIX="codenvy_"

  REFERENCE_HOST_ENVIRONMENT_FILE="${CODENVY_HOST_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
  REFERENCE_HOST_COMPOSE_FILE="${CODENVY_HOST_INSTANCE}/${CODENVY_COMPOSE_FILE}"
  REFERENCE_CONTAINER_ENVIRONMENT_FILE="${CODENVY_CONTAINER_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
  REFERENCE_CONTAINER_COMPOSE_FILE="${CODENVY_CONTAINER_INSTANCE}/${CODENVY_COMPOSE_FILE}"

  CODENVY_MANIFEST_DIR="/version"
  CODENVY_OFFLINE_FOLDER="/codenvy/backup"

  CODENVY_HOST_CONFIG_MANIFESTS_FOLDER="$CODENVY_HOST_CONFIG/manifests"
  CODENVY_CONTAINER_CONFIG_MANIFESTS_FOLDER="$CODENVY_CONTAINER_CONFIG/manifests"

  CODENVY_HOST_CONFIG_MODULES_FOLDER="$CODENVY_HOST_CONFIG/modules"
  CODENVY_CONTAINER_CONFIG_MODULES_FOLDER="$CODENVY_CONTAINER_CONFIG/modules"

  # TODO: Change this to use the current folder or perhaps ~?
  if is_boot2docker && has_docker_for_windows_client; then
    if [[ "${CODENVY_HOST_INSTANCE,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_HOST_INSTANCE=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_INSTANCE set to $CODENVY_HOST_INSTANCE"
    fi
    if [[ "${CODENVY_HOST_CONFIG,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_HOST_CONFIG=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_CONFIG set to $CODENVY_HOST_CONFIG"
    fi
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
  if [ "$(docker images -q alpine:3.4 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image alpine:3.4"
    log "docker pull alpine:3.4 >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull alpine:3.4 >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image alpine:3.4 unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi

  if [ "$(docker images -q appropriate/curl 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image appropriate/curl:latest"
    log "docker pull appropriate/curl:latest >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull appropriate/curl >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image appropriate/curl:latest unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi

  if [ "$(docker images -q eclipse/che-ip:nightly 2> /dev/null)" = "" ]; then
    info "cli" "Pulling image eclipse/che-ip:nightly"
    log "docker pull eclipse/che-ip:nightly >> \"${LOGS}\" 2>&1"
    TEST=""
    docker pull eclipse/che-ip:nightly >> "${LOGS}" 2>&1 || TEST=$?
    if [ "$TEST" = "1" ]; then
      error "Image eclipse/che-ip:nightly unavailable. Not on dockerhub or built locally."
      return 2;
    fi
  fi
}

check_host_volume_mount() {
  echo 'test' > /codenvy/test > "${LOGS}" 2>&1
  
  if [[ ! -f /codenvy/test ]]; then
    error "Docker installed, but unable to write files to your host."
    error "Have you enabled Docker to allow mounting host directories?"
    error "Did our CLI not have user rights to create files on your host?"
    return 2;
  fi

  rm -rf /codenvy/test 
}

cli_parse () {
  debug $FUNCNAME
  if [ $# -eq 0 ]; then
    CHE_CLI_ACTION="help"
  else
    case $1 in
      version|init|config|start|stop|restart|destroy|rmi|config|upgrade|download|backup|restore|offline|update|add-node|remove-node|list-nodes|info|network|debug|help|-h|--help)
        CHE_CLI_ACTION=$1
      ;;
      *)
        # unknown option
        error "You passed an unknown command line option."
        return 1;
      ;;
    esac
  fi
}

cli_cli() {
  case ${CHE_CLI_ACTION} in
    download)
      shift
      cmd_download "$@"
    ;;
    init)
      shift
      cmd_init "$@"
    ;;
    config)
      shift
      cmd_config "$@"
    ;;
    start)
      shift
      cmd_start "$@"
    ;;
    stop)
      shift
      cmd_stop "$@"
    ;;
    restart)
      shift
      cmd_restart "$@"
    ;;
    destroy)
      shift
      cmd_destroy "$@"
    ;;
    rmi)
      shift
      cmd_rmi "$@"
    ;;
    upgrade)
      shift
      cmd_upgrade "$@"
    ;;
    version)
      shift
      cmd_version "$@"
    ;;
    backup)
      shift
      cmd_backup "$@"
    ;;
    restore)
      shift
      cmd_restore "$@"
    ;;
    offline)
      shift
      cmd_offline
    ;;
    info)
      shift
      cmd_info "$@"
    ;;
    debug)
      shift
      cmd_debug "$@"
    ;;
    network)
      shift
      cmd_network "$@"
    ;;
    add-node)
      shift
      cmd_add_node
    ;;
    remove-node)
      shift
      cmd_remove_node "$@"
    ;;
    list-nodes)
      shift
      cmd_list_nodes
    ;;
    help)
      usage
    ;;
  esac
}

get_mount_path() {
  debug $FUNCNAME
  FULL_PATH=$(get_full_path "${1}")
  POSIX_PATH=$(convert_windows_to_posix "${FULL_PATH}")
  CLEAN_PATH=$(get_clean_path "${POSIX_PATH}")
  echo $CLEAN_PATH
}

get_full_path() {
  debug $FUNCNAME
  # create full directory path
  echo "$(cd "$(dirname "${1}")"; pwd)/$(basename "$1")"
}

convert_windows_to_posix() {
  debug $FUNCNAME
  echo "/"$(echo "$1" | sed 's/\\/\//g' | sed 's/://')
}

convert_posix_to_windows() {
  debug $FUNCNAME
  # Remove leading slash
  VALUE="${1:1}"

  # Get first character (drive letter)
  VALUE2="${VALUE:0:1}"

  # Replace / with \
  VALUE3=$(echo ${VALUE} | tr '/' '\\' | sed 's/\\/\\\\/g')

  # Replace c\ with c:\ for drive letter
  echo "$VALUE3" | sed "s/./$VALUE2:/1"
}

get_clean_path() {
  debug $FUNCNAME
  INPUT_PATH=$1
  # \some\path => /some/path
  OUTPUT_PATH=$(echo ${INPUT_PATH} | tr '\\' '/')
  # /somepath/ => /somepath
  OUTPUT_PATH=${OUTPUT_PATH%/}
  # /some//path => /some/path
  OUTPUT_PATH=$(echo ${OUTPUT_PATH} | tr -s '/')
  # "/some/path" => /some/path
  OUTPUT_PATH=${OUTPUT_PATH//\"}
  echo ${OUTPUT_PATH}
}

get_docker_host_ip() {
  debug $FUNCNAME
#  case $(get_docker_install_type) in
#   boot2docker)
#     NETWORK_IF="eth1"
#   ;;
#   native)
#     NETWORK_IF="docker0"
#   ;;
#   *)
#     NETWORK_IF="eth0"
#   ;;
#  esac

  echo $GLOBAL_HOST_IP
}

get_docker_install_type() {
  debug $FUNCNAME
  if is_boot2docker; then
    echo "boot2docker"
  elif is_docker_for_windows; then
    echo "docker4windows"
  elif is_docker_for_mac; then
    echo "docker4mac"
  else
    echo "native"
  fi
}


has_docker_for_windows_client(){
  debug $FUNCNAME
  if [[ $(get_docker_host_ip) = "10.0.75.2" ]]; then
    return 0
  else
    return 1
  fi
}

is_boot2docker() {
  debug $FUNCNAME
  if uname -r | grep -q 'boot2docker'; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  debug $FUNCNAME
  if uname -r | grep -q 'moby' && ! has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_native() {
  debug $FUNCNAME
  if [ $(get_docker_install_type) = "native" ]; then
    return 0
  else
    return 1
  fi
}


has_env_variables() {
  debug $FUNCNAME
  PROPERTIES=$(env | grep CODENVY_)

  if [ "$PROPERTIES" = "" ]; then
    return 1
  else
    return 0
  fi
}

update_image_if_not_found() {
  debug $FUNCNAME

  text "${GREEN}INFO:${NC} (${CHE_MINI_PRODUCT_NAME} download): Checking for image '$1'..."
  CURRENT_IMAGE=$(docker images -q "$1")
  if [ "${CURRENT_IMAGE}" == "" ]; then
    text "not found\n"
    update_image $1
  else
    text "found\n"
  fi
}

update_image() {
  debug $FUNCNAME

  if [ "${1}" == "--force" ]; then
    shift
    info "download" "Removing image $1"
    log "docker rmi -f $1 >> \"${LOGS}\""
    docker rmi -f $1 >> "${LOGS}" 2>&1 || true
  fi

  if [ "${1}" == "--pull" ]; then
    shift
  fi

  info "download" "Pulling image $1"
  text "\n"
  log "docker pull $1 >> \"${LOGS}\" 2>&1"
  TEST=""
  docker pull $1 || TEST=$?
  if [ "$TEST" = "1" ]; then
    error "Image $1 unavailable. Not on dockerhub or built locally."
    return 2;
  fi
  text "\n"
}

port_open(){

#  log "netstat -an | grep 0.0.0.0:$1 >> \"${LOGS}\" 2>&1"
#  netstat -an | grep 0.0.0.0:$1 >> "${LOGS}" 2>&1
#  docker run --rm --net host alpine netstat -an | grep ${CODENVY_HOST}:$1 >> "${LOGS}" 2>&1

  docker run -d -p $1:$1 --name fake alpine:3.4 httpd -f -p $1 -h /etc/ > /dev/null 2>&1
  NETSTAT_EXIT=$?
  docker rm -f fake > /dev/null 2>&1

  if [ $NETSTAT_EXIT = 125 ]; then
    return 1
  else
    return 0
  fi
}

container_exist_by_name(){
  docker inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker inspect -f '{{.Id}}' ${1}"
  docker inspect -f '{{.Id}}' ${1}
}

wait_until_container_is_running() {
  CONTAINER_START_TIMEOUT=${1}

  ELAPSED=0
  until container_is_running ${2} || [ ${ELAPSED} -eq "${CONTAINER_START_TIMEOUT}" ]; do
    log "sleep 1"
    sleep 1
    ELAPSED=$((ELAPSED+1))
  done
}

container_is_running() {
  if [ "$(docker ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
    return 1
  else
    return 0
  fi
}

wait_until_server_is_booted () {
  SERVER_BOOT_TIMEOUT=${1}

  ELAPSED=0
  until server_is_booted ${2} || [ ${ELAPSED} -eq "${SERVER_BOOT_TIMEOUT}" ]; do
    log "sleep 2"
    sleep 2
    # Total hack - having to restart haproxy for some reason on windows
    if is_docker_for_windows || is_docker_for_mac; then
      log "docker restart codenvy_haproxy_1 >> \"${LOGS}\" 2>&1"
      docker restart codenvy_haproxy_1 >> "${LOGS}" 2>&1
    fi
    ELAPSED=$((ELAPSED+1))
  done
}

server_is_booted() {
  HTTP_STATUS_CODE=$(curl -I -k $CODENVY_HOST/api/ \
                     -s -o "${LOGS}" --write-out "%{http_code}")
  if [[ "${HTTP_STATUS_CODE}" = "200" ]] || [[ "${HTTP_STATUS_CODE}" = "302" ]]; then
    return 0
  else
    return 1
  fi
}

check_if_booted() {
  CURRENT_CODENVY_SERVER_CONTAINER_ID=$(get_server_container_id $CODENVY_SERVER_CONTAINER_NAME)
  wait_until_container_is_running 20 ${CURRENT_CODENVY_SERVER_CONTAINER_ID}
  if ! container_is_running ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for ${CHE_MINI_PRODUCT_NAME} container to start."
    return 2
  fi

  info "start" "Server logs at \"docker logs -f ${CODENVY_SERVER_CONTAINER_NAME}\""
  info "start" "Server booting..."
  wait_until_server_is_booted 60 ${CURRENT_CODENVY_SERVER_CONTAINER_ID}

  if server_is_booted ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
    info "start" "Booted and reachable"
    info "start" "Ver: $(get_installed_version)"
    if ! is_docker_for_mac; then
      info "start" "Use: http://${CODENVY_HOST}"
      info "start" "API: http://${CODENVY_HOST}/swagger"
    else
      info "start" "Use: http://localhost"
      info "start" "API: http://localhost/swagger"
    fi
  else
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CODENVY_SERVER_CONTAINER_NAME}\" to inspect the issue."
    return 2
  fi
}

#TODO - is_initialized will return as initialized with empty directories
is_initialized() {
  debug $FUNCNAME
  if [[ -d "${CODENVY_CONTAINER_CONFIG_MANIFESTS_FOLDER}" ]] && \
     [[ -d "${CODENVY_CONTAINER_CONFIG_MODULES_FOLDER}" ]] && \
     [[ -f "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" ]] && \
     [[ -f "${CODENVY_CONTAINER_CONFIG}/${CODENVY_VERSION_FILE}" ]]; then
    return 0
  else
    return 1
  fi
}

has_version_registry() {
  if [ -d /version/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in /version/* ; do
    text " ${version##*/}\n"
  done
}

version_error(){
  text "\nWe could not find version '$1'. Available versions:\n"
  list_versions
  text "\nSet CODENVY_VERSION=<version> and rerun.\n\n"
}

### Returns the list of Codenvy images for a particular version of Codenvy
### Sets the images as environment variables after loading from file
get_image_manifest() {
  info "cli" "Checking registry for version '$1' images"
  if ! has_version_registry $1; then
    version_error $1
    return 1;
  fi

  IMAGE_LIST=$(cat /version/$1/images)
  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    log "eval $SINGLE_IMAGE"
    eval $SINGLE_IMAGE
  done
}

can_upgrade() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)

    # Loop through and find all matching versions
    if [[ "${VER}" == "${1}" ]]; then
      if [[ "${UPG}" == "${2}" ]]; then
        return 0
      fi
    fi
  done < "$CODENVY_MANIFEST_DIR"/upgrades

  return 1
}

print_upgrade_manifest() {
  #  4.7.2 -> 5.0.0-M2-SNAPSHOT  <insert-syntax>
  #  4.7.2 -> 4.7.3              <insert-syntax>
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    UPG=$(echo $line | cut -d ' ' -f2)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CODENVY_MANIFEST_DIR"/upgrades
}

print_version_manifest() {
  while IFS='' read -r line || [[ -n "$line" ]]; do
    VER=$(echo $line | cut -d ' ' -f1)
    CHA=$(echo $line | cut -d ' ' -f2)
    UPG=$(echo $line | cut -d ' ' -f3)
    text "  "
    text "%s" $VER
    for i in `seq 1 $((25-${#VER}))`; do text " "; done
    text "%s" $CHA
    for i in `seq 1 $((18-${#CHA}))`; do text " "; done
    text "%s" $UPG
    text "\n"
  done < "$CODENVY_MANIFEST_DIR"/versions
}

get_installed_version() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CODENVY_CONTAINER_CONFIG}"/$CODENVY_VERSION_FILE
  fi
}

get_installed_installdate() {
  if ! is_initialized; then
    echo "<not-installed>"
  else
    cat "${CODENVY_CONFIG}"/$CODENVY_VERSION_FILE
  fi
}

# Usage:
#   confirm_operation <Warning message> [--force|--no-force]
confirm_operation() {
  debug $FUNCNAME

  FORCE_OPERATION=${2:-"--no-force"}

  if [ ! "${FORCE_OPERATION}" == "--quiet" ]; then
    # Warn user with passed message
    info "${1}"
    text "\n"
    read -p "      Are you sure? [N/y] " -n 1 -r
    text "\n\n"
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
      return 1;
    else
      return 0;
    fi
  fi
}

# Runs puppet image to generate codenvy configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME

  if is_docker_for_windows; then
    REGISTRY_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/registry/registry.env")
    POSTGRES_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/postgres/postgres.env")
    CODENVY_ENV_FILE=$(convert_posix_to_windows "${CODENVY_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env")
  else
    REGISTRY_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/registry/registry.env"
    POSTGRES_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/postgres/postgres.env"
    CODENVY_ENV_FILE="${CODENVY_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env"
  fi
  # Note - bug in docker requires relative path for env, not absolute
  log "docker_run -it --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CODENVY_VERSION/images \
                  -v \"${CODENVY_HOST_INSTANCE}\":/opt/codenvy:rw \
                  -v \"${CODENVY_HOST_CONFIG_MANIFESTS_FOLDER}\":/etc/puppet/manifests:ro \
                  -v \"${CODENVY_HOST_CONFIG_MODULES_FOLDER}\":/etc/puppet/modules:ro \
                  -e "REGISTRY_ENV_FILE=${REGISTRY_ENV_FILE}" \
                  -e "POSTGRES_ENV_FILE=${POSTGRES_ENV_FILE}" \
                  -e "CODENVY_ENV_FILE=${CODENVY_ENV_FILE}" \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/codenvy.pp --show_diff \"$@\""
  docker_run -it  --env-file="${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" \
                  --env-file=/version/$CODENVY_VERSION/images \
                  -v "${CODENVY_HOST_INSTANCE}":/opt/codenvy:rw \
                  -v "${CODENVY_HOST_CONFIG_MANIFESTS_FOLDER}":/etc/puppet/manifests:ro \
                  -v "${CODENVY_HOST_CONFIG_MODULES_FOLDER}":/etc/puppet/modules:ro \
                  -e "REGISTRY_ENV_FILE=${REGISTRY_ENV_FILE}" \
                  -e "POSTGRES_ENV_FILE=${POSTGRES_ENV_FILE}" \
                  -e "CODENVY_ENV_FILE=${CODENVY_ENV_FILE}" \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/codenvy.pp --show_diff "$@"
}

# return date in format which can be used as a unique file or dir name
# example 2016-10-31-1477931458
get_current_date() {
    date +'%Y-%m-%d-%s'
}

###########################################################################
### END HELPER FUNCTIONS
###
### START CLI COMMANDS
###########################################################################
cmd_download() {
  FORCE_UPDATE=${1:-"--no-force"}

  get_image_manifest $CODENVY_VERSION

  IFS=$'\n'
  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    if [[ $FORCE_UPDATE == "--force" ]] ||
       [[ $FORCE_UPDATE == "--pull" ]]; then
      update_image $FORCE_UPDATE $VALUE_IMAGE
    else
      update_image_if_not_found $VALUE_IMAGE
    fi
  done
}

cmd_init() {
  FORCE_UPDATE=${1:-"--no-force"}
  if [ "${FORCE_UPDATE}" == "--no-force" ]; then
    # If codenvy.environment file exists, then fail
    if is_initialized; then
      info "init" "Already initialized."
      return 1
    fi
  fi

  cmd_download $FORCE_UPDATE

  if [ -z ${IMAGE_INIT+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  info "init" "Installing configuration and bootstrap variables:"
  log "mkdir -p \"${CODENVY_CONTAINER_CONFIG}\""
  mkdir -p "${CODENVY_CONTAINER_CONFIG}"
  log "mkdir -p \"${CODENVY_CONTAINER_INSTANCE}\""
  mkdir -p "${CODENVY_CONTAINER_INSTANCE}"

  if [ ! -w "${CODENVY_CONTAINER_CONFIG}" ]; then
    error "CODENVY_CONTAINER_CONFIG is not writable. Aborting."
    return 1;
  fi

  if [ ! -w "${CODENVY_CONTAINER_INSTANCE}" ]; then
    error "CODENVY_CONTAINER_INSTANCE is not writable. Aborting."
    return 1;
  fi

  # in development mode we use init files from repo otherwise we use it from docker image
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    docker_run -v "${CODENVY_HOST_CONFIG}":/copy \
               -v "${CODENVY_HOST_DEVELOPMENT_REPO}":/files \
                   $IMAGE_INIT
  else
    docker_run -v "${CODENVY_HOST_CONFIG}":/copy $IMAGE_INIT
  fi

  # After initialization, add codenvy.env with self-discovery.
  sed -i'.bak' "s|#CODENVY_HOST=.*|CODENVY_HOST=${CODENVY_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_HOST=${CODENVY_HOST}"

  sed -i'.bak' "s|#CODENVY_VERSION=.*|CODENVY_VERSION=${CODENVY_VERSION}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_VERSION=${CODENVY_VERSION}"
  sed -i'.bak' "s|#CODENVY_CONFIG=.*|CODENVY_CONFIG=${CODENVY_HOST_CONFIG}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_CONFIG=${CODENVY_HOST_CONFIG}"
  sed -i'.bak' "s|#CODENVY_INSTANCE=.*|CODENVY_INSTANCE=${CODENVY_HOST_INSTANCE}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_INSTANCE=${CODENVY_HOST_INSTANCE}"
  sed -i'.bak' "s|#CODENVY_SWARM_NODES=.*|CODENVY_SWARM_NODES=${CODENVY_HOST}:23750|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"

  if [ "${CODENVY_DEVELOPMENT_MODE}" == "on" ]; then
    sed -i'.bak' "s|#CODENVY_ENVIRONMENT=.*|CODENVY_ENVIRONMENT=development|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_ENVIRONMENT=development"
    sed -i'.bak' "s|#CODENVY_DEVELOPMENT_REPO=.*|CODENVY_DEVELOPMENT_REPO=${CODENVY_HOST_DEVELOPMENT_REPO}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_DEVELOPMENT_REPO=${CODENVY_HOST_DEVELOPMENT_REPO}"
    sed -i'.bak' "s|#CODENVY_DEVELOPMENT_TOMCAT=.*|CODENVY_DEVELOPMENT_TOMCAT=${CODENVY_DEVELOPMENT_TOMCAT}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_DEVELOPMENT_TOMCAT=${CODENVY_DEVELOPMENT_TOMCAT}"
  else
    sed -i'.bak' "s|#CODENVY_ENVIRONMENT=.*|CODENVY_ENVIRONMENT=production|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_ENVIRONMENT=production"
  fi

  rm -rf "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}".bak > /dev/null 2>&1

  # Write the Codenvy version to codenvy.ver
  echo "$CODENVY_VERSION" > "${CODENVY_CONTAINER_CONFIG}/${CODENVY_VERSION_FILE}"
}

cmd_config() {

  # If the system is not initialized, initalize it.
  # If the system is already initialized, but a user wants to update images, then re-download.
  FORCE_UPDATE=${1:-"--no-force"}
  if ! is_initialized; then
    cmd_init $FORCE_UPDATE
  elif [[ "${FORCE_UPDATE}" == "--pull" ]] || \
       [[ "${FORCE_UPDATE}" == "--force" ]]; then
    cmd_download $FORCE_UPDATE
  fi

  # If the CODENVY_VERSION set by an environment variable does not match the value of
  # the codenvy.ver file of the installed instance, then do not proceed as there is a
  # confusion between what the user has set and what the instance expects.
  INSTALLED_VERSION=$(get_installed_version)
  if [[ $CODENVY_VERSION != $INSTALLED_VERSION ]]; then
    info "config" "CODENVY_VERSION=$CODENVY_VERSION does not match ${CODENVY_ENVIRONMENT_FILE}=$INSTALLED_VERSION. Aborting."
    info "config" "This happens if the <version> of your Docker image is different from ${CODENVY_HOST_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
    return 1
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  # Development mode
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    # if dev mode is on, pick configuration sources from repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_run -v "${CODENVY_HOST_CONFIG}":/copy \
               -v "${CODENVY_HOST_DEVELOPMENT_REPO}":/files \
                  $IMAGE_INIT

    # in development mode to avoid permissions issues we copy tomcat assembly to ${CODENVY_INSTANCE}
    # if codenvy development tomcat exist we remove it
    if [[ -d "${CODENVY_CONTAINER_INSTANCE}/dev" ]]; then
        log "docker_run -v \"${CODENVY_HOST_INSTANCE}/dev\":/root/dev alpine:3.4 sh -c \"rm -rf /root/dev/*\""
        docker_run -v "${CODENVY_HOST_INSTANCE}/dev":/root/dev alpine:3.4 sh -c "rm -rf /root/dev/*"
        log "rm -rf \"${CODENVY_HOST_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CODENVY_CONTAINER_INSTANCE}/dev"
    fi
    # copy codenvy development tomcat to ${CODENVY_INSTANCE} folder
    cp -r "$(get_mount_path $(echo $CODENVY_CONTAINER_DEVELOPMENT_REPO/$DEFAULT_CODENVY_DEVELOPMENT_TOMCAT-*/))" \
        "${CODENVY_CONTAINER_INSTANCE}/dev"
  fi

  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."
  # Run the docker configurator
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    # generate configs and print puppet output logs to console if dev mode is on
    generate_configuration_with_puppet
  else
    generate_configuration_with_puppet >> "${LOGS}"
  fi

  # Replace certain environment file lines with their container counterparts
  info "config" "Customizing docker-compose for running in a container"
  # If this is windows, we need to add a special volume for postgres
  if has_docker_for_windows_client; then
    sed "s|^.*postgresql\/data.*$|\ \ \ \ \ \ \-\ \'codenvy-postgresql-volume\:\/var\/lib\/postgresql\/data\:Z\'|" -i "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    echo "" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "volumes:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "  codenvy-postgresql-volume:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "     external: true" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    # On Windows, it is not possible to volume mount postgres data folder directly
    # This creates a named volume which will store postgres data in docker for win VM
    # TODO - in future, we can write synchronizer utility to copy data from win VM to host
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
  fi
}

cmd_start() {
  debug $FUNCNAME

  # If Codenvy is already started or booted, then terminate early.
  if container_exist_by_name $CODENVY_SERVER_CONTAINER_NAME; then
    CURRENT_CODENVY_SERVER_CONTAINER_ID=$(get_server_container_id $CODENVY_SERVER_CONTAINER_NAME)
    if container_is_running ${CURRENT_CODENVY_SERVER_CONTAINER_ID} && \
       server_is_booted ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
       info "start" "$CHE_MINI_PRODUCT_NAME is already running"
       info "start" "Server logs at \"docker logs -f ${CODENVY_SERVER_CONTAINER_NAME}\""
       info "start" "Ver: $(get_installed_version)"
       if ! is_docker_for_mac; then
         info "start" "Use: http://${CODENVY_HOST}"
         info "start" "API: http://${CODENVY_HOST}/swagger"
       else
         info "start" "Use: http://localhost"
         info "start" "API: http://localhost/swagger"
       fi
       return
    fi
  fi

  # To protect users from accidentally updating their Codenvy servers when they didn't mean
  # to, which can happen if CODENVY_VERSION=latest
  FORCE_UPDATE=${1:-"--no-force"}
  # Always regenerate puppet configuration from environment variable source, whether changed or not.
  # If the current directory is not configured with an .env file, it will initialize
  cmd_config $FORCE_UPDATE

  # Begin tests of open ports that we require
  info "start" "Preflight checks"
  text   "         port 80 (http):       $(port_open 80 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 443 (https):     $(port_open 443 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 5000 (registry): $(port_open 5000 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  if ! $(port_open 80) || ! $(port_open 443) || ! $(port_open 5000); then
    echo ""
    error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program."
    return 1;
  fi
  text "\n"

  # Start Codenvy
  # Note bug in docker requires relative path, not absolute path to compose file
  info "start" "Starting containers..."
  log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME up -d >> \"${LOGS}\" 2>&1"
  docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                 -p=$CHE_MINI_PRODUCT_NAME up -d >> "${LOGS}" 2>&1
  check_if_booted
}

cmd_stop() {
  debug $FUNCNAME

  if [ $# -gt 0 ]; then
    error "${CHE_MINI_PRODUCT_NAME} stop: You passed unknown options. Aborting."
    return
  fi

  info "stop" "Stopping containers..."
  log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME stop >> \"${LOGS}\" 2>&1 || true"
  docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                 -p=$CHE_MINI_PRODUCT_NAME stop >> "${LOGS}" 2>&1 || true
  info "stop" "Removing containers..."
  log "y | docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME rm >> \"${LOGS}\" 2>&1 || true"
  docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                 -p=$CHE_MINI_PRODUCT_NAME rm --force >> "${LOGS}" 2>&1 || true
}

cmd_restart() {
  debug $FUNCNAME

  FORCE_UPDATE=${1:-"--no-force"}
    info "restart" "Restarting..."
    cmd_stop
    cmd_start ${FORCE_UPDATE}
}

cmd_destroy() {
  debug $FUNCNAME

  WARNING="destroy !!! Stopping services and !!! deleting data !!! this is unrecoverable !!!"
  if ! confirm_operation "${WARNING}" "$@"; then
    return;
  fi

  cmd_stop

  info "destroy" "Deleting instance and config..."
  log "docker_run -v \"${CODENVY_HOST_CONFIG}\":/codenvy-config -v \"${CODENVY_HOST_INSTANCE}\":/codenvy-instance alpine:3.4 sh -c \"rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*\""
  docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy-config \
             -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
                alpine:3.4 sh -c "rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*"
  log "rm -rf \"${CODENVY_CONTAINER_CONFIG}\" >> \"${LOGS}\""
  log "rm -rf \"${CODENVY_CONTAINER_INSTANCE}\" >> \"${LOGS}\""
  rm -rf "${CODENVY_CONTAINER_CONFIG}"
  rm -rf "${CODENVY_CONTAINER_INSTANCE}"
  if has_docker_for_windows_client; then
    log "docker volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
  fi
}

cmd_rmi() {
  info "rmi" "Checking registry for version '$CODENVY_VERSION' images"
  if ! has_version_registry $CODENVY_VERSION; then
    version_error $CODENVY_VERSION
    return 1;
  fi

  WARNING="rmi !!! Removing images disables codenvy and forces a pull !!!"
  if ! confirm_operation "${WARNING}" "$@"; then
    return;
  fi

  IMAGE_LIST=$(cat "$CODENVY_MANIFEST_DIR"/$CODENVY_VERSION/images)
  IFS=$'\n'
  info "rmi" "Removing ${CHE_MINI_PRODUCT_NAME} Docker images..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    info "rmi" "Removing $VALUE_IMAGE..."
    log "docker rmi -f ${VALUE_IMAGE} >> \"${LOGS}\" 2>&1 || true"
    docker rmi -f $VALUE_IMAGE >> "${LOGS}" 2>&1 || true
  done
}

cmd_upgrade() {
  debug $FUNCNAME
  info "upgrade" "Not yet implemented"

  if [ $# -eq 0 ]; then
    info "upgrade" "No upgrade target provided. Run '${CHE_MINI_PRODUCT_NAME} version' for a list of upgradeable versions."
    return 2;
  fi

  if ! can_upgrade $(get_installed_version) ${1}; then
    info "upgrade" "Your current version $(get_installed_version) is not upgradeable to $1."
    info "upgrade" "Run '${CHE_MINI_PRODUCT_NAME} version' to see your upgrade options."
    return 2;
  fi

  # If here, this version is validly upgradeable.  You can upgrade from
  # $(get_installed_version) to $1
  echo "remove me -- you entered a version that you can upgrade to"

}

cmd_version() {
  debug $FUNCNAME

  error "!!! this information is experimental - upgrade not yet available !!!"
  echo ""
  text "$CHE_PRODUCT_NAME:\n"
  text "  Version:      %s\n" $(get_installed_version)
  text "  Installed:    %s\n" $(get_installed_installdate)
  text "  CLI version:  $CHE_CLI_VERSION\n"

  if is_initialized; then
    text "\n"
    text "Upgrade Options:\n"
    text "  INSTALLED VERSION        UPRADEABLE TO\n"
    print_upgrade_manifest $(get_installed_version)
  fi

  text "\n"
  text "Available:\n"
  text "  VERSION                  CHANNEL           UPGRADEABLE FROM\n"
  if is_initialized; then
    print_version_manifest $(get_installed_version)
  else
    print_version_manifest $CODENVY_VERSION
  fi
}

cmd_backup() {
  debug $FUNCNAME

  # possibility to skip codenvy projects backup
  SKIP_BACKUP_CODENVY_DATA=${1:-"--no-skip-data"}
  if [[ "${SKIP_BACKUP_CODENVY_DATA}" == "--skip-data" ]]; then
    TAR_EXTRA_EXCLUDE="--exclude=data/codenvy"
  else
    TAR_EXTRA_EXCLUDE=""
  fi

  if [[ ! -d "${CODENVY_CONTAINER_CONFIG}" ]] || \
     [[ ! -d "${CODENVY_CONTAINER_INSTANCE}" ]]; then
    error "Cannot find existing CODENVY_CONFIG or CODENVY_INSTANCE."
    return;
  fi

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "$CHE_MINI_PRODUCT_NAME is running. Stop before performing a backup."
    return 2;
  fi

  if [[ ! -d "${CODENVY_CONTAINER_BACKUP}" ]]; then
    mkdir -p "${CODENVY_CONTAINER_BACKUP}"
  fi

  # check if backups already exist and if so we move it with time stamp in name
  if [[ -f "${CODENVY_CONTAINER_BACKUP}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" ]]; then
    mv "${CODENVY_CONTAINER_BACKUP}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" \
        "${CODENVY_CONTAINER_BACKUP}/moved-$(get_current_date)-${CODENVY_CONFIG_BACKUP_FILE_NAME}"
  fi
  if [[ -f "${CODENVY_CONTAINER_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" ]]; then
    mv "${CODENVY_CONTAINER_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
        "${CODENVY_CONTAINER_BACKUP}/moved-$(get_current_date)-${CODENVY_INSTANCE_BACKUP_FILE_NAME}"
  fi

  info "backup" "Saving configuration..."
  docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy-config \
             -v "${CODENVY_HOST_BACKUP}":/root/backup \
                 alpine:3.4 sh -c "tar czf /root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME} -C /root/codenvy-config ."

  info "backup" "Saving instance data..."
  # if windows we backup data volume
  if has_docker_for_windows_client; then
    docker_run -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
               -v "${CODENVY_HOST_BACKUP}":/root/backup \
               -v codenvy-postgresql-volume:/root/codenvy-instance/data/postgres \
                 alpine:3.4 sh -c "tar czf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance . --exclude=logs ${TAR_EXTRA_EXCLUDE}"
  else
    docker_run -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
              -v "${CODENVY_HOST_BACKUP}":/root/backup \
                 alpine:3.4 sh -c "tar czf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance . --exclude=logs ${TAR_EXTRA_EXCLUDE}"
  fi

  info ""
  info "backup" "Configuration data saved in ${CODENVY_HOST_BACKUP}/${CODENVY_CONFIG_BACKUP_FILE_NAME}"
  info "backup" "Instance data saved in ${CODENVY_HOST_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}"
}

cmd_restore() {
  debug $FUNCNAME

  if [[ -d "${CODENVY_CONTAINER_CONFIG}" ]] || \
     [[ -d "${CODENVY_CONTAINER_INSTANCE}" ]]; then

    WARNING="Restoration overwrites existing configuration and data. Are you sure?"
    if ! confirm_operation "${WARNING}" "$@"; then
      return;
    fi
  fi

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "Codenvy is running. Stop before performing a restore. Aborting"
    return;
  fi

  if [[ ! -f "${CODENVY_CONTAINER_BACKUP}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" ]] || \
     [[ ! -f "${CODENVY_CONTAINER_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" ]]; then
    error "Backup files not found. To do restore please do backup first."
    return;
  fi

  # remove config and instance folders
  log "docker_run -v \"${CODENVY_HOST_CONFIG}\":/codenvy-config \
                  -v \"${CODENVY_HOST_INSTANCE}\":/codenvy-instance \
                    alpine:3.4 sh -c \"rm -rf /root/codenvy-instance/* \
                                   && rm -rf /root/codenvy-config/*\""
  docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy-config \
             -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
                alpine:3.4 sh -c "rm -rf /root/codenvy-instance/* \
                              && rm -rf /root/codenvy-config/*"
  log "rm -rf \"${CODENVY_CONTAINER_CONFIG}\" >> \"${LOGS}\""
  log "rm -rf \"${CODENVY_CONTAINER_INSTANCE}\" >> \"${LOGS}\""
  rm -rf "${CODENVY_CONTAINER_CONFIG}"
  rm -rf "${CODENVY_CONTAINER_INSTANCE}"

  info "restore" "Recovering configuration..."
  mkdir -p "${CODENVY_CONTAINER_CONFIG}"
  docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy-config \
             -v "${CODENVY_HOST_BACKUP}/${CODENVY_CONFIG_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME}" \
             alpine:3.4 sh -c "tar xf /root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME} -C /root/codenvy-config"

  info "restore" "Recovering instance data..."
  mkdir -p "${CODENVY_CONTAINER_INSTANCE}"
  if has_docker_for_windows_client; then
    log "docker volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
    docker_run -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
               -v "${CODENVY_HOST_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
               -v codenvy-postgresql-volume:/root/codenvy-instance/data/postgres \
               alpine:3.4 sh -c "tar xf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance"
  else
    docker_run -v "${CODENVY_HOST_INSTANCE}":/root/codenvy-instance \
               -v "${CODENVY_HOST_BACKUP}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
               alpine:3.4 sh -c "tar xf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance"
  fi
}

cmd_offline() {
  info "offline" "Checking registry for version '$CODENVY_VERSION' images"
  if ! has_version_registry $CODENVY_VERSION; then
    version_error $CODENVY_VERSION
    return 1;
  fi

  # Make sure the images have been pulled and are in your local Docker registry
  cmd_download

  mkdir -p $CODENVY_OFFLINE_FOLDER

  IMAGE_LIST=$(cat "$CODENVY_MANIFEST_DIR"/$CODENVY_VERSION/images)
  IFS=$'\n'
  info "offline" "Saving ${CHE_MINI_PRODUCT_NAME} Docker images as tar files..."

  for SINGLE_IMAGE in $IMAGE_LIST; do
    VALUE_IMAGE=$(echo $SINGLE_IMAGE | cut -d'=' -f2)
    TAR_NAME=$(echo $VALUE_IMAGE | sed "s|\/|_|")
    info "offline" "Saving $CODENVY_HOST_BACKUP/$TAR_NAME.tar..."
    if ! $(docker save $VALUE_IMAGE > $CODENVY_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CODENVY_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  done

  info "offline" "Done!"
}

cmd_info() {
  debug $FUNCNAME
  if [ $# -eq 0 ]; then
    TESTS="--debug"
  else
    TESTS=$1
  fi

  case $TESTS in
    --all|-all)
      cmd_debug
      cmd_network
    ;;
    --network|-network)
      cmd_network
    ;;
    --debug|-debug)
      cmd_debug
    ;;
    *)
      info "info" "Unknown info flag passed: $1."
      return;
    ;;
  esac
}

cmd_debug() {
  debug $FUNCNAME
  info "---------------------------------------"
  info "------------   CLI INFO   -------------"
  info "---------------------------------------"
  info ""
  info "-----------  CODENVY INFO  ------------"
  info "CODENVY_VERSION           = ${CODENVY_VERSION}"
  info "CODENVY_INSTANCE          = ${CODENVY_HOST_INSTANCE}"
  info "CODENVY_CONFIG            = ${CODENVY_HOST_CONFIG}"
  info "CODENVY_HOST              = ${CODENVY_HOST}"
  info "CODENVY_REGISTRY          = ${CODENVY_MANIFEST_DIR}"
  info "CODENVY_DEVELOPMENT_MODE  = ${CODENVY_DEVELOPMENT_MODE}"
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    info "CODENVY_DEVELOPMENT_REPO  = ${CODENVY_HOST_DEVELOPMENT_REPO}"
  fi
  info "CODENVY_BACKUP            = ${CODENVY_HOST_BACKUP}"
  info ""
  info "-----------  PLATFORM INFO  -----------"
#  info "CLI DEFAULT PROFILE       = $(has_default_profile && echo $(get_default_profile) || echo "not set")"
  info "CLI_VERSION               = ${CHE_CLI_VERSION}"
  info "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  info "IS_NATIVE                 = $(is_native && echo "YES" || echo "NO")"
  info "IS_WINDOWS                = $(has_docker_for_windows_client && echo "YES" || echo "NO")"
  info "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo "YES" || echo "NO")"
  info "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_client && echo "YES" || echo "NO")"
  info "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo "YES" || echo "NO")"
  info "IS_BOOT2DOCKER            = $(is_boot2docker && echo "YES" || echo "NO")"
  info ""
}

cmd_network() {
  debug $FUNCNAME

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  info ""
  info "---------------------------------------"
  info "--------   CONNECTIVITY TEST   --------"
  info "---------------------------------------"
  # Start a fake workspace agent
  log "docker run -d -p 12345:80 --name fakeagent alpine:3.4 httpd -f -p 80 -h /etc/ >> \"${LOGS}\""
  docker run -d -p 12345:80 --name fakeagent alpine:3.4 httpd -f -p 80 -h /etc/ >> "${LOGS}"

  AGENT_INTERNAL_IP=$(docker inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
  AGENT_INTERNAL_PORT=80
  AGENT_EXTERNAL_IP=$CODENVY_HOST
  AGENT_EXTERNAL_PORT=12345


  ### TEST 1: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I localhost:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent (localhost): Connection succeeded"
  else
      info "Browser    => Workspace Agent (localhost): Connection failed"
  fi

  ### TEST 1a: Simulate browser ==> workspace agent HTTP connectivity
  HTTP_CODE=$(curl -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                          -s -o "${LOGS}" --connect-timeout 5 \
                          --write-out "%{http_code}") || echo "28" >> "${LOGS}"

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection succeeded"
  else
      info "Browser    => Workspace Agent ($AGENT_EXTERNAL_IP): Connection failed"
  fi

  ### TEST 2: Simulate Che server ==> workspace agent (external IP) connectivity
  export HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                ${IMAGE_CODENVY} \
                                  -I ${AGENT_EXTERNAL_IP}:${AGENT_EXTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (External IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (External IP): Connection failed"
  fi

  ### TEST 3: Simulate Che server ==> workspace agent (internal IP) connectivity
  export HTTP_CODE=$(docker_run --name fakeserver \
                                --entrypoint=curl \
                                ${IMAGE_CODENVY} \
                                  -I ${AGENT_INTERNAL_IP}:${AGENT_INTERNAL_PORT}/alpine-release \
                                  -s -o "${LOGS}" \
                                  --write-out "%{http_code}")

  if [ "${HTTP_CODE}" = "200" ]; then
      info "Server     => Workspace Agent (Internal IP): Connection succeeded"
  else
      info "Server     => Workspace Agent (Internal IP): Connection failed"
  fi

  log "docker rm -f fakeagent >> \"${LOGS}\""
  docker rm -f fakeagent >> "${LOGS}"
}

# Prints command that should be executed on a node to add it to swarm cluster
cmd_add_node() {
  info "add-node" "1. For the node you want to add, verify that Docker is installed."
  info "add-node" "2. Collect the externally accessible IP address or DNS of the node."
  info "add-node" "3. Grab your Codenvy admin user name and password."
  info "add-node" "4. SSH into the remote node and execute:"
  printf "                             ${YELLOW}bash <(curl -sSL http://${CODENVY_HOST}/api/nodes/script) --user <admin-user> --password <admin-pass> --ip <node-ip>${NC}"
  echo ""
  info "add-node" "5. The node will configure itself and update the Codenvy cluster."
}

# Removes node from swarm cluster configuration and restarts swarm container
cmd_remove_node() {
  if [ $# -eq 0 ]; then
    error "No IP provided"
    return 1
  fi

  NODE_IP=${1}
  NODES=$(grep "^CODENVY_SWARM_NODES=" "${CODENVY_CONFIG}/codenvy.env" | sed "s/.*=//")
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
  sed -i'.bak' -e "s|^CODENVY_SWARM_NODES=.*|${REMOVE_NODE}|" "${CODENVY_CONFIG}/codenvy.env"

  # TODO: Restart should not be needed.  Find way to deregister nodes.
  cmd_restart
}

# Shows list of the docker nodes in the swarm cluster
cmd_list_nodes() {
  ## TODO use
  ## - config to get all configured nodes
  ## - instance to get all registered nodes
  ## - call to swarm api to get all enabled nodes
  ## Print output that marks all the nodes in accordance to it state
  cat "${CODENVY_INSTANCE}/config/swarm/node_list"
}
