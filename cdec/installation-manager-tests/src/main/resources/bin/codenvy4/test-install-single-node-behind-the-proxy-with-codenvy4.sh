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
    printAndLog "TEST CASE: Install Codenvy 4.x single-node behind the proxy in RHEL OS"
    vagrantUp ${SINGLE_NODE_RHEL_VAGRANT_FILE}
else
    printAndLog "TEST CASE: Install Codenvy 4.x single-node behind the proxy"
    vagrantUp ${SINGLE_NODE_VAGRANT_FILE}
fi

# install Codenvy 4.x behind the proxy
executeSshCommand "echo '$PROXY_IP $PROXY_SERVER' | sudo tee --append /etc/hosts > /dev/null"

installCodenvy ${LATEST_CODENVY4_VERSION} --http-proxy-for-installation=$HTTP_PROXY --https-proxy-for-installation=$HTTPS_PROXY --no-proxy-for-installation="'127.0.0.1|localhost'" --http-proxy-for-codenvy=$HTTP_PASSWORDLESS_PROXY --https-proxy-for-codenvy=$HTTPS_PASSWORDLESS_PROXY --no-proxy-for-codenvy="'127.0.0.1|codenvy'" --http-proxy-for-codenvy-workspaces=$HTTP_PROXY --https-proxy-for-codenvy-workspaces=$HTTPS_PROXY --no-proxy-for-codenvy-workspaces="'127.0.0.1|codenvy'" --http-proxy-for-docker-daemon=$HTTP_PASSWORDLESS_PROXY --https-proxy-for-docker-daemon=$HTTPS_PASSWORDLESS_PROXY --no-proxy-for-docker-daemon="'127.0.0.1|localhost'" --docker-registry-mirror=$HTTPS_PASSWORDLESS_PROXY
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}

# validate proxy settings in system config files
executeSshCommand "cat ~/.bashrc"
validateExpectedString ".*export.http_proxy=$HTTP_PROXY.*"
validateExpectedString ".*export.https_proxy=$HTTPS_PROXY.*"
validateExpectedString ".*export.no_proxy='127.0.0.1|localhost'.*"

executeSshCommand "cat /etc/wgetrc"
validateExpectedString ".*use_proxy=on.*"
validateExpectedString ".http_proxy=$HTTP_PROXY.*"
validateExpectedString ".https_proxy=$HTTPS_PROXY.*"
validateExpectedString ".no_proxy='127.0.0.1|localhost'.*"

executeSshCommand "cat /etc/yum.conf"
validateExpectedString ".*proxy=$PROXY_SERVER_WITHOUT_CREDENTIALS.*"
validateExpectedString ".*proxy_username=$PROXY_USERNAME.*"
validateExpectedString ".*proxy_password=$PROXY_PASSWORD.*"

# validate codenvy properties which are defined in options of bootstrap script
executeSshCommand "cat $PATH_TO_CODENVY4_PUPPET_MANIFEST"
validateExpectedString ".*http_proxy_for_codenvy.=.\"$HTTP_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*https_proxy_for_codenvy.=.\"$HTTPS_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*no_proxy_for_codenvy.=.\"127.0.0.1|codenvy\".*"

validateExpectedString ".*http_proxy_for_codenvy_workspaces.=.\"$HTTP_PROXY\".*"
validateExpectedString ".*https_proxy_for_codenvy_workspaces.=.\"$HTTPS_PROXY\".*"
validateExpectedString ".*no_proxy_for_codenvy_workspaces.=.\"127.0.0.1|codenvy\".*"

validateExpectedString ".*http_proxy_for_docker_daemon.=.\"$HTTPS_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*https_proxy_for_docker_daemon.=.\"$HTTP_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*docker_registry_mirror.=.\"$HTTP_PASSWORDLESS_PROXY\".*"

# validate docker settings
executeSshCommand "sudo systemctl status docker"
validateExpectedString ".*--registry-mirror=$HTTPS_PASSWORDLESS_PROXY.*"

executeSshCommand "cat /etc/sysconfig/docker"
validateExpectedString ".*HTTP_PROXY=\"$HTTP_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*HTTPS_PROXY=\"$HTTPS_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*NO_PROXY=\"127.0.0.1|localhost\".*"

## check creation of workspace
authWithoutRealmAndServerDns "admin" "password"

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
doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"

printAndLog "RESULT: PASSED"
vagrantDestroy
