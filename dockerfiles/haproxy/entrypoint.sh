#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# Make sure service is running
exec rsyslogd -n &

# Touch the log file so we can tail on it
touch /var/log/haproxy/haproxy.log

# Throw the log to output
tail -f /var/log/haproxy/haproxy.log &

# Start haproxy
exec haproxy -f /usr/local/etc/haproxy/haproxy.cfg
