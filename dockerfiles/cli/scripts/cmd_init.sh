#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#


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
