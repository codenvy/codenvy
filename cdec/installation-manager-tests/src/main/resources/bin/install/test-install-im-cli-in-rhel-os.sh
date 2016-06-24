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

[ -f "./lib.sh" ] && . ./lib.sh
[ -f "../lib.sh" ] && . ../lib.sh

printAndLog "TEST CASE: Install IM CLI in RHEL OS"
vagrantUp ${SINGLE_NODE_RHEL_VAGRANT_FILE}

# remove RHEL OS subscription
executeSshCommand --bypass-validation "sudo subscription-manager remove --all"
executeSshCommand --bypass-validation "sudo subscription-manager unregister"

# check verification if OS is registered
installImCliClient --valid-exit-code=1
validateExpectedString ".*RHEL.OS.isn't.registered.*NOTE\:.You.could.use.command.'sudo.subscription-manager.register'.to.register.it.*"

# check on accessibility of required repos
executeSshCommand "sudo subscription-manager register --username riuvshin@codenvy.com --password codenvy --auto-attach"
installImCliClient --valid-exit-code=1
validateExpectedString ".*Required.repository.'rhel-7-server-optional-rpms'.isn't.enabled.*NOTE\:.You.could.use.command.'sudo.subscription-manager.repos.--enable=rhel-7-server-optional-rpms'.to.enable.it.*"

executeSshCommand "sudo subscription-manager repos --enable=rhel-7-server-optional-rpms"
installImCliClient --valid-exit-code=1
validateExpectedString ".*Required.repository.'rhel-7-server-extras-rpms'.isn't.enabled.*NOTE\:.You.could.use.command.'sudo.subscription-manager.repos.--enable=rhel-7-server-extras-rpms'.to.enable.it.*"

# check on successful installation of IM CLI
executeSshCommand "sudo subscription-manager repos --enable=rhel-7-server-extras-rpms"
installImCliClient
validateInstalledImCliClientVersion

printAndLog "RESULT: PASSED"
vagrantDestroy
