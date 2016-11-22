#!/bin/bash
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

cmd_version() {
  debug $FUNCNAME

  # Do not perform any logging in this method as it is runnable before the system is bootstrap
  echo ""
  text "$CHE_PRODUCT_NAME:\n"
  text "  CLI Version:             %s\n" $(get_image_version)
  if is_initialized; then
    text "  Configured Version:      %s\n" $(get_envfile_version)
  else
    text "  Configured Version:      <not-configured>\n"
  fi
  if is_configed; then
    text "  Installed Version:       %s\n" $(get_installed_version)
  else
    text "  Installed Version:       <not-installed>\n"
  fi

  # TODO: Implement way to generate latest version information.
  # 1. We could do a docker pull ${CHE_MINI_PRODUCT_NAME}/cli:latest and then inspect file inside.
  #    But this would require the product in non-offline mode and you'd have to run the container
  #    to find the value.
  #
  # 2. We could query DockerHub for a list of known tags. However, this requires active authentication
  #    to DockerHub (no anonymous mode) and this would require the product not be in offline mode.
}
