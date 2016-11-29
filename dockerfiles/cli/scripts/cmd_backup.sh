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

  # possibility to skip ${CHE_FORMAL_PRODUCT_NAME} projects backup
  SKIP_BACKUP_CODENVY_DATA=${1:-"--no-skip-data"}
  if [[ "${SKIP_BACKUP_CODENVY_DATA}" == "--skip-data" ]]; then
    TAR_EXTRA_EXCLUDE="--exclude=instance/data${CHE_CONTAINER_ROOT}"
  else
    TAR_EXTRA_EXCLUDE=""
  fi

  if [[ ! -d "${CHE_CONTAINER_CONFIG}" ]]; then
    error "Cannot find existing CHE_CONFIG or CHE_INSTANCE."
    return;
  fi

  if get_server_container_id "${CHE_SERVER_CONTAINER_NAME}" >> "${LOGS}" 2>&1; then
    error "$CHE_MINI_PRODUCT_NAME is running. Stop before performing a backup."
    return 2;
  fi

  if [[ ! -d "${CHE_CONTAINER_BACKUP}" ]]; then
    mkdir -p "${CHE_CONTAINER_BACKUP}"
  fi

  # check if backups already exist and if so we move it with time stamp in name
  if [[ -f "${CHE_CONTAINER_BACKUP}/${CHE_BACKUP_FILE_NAME}" ]]; then
    mv "${CHE_CONTAINER_BACKUP}/${CHE_BACKUP_FILE_NAME}" \
        "${CHE_CONTAINER_BACKUP}/moved-$(get_current_date)-${CHE_BACKUP_FILE_NAME}"
  fi

  info "backup" "Saving codenvy data..."
  # if windows we backup data volume
  if has_docker_for_windows_client; then
    docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
               -v "${CHE_HOST_BACKUP}":/root/backup \
               -v codenvy-postgresql-volume:/root${CHE_CONTAINER_ROOT}/data/postgres \
                 alpine:3.4 sh -c "tar czf /root/backup/${CHE_BACKUP_FILE_NAME} -C /root${CHE_CONTAINER_ROOT} . --exclude='backup' --exclude='instance/dev' --exclude='instance/logs' ${TAR_EXTRA_EXCLUDE}"
  else
    docker_run -v "${CHE_HOST_CONFIG}":/root${CHE_CONTAINER_ROOT} \
               -v "${CHE_HOST_BACKUP}":/root/backup \
                 alpine:3.4 sh -c "tar czf /root/backup/${CHE_BACKUP_FILE_NAME} -C /root${CHE_CONTAINER_ROOT} . --exclude='backup' --exclude='instance/dev' --exclude='instance/logs' ${TAR_EXTRA_EXCLUDE}"
  fi

  info ""
  info "backup" "Codenvy data saved in ${CHE_HOST_BACKUP}/${CHE_BACKUP_FILE_NAME}"
}


# return date in format which can be used as a unique file or dir name
# example 2016-10-31-1477931458
get_current_date() {
    date +'%Y-%m-%d-%s'
}
