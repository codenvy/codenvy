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

printAndLog "TEST CASE: Install IM CLI in RHEL OS"
vagrantUp ${SINGLE_NODE_RHEL_VAGRANT_FILE}

# remove RHEL OS subscription
executeSshCommand --bypass-validation "sudo subscription-manager remove --all"
executeSshCommand --bypass-validation "sudo subscription-manager unregister"

# check verification if OS is registered
installImCliClient --valid-exit-code=1
validateExpectedString ".*RHEL.OS.subscription.is.invalid.*NOTE\:.Please,.make.sure.that.this.system.is.register.and.next.repositories.are.enabled\:.*NOTE\:.'rhel-7-server-optional-rpms',.'rhel-7-server-extras-rpms'.*"

# check on accessibility of required repos
executeSshCommand "sudo subscription-manager register --username riuvshin@codenvy.com --password codenvy --auto-attach"
installImCliClient --valid-exit-code=1
validateExpectedString ".*Next.required.repositories.aren't.enabled\:.'rhel-7-server-optional-rpms',.'rhel-7-server-extras-rpms'.*NOTE:.You.could.use.command.'sudo.subscription-manager.repos.--enable=<repo-name>'.to.enable.them..*"

executeSshCommand "sudo subscription-manager repos --enable=rhel-7-server-optional-rpms"
installImCliClient --valid-exit-code=1
validateExpectedString ".*Next.required.repositories.aren't.enabled\:.'rhel-7-server-extras-rpms'.*NOTE:.You.could.use.command.'sudo.subscription-manager.repos.--enable=<repo-name>'.to.enable.them..*"

# check on successful installation of IM CLI
executeSshCommand "sudo subscription-manager repos --enable=rhel-7-server-extras-rpms"
installImCliClient
validateInstalledImCliClientVersion

printAndLog "RESULT: PASSED"
vagrantDestroy
