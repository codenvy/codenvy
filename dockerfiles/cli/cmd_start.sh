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
  COMPOSE_UP_COMMAND="docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=\"${CHE_MINI_PRODUCT_NAME}\" up -d"

  if [ "${CODENVY_DEVELOPMENT_MODE}" != "on" ]; then
    COMPOSE_UP_COMMAND+=" >> \"${LOGS}\" 2>&1"
  fi

  log ${COMPOSE_UP_COMMAND}
  eval ${COMPOSE_UP_COMMAND}
  check_if_booted
}

cmd_stop() {
  debug $FUNCNAME

  if [ $# -gt 0 ]; then
    error "${CHE_MINI_PRODUCT_NAME} stop: You passed unknown options. Aborting."
    return
  fi

  info "stop" "Stopping containers..."
  if is_initialized; then
    log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME stop >> \"${LOGS}\" 2>&1 || true"
    docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                   -p=$CHE_MINI_PRODUCT_NAME stop >> "${LOGS}" 2>&1 || true
    info "stop" "Removing containers..."
    log "docker_compose --file=\"${REFERENCE_CONTAINER_COMPOSE_FILE}\" -p=$CHE_MINI_PRODUCT_NAME rm >> \"${LOGS}\" 2>&1 || true"
    docker_compose --file="${REFERENCE_CONTAINER_COMPOSE_FILE}" \
                   -p=$CHE_MINI_PRODUCT_NAME rm --force >> "${LOGS}" 2>&1 || true
  fi
}

cmd_restart() {
  debug $FUNCNAME

  FORCE_UPDATE=${1:-"--no-force"}
    info "restart" "Restarting..."
    cmd_stop
    cmd_start ${FORCE_UPDATE}
}

port_open(){
  debug $FUNCNAME

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

  info "start" "Services booting..."
  info "start" "Server logs at \"docker logs -f ${CODENVY_SERVER_CONTAINER_NAME}\""
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
