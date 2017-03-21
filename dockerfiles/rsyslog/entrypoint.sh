#!/bin/sh
# Copyright (c) 2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# rm /var/run/rsyslogd.pid if exist before rsyslog start
if [ -f  /var/run/rsyslogd.pid ]; then
    rm -rf /var/run/rsyslogd.pid
fi

exec rsyslogd -n
