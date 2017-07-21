# Copyright (c) 2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/swarm:<version> .
#
# use:
#    docker run codenvy/swarm:<version>

FROM alpine:3.4

RUN apk --update add ca-certificates wget \
    && wget -O swarm.tgz https://github.com/docker/swarm/releases/download/v1.2.8/swarm-v1.2.8-linux-x86_64.tgz \
    && tar xvzf swarm.tgz \
    && rm -rf swarm.tgz

ENV SWARM_HOST :2375
EXPOSE 2375

ENTRYPOINT ["/swarm"]
CMD ["--help"]
