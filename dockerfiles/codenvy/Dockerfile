# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Codenvy Image
#
# build:
#   ./build.sh
#
# use:
#    docker run codenvy/codenvy:<version>

FROM openjdk:8u111-jre-alpine

ENV LANG=C.UTF-8 \
    TERM=xterm \
    DOCKER_BUCKET=get.docker.com \
    DOCKER_VERSION=1.6.0

RUN apk add --update curl bash rsync sudo openssh postgresql-client postfix tini && \
    mkdir -p /opt/codenvy-data/ /opt/codenvy-data/logs /opt/codenvy-data/fs \
             /opt/codenvy-data/che-machines /opt/codenvy-data/che-machines-logs \
             /opt/codenvy-data/conf /opt/codenvy-data/conf/logback && \
    curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
     > /usr/bin/docker; chmod +x /usr/bin/docker

CMD [ "postfix", "-c", "/etc/postfix", "start" ]

COPY entrypoint.sh /entrypoint.sh
COPY open-jdk-source-file-location /open-jdk-source-file-location
RUN chmod +x entrypoint.sh
# Use Tini as real entrypoint - init process that helps reaping zombies
ENTRYPOINT ["/sbin/tini", "--", "/entrypoint.sh"]
ADD codenvy-onpremises.tar.gz /opt/codenvy-tomcat
