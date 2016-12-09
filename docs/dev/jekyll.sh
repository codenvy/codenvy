#!/bin/sh 
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
init_logging() {
  BLUE='\033[1;34m'
  GREEN='\033[0;32m'
  RED='\033[0;31m'
  NC='\033[0m'
}

init_global_variables() {
    USAGE="
jekyll.sh [<port>]
    
    <port>    Port to bind Jekyll server too."

    CHE_MINI_PRODUCT_NAME=codenvy
    
    JEKYLL_ARGS="serve"
    REFERENCE_CONTAINER_COMPOSE_FILE=$(echo $(pwd)/docker-compose.yml) 
    export CONTAINER_NAME="codenvy_docs_x"
    export IMAGE_NAME="codenvy/docs:dev"
    
    DOCKER_CLEAN_OLD_COMMAND="docker rm -f \$(docker ps -aq --filter \"name=${CONTAINER_NAME}\")  > /dev/null 2>&1"
    
    COPY_SSHKEY_COMMAND="docker cp ${CONTAINER_NAME}:/home/jekyll/.ssh/id_rsa ${HOME}/.ssh/jekyll_id_rsa && \
      chown -R root:root ${HOME}/.ssh/jekyll_id_rsa && chmod -R 600 ${HOME}/.ssh/jekyll_id_rsa"
    
    export UNISON_SYNC_PATH="$(cd ../ && pwd )"
    UNISON_REPEAT=""
    UNISON_AGENT_COMMAND="LD_LIBRARY_PATH="${PWD}" UNISON=${PWD} ${PWD}/unison ${UNISON_SYNC_PATH} ssh://\${UNISON_SSH_USER}@\${SSH_IP}:\${UNISON_SSH_PORT}//srv/jekyll 
       \${UNISON_REPEAT} -sshargs '-i ${HOME}/.ssh/jekyll_id_rsa/id_rsa -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no'  > /dev/null 2>&1" 
      
    JEKYLL_COMMAND="docker exec -d ${CONTAINER_NAME} jekyll serve > /dev/null 2>&1"
  }
  
check_status() {
    status=$?
    
	if [ $status -ne 0 ]; then
	  if [ $status -ne 3 ]; then
	    error "ERROR: Fatal error occurred ($status)"
	    exit 1
	  else
	    warn "Fatal error occurred ($status)"
	  fi
	fi
        
}

parse_command_line () {
  if [ $# -ne 0 ]; then
    if [ "$1" = "--help" ]; then
      usage
      return 1
    else
      #must be port
      export JEKYLL_BIND_PORT="${1}:"
    fi
  else
    export JEKYLL_BIND_PORT=""
  fi

}

usage () {
  printf "%s" "${USAGE}"
}

info() {
  printf  "${GREEN}INFO:${NC} %s\n" "${1}"
}

warn() {
  printf  "${RED}WARNING:${NC} %s\n" "${1}"
}

debug() {
  printf  "${BLUE}DEBUG:${NC} %s\n" "${1}"
}

error() {
  echo  "---------------------------------------"
  echo "!!!"
  echo "!!! ${1}"
  echo "!!!"
  echo  "---------------------------------------"
  return 1
}

stop_sync() {
  echo ""
  info "Received interrupt signal. Exiting."
  exit 1
}

# See: https://sipb.mit.edu/doc/safe-shell/
set -u

# on callback, kill the last background process, which is `tail -f /dev/null` and execute the specified handler
trap 'stop_sync' 1 15 2

init_logging
init_global_variables
parse_command_line "$@"

if [ ! -e /var/run/docker.sock ]; then
    error "(${CHE_MINI_PRODUCT_NAME} Jekyll): File /var/run/docker.sock does not exist. Add to server extra volume mounts and restart server."
    exit 1
fi

eval ${DOCKER_CLEAN_OLD_COMMAND} 
# docker-compose had error when put into eval. Maybe due to losing current directory value of build.

info "(${CHE_MINI_PRODUCT_NAME} Jekyll): Starting Jekyll container. Jekyll server will start after initial unison sync of /srv/jekyll folder."
docker-compose --file "${REFERENCE_CONTAINER_COMPOSE_FILE}" -p "${CHE_MINI_PRODUCT_NAME}" up -d --build --no-recreate > /dev/null 2>&1
check_status
eval ${COPY_SSHKEY_COMMAND}
check_status

export SSH_IP=$(docker inspect --format='{{.NetworkSettings.Gateway}}' $(docker ps -aq --filter "name=${CONTAINER_NAME}") )
if [ "${SSH_IP}" = "" ]; then
    error "(${CHE_MINI_PRODUCT_NAME} Jekyll): Something went wrong. No gateway address assigned to container." 
fi
export UNISON_SSH_PORT=$(docker inspect --format='{{(index (index .NetworkSettings.Ports "22/tcp") 0).HostPort}}' $(docker ps -aq --filter "name=${CONTAINER_NAME}") )
export JEKYLL_PORT=$(docker inspect --format='{{(index (index .NetworkSettings.Ports "4000/tcp") 0).HostPort}}' $(docker ps -aq --filter "name=${CONTAINER_NAME}") )
export UNISON_SSH_USER=$(docker inspect --format='{{.Config.User}}' $(docker ps -aq --filter "name=${CONTAINER_NAME}") )

# ssh -Tv -i ${HOME}/.ssh/jekyll_id_rsa/id_rsa -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -p ${SSH_PORT} ${SSH_USER}@${SSH_IP} unison -version

info "(${CHE_MINI_PRODUCT_NAME} Jekyll): Starting Initial sync to Jekyll docker container... Please wait."
START_TIME=$(date +%s)
eval ${UNISON_AGENT_COMMAND}
check_status
ELAPSED_TIME=$(expr $(date +%s) - $START_TIME)
info "(${CHE_MINI_PRODUCT_NAME} Jekyll): Initial sync to Jekyll docker container took $ELAPSED_TIME seconds."
info "(${CHE_MINI_PRODUCT_NAME} Jekyll): Starting Jekyll server at http://<host ip>:${JEKYLL_PORT}/."
eval ${JEKYLL_COMMAND}
check_status
info "(${CHE_MINI_PRODUCT_NAME} Jekyll): Background sync continues every 2 seconds."
info "(${CHE_MINI_PRODUCT_NAME} Jekyll): This terminal will block while the synchronization continues."
info "(${CHE_MINI_PRODUCT_NAME} Jekyll): To stop, issue a SIGTERM or SIGINT, usually CTRL-C."
UNISON_REPEAT="-repeat 2"
while [ 1 ]
do
    sleep 2
    eval ${UNISON_AGENT_COMMAND}
    check_status
done