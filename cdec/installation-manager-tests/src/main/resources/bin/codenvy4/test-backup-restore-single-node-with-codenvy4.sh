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

. ./lib.sh

if [[ -n "$1" ]] && [[ "$1" == "rhel" ]]; then
    RHEL_OS=true
    printAndLog "TEST CASE: Backup and restore single-node Codenvy 4.x On Premise in RHEL OS"
    vagrantUp ${SINGLE_NODE_RHEL_VAGRANT_FILE}
else
    printAndLog "TEST CASE: Backup and restore single-node Codenvy 4.x On Premise"
    vagrantUp ${SINGLE_NODE_VAGRANT_FILE}
fi

# install Codenvy 4.x
installCodenvy ${LATEST_CODENVY4_VERSION}
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}
authWithoutRealmAndServerDns "admin" "password"

# backup at start
executeIMCommand "backup"
fetchJsonParameter "file"
BACKUP_AT_START=${OUTPUT}

# modify data: add account, workspace, project, user
executeIMCommand "password" "password" "new-password"
authWithoutRealmAndServerDns "admin" "new-password"

# create user "cdec.im.test@gmail.com"
doPost "application/json" "{\"name\":\"cdec\",\"email\":\"cdec.im.test@gmail.com\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user/create" "${TOKEN}"
fetchJsonParameter "id"
USER_ID=${OUTPUT}

authWithoutRealmAndServerDns "cdec" "pwd123ABC"

# create workspace "workspace-1"
doPost "application/json" "{\"environments\":[{\"name\":\"workspace-1\",\"machineConfigs\":[{\"links\":[],\"limits\":{\"ram\":1000},\"name\":\"ws-machine\",\"type\":\"docker\",\"source\":{\"location\":\"http://${HOST_URL}/api/recipe/recipe_ubuntu/script\",\"type\":\"recipe\"},\"dev\":true}]}],\"defaultEnv\":\"workspace-1\",\"projects\":[],\"name\":\"workspace-1\",\"attributes\":{},\"temporary\":false}" "http://${HOST_URL}/api/workspace/?token=${TOKEN}"
fetchJsonParameter "id"
WORKSPACE_ID=${OUTPUT}

# run workspace "workspace-1"
doPost "application/json" "{}" "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}/runtime?token=${TOKEN}"

# verify is workspace running
doSleep "10m"  "Wait until workspace starts to avoid 'java.lang.NullPointerException' error on verifying workspace state"

# obtain network ports
doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"
fetchJsonParameter "network.ports"
NETWORK_PORTS=${OUTPUT}

EXT_HOST_PORT_REGEX="4401/tcp=\[PortBinding\{hostIp='127.0.0.1', hostPort='([0-9]*)'\}\]"
EXT_HOST_PORT=$([[ "$NETWORK_PORTS" =~ $EXT_HOST_PORT_REGEX ]] && echo ${BASH_REMATCH[1]})
URL_OF_PROJECT_API="http://${HOST_URL}:81/${EXT_HOST_PORT}_codenvy/wsagent/ext/project"

# obtain machine token
doGet "http://${HOST_URL}/api/machine/token/${WORKSPACE_ID}?token=${TOKEN}"
fetchJsonParameter "machineToken"
MACHINE_TOKEN=${OUTPUT}

# create project "project-1" of type "console-java" in workspace "workspace-1"
doPost "application/json" "{\"location\":\"https://github.com/che-samples/console-java-simple.git\",\"parameters\":{},\"type\":\"git\"}" "${URL_OF_PROJECT_API}/import/project-1?token=${MACHINE_TOKEN}"

doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"
validateExpectedString ".*\"path\":\"/project-1.*"

# create factory from template "minimal"
doPost "application/json" "{\"v\": \"4.0\",\"workspace\": {\"projects\": [{\"links\": [],\"name\": \"Spring\",\"attributes\": {\"languageVersion\": [\"1.6\"],\"language\": [\"java\"]},\"type\": \"maven\", \"source\": {\"location\": \"https://github.com/codenvy-templates/web-spring-java-simple.git\",\"type\": \"git\",\"parameters\": {\"keepVcs\": \"false\", \"branch\": \"3.1.0\"}},\"modules\": [],\"path\": \"/Spring\",\"mixins\": [\"git\"],\"problems\": []}], \"defaultEnv\": \"wss\",\"name\": \"wss\",\"environments\": [{\"machineConfigs\": [{\"dev\": true,\"limits\": {\"ram\":2048},\"source\": {\"location\": \"http://${HOST_URL}/api/recipe/recipe_ubuntu/script\",\"type\": \"recipe\"}, \"name\": \"dev-machine\",\"type\": \"docker\"}],\"name\": \"wss\"}],\"links\": []}}" "http://${HOST_URL}/api/factory?token=${TOKEN}"
fetchJsonParameter "id"
FACTORY_ID=${OUTPUT}

# backup with modifications
executeIMCommand "backup"
fetchJsonParameter "file"
BACKUP_WITH_MODIFICATIONS=${OUTPUT}

# verify that there is project-1 on file system
executeSshCommand "sudo ls -R /home/codenvy/codenvy-data/fs"
validateExpectedString ".*/home/codenvy/codenvy-data/fs/[0-9a-z/]*/${WORKSPACE_ID}/project-1/src/main/java/org/eclipse/che/examples\:.*HelloWorld.java.*"

# restore initial state
executeIMCommand "restore" ${BACKUP_AT_START}

# check if data at start was restored correctly
authWithoutRealmAndServerDns "admin" "password"

doGet "http://${HOST_URL}/api/user/${USER_ID}?token=${TOKEN}"
validateExpectedString ".*User.*not.found.*"

doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*Workspace.not.found.*"

# verify that there is no project-1 on file system
executeSshCommand "sudo ls /home/codenvy/codenvy-data/fs"
validateExpectedString ""

doGet "http://${HOST_URL}/api/factory/${FACTORY_ID}?token=${TOKEN}"
validateExpectedString ".*Factory.*not.found.*"

# restore state after modifications
executeIMCommand "restore" ${BACKUP_WITH_MODIFICATIONS}

# check if modified data was restored correctly
authWithoutRealmAndServerDns "admin" "new-password"

doGet "http://${HOST_URL}/api/user/${USER_ID}?token=${TOKEN}"
validateExpectedString ".*cdec.im.test@gmail.com.*"

authWithoutRealmAndServerDns "cdec.im.test@gmail.com" "pwd123ABC"

doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*project-1.*workspace-1.*"

# verify that there is project-1 on file system
executeSshCommand "sudo ls -R /home/codenvy/codenvy-data/fs"
validateExpectedString ".*/home/codenvy/codenvy-data/fs/[0-9a-z/]*/${WORKSPACE_ID}/project-1/src/main/java/org/eclipse/che/examples\:.*HelloWorld.java.*"

doGet "http://${HOST_URL}/api/factory/${FACTORY_ID}?token=${TOKEN}"
validateExpectedString ".*\"name\"\:\"wss\".*"

printAndLog "RESULT: PASSED"
vagrantDestroy
