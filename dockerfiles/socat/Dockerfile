# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/socat:<version> .
#
# use:
#    docker run codenvy/socat:<version>

FROM alpine:3.4
RUN apk --update add "socat=1.7.3.1-r0"
ENTRYPOINT ["socat"]
