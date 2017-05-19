#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#


cmd_start_check_ports() {

  # Develop array of port #, description.
  # Format of array is "<port>;<port_string>" where the <port_string> is the text to appear in console
  local PORT_ARRAY=(
     "80;port 80 (http):         "
     "443;port 443 (https):       "
     "2181;port 2181 (zookeeper):  "
     "5000;port 5000 (registry):   "
     "23750;port 23750 (socat):     "
     "23751;port 23751 (swarm):     "
     "32000;port 32000 (jmx):       "
     "32001;port 32001 (jmx):       "
    )

  # If dev mode is on, then we also need to check the debug port set by the user for availability
  if debug_server; then
    USER_DEBUG_PORT=$(get_value_of_var_from_env_file CODENVY_DEBUG_PORT)

    if [[ "$USER_DEBUG_PORT" = "" ]]; then
      # If the user has not set a debug port, then use the default
      CODENVY_DEBUG_PORT=8000
      CHE_DEBUG_PORT=8000
    else 
      # Otherwise, this is the value set by the user
      CODENVY_DEBUG_PORT=$USER_DEBUG_PORT
      CHE_DEBUG_PORT=$USER_DEBUG_PORT
    fi

    PORT_ARRAY+=("$CODENVY_DEBUG_PORT;port ${CODENVY_DEBUG_PORT} (debug):      ")
    PORT_ARRAY+=("9000;port 9000 (lighttpd):   ")
    PORT_ARRAY+=("5432;port 5432 (postgresql): ")
  fi

  if check_all_ports "${PORT_ARRAY[@]}"; then
    print_ports_as_ok "${PORT_ARRAY[@]}"
  else
    find_and_print_ports_as_notok "${PORT_ARRAY[@]}"
  fi
}

cmd_start_check_postflight() {
  info "start" "Postflight checks"
  SWARM_NODE_CONFIG=$(get_value_of_var_from_env_file CODENVY_SWARM_NODES)

  POSTFLIGHT=""
  IFS=$','
  for SINGLE_SWARM_NODE in ${SWARM_NODE_CONFIG}; do
    CURL_CODE=$(curl -s $SINGLE_SWARM_NODE/info -o /dev/null --write-out %{http_code} || true)
    if [[ "${CURL_CODE}" = "200" ]]; then
      text "         ($SINGLE_SWARM_NODE/info):  ${GREEN}[OK]${NC}\n"
    else
      text "         ($SINGLE_SWARM_NODE/info):  ${RED}[NOT OK]${NC}\n"
      POSTFLIGHT="fail"
    fi
  done

  if [ "${POSTFLIGHT}" = "fail" ]; then 
    text "\n"
    error "Could not reach all Swarm nodes - workspaces may not start."
    error "  1. Are the swarm ports open on your firewall?"
    error "  2. Did the Docker daemon fail to start on any node?"
    error "  3. Are the swarm entries entered properly in codenvy.env?"
  fi

  text "\n"
}
