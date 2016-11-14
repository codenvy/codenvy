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

# Move this into a dedicated function that is only called when the variable is absolutely
# needed. This will speed performance for methods that do not need this value set.
# this is the only place where we call docker instead of docker_exec because docker_exec function
# depends on that GLOBAL_HOST_ARCH
cli_init() {
  DEFAULT_CODENVY_VERSION="nightly"
  DEFAULT_CODENVY_UTILITY_VERSION="nightly"
  DEFAULT_CODENVY_CLI_ACTION="help"
  DEFAULT_CODENVY_DEVELOPMENT_MODE="off"
  DEFAULT_CODENVY_DEVELOPMENT_REPO=$(get_mount_path $PWD)
  DEFAULT_CODENVY_DEVELOPMENT_TOMCAT="assembly/onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone"

  init_host_ip
  DEFAULT_CODENVY_HOST=$GLOBAL_HOST_IP
  CODENVY_HOST=${CODENVY_HOST:-${DEFAULT_CODENVY_HOST}}

  DEFAULT_CODENVY_CONFIG=$(get_mount_path $PWD)/config
  DEFAULT_CODENVY_INSTANCE=$(get_mount_path $PWD)/instance
  DEFAULT_CODENVY_BACKUP_FOLDER=$(get_mount_path $PWD)

  CODENVY_VERSION_FILE="codenvy.ver"
  CODENVY_ENVIRONMENT_FILE="codenvy.env"
  CODENVY_COMPOSE_FILE="docker-compose.yml"
  CODENVY_SERVER_CONTAINER_NAME="codenvy_codenvy_1"
  CODENVY_CONFIG_BACKUP_FILE_NAME="codenvy_config_backup.tar"
  CODENVY_INSTANCE_BACKUP_FILE_NAME="codenvy_instance_backup.tar"
  CHE_GLOBAL_VERSION_IMAGE="codenvy/version"
  DOCKER_CONTAINER_NAME_PREFIX="codenvy_"

  # For some situations, Docker requires a path for volume mount which is posix-based.
  # In other cases, the same file needs to be in windows format
  if has_docker_for_windows_client; then
    CODENVY_CONFIG=$(convert_posix_to_windows $(echo "${CODENVY_CONFIG:-${DEFAULT_CODENVY_CONFIG}}"))
    CODENVY_INSTANCE=$(convert_posix_to_windows $(echo "${CODENVY_INSTANCE:-${DEFAULT_CODENVY_INSTANCE}}"))
    CODENVY_BACKUP_FOLDER=$(convert_posix_to_windows $(echo "${CODENVY_BACKUP_FOLDER:-${DEFAULT_CODENVY_BACKUP_FOLDER}}"))
    REFERENCE_ENVIRONMENT_FILE="${CODENVY_CONFIG}\\\\${CODENVY_ENVIRONMENT_FILE}"
    REFERENCE_COMPOSE_FILE="${CODENVY_INSTANCE}\\\\${CODENVY_COMPOSE_FILE}"
  else
    CODENVY_CONFIG=${CODENVY_CONFIG:-${DEFAULT_CODENVY_CONFIG}}
    CODENVY_INSTANCE=${CODENVY_INSTANCE:-${DEFAULT_CODENVY_INSTANCE}}
    CODENVY_BACKUP_FOLDER=${CODENVY_BACKUP_FOLDER:-${DEFAULT_CODENVY_BACKUP_FOLDER}}
    REFERENCE_ENVIRONMENT_FILE="${CODENVY_CONFIG}/${CODENVY_ENVIRONMENT_FILE}"
    REFERENCE_COMPOSE_FILE="${CODENVY_INSTANCE}/${CODENVY_COMPOSE_FILE}"
  fi

  CODENVY_VERSION=${CODENVY_VERSION:-${DEFAULT_CODENVY_VERSION}}
  CODENVY_UTILITY_VERSION=${CODENVY_UTILITY_VERSION:-${DEFAULT_CODENVY_UTILITY_VERSION}}
  CODENVY_CLI_ACTION=${CODENVY_CLI_ACTION:-${DEFAULT_CODENVY_CLI_ACTION}}
  CODENVY_DEVELOPMENT_MODE=${CODENVY_DEVELOPMENT_MODE:-${DEFAULT_CODENVY_DEVELOPMENT_MODE}}
  CODENVY_DEVELOPMENT_REPO=$(get_mount_path ${CODENVY_DEVELOPMENT_REPO:-${DEFAULT_CODENVY_DEVELOPMENT_REPO}})
  CODENVY_DEVELOPMENT_TOMCAT="${CODENVY_INSTANCE}/dev"

  if [ "${CODENVY_DEVELOPMENT_MODE}" == "on" ]; then
    if [[ ! -d "${CODENVY_DEVELOPMENT_REPO}"  ]] || [[ ! -d "${CODENVY_DEVELOPMENT_REPO}/assembly" ]]; then
      info "cli" "Development mode is on and could not find valid repo or packaged assembly"
      info "cli" "Please launch codenvy.sh from the codenvy repo or set CODENVY_DEVELOPMENT_REPO to the root of your git clone repo"
      return 2
    fi
    if [[ ! -d $(echo "${CODENVY_DEVELOPMENT_REPO}"/"${DEFAULT_CODENVY_DEVELOPMENT_TOMCAT}"-*/) ]]; then
      info "cli" "Development mode is on and could not find valid Tomcat assembly"
      info "cli" "Have you built /assembly/onpremises-ide-packaging-tomcat-codenvy-allinone yet?"
      return 2
    fi
  fi

  CODENVY_MANIFEST_DIR=$(get_mount_path ~/."${CHE_MINI_PRODUCT_NAME}"/manifests)
  CODENVY_OFFLINE_FOLDER=$(get_mount_path $PWD)/offline
  CODENVY_CONFIG_MANIFESTS_FOLDER="$CODENVY_CONFIG/manifests"
  CODENVY_CONFIG_MODULES_FOLDER="$CODENVY_CONFIG/modules"

  # TODO: Change this to use the current folder or perhaps ~?
  if is_boot2docker && has_docker_for_windows_client; then
    if [[ "${CODENVY_INSTANCE,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_INSTANCE=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_INSTANCE set to $CODENVY_INSTANCE"
    fi
    if [[ "${CODENVY_CONFIG,,}" != *"${USERPROFILE,,}"* ]]; then
      CODENVY_CONFIG=$(get_mount_path "${USERPROFILE}/.${CHE_MINI_PRODUCT_NAME}/")
      warning "Boot2docker for Windows - CODENVY_CONFIG set to $CODENVY_CONFIG"
    fi
  fi
}

### Should we load profile before we parse the command line?

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
  case $(get_docker_install_type) in
   boot2docker)
     NETWORK_IF="eth1"
   ;;
   native)
     NETWORK_IF="docker0"
   ;;
   *)
     NETWORK_IF="eth0"
   ;;
  esac

  log "docker_exec run --rm --net host \
            alpine sh -c \
            \"ip a show ${NETWORK_IF}\" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print \$2}'"
  docker_exec run --rm --net host \
            alpine sh -c \
            "ip a show ${NETWORK_IF}" | \
            grep 'inet ' | \
            cut -d/ -f1 | \
            awk '{ print $2}'
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

is_boot2docker() {
  debug $FUNCNAME
  init_uname
  if echo "$GLOBAL_UNAME" | grep -q "boot2docker"; then
    return 0
  else
    return 1
  fi
}

is_docker_for_windows() {
  debug $FUNCNAME
  if is_moby_vm && has_docker_for_windows_client; then
    return 0
  else
    return 1
  fi
}

is_docker_for_mac() {
  debug $FUNCNAME
  if is_moby_vm && ! has_docker_for_windows_client; then
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

is_moby_vm() {
  debug $FUNCNAME
  init_name_map
  if echo "$GLOBAL_NAME_MAP" | grep -q "moby"; then
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
  CURRENT_IMAGE=$(docker_exec images -q "$1")
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
  docker pull $1 >> "${LOGS}" 2>&1 || TEST=$?
  if [ "$TEST" = "1" ]; then
    error "Image $1 unavailable. Not on dockerhub or built locally."
    return 1;
  fi
  text "\n"
}

port_open(){
  log "netstat -an | grep 0.0.0.0:$1 >> \"${LOGS}\" 2>&1"
  netstat -an | grep 0.0.0.0:$1 >> "${LOGS}" 2>&1
  NETSTAT_EXIT=$?

  if [ $NETSTAT_EXIT = 0 ]; then
    return 1
  else
    return 0
  fi
}

container_exist_by_name(){
  docker_exec inspect ${1} > /dev/null 2>&1
  if [ "$?" == "0" ]; then
    return 0
  else
    return 1
  fi
}

get_server_container_id() {
  log "docker_exec inspect -f '{{.Id}}' ${1}"
  docker_exec inspect -f '{{.Id}}' ${1}
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
  if [ "$(docker_exec ps -qa -f "status=running" -f "id=${1}" | wc -l)" -eq 0 ]; then
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
      log "docker_exec restart codenvy_haproxy_1 >> \"${LOGS}\" 2>&1"
      docker_exec restart codenvy_haproxy_1 >> "${LOGS}" 2>&1
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
    return 1
  fi

  info "start" "Server logs at \"docker logs -f ${CODENVY_SERVER_CONTAINER_NAME}\""
  info "start" "Server booting..."
  wait_until_server_is_booted 60 ${CURRENT_CODENVY_SERVER_CONTAINER_ID}

  if server_is_booted ${CURRENT_CODENVY_SERVER_CONTAINER_ID}; then
    info "start" "Booted and reachable"
    info "start" "Ver: $(get_installed_version)"
    info "start" "Use: http://${CODENVY_HOST}"
    info "start" "API: http://${CODENVY_HOST}/swagger"
  else
    error "(${CHE_MINI_PRODUCT_NAME} start): Timeout waiting for server. Run \"docker logs ${CODENVY_SERVER_CONTAINER_NAME}\" to inspect the issue."
    return 1
  fi
}

#TODO - is_initialized will return as initialized with empty directories
is_initialized() {
  debug $FUNCNAME
  if [[ -d "${CODENVY_CONFIG_MANIFESTS_FOLDER}" ]] && \
     [[ -d "${CODENVY_CONFIG_MODULES_FOLDER}" ]] && \
     [[ -f "${REFERENCE_ENVIRONMENT_FILE}" ]] && \
     [[ -f "${CODENVY_CONFIG}/${CODENVY_VERSION_FILE}" ]]; then
    return 0
  else
    return 1
  fi
}

has_version_registry() {
  if [ -d ~/."${CHE_MINI_PRODUCT_NAME}"/manifests/$1 ]; then
    return 0;
  else
    return 1;
  fi
}

get_version_registry() {
  info "cli" "Downloading version registry..."

  ### Remove these comments once in production
  log "docker_exec pull $CHE_GLOBAL_VERSION_IMAGE >> \"${LOGS}\" 2>&1 || true"
  docker_exec pull $CHE_GLOBAL_VERSION_IMAGE >> "${LOGS}" 2>&1 || true
  log "docker_exec run --rm -v \"${CODENVY_MANIFEST_DIR}\":/copy $CHE_GLOBAL_VERSION_IMAGE"
  docker_exec run --rm -v "${CODENVY_MANIFEST_DIR}":/copy $CHE_GLOBAL_VERSION_IMAGE
}

list_versions(){
  # List all subdirectories and then print only the file name
  for version in "${CODENVY_MANIFEST_DIR}"/* ; do
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

  IMAGE_LIST=$(cat "$CODENVY_MANIFEST_DIR"/$1/images)
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
    cat "${CODENVY_CONFIG}"/$CODENVY_VERSION_FILE
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
  info "config" "Generating $CHE_MINI_PRODUCT_NAME configuration..."
  # Note - bug in docker requires relative path for env, not absolute
  log "docker_exec run -it --rm \
                  --env-file=\"${REFERENCE_ENVIRONMENT_FILE}\" \
                  -v \"${CODENVY_INSTANCE}\":/opt/codenvy:rw \
                  -v \"${CODENVY_CONFIG_MANIFESTS_FOLDER}\":/etc/puppet/manifests:ro \
                  -v \"${CODENVY_CONFIG_MODULES_FOLDER}\":/etc/puppet/modules:ro \
                      $IMAGE_PUPPET \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/codenvy.pp --show_diff \"$@\""
  docker_exec run -it --rm \
                  --env-file="${REFERENCE_ENVIRONMENT_FILE}" \
                  -v "${CODENVY_INSTANCE}":/opt/codenvy:rw \
                  -v "${CODENVY_CONFIG_MANIFESTS_FOLDER}":/etc/puppet/manifests:ro \
                  -v "${CODENVY_CONFIG_MODULES_FOLDER}":/etc/puppet/modules:ro \
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

  get_version_registry
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
  log "mkdir -p \"${CODENVY_CONFIG}\""
  mkdir -p "${CODENVY_CONFIG}"
  log "mkdir -p \"${CODENVY_INSTANCE}\""
  mkdir -p "${CODENVY_INSTANCE}"

  if [ ! -w "${CODENVY_CONFIG}" ]; then
    error "CODENVY_CONFIG is not writable. Aborting."
    return 1;
  fi

  if [ ! -w "${CODENVY_INSTANCE}" ]; then
    error "CODENVY_INSTANCE is not writable. Aborting."
    return 1;
  fi

  # in development mode we use init files from repo otherwise we use it from docker image
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    docker_exec run --rm \
                    -v "${CODENVY_CONFIG}":/copy \
                    -v "${CODENVY_DEVELOPMENT_REPO}":/files \
                       $IMAGE_INIT
  else
    docker_exec run --rm -v "${CODENVY_CONFIG}":/copy $IMAGE_INIT
  fi

  # After initialization, add codenvy.env with self-discovery.
  sed -i'.bak' "s|#CODENVY_HOST=.*|CODENVY_HOST=${CODENVY_HOST}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_HOST=${CODENVY_HOST}"
  sed -i'.bak' "s|#CODENVY_VERSION=.*|CODENVY_VERSION=${CODENVY_VERSION}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_VERSION=${CODENVY_VERSION}"
  sed -i'.bak' "s|#CODENVY_CONFIG=.*|CODENVY_CONFIG=${CODENVY_CONFIG}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_CONFIG=${CODENVY_CONFIG}"
  sed -i'.bak' "s|#CODENVY_INSTANCE=.*|CODENVY_INSTANCE=${CODENVY_INSTANCE}|" "${REFERENCE_ENVIRONMENT_FILE}"
  info "init" "  CODENVY_INSTANCE=${CODENVY_INSTANCE}"
  sed -i'.bak' "s|#CODENVY_SWARM_NODES=.*|CODENVY_SWARM_NODES=${CODENVY_HOST}:23750|" "${REFERENCE_ENVIRONMENT_FILE}"

  if [ "${CODENVY_DEVELOPMENT_MODE}" == "on" ]; then
    sed -i'.bak' "s|#CODENVY_ENVIRONMENT=.*|CODENVY_ENVIRONMENT=development|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_ENVIRONMENT=development"
    sed -i'.bak' "s|#CODENVY_DEVELOPMENT_REPO=.*|CODENVY_DEVELOPMENT_REPO=${CODENVY_DEVELOPMENT_REPO}|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_DEVELOPMENT_REPO=${CODENVY_DEVELOPMENT_REPO}"
    sed -i'.bak' "s|#CODENVY_DEVELOPMENT_TOMCAT=.*|CODENVY_DEVELOPMENT_TOMCAT=${CODENVY_DEVELOPMENT_TOMCAT}|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_DEVELOPMENT_TOMCAT=${CODENVY_DEVELOPMENT_TOMCAT}"
  else
    sed -i'.bak' "s|#CODENVY_ENVIRONMENT=.*|CODENVY_ENVIRONMENT=production|" "${REFERENCE_ENVIRONMENT_FILE}"
    info "init" "  CODENVY_ENVIRONMENT=production"
  fi

  rm -rf "${REFERENCE_ENVIRONMENT_FILE}".bak > /dev/null 2>&1

  # Write the Codenvy version to codenvy.ver
  echo "$CODENVY_VERSION" > "${CODENVY_CONFIG}/${CODENVY_VERSION_FILE}"
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
    return 1
  fi

  if [ -z ${IMAGE_PUPPET+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  # Development mode
  if [ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]; then
    # if dev mode is on, pick configuration sources from repo.
    # please note that in production mode update of configuration sources must be only on update.
    docker_exec run --rm \
                    -v "${CODENVY_CONFIG}":/copy \
                    -v "${CODENVY_DEVELOPMENT_REPO}":/files \
                       $IMAGE_INIT

    # in development mode to avoid permissions issues we copy tomcat assembly to ${CODENVY_INSTANCE}
    # if codenvy development tomcat exist we remove it
    if [[ -d "${CODENVY_INSTANCE}/dev" ]]; then
        log "docker_exec run --rm -v \"${CODENVY_INSTANCE}/dev\":/root/dev alpine sh -c \"rm -rf /root/dev/*\""
        docker_exec run --rm -v "${CODENVY_INSTANCE}/dev":/root/dev alpine sh -c "rm -rf /root/dev/*"
        log "rm -rf \"${CODENVY_INSTANCE}/dev\" >> \"${LOGS}\""
        rm -rf "${CODENVY_INSTANCE}/dev"
    fi
    # copy codenvy development tomcat to ${CODENVY_INSTANCE} folder
    cp -r "$(get_mount_path $(echo $CODENVY_DEVELOPMENT_REPO/$DEFAULT_CODENVY_DEVELOPMENT_TOMCAT-*/))" \
        "${CODENVY_INSTANCE}/dev"

    # generate configs and print puppet output logs to console if dev mode is on
    generate_configuration_with_puppet
  else
    generate_configuration_with_puppet >> "${LOGS}"
  fi

  # Replace certain environment file lines with wind
  if has_docker_for_windows_client; then
    info "config" "Customizing docker-compose for Windows"
    CODENVY_ENVFILE_REGISTRY="${CODENVY_INSTANCE}\\\config\\\registry\\\registry.env"
    CODENVY_ENVFILE_POSTGRES="${CODENVY_INSTANCE}\\\config\\\postgres\\\postgres.env"
    CODENVY_ENVFILE_CODENVY="${CODENVY_INSTANCE}\\\config\\\codenvy\\\\$CHE_MINI_PRODUCT_NAME.env"

    sed "s|^.*registry\.env.*$|\ \ \ \ \ \ \-\ \'${CODENVY_ENVFILE_REGISTRY}\'|" -i "${REFERENCE_COMPOSE_FILE}"
    sed "s|^.*postgres\.env.*$|\ \ \ \ \ \ \-\ \'${CODENVY_ENVFILE_POSTGRES}\'|" -i "${REFERENCE_COMPOSE_FILE}"
    sed "s|^.*codenvy\.env.*$|\ \ \ \ \ \ \-\ \'${CODENVY_ENVFILE_CODENVY}\'|" -i "${REFERENCE_COMPOSE_FILE}"
    sed "s|^.*postgresql\/data.*$|\ \ \ \ \ \ \-\ \'codenvy-postgresql-volume\:\/var\/lib\/postgresql\/data\:Z\'|" -i "${REFERENCE_COMPOSE_FILE}"

    echo "" >> "${REFERENCE_COMPOSE_FILE}"
    echo "volumes:" >> "${REFERENCE_COMPOSE_FILE}"
    echo "  codenvy-postgresql-volume:" >> "${REFERENCE_COMPOSE_FILE}"
    echo "     external: true" >> "${REFERENCE_COMPOSE_FILE}"

    # On Windows, it is not possible to volume mount postgres data folder directly
    # This creates a named volume which will store postgres data in docker for win VM
    # TODO - in future, we can write synchronizer utility to copy data from win VM to host
    log "docker_exec volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker_exec volume create --name=codenvy-postgresql-volume >> "${LOGS}"
  fi;
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
       info "start" "Use: http://${CODENVY_HOST}"
       info "start" "API: http://${CODENVY_HOST}/swagger"
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
    error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program. Aborting..."
    return 1;
  fi
  text "\n"

  # Start Codenvy
  # Note bug in docker requires relative path, not absolute path to compose file
  info "start" "Starting containers..."
  log "docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME up -d >> \"${LOGS}\" 2>&1"
  docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME up -d >> "${LOGS}" 2>&1
  check_if_booted
}

cmd_stop() {
  debug $FUNCNAME

  if [ $# -gt 0 ]; then
    error "${CHE_MINI_PRODUCT_NAME} stop: You passed unknown options. Aborting."
    return
  fi

  info "stop" "Stopping containers..."
  log "docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME stop >> \"${LOGS}\" 2>&1 || true"
  docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME stop >> "${LOGS}" 2>&1 || true
  info "stop" "Removing containers..."
  log "yes | docker-compose --file=\"${REFERENCE_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME rm >> \"${LOGS}\" 2>&1 || true"
  yes | docker-compose --file="${REFERENCE_COMPOSE_FILE}" -p=$CHE_MINI_PRODUCT_NAME rm >> "${LOGS}" 2>&1 || true
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
  info "destroy" "Deleting instance and config"
  log "docker_exec run --rm -v \"${CODENVY_CONFIG}\":/codenvy-config -v \"${CODENVY_INSTANCE}\":/codenvy-instance alpine sh -c \"rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*\""
  docker_exec run --rm -v "${CODENVY_CONFIG}":/root/codenvy-config -v "${CODENVY_INSTANCE}":/root/codenvy-instance alpine sh -c "rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*"
  log "rm -rf \"${CODENVY_CONFIG}\" >> \"${LOGS}\""
  log "rm -rf \"${CODENVY_INSTANCE}\" >> \"${LOGS}\""
  rm -rf "${CODENVY_CONFIG}"
  rm -rf "${CODENVY_INSTANCE}"
  if has_docker_for_windows_client; then
    log "docker_exec volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker_exec volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
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
    log "docker_exec rmi -f ${VALUE_IMAGE} >> \"${LOGS}\" 2>&1 || true"
    docker_exec rmi -f $VALUE_IMAGE >> "${LOGS}" 2>&1 || true
  done

  # This is Codenvy's singleton instance with the version registry
  info "rmi" "Removing $CHE_GLOBAL_VERSION_IMAGE"
  docker_exec rmi -f $CHE_GLOBAL_VERSION_IMAGE >> "${LOGS}" 2>&1 || true
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
  get_version_registry
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

  if [[ ! -d "${CODENVY_CONFIG}" ]] || \
     [[ ! -d "${CODENVY_INSTANCE}" ]]; then
    error "Cannot find existing CODENVY_CONFIG or CODENVY_INSTANCE. Aborting."
    return;
  fi

  if [[ ! -d "${CODENVY_BACKUP_FOLDER}" ]]; then
    error "CODENVY_BACKUP_FOLDER does not exist. Aborting."
    return;
  fi

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "$CHE_MINI_PRODUCT_NAME is running. Stop before performing a backup. Aborting."
    return;
  fi

  # check if backups already exist and if so we move it with time stamp in name
  if [[ -f "${CODENVY_BACKUP_FOLDER}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" ]]; then
    mv "${CODENVY_BACKUP_FOLDER}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" \
        "${CODENVY_BACKUP_FOLDER}/moved-$(get_current_date)-${CODENVY_CONFIG_BACKUP_FILE_NAME}"
  fi
  if [[ -f "${CODENVY_BACKUP_FOLDER}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" ]]; then
    mv "${CODENVY_BACKUP_FOLDER}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
        "${CODENVY_BACKUP_FOLDER}/moved-$(get_current_date)-${CODENVY_INSTANCE_BACKUP_FILE_NAME}"
  fi

  info "backup" "Saving configuration..."
  docker_exec run --rm \
    -v "${CODENVY_CONFIG}":/root/codenvy-config \
    -v "${CODENVY_BACKUP_FOLDER}":/root/backup \
    alpine sh -c "tar czf /root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME} -C /root/codenvy-config ."

  info "backup" "Saving instance data..."
  # if windows we backup data volume
  if has_docker_for_windows_client; then
    docker_exec run --rm \
        -v "${CODENVY_INSTANCE}":/root/codenvy-instance \
        -v "${CODENVY_BACKUP_FOLDER}":/root/backup \
        -v codenvy-postgresql-volume:/root/codenvy-instance/data/postgres \
        alpine sh -c "tar czf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance . --exclude=logs ${TAR_EXTRA_EXCLUDE}"
  else
    docker_exec run --rm \
        -v "${CODENVY_INSTANCE}":/root/codenvy-instance \
        -v "${CODENVY_BACKUP_FOLDER}":/root/backup \
        alpine sh -c "tar czf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance . --exclude=logs ${TAR_EXTRA_EXCLUDE}"
  fi
}

cmd_restore() {
  debug $FUNCNAME

  if [[ -d "${CODENVY_CONFIG}" ]] || \
     [[ -d "${CODENVY_INSTANCE}" ]]; then

    WARNING="Restoration overwrites existing configuration and data. Are you sure?"
    if ! confirm_operation "${WARNING}" "$@"; then
      return;
    fi
  fi

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "Codenvy is running. Stop before performing a restore. Aborting"
    return;
  fi

  if [[ ! -f "${CODENVY_BACKUP_FOLDER}/${CODENVY_CONFIG_BACKUP_FILE_NAME}" ]] || \
     [[ ! -f "${CODENVY_BACKUP_FOLDER}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" ]]; then
    error "Backup files not found. To do restore please do backup first."
    return;
  fi

  # remove config and instance folders
  log "docker_exec run --rm -v \"${CODENVY_CONFIG}\":/codenvy-config -v \"${CODENVY_INSTANCE}\":/codenvy-instance alpine sh -c \"rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*\""
  docker_exec run --rm -v "${CODENVY_CONFIG}":/root/codenvy-config -v "${CODENVY_INSTANCE}":/root/codenvy-instance alpine sh -c "rm -rf /root/codenvy-instance/* && rm -rf /root/codenvy-config/*"
  log "rm -rf \"${CODENVY_CONFIG}\" >> \"${LOGS}\""
  log "rm -rf \"${CODENVY_INSTANCE}\" >> \"${LOGS}\""
  rm -rf "${CODENVY_CONFIG}"
  rm -rf "${CODENVY_INSTANCE}"

  info "restore" "Recovering configuration..."
  mkdir -p "${CODENVY_CONFIG}"
  docker_exec run --rm \
    -v "${CODENVY_CONFIG}":/root/codenvy-config \
    -v "${CODENVY_BACKUP_FOLDER}/${CODENVY_CONFIG_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME}" \
    alpine sh -c "tar xf /root/backup/${CODENVY_CONFIG_BACKUP_FILE_NAME} -C /root/codenvy-config"

  info "restore" "Recovering instance data..."
  mkdir -p "${CODENVY_INSTANCE}"
  if has_docker_for_windows_client; then
    log "docker volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
    docker_exec run --rm \
        -v "${CODENVY_INSTANCE}":/root/codenvy-instance \
        -v "${CODENVY_BACKUP_FOLDER}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
        -v codenvy-postgresql-volume:/root/codenvy-instance/data/postgres \
        alpine sh -c "tar xf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance"
  else
    docker_exec run --rm \
        -v "${CODENVY_INSTANCE}":/root/codenvy-instance \
        -v "${CODENVY_BACKUP_FOLDER}/${CODENVY_INSTANCE_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME}" \
        alpine sh -c "tar xf /root/backup/${CODENVY_INSTANCE_BACKUP_FILE_NAME} -C /root/codenvy-instance"
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
    info "offline" "Saving $CODENVY_OFFLINE_FOLDER/$TAR_NAME.tar..."
    if ! $(docker_exec save $VALUE_IMAGE > $CODENVY_OFFLINE_FOLDER/$TAR_NAME.tar); then
      error "Docker was interrupted while saving $CODENVY_OFFLINE_FOLDER/$TAR_NAME.tar"
      return 1;
    fi
  done

  # This is Codenvy's singleton instance with the version registry
  docker_exec save $CHE_GLOBAL_VERSION_IMAGE > "${CODENVY_OFFLINE_FOLDER}"/codenvy_version.tar
  info "offline" "Images saved as tars in $CODENVY_OFFLINE_FOLDER"
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
  info "CODENVY_INSTANCE          = ${CODENVY_INSTANCE}"
  info "CODENVY_CONFIG            = ${CODENVY_CONFIG}"
  info "CODENVY_HOST              = ${CODENVY_HOST}"
  info "CODENVY_REGISTRY          = ${CODENVY_MANIFEST_DIR}"
  info "CODENVY_DEVELOPMENT_MODE  = ${CODENVY_DEVELOPMENT_MODE}"
  info "CODENVY_DEVELOPMENT_REPO  = ${CODENVY_DEVELOPMENT_REPO}"
  info "CODENVY_BACKUP_FOLDER     = ${CODENVY_BACKUP_FOLDER}"
  info ""
  info "-----------  PLATFORM INFO  -----------"
#  info "CLI DEFAULT PROFILE       = $(has_default_profile && echo $(get_default_profile) || echo "not set")"
  info "CLI_VERSION               = ${CHE_CLI_VERSION}"
  info "DOCKER_INSTALL_TYPE       = $(get_docker_install_type)"
  info "IS_NATIVE                 = $(is_native && echo \"YES\" || echo \"NO\")"
  info "IS_WINDOWS                = $(has_docker_for_windows_client && echo \"YES\" || echo \"NO\")"
  info "IS_DOCKER_FOR_WINDOWS     = $(is_docker_for_windows && echo \"YES\" || echo \"NO\")"
  info "HAS_DOCKER_FOR_WINDOWS_IP = $(has_docker_for_windows_client && echo \"YES\" || echo \"NO\")"
  info "IS_DOCKER_FOR_MAC         = $(is_docker_for_mac && echo \"YES\" || echo \"NO\")"
  info "IS_BOOT2DOCKER            = $(is_boot2docker && echo \"YES\" || echo \"NO\")"
  info "IS_MOBY_VM                = $(is_moby_vm && echo \"YES\" || echo \"NO\")"
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
  log "docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ >> \"${LOGS}\""
  docker_exec run -d -p 12345:80 --name fakeagent alpine httpd -f -p 80 -h /etc/ >> "${LOGS}"

  AGENT_INTERNAL_IP=$(docker_exec inspect --format='{{.NetworkSettings.IPAddress}}' fakeagent)
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
  export HTTP_CODE=$(docker_exec run --rm --name fakeserver \
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
  export HTTP_CODE=$(docker_exec run --rm --name fakeserver \
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

  log "docker_exec rm -f fakeagent >> \"${LOGS}\""
  docker_exec rm -f fakeagent >> "${LOGS}"
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
