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
#    docker run -p 8000:8000 codenvy/agents

FROM alpine:3.4

RUN apk update \
    && apk add lighttpd \
    && echo "server.port = 9000" >> /etc/lighttpd/lighttpd.conf \
    && ln -s /home/user/che /var/www/localhost/htdocs/agents

RUN addgroup -S user && adduser -S -g user user

COPY dependency /home/user/che/

EXPOSE 9000

CMD ["lighttpd", "-f", "/etc/lighttpd/lighttpd.conf", "-D"]
