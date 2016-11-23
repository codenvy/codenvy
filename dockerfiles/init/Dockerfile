# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Initializes an empty directory with the templates needed to configure and run Codenvy.
#
# build:
#   cd ..\..\ <-- Root of the codenvy source repository
#   docker build -f dockerfiles/init/Dockerfile -t codenvy/init:<version> .
#
# use:
#    docker run -v $(pwd):/codenvy codenvy/init:<version>

FROM puppet/puppet-agent-alpine:4.6.2

# install openssh needed to generate ssh keys
RUN apk --update add openssh \
  && rm -rf /var/cache/apk/*

RUN mkdir -p /files \
    && mkdir -p /files/modules \
    && mkdir -p /files/manifests \
    && mkdir -p /files/docs

COPY manifests /files/manifests
COPY modules /files/modules
COPY docs /files/docs
COPY dockerfiles/init/entrypoint.sh /
RUN chmod +x entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
