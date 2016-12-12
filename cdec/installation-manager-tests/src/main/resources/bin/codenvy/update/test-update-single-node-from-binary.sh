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

printAndLog "TEST CASE: Update single-node Codenvy from binary"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${PREV_CODENVY_VERSION}
validateInstalledCodenvyVersion ${PREV_CODENVY_VERSION}
executeSshCommand "cat codenvy/codenvy.properties | grep 'install_monitoring_tools=false'"
authWithoutRealmAndServerDns "admin" "password"

executeSshCommand "sudo sed -i 's/${HOST_URL}/${NEW_HOST_URL}/g' /etc/hosts"
executeIMCommand "config" "--hostname" "${NEW_HOST_URL}"

executeIMCommand "download" "codenvy" "${LATEST_CODENVY_VERSION}"

BINARIES="/home/vagrant/codenvy/updates/codenvy/${LATEST_CODENVY_VERSION}/codenvy-${LATEST_CODENVY_VERSION}.zip"

# install from local folder
executeIMCommand "install" "--binaries=$BINARIES" "codenvy" "${LATEST_CODENVY_VERSION}"
validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

printAndLog "RESULT: PASSED"
vagrantDestroy
