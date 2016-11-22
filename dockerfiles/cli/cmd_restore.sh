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
