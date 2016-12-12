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

. $1

printAndLog "TEST CASE: Install IM CLI"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installImCliClient ${LATEST_IM_CLI_CLIENT_VERSION} --unknown-parameter=value --unknown-flag typo
validateExpectedString ".*You.passed.unrecognized.parameters\:.*'--unknown-parameter=value'.*'--unknown-flag'.*'typo'.*"
validateInstalledImCliClientVersion

executeSshCommand "test -d /home/vagrant/codenvy/cli"
executeSshCommand --valid-exit-code=1 "test -d /home/vagrant/codenvy-im"

executeSshCommand "grep 'export CODENVY_IM_BASE=/home/vagrant/codenvy' ~/.bashrc"
executeSshCommand "grep 'export PATH=\$PATH:\$CODENVY_IM_BASE/cli/bin' ~/.bashrc"

executeIMCommand "config" "--im-cli"
validateExpectedString ".*backup_directory=/home/vagrant/codenvy/backups.*base_directory=/home/vagrant/codenvy.*download.directory=/home/vagrant/codenvy/updates.*report_directory=/home/vagrant/codenvy/reports.*saas.server.url=$SAAS_SERVER.*update.server.url=$UPDATE_SERVER.*"

printAndLog "RESULT: PASSED"
vagrantDestroy
