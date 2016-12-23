# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/agents .
#
# use:
#    docker run -p 9000:9000 codenvy/agents

FROM alpine:3.4

RUN apk update \
    && apk add lighttpd \
    && ln -s /data /var/www/localhost/htdocs/agent-binaries \
    && rm -rf /var/cache/apk/* /tmp/*

COPY target/dockerfiles-agents-*/agents /data

EXPOSE 9000

CMD ["lighttpd", "-f", "/etc/lighttpd/lighttpd.conf", "-D"]
