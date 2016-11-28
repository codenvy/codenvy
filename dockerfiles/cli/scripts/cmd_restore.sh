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

cmd_restore() {
  debug $FUNCNAME

  if [[ -d "${CODENVY_CONTAINER_CONFIG}" ]]; then
    WARNING="Restoration overwrites existing configuration and data. Are you sure?"
    if ! confirm_operation "${WARNING}" "$@"; then
      return;
    fi
  fi

  if get_server_container_id "${CODENVY_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "Codenvy is running. Stop before performing a restore. Aborting"
    return;
  fi

  if [[ ! -f "${CODENVY_CONTAINER_BACKUP}/${CODENVY_BACKUP_FILE_NAME}" ]]; then
    error "Backup files not found. To do restore please do backup first."
    return;
  fi

  # remove config and instance folders
  log "docker_run -v \"${CODENVY_HOST_CONFIG}\":/codenvy \
                    alpine:3.4 sh -c \"rm -rf /root/codenvy/docs \
                                   && rm -rf /root/codenvy/instance \
                                   && rm -rf /root/codenvy/codenvy.env\""
  docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy \
                alpine:3.4 sh -c "rm -rf /root/codenvy/docs \
                              && rm -rf /root/codenvy/instance \
                              &&  rm -rf /root/codenvy/codenvy.env"

  info "restore" "Recovering codenvy data..."
  if has_docker_for_windows_client; then
    log "docker volume rm codenvy-postgresql-volume >> \"${LOGS}\" 2>&1 || true"
    docker volume rm codenvy-postgresql-volume >> "${LOGS}" 2>&1 || true
    log "docker volume create --name=codenvy-postgresql-volume >> \"${LOGS}\""
    docker volume create --name=codenvy-postgresql-volume >> "${LOGS}"
    docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy \
               -v "${CODENVY_HOST_BACKUP}/${CODENVY_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_BACKUP_FILE_NAME}" \
               -v codenvy-postgresql-volume:/root/codenvy/instance/data/postgres \
               alpine:3.4 sh -c "tar xf /root/backup/${CODENVY_BACKUP_FILE_NAME} -C /root/codenvy"
  else
    docker_run -v "${CODENVY_HOST_CONFIG}":/root/codenvy \
               -v "${CODENVY_HOST_BACKUP}/${CODENVY_BACKUP_FILE_NAME}":"/root/backup/${CODENVY_BACKUP_FILE_NAME}" \
               alpine:3.4 sh -c "tar xf /root/backup/${CODENVY_BACKUP_FILE_NAME} -C /root/codenvy"
  fi
}
