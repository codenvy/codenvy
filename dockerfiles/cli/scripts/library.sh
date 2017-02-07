#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

cli_parse () {
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

server_is_booted_extra_check() {
  # Total hack - having to restart haproxy for some reason on windows
  if is_docker_for_windows || is_docker_for_mac; then
    log "docker restart codenvy_haproxy_1 >> \"${LOGS}\" 2>&1"
    docker restart codenvy_haproxy_1 >> "${LOGS}" 2>&1
  fi
}
