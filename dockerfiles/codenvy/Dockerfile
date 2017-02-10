# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Codenvy Image
#
# build:
#   cd ..\..\ <-- Root of the codenvy source repository
#   docker build -f dockerfiles/codenvy/Dockerfile -t codenvy/codenvy:<version> .
#
# use:
#    docker run codenvy/codenvy:<version>

FROM openjdk:8u111-jre-alpine

ENV LANG=C.UTF-8 \
    TERM=xterm \
    DOCKER_BUCKET=get.docker.com \
    DOCKER_VERSION=1.6.0

RUN echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories && \
    apk add --update curl bash unzip rsync sudo openssh mc postgresql-client postfix && \
    mkdir -p /opt/codenvy-data/ /opt/codenvy-data/logs /opt/codenvy-data/fs \
             /opt/codenvy-data/che-machines /opt/codenvy-data/che-machines-logs \
             /opt/codenvy-data/conf /opt/codenvy-data/conf/logback \
             /opt/codenvy-data/license && \
    curl -sL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" \
     > /usr/bin/docker; chmod +x /usr/bin/docker

CMD [ "postfix", "-c", "/etc/postfix", "start" ]

COPY dockerfiles/codenvy/entrypoint.sh /
RUN chmod +x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
ADD assembly/assembly-main/target/codenvy-onpremises-*.tar.gz /opt/codenvy-tomcat
