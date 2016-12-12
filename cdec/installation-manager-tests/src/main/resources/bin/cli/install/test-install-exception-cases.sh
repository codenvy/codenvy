#!/bin/bash
#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2015] Codenvy, S.A.
# All Rights Reserved.
# NOTICE: All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any. The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

# load lib.sh from path stored in parameter 1
. $1

printAndLog "TEST CASE: Install exception cases"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installImCliClient
validateInstalledImCliClientVersion

executeIMCommand "--valid-exit-code=1" "install" "codenvy" "${LATEST_CODENVY3_VERSION}"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"version\".\:.\"${LATEST_CODENVY3_VERSION}\".*\"status\".\:.\"FAILURE\".*\"message\".\:.\"Binaries.to.install.codenvy\:${LATEST_CODENVY3_VERSION}.not.found\".*"

executeIMCommand "--valid-exit-code=1" "install" "unknown"
validateExpectedString ".*Artifact..unknown..not.found*"

executeIMCommand "--valid-exit-code=1" "install" "codenvy" "1.0.0"
validateExpectedString ".*Can.t.download.installation.properties.*"

printAndLog "RESULT: PASSED"
vagrantDestroy
