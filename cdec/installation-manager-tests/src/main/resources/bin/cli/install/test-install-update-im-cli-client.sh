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

printAndLog "TEST CASE: Install and update IM CLI client"

vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

# install IM CLI
installImCliClient ${PREV_IM_CLI_CLIENT_VERSION} --install-directory=codenvy-im

# test auto-update at the start of executing some command
executeIMCommand "version"
validateExpectedString ".*The.Codenvy.CLI.is.out.of.date\..We.are.doing.an.automatic.update\..Relaunch.*"

executeIMCommand "version"
validateExpectedString ".*Installation.Manager.CLI.is.being.updated.\.\.\..*"
executeSshCommand --valid-exit-code=1 "test -d /home/vagrant/codenvy-im-data"
executeSshCommand "test -d /home/vagrant/codenvy-im/updates"

validateInstalledImCliClientVersion

executeIMCommand "config" "--im-cli"
validateExpectedString ".*backup_directory=/home/vagrant/codenvy-im/backups.*base_directory=/home/vagrant/codenvy-im.*download.directory=/home/vagrant/codenvy-im/updates.*report_directory=/home/vagrant/codenvy-im/reports.*saas.server.url=$SAAS_SERVER.*update.server.url=$UPDATE_SERVER.*"

printAndLog "RESULT: PASSED"

vagrantDestroy
