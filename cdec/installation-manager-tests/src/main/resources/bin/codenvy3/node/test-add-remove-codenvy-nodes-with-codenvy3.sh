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

printAndLog "TEST CASE: Add and remove Codenvy 3.x nodes"
vagrantUp ${MULTI_CODENVY3_NODE_WITH_ADDITIONAL_NODES_VAGRANT_FILE}

installCodenvy ${LATEST_CODENVY3_VERSION}
validateInstalledCodenvyVersion

# add runner
executeIMCommand "add-node" "runner2.${HOST_URL}"
doSleep 2m

auth "admin" "password"
doGet "http://${HOST_URL}/api/admin/runner/server?token=${TOKEN}"
validateExpectedString ".*http://runner2.${HOST_URL}:8080/runner/internal/runner.*"

# add builder
executeIMCommand "add-node" "builder2.${HOST_URL}"
doSleep 2m

auth "admin" "password"
doGet "http://${HOST_URL}/api/admin/builder/server?token=${TOKEN}"
validateExpectedString ".*http://builder2.${HOST_URL}:8080/builder/internal/builder.*"

# Incorrect name
executeIMCommand "--valid-exit-code=1" "add-node" "bla-bla-bla"
validateExpectedString ".*Illegal.DNS.name.'bla-bla-bla'.of.node..Correct.DNS.name.templates\:.\['builder<number>.${HOST_URL}',.'runner<number>.${HOST_URL}'\].*"

# Host is not reachable
executeIMCommand "--valid-exit-code=1" "add-node" "builder3.${HOST_URL}"
validateExpectedString ".*Can.t.connect.to.host..vagrant@builder3.${HOST_URL}:22.*"

# Runner has been already set up
executeIMCommand "--valid-exit-code=1" "add-node" "runner2.${HOST_URL}"
validateExpectedString ".*Node..runner2.${HOST_URL}..has.been.already.used.*"

# remove runner
executeIMCommand "remove-node" "runner2.${HOST_URL}"
doSleep 2m

auth "admin" "password"
doGet "http://${HOST_URL}/api/admin/runner/server?token=${TOKEN}"
validateErrorString ".*http://runner2.${HOST_URL}:8080/runner/internal/runner.*"

# remove builder
executeIMCommand "remove-node" "builder2.${HOST_URL}"
doSleep 2m

auth "admin" "password"
doGet "http://${HOST_URL}/api/admin/builder/server?token=${TOKEN}"
validateErrorString ".*http://builder2.${HOST_URL}:8080/builder/internal/builder.*"

# remove already removed runner
executeIMCommand "--valid-exit-code=1" "remove-node" "runner2.${HOST_URL}"
validateExpectedString ".*Node..runner2.${HOST_URL}..is.not.found.*"

printAndLog "RESULT: PASSED"
vagrantDestroy
