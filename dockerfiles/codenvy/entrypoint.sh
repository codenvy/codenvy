#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# utility functions
is_docker_for_mac_or_windows() {
  if uname -r | grep -q 'moby'; then
    return 0
  else
    return 1
  fi
}

get_docker_external_hostname() {
  if is_docker_for_mac_or_windows; then
    echo "localhost"
  else
    echo ""
  fi
}

has_external_hostname() {
  if [ "${HOSTNAME}" = "" ]; then
    return 1
  else
    return 0
  fi
}


#
# Check if Postgres is running. There's a 5 min timeout
# for Postgres to start. If it does not Codenvy Tomcat
# never starts, i.e. CMD is never executed
#

if [[ -z "$PGUSER" || -z "$PGPASSWORD" ]]; then

echo "Postgres variables are not set. Exiting"
echo "Environment variables have the following values:"
echo "User: $PGUSER"
echo "Password: $PGPASSWORD"
exit
fi

echo "Waiting for Postgres to boot..."

MAX_TRIES=60
COUNT=0
  while [ $COUNT -lt $MAX_TRIES ]; do
    psql -h postgres -d template1 -c '\l'
	if [ $? -eq 0 ]; then
    	echo '[INFO]: Postgres is up and running. Starting Codenvy Tomcat...'
    	break
	fi
      let COUNT=COUNT+1
      echo '[INFO]: Postgres is unavailable. Trying in 5 seconds...'
      sleep 5
             if [ "$COUNT" -eq "60" ]; then
                echo "[ERROR]: Postgres failed to start in 5 minutes. Check Postgres container logs. Codenvy Tomcat start canceled."
                exit
             fi
  done

# CHE_DOCKER_IP_EXTERNAL must be set on mac/windows as internal docker ip is not reachable from host
# example : docker ip = 192.168.65.2 but client on host needs to use localhost
HOSTNAME=${CHE_DOCKER_IP_EXTERNAL:-$(get_docker_external_hostname)}
if has_external_hostname; then
  export CHE_DOCKER_IP_EXTERNAL=${HOSTNAME}
fi

# start codenvy tomcat
exec /opt/codenvy-tomcat/bin/catalina.sh ${JPDA} run
