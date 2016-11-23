# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#
# Contains the Codenvy CLI.
#
# build:
#   docker build -t codenvy/cli .
#
# use:
#    docker run -v $(pwd):/codenvy codenvy/cli [command]

FROM alpine:3.4

RUN  apk add --update "rsyslog=8.18.0-r0" \
  && rm -rf /var/cache/apk/*

COPY rsyslog.conf /etc/rsyslog.conf

COPY haproxy.conf /etc/rsyslog.d/haproxy.conf

RUN mkdir -p /var/log/haproxy

ENTRYPOINT [ "rsyslogd", "-n" ]
