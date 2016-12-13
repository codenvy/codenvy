# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contains the rsyslog server
#
# build:
#   docker build -t codenvy/rsyslog:<version> .
#
# use:
#    docker run codenvy/rsyslog:<version>

FROM alpine:3.4

RUN  apk add --update "rsyslog=8.18.0-r0" \
  && rm -rf /var/cache/apk/*

ENTRYPOINT [ "rsyslogd", "-n" ]
