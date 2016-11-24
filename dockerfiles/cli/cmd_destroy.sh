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

cmd_destroy() {
  debug $FUNCNAME

  QUIET=""
  DESTROY_CLI="false"

  while [ $# -gt 0 ]; do
    case $1 in
      --quiet)
        QUIET="--quiet"
        shift ;;
      --cli)
        DESTROY_CLI="true"
        shift ;;
      *) error "Unknown parameter: $1" ; return 2 ;;
    esac
  done

  WARNING="destroy !!! Stopping services and !!! deleting data !!! this is unrecoverable !!!"
  if ! confirm_operation "${WARNING}" "${QUIET}"; then
    return;
  fi

  cmd_stop

  info "destroy" "Deleting instance and config..."
  log "docker_run -v \"${CODENVY_HOST_INSTANCE}\":/root/instance alpine:3.4 sh -c \"rm -rf /root/instance/*\""
  docker_run -v "${CODENVY_HOST_INSTANCE}":/root/instance alpine:3.4 sh -c "rm -rf /root/instance/*"

  # If development mode, delete extra /dev folder
  if [[ "${CODENVY_DEVELOPMENT_MODE}" = "on" ]]; then
    docker_run -v "${CODENVY_HOST_INSTANCE}"/dev:/root/instance alpine:3.4 sh -c "rm -rf /root/instance/*"
  fi

  rm -rf "${CODENVY_CONTAINER_INSTANCE}"

  if has_docker_for_windows_client; then
    docker volume rm codenvy-postgresql-volume > /dev/null 2>&1  || true
  fi

  # Sometimes users want the CLI after they have destroyed their instance
  # If they pass destroy --cli then we will also destroy the CLI
  if [[ "${DESTROY_CLI}" = "true" ]]; then
    if [[ "${CLI_MOUNT}" = "not set" ]]; then
      info "destroy" "Did not delete cli.log - ':/cli' not mounted"
    else
      info "destroy" "Deleting cli.log..."
      docker_run -v "${CLI_MOUNT}":/root/cli alpine:3.4 sh -c "rm -rf /root/cli/cli.log"
    fi
  fi
}
