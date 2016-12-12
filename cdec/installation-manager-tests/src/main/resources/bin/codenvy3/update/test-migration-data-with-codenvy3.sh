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

printAndLog "TEST CASE: Migration Data of Codenvy 3.x"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

# install Codenvy
installCodenvy ${LATEST_CODENVY3_VERSION}
validateInstalledCodenvyVersion
auth "admin" "password"

# modify data: add accout, workspace, project, user, factory
auth "admin" "password"

doPost "application/json" "{\"name\":\"account-1\"}" "http://${HOST_URL}/api/account?token=${TOKEN}"
fetchJsonParameter "id"
ACCOUNT_ID=${OUTPUT}

doPost "application/json" "{\"name\":\"workspace-1\",\"accountId\":\"${ACCOUNT_ID}\"}" "http://${HOST_URL}/api/workspace?token=${TOKEN}"
fetchJsonParameter "id"
WORKSPACE_ID=${OUTPUT}

doPost "application/json" "{\"type\":\"blank\",\"visibility\":\"public\"}" "http://${HOST_URL}/api/project/${WORKSPACE_ID}?name=project-1&token=${TOKEN}"

doPost "application/json" "{\"name\":\"user-1\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user/create?token=${TOKEN}"
fetchJsonParameter "id"
USER_ID=${OUTPUT}

doPost "application/json" "{\"userId\":\"${USER_ID}\",\"roles\":[\"account/owner\"]}" "http://${HOST_URL}/api/account/${ACCOUNT_ID}/members?token=${TOKEN}"
fetchJsonParameter "id"
ACCOUNT_ID=${OUTPUT}

authOnSite "user-1" "pwd123ABC"

createDefaultFactory ${TOKEN}
fetchJsonParameter "id"
FACTORY_ID=${OUTPUT}

# backup
executeIMCommand "backup"
fetchJsonParameter "file"
BACKUP=${OUTPUT}

executeSshCommand "cp ${BACKUP} /vagrant/backup.tar.gz"
vagrantDestroy

# restore
vagrantUp ${MULTI_CODENVY3_NODE_VAGRANT_FILE}
installCodenvy ${LATEST_CODENVY3_VERSION}
validateInstalledCodenvyVersion
executeSshCommand "mkdir /home/vagrant/codenvy/backups"
executeSshCommand "cp /vagrant/backup.tar.gz ${BACKUP}"
executeIMCommand "restore" ${BACKUP}

# check data
auth "admin" "password"

doGet "http://${HOST_URL}/api/account/${ACCOUNT_ID}?token=${TOKEN}"
fetchJsonParameter "id"

doGet "http://${HOST_URL}/api/project/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*project-1.*"

doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
fetchJsonParameter "id"

doGet "http://${HOST_URL}/api/user/${USER_ID}?token=${TOKEN}"
fetchJsonParameter "id"

doGet "http://${HOST_URL}/api/factory/${FACTORY_ID}?token=${TOKEN}"
fetchJsonParameter "id"

authOnSite "user-1" "pwd123ABC"

printAndLog "RESULT: PASSED"
vagrantDestroy
