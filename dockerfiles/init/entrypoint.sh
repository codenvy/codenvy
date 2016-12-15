#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# do not copy codenvy.env if exist
if [ ! -f  /copy/codenvy.env ]; then
    cp /etc/puppet/manifests/codenvy.env /copy
fi
