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

cmd_init() {

  # set an initial value for the flag
  FORCE_UPDATE="--no-force"
  AUTO_ACCEPT_LICENSE="false"
  REINIT="false"

  while [ $# -gt 0 ]; do
    case $1 in
      --no-force|--force|--pull|--offline)
        FORCE_UPDATE=$1
        shift ;;
      --accept-license)
        AUTO_ACCEPT_LICENSE="true"
        shift ;;
      --reinit)
        REINIT="true"
        shift ;;
      *) error "Unknown parameter: $1" ; return 2 ;;
    esac
  done

  if [ "${FORCE_UPDATE}" == "--no-force" ]; then
    # If codenvy.environment file exists, then fail
    if is_initialized; then
      if [[ "${REINIT}" = "true" ]]; then
        # Save the environment file.
        cp -rf "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}" "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}".reinit
      else       
        info "init" "Already initialized."
        return 2
      fi
    fi
  fi

  if [[ "${CODENVY_IMAGE_VERSION}" = "nightly" ]]; then
    warning "($CHE_MINI_PRODUCT_NAME init): 'nightly' installations cannot be upgraded to non-nightly versions"
  fi

  cmd_download $FORCE_UPDATE

  if [ -z ${IMAGE_INIT+x} ]; then
    get_image_manifest $CODENVY_VERSION
  fi

  if require_license; then
    if [[ "${AUTO_ACCEPT_LICENSE}" = "false" ]]; then
      info ""
      info "init" "Do you accept the ${CHE_MINI_PRODUCT_NAME} license? (https://codenvy.com/legal/fair-source/)"
      text "\n"
      read -p "      I accept the license: [Y/n] " -n 1 -r
      text "\n"
      if [[ $REPLY =~ ^[Nn]$ ]]; then
        return 2;
      fi
    fi
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

  # After initialization, add environment file.
  if [[ "${REINIT}" = "true" ]]; then
    # If this is a reinit, move the .reinit file and use it as the environment file
    mv -rf "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}".reinit "${REFERENCE_CONTAINER_ENVIRONMENT_FILE}"
  else       
    # Otherwise, we are using the templated version and making some modifications.
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
  fi
}

require_license() {
  if [[ "${CODENVY_LICENSE}" = "true" ]]; then
    return 0
  else
    return 1
  fi
}
