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

printAndLog "TEST CASE: Install the latest single-node Codenvy 3.x On Premise"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${LATEST_CODENVY3_VERSION}
validateInstalledCodenvyVersion

auth "admin" "password"

# change Codenvy hostname
executeIMCommand "config" "--hostname" "${NEW_HOST_URL}"

# verify changes on api node
executeSshCommand "sudo cat /home/codenvy/codenvy-data/cloud-ide-local-configuration/general.properties"
executeSshCommand "sudo grep \"api.endpoint=http://${NEW_HOST_URL}/api\" /home/codenvy/codenvy-data/cloud-ide-local-configuration/general.properties"

# verify changes on installation-manager service
executeSshCommand "sudo cat /home/codenvy-im/codenvy-im-data/conf/installation-manager.properties"
executeSshCommand "sudo grep \"api.endpoint=http://${NEW_HOST_URL}/api\" /home/codenvy-im/codenvy-im-data/conf/installation-manager.properties"

auth "admin" "password" "http://${NEW_HOST_URL}"

# test re-install
# remove codenvy binary
executeSshCommand "sudo rm -rf /home/codenvy/codenvy-tomcat/webapps"
executeSshCommand "sudo rm -rf /home/codenvy-im/codenvy-im-tomcat/webapps"

# preform re-install
executeIMCommand "install" "--reinstall" "codenvy"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"status\".\:.\"SUCCESS\".*\"status\".\:.\"OK\".*"

validateInstalledCodenvyVersion

printAndLog "RESULT: PASSED"
vagrantDestroy
