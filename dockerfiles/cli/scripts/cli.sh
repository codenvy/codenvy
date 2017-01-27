#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

cli_pre_init() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host eclipse/che-ip:nightly)}
  DEFAULT_CODENVY_HOST=$GLOBAL_HOST_IP
  CODENVY_HOST=${CODENVY_HOST:-${DEFAULT_CODENVY_HOST}}
  CODENVY_PORT=80
}

cli_post_init() {
  CHE_SERVER_CONTAINER_NAME="${CHE_MINI_PRODUCT_NAME}_${CHE_MINI_PRODUCT_NAME}_1"
}

cli_parse () {
  debug $FUNCNAME
  COMMAND="cmd_$1"

  case $1 in
      init|config|start|stop|restart|backup|restore|info|offline|add-node|list-nodes|remove-node|destroy|download|rmi|upgrade|version|ssh|sync|action|test|compile|dir|help)
      ;;
      *)
         error "You passed an unknown command."
         usage
         return 2
      ;;
  esac
}

get_boot_url() {
  echo "$CODENVY_HOST/api/"
}

get_display_url() {
  if ! is_docker_for_mac; then
    echo "http://${CODENVY_HOST}"
  else
    echo "http://localhost"
  fi
}

cmd_backup_extra_args() {
  # if windows we backup data volume
  if has_docker_for_windows_client; then
    echo " -v codenvy-postgresql-volume:/root${CHE_CONTAINER_ROOT}/data/postgres "
  else
    echo ""
  fi
}

cmd_destroy_post_action() {
  if has_docker_for_windows_client; then
    docker volume rm codenvy-postgresql-volume > /dev/null 2>&1  || true
  fi
}

cmd_restore_pre_action() {
  if has_docker_for_windows_client; then
    log "docker volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
  fi
}

cmd_restore_extra_args() {
  if has_docker_for_windows_client; then
    echo " -v codenvy-postgresql-volume:/root${CHE_CONTAINER_ROOT}/instance/data/postgres "
  else
    echo ""
  fi
}

server_is_booted_extra_check() {
  # Total hack - having to restart haproxy for some reason on windows
  if is_docker_for_windows || is_docker_for_mac; then
    log "docker restart codenvy_haproxy_1 >> \"${LOGS}\" 2>&1"
    docker restart codenvy_haproxy_1 >> "${LOGS}" 2>&1
  fi
}

cmd_init_reinit_pre_action() {
  sed -i'.bak' "s|#CODENVY_HOST=.*|CODENVY_HOST=${CODENVY_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  sed -i'.bak' "s|#CODENVY_SWARM_NODES=.*|CODENVY_SWARM_NODES=${CODENVY_HOST}:23750|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"

  # For testing purposes only
  #HTTP_PROXY=8.8.8.8
  #HTTPS_PROXY=http://4.4.4.4:9090
  #NO_PROXY="locahost,127.0.0.1"

  if [[ ! ${HTTP_PROXY} = "" ]]; then
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTP_PROXY_FOR_${CHE_PRODUCT_NAME}=.*|${CHE_PRODUCT_NAME}_HTTP_PROXY_FOR_${CHE_PRODUCT_NAME}=${HTTP_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTP_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=.*|${CHE_PRODUCT_NAME}_HTTP_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=${HTTP_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
  if [[ ! ${HTTPS_PROXY} = "" ]]; then
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTPS_PROXY_FOR_${CHE_PRODUCT_NAME}=.*|${CHE_PRODUCT_NAME}_HTTPS_PROXY_FOR_${CHE_PRODUCT_NAME}=${HTTPS_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_HTTPS_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=.*|${CHE_PRODUCT_NAME}_HTTPS_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=${HTTPS_PROXY}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
  if [[ ! ${HTTP_PROXY} = "" ]] ||
     [[ ! ${HTTPS_PROXY} = "" ]]; then
    #
    # NOTE --- Notice that if no proxy is set, we must append 'codenvy-swarm' to this for docker networking
    #
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_NO_PROXY_FOR_${CHE_PRODUCT_NAME}=.*|${CHE_PRODUCT_NAME}_NO_PROXY_FOR_${CHE_PRODUCT_NAME}=127.0.0.1,localhost,${NO_PROXY},codenvy-swarm,${CODENVY_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
    sed -i'.bak' "s|#${CHE_PRODUCT_NAME}_NO_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=.*|${CHE_PRODUCT_NAME}_NO_PROXY_FOR_${CHE_PRODUCT_NAME}_WORKSPACES=127.0.0.1,localhost,${NO_PROXY},${CODENVY_HOST}|" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  fi
}

cmd_start_check_ports() {

  # If dev mode is on, then we also need to check the debug port set by the user for availability
  if debug_server; then
    USER_DEBUG_PORT=$(docker_run --env-file="${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" alpine sh -c 'echo $CODENVY_DEBUG_PORT')

    if [[ "$USER_DEBUG_PORT" = "" ]]; then
      # If the user has not set a debug port, then use the default
      CODENVY_DEBUG_PORT=8000
      CHE_DEBUG_PORT=8000
    else 
      # Otherwise, this is the value set by the user
      CODENVY_DEBUG_PORT=$USER_DEBUG_PORT
      CHE_DEBUG_PORT=$USER_DEBUG_PORT
    fi
  fi

  PORT_BREAK="no" 
  text   "         port 80 (http):        $(port_open 80 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 443 (https):      $(port_open 443 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 2181 (zookeeper): $(port_open 2181 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 5000 (registry):  $(port_open 5000 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 23750 (socat):    $(port_open 23750 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 23751 (swarm):    $(port_open 23751 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 32001 (jmx):      $(port_open 32001 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  text   "         port 32101 (jmx):      $(port_open 32101 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  if debug_server; then
    text   "         port ${CODENVY_DEBUG_PORT} (debug):     $(port_open ${CODENVY_DEBUG_PORT} && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
    text   "         port 9000 (lighttpd):  $(port_open 9000 && echo "${GREEN}[AVAILABLE]${NC}" || echo "${RED}[ALREADY IN USE]${NC}") \n"
  fi

  if ! $(port_open 80) || \
     ! $(port_open 443) || \
     ! $(port_open 2181) || \
     ! $(port_open 5000) || \
     ! $(port_open 23750) || \
     ! $(port_open 23751) || \
     ! $(port_open 32001) || \
     ! $(port_open 32101); then
     echo ""
     error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program."
     return 2;
  fi
  if debug_server; then
    if ! $(port_open ${CODENVY_DEBUG_PORT}) || ! $(port_open 9000); then
      echo ""
      error "Ports required to run $CHE_MINI_PRODUCT_NAME are used by another program."
      return 1;
    fi
  fi  
}

cmd_config_post_action() {
  # If this is windows, we need to add a special volume for postgres
  if has_docker_for_windows_client; then
    sed "s|^.*postgresql\/data.*$|\ \ \ \ \ \ \-\ \'codenvy-postgresql-volume\:\/var\/lib\/postgresql\/data\:Z\'|" -i "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    echo "" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "volumes:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "  codenvy-postgresql-volume:" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"
    echo "     external: true" >> "${REFERENCE_CONTAINER_COMPOSE_FILE}"

    # This is a post-config creation, so we should also do this to the host version of the file
    sed "s|^.*postgresql\/data.*$|\ \ \ \ \ \ \-\ \'codenvy-postgresql-volume\:\/var\/lib\/postgresql\/data\:Z\'|" -i "${REFERENCE_CONTAINER_COMPOSE_HOST_FILE}"

    echo "" >> "${REFERENCE_CONTAINER_COMPOSE_HOST_FILE}"
    echo "volumes:" >> "${REFERENCE_CONTAINER_COMPOSE_HOST_FILE}"
    echo "  codenvy-postgresql-volume:" >> "${REFERENCE_CONTAINER_COMPOSE_HOST_FILE}"
    echo "     external: true" >> "${REFERENCE_CONTAINER_COMPOSE_HOST_FILE}"

    # On Windows, it is not possible to volume mount postgres data folder directly
    # This creates a named volume which will store postgres data in docker for win VM
    # TODO - in future, we can write synchronizer utility to copy data from win VM to host
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
  fi

  if local_repo; then
    # copy workspace agent assembly to instance folder
    if [[ ! -f $(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${WS_AGENT_IN_REPO}) ]]; then
      warning "You volume mounted a valid $CHE_FORMAL_PRODUCT_NAME repo to ':/repo', but we could not find a ${CHE_FORMAL_PRODUCT_NAME} workspace agent assembly."
      warning "Have you built ${WS_AGENT_IN_REPO_MODULE_NAME} with 'mvn clean install'?"
      return 2
    fi
    cp "$(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${WS_AGENT_IN_REPO})" \
        "${CHE_CONTAINER_INSTANCE}/dev/${WS_AGENT_ASSEMBLY}"

    # copy terminal agent assembly to instance folder
    if [[ ! -f $(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${TERMINAL_AGENT_IN_REPO}) ]]; then
      warning "You volume mounted a valid $CHE_FORMAL_PRODUCT_NAME repo to ':/repo', but we could not find a ${CHE_FORMAL_PRODUCT_NAME} terminal agent assembly."
      warning "Have you built ${TERMINAL_AGENT_IN_REPO_MODULE_NAME} with 'mvn clean install'?"
      return 2
    fi
    cp "$(echo ${CHE_CONTAINER_DEVELOPMENT_REPO}/${TERMINAL_AGENT_IN_REPO})" \
        "${CHE_CONTAINER_INSTANCE}/dev/${TERMINAL_AGENT_ASSEMBLY}"
  fi
}

# Runs puppet image to generate ${CHE_FORMAL_PRODUCT_NAME} configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME

  if is_docker_for_windows; then
    POSTGRES_ENV_FILE=$(convert_posix_to_windows "${CHE_HOST_INSTANCE}/config/postgres/postgres.env")
    CODENVY_ENV_FILE=$(convert_posix_to_windows "${CHE_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env")
  else
    POSTGRES_ENV_FILE="${CHE_HOST_INSTANCE}/config/postgres/postgres.env"
    CODENVY_ENV_FILE="${CHE_HOST_INSTANCE}/config/codenvy/$CHE_MINI_PRODUCT_NAME.env"
  fi

  if debug_server; then
    CHE_ENVIRONMENT="development"
    WRITE_LOGS=""
  else
    CHE_ENVIRONMENT="production"
    WRITE_LOGS=">> \"${LOGS}\""
  fi

  if local_repo; then
    CHE_REPO="on"
    WRITE_PARAMETERS=" -e \"PATH_TO_CHE_ASSEMBLY=${CHE_ASSEMBLY}\""
    WRITE_PARAMETERS+=" -e \"PATH_TO_WS_AGENT_ASSEMBLY=${CHE_HOST_INSTANCE}/dev/${WS_AGENT_ASSEMBLY}\""
    WRITE_PARAMETERS+=" -e \"PATH_TO_TERMINAL_AGENT_ASSEMBLY=${CHE_HOST_INSTANCE}/dev/${TERMINAL_AGENT_ASSEMBLY}\""

    # add local mounts only if they are present
    if [[ -d "/repo/dockerfiles/init/manifests" ]]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests\":/etc/puppet/manifests:ro"
    fi
    if [[ -d "/repo/dockerfiles/init/modules" ]]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/modules\":/etc/puppet/modules:ro"
    fi

    # Handle override/addon
    if [[ -d "/repo/dockerfiles/init/addon/" ]]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/addon/addon.pp\":/etc/puppet/manifests/addon.pp:ro"
      if [ -d "/repo/dockerfiles/init/addon/modules" ]; then
        WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/addon/modules/\":/etc/puppet/addon/:ro"
      fi
    fi
  else
    CHE_REPO="off"
    WRITE_PARAMETERS=""
  fi

  GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  ${WRITE_PARAMETERS} \
                  -e \"POSTGRES_ENV_FILE=${POSTGRES_ENV_FILE}\" \
                  -e \"CODENVY_ENV_FILE=${CODENVY_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_ENVIRONMENT=${CHE_ENVIRONMENT}\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_REPO=${CHE_REPO}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/:/etc/puppet/addon/ \
                                /etc/puppet/manifests/ --show_diff ${WRITE_LOGS}"

  log ${GENERATE_CONFIG_COMMAND}
  eval ${GENERATE_CONFIG_COMMAND}
}

