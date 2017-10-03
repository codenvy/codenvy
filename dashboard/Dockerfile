# Copyright (c) 2012-2017 Red Hat, Inc
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# This is a Dockerfile allowing to build dashboard by using a docker container.
# Build step: $ docker build -t codenvy-dashboard
# It builds an archive file that can be used by doing later
#  $ docker run --rm codenvy-dashboard | tar -C target/ -zxf -
FROM node:6.11.2

RUN apt-get update && \
    apt-get install -y git
RUN mkdir -p /dashboard/.tmp
RUN useradd -ms /bin/bash node-user
COPY . /dashboard/
RUN chown -R node-user:node-user /dashboard/
USER node-user
RUN cd /dashboard && npm install
# workaround to prevent caching old che-dashboard
ADD https://api.github.com/repos/eclipse/che/git/refs/heads/master /tmp/che-dashboard-version.json
# Disable dev mode before build
RUN sed -i -e "s#var DEV = true;#var DEV = false;#" /dashboard/src/app/index.module.ts
RUN cd /dashboard && npm run build
RUN cd /dashboard && npm run test
# Enable dev mode after build
RUN sed -i -e "s#var DEV = false;#var DEV = true;#" /dashboard/src/app/index.module.ts
# Change base HREF of the application that will be hosted on /dashboard
RUN sed -i -e "s#base href=\"/\"#base href=\"/dashboard/\"#" /dashboard/target/dist/index.html

RUN cd /dashboard/target && tar zcf /tmp/dashboard.tar.gz dist/
CMD zcat /tmp/dashboard.tar.gz
