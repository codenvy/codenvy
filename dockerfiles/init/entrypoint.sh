#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#

# do not copy codenvy.env if exist
if [ ! -f  /copy/codenvy.env ]; then
    # if exist add addon env values to main env file.
    if [ -f /etc/puppet/addon.env ]; then
        cat /etc/puppet/addon.env >> /etc/puppet/manifests/che.env
    fi
    cp /etc/puppet/manifests/codenvy.env /copy
fi
