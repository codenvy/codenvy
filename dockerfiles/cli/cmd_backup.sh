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


# return date in format which can be used as a unique file or dir name
# example 2016-10-31-1477931458
get_current_date() {
    date +'%Y-%m-%d-%s'
}
