#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

post_cmd_config() {
  # If this is windows, we need to add a special volume for postgres
  if has_docker_for_windows_client; then
    # Only perform this action if it has not been done before.
    if ! grep -q -F 'external: true' "${REFERENCE_CONTAINER_COMPOSE_FILE}"; then    
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

  CHE_REPO="off"
  WRITE_PARAMETERS=""

  if local_repo || local_assembly; then
    WRITE_PARAMETERS=" -e \"PATH_TO_CHE_ASSEMBLY=${CHE_ASSEMBLY}\""
  fi

  if local_assembly; then
    if [[ -f $(echo ${CHE_CONTAINER_ASSEMBLY_FULL_PATH}/lib/${WS_AGENT_ASSEMBLY}) ]]; then
      info "config" "Using ws-agent from main assembly"
      WRITE_PARAMETERS+=" -e \"PATH_TO_WS_AGENT_ASSEMBLY=${CHE_HOST_INSTANCE}/dev/codenvy-tomcat/lib/${WS_AGENT_ASSEMBLY}\""
    fi
  fi

  if local_repo; then
    CHE_REPO="on"
    WRITE_PARAMETERS+=" -e \"PATH_TO_WS_AGENT_ASSEMBLY=${CHE_HOST_INSTANCE}/dev/${WS_AGENT_ASSEMBLY}\""

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
  fi

  for element in "${CLI_ENV_ARRAY[@]}" 
  do
    var1=$(echo $element | cut -f1 -d=)
    var2=$(echo $element | cut -f2 -d=)

    if [[ $var1 == CHE_* ]] ||
       [[ $var1 == ${CHE_PRODUCT_NAME}_* ]]; then
      WRITE_PARAMETERS+=" -e \"$var1=$var2\""
    fi
  done

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
