#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#


cmd_destroy_post_action() {
  if has_docker_for_windows_client; then
    docker volume rm codenvy-postgresql-volume > /dev/null 2>&1  || true
  fi
}
