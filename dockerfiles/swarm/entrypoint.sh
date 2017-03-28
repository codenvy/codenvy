#!/bin/sh

# Copyright (c) 2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

set -e

if [ ! -d  /var/log/swarm ]; then
    mkdir -p /var/log/swarm
fi

exec /swarm manage -H 0.0.0.0:2375 file:///node_list 2>&1 | tee -a /var/log/swarm/swarm.log &

pid=`pidof swarm`
trap "echo 'Stopping PID $pid'; kill -SIGTERM $pid" SIGINT SIGTERM
# A signal emitted while waiting will make the wait command return code > 128
# Let's wrap it in a loop that doesn't end before the process is indeed stopped
while kill -0 $pid > /dev/null 2>&1; do
    wait
done
