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

printAndLog "TEST CASE: Download all updates"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installImCliClient
validateInstalledImCliClientVersion

executeIMCommand "download"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"version\".\:.\"${LATEST_STABLE_CODENVY_VERSION}\".*\"file\".\:.\".*codenvy-${LATEST_STABLE_CODENVY_VERSION}.zip\".*\"status\".\:.\"DOWNLOADED\".*"

executeIMCommand "download" "--list-local"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"version\".\:.\"${LATEST_STABLE_CODENVY_VERSION}\".*\"file\".\:.\".*codenvy-${LATEST_STABLE_CODENVY_VERSION}.zip\".*\"status\".\:.\"READY_TO_INSTALL\".*"

executeIMCommand "download" "--list-remote"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"version\".\:.\"${LATEST_STABLE_CODENVY_VERSION}\".*\"status\".\:.\"DOWNLOADED\".*\"artifact\".\:.\"codenvy\".*\"version\".\:.\"${PREV_CODENVY3_VERSION}\".*\"status\".\:.\"AVAILABLE_TO_DOWNLOAD\".*"

executeIMCommand "--valid-exit-code=1" "download" "unknown"
executeIMCommand "--valid-exit-code=1" "download" "codenvy" "1.0.0"

printAndLog "RESULT: PASSED"
vagrantDestroy
