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

printAndLog "TEST CASE: Audit"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

# install Codenvy on-prem
installCodenvy ${LATEST_CODENVY_VERSION}
validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

# install IM CLI
installImCliClient ${LATEST_IM_CLI_CLIENT_VERSION}
validateInstalledImCliClientVersion ${LATEST_IM_CLI_CLIENT_VERSION}

addCodenvyLicenseConfiguration
storeCodenvyLicense

authWithoutRealmAndServerDns "admin" "password"

#create user "user1.im.test@email.com"
doPost "application/json" "{\"name\":\"user1\",\"email\":\"user1.im.test@email.com\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user" "${TOKEN}"

#create user "user2.im.test@email.com"
doPost "application/json" "{\"name\":\"user2\",\"email\":\"user2.im.test@email.com\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user" "${TOKEN}"
fetchJsonParameter "id"
USER_ID=${OUTPUT}

authWithoutRealmAndServerDns "user1" "pwd123ABC"
# create workspace 1
WORKSPACE_NAME="workspace-1"
doPost "application/json" "{\"defaultEnv\":\"default\",\"commands\":[{\"commandLine\":\"mvn clean install -f $\{current.project.path}\",\"name\":\"build\",\"type\":\"mvn\",\"attributes\":{}}],\"projects\":[],\"name\":\"${WORKSPACE_NAME}\",\"environments\":{\"default\":{\"recipe\":{\"location\":\"codenvy/ubuntu_jdk8\",\"type\":\"dockerimage\"},\"machines\":{\"dev-machine\":{\"servers\":{},\"agents\":[\"org.eclipse.che.terminal\",\"org.eclipse.che.ws-agent\",\"org.eclipse.che.ssh\"],\"attributes\":{\"memoryLimitBytes\":1610612736},\"source\":{\"type\":\"dockerfile\",\"content\":\"FROM codenvy/ubuntu_jdk8\"}}}}},\"links\":[],\"description\":null}" "http://${HOST_URL}/api/workspace/?token=${TOKEN}"

# create workspace 2
WORKSPACE_NAME="workspace-2"
doPost "application/json" "{\"defaultEnv\":\"default\",\"commands\":[{\"commandLine\":\"mvn clean install -f $\{current.project.path}\",\"name\":\"build\",\"type\":\"mvn\",\"attributes\":{}}],\"projects\":[],\"name\":\"${WORKSPACE_NAME}\",\"environments\":{\"default\":{\"recipe\":{\"location\":\"codenvy/ubuntu_jdk8\",\"type\":\"dockerimage\"},\"machines\":{\"dev-machine\":{\"servers\":{},\"agents\":[\"org.eclipse.che.terminal\",\"org.eclipse.che.ws-agent\",\"org.eclipse.che.ssh\"],\"attributes\":{\"memoryLimitBytes\":1610612736},\"source\":{\"type\":\"dockerfile\",\"content\":\"FROM codenvy/ubuntu_jdk8\"}}}}},\"links\":[],\"description\":null}" "http://${HOST_URL}/api/workspace/?token=${TOKEN}"
fetchJsonParameter "id"
WORKSPACE_ID=${OUTPUT}

authWithoutRealmAndServerDns "user2" "pwd123ABC"
# create workspace 1
WORKSPACE_NAME="workspace-1"
doPost "application/json" "{\"defaultEnv\":\"default\",\"commands\":[{\"commandLine\":\"mvn clean install -f $\{current.project.path}\",\"name\":\"build\",\"type\":\"mvn\",\"attributes\":{}}],\"projects\":[],\"name\":\"${WORKSPACE_NAME}\",\"environments\":{\"default\":{\"recipe\":{\"location\":\"codenvy/ubuntu_jdk8\",\"type\":\"dockerimage\"},\"machines\":{\"dev-machine\":{\"servers\":{},\"agents\":[\"org.eclipse.che.terminal\",\"org.eclipse.che.ws-agent\",\"org.eclipse.che.ssh\"],\"attributes\":{\"memoryLimitBytes\":1610612736},\"source\":{\"type\":\"dockerfile\",\"content\":\"FROM codenvy/ubuntu_jdk8\"}}}}},\"links\":[],\"description\":null}" "http://${HOST_URL}/api/workspace/?token=${TOKEN}"

authWithoutRealmAndServerDns "user1" "pwd123ABC"
# setup workspace permissions
doPost "application/json" "{\"userId\":\"${USER_ID}\",\"domainId\":\"workspace\",\"instanceId\":\"${WORKSPACE_ID}\",\"actions\":[\"read\",\"use\",\"run\",\"configure\"]}" "http://${HOST_URL}/api/permissions" "${TOKEN}"

# try to execute audit from user
executeIMCommand "--valid-exit-code=1" "audit"
validateExpectedString ".*Please,.login.into.Codenvy.*"

#execute audit from admin
executeIMCommand "login" "admin" "password"
executeIMCommand "audit"

validateExpectedString ".*Number.of.all.users:.3.*Number.of.users.licensed:.10.*Date.when.license.expires:.31.December.2050.*admin@codenvy.onprem.is.owner.of.0.workspaces.and.has.permissions.in.0.workspaces.*user1.im.test@email.com.is.owner.of.2.workspaces.and.has.permissions.in.2.workspaces.*workspace-1,.is.owner:.true,.permissions:.\[read,.use,.run,.configure,.setPermissions,.delete\].*workspace-2,.is.owner:.true,.permissions:.\[read,.use,.run,.configure,.setPermissions,.delete\].*user2.im.test@email.com.is.owner.of.1.workspace.and.has.permissions.in.2.workspaces.*workspace-2,.is.owner:.false,.permissions:.\[read,.use,.run,.configure\].*workspace-1,.is.owner:.true,.permissions:.\[read,.use,.run,.configure,.setPermissions,.delete\]"
