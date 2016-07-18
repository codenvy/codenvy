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
    vagrantUp ${SINGLE_CODENVY4_RHEL_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
else
    printAndLog "TEST CASE: Install Codenvy 4.x single-node behind the proxy"
    vagrantUp ${SINGLE_CODENVY4_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
fi

NO_PROXY="codenvy"

# install Codenvy 4.x behind the proxy
executeSshCommand "echo '$PROXY_IP $PROXY_SERVER' | sudo tee --append /etc/hosts > /dev/null"

installCodenvy ${LATEST_CODENVY4_VERSION} --http-proxy-for-installation=$HTTP_PROXY --https-proxy-for-installation=$HTTPS_PROXY --no-proxy-for-installation="'$NO_PROXY'" --http-proxy-for-codenvy=$HTTP_PROXY --https-proxy-for-codenvy=$HTTPS_PROXY --no-proxy-for-codenvy="'$NO_PROXY'" --http-proxy-for-codenvy-workspaces=$HTTP_PROXY --https-proxy-for-codenvy-workspaces=$HTTPS_PROXY --no-proxy-for-codenvy-workspaces="'$NO_PROXY'" --http-proxy-for-docker-daemon=$HTTP_PASSWORDLESS_PROXY --https-proxy-for-docker-daemon=$HTTPS_PASSWORDLESS_PROXY --no-proxy-for-docker-daemon="'$NO_PROXY'" --docker-registry-mirror=$HTTPS_PASSWORDLESS_PROXY
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}

# validate proxy settings in system config files
executeSshCommand "cat ~/.bashrc"
validateExpectedString ".*export.http_proxy=$HTTP_PROXY.*"
validateExpectedString ".*export.https_proxy=$HTTPS_PROXY.*"
validateExpectedString ".*export.no_proxy='$NO_PROXY'.*"

executeSshCommand "cat /etc/wgetrc"
validateExpectedString ".*use_proxy=on.*"
validateExpectedString ".http_proxy=$HTTP_PROXY.*"
validateExpectedString ".https_proxy=$HTTPS_PROXY.*"
validateExpectedString ".no_proxy='$NO_PROXY'.*"

executeSshCommand "cat /etc/yum.conf"
validateExpectedString ".*proxy=$PROXY_SERVER_WITHOUT_CREDENTIALS.*"
validateExpectedString ".*proxy_username=$PROXY_USERNAME.*"
validateExpectedString ".*proxy_password=$PROXY_PASSWORD.*"

# validate codenvy properties which are defined in options of bootstrap script
executeSshCommand "cat $PATH_TO_CODENVY4_PUPPET_MANIFEST"
validateExpectedString ".*http_proxy_for_codenvy.=.\"$HTTP_PROXY\".*"
validateExpectedString ".*https_proxy_for_codenvy.=.\"$HTTPS_PROXY\".*"
validateExpectedString ".*no_proxy_for_codenvy.=.\"$NO_PROXY\".*"

validateExpectedString ".*http_proxy_for_codenvy_workspaces.=.\"$HTTP_PROXY\".*"
validateExpectedString ".*https_proxy_for_codenvy_workspaces.=.\"$HTTPS_PROXY\".*"
validateExpectedString ".*no_proxy_for_codenvy_workspaces.=.\"$NO_PROXY\".*"

validateExpectedString ".*http_proxy_for_docker_daemon.=.\"$HTTPS_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*https_proxy_for_docker_daemon.=.\"$HTTP_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*docker_registry_mirror.=.\"$HTTP_PASSWORDLESS_PROXY\".*"

# validate docker settings
executeSshCommand "sudo systemctl status docker"
validateExpectedString ".*--registry-mirror=$HTTPS_PASSWORDLESS_PROXY.*"

executeSshCommand "cat /etc/sysconfig/docker"
validateExpectedString ".*HTTP_PROXY=\"$HTTP_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*HTTPS_PROXY=\"$HTTPS_PASSWORDLESS_PROXY\".*"
validateExpectedString ".*NO_PROXY=\"$NO_PROXY\".*"

## check creation of workspace
authWithoutRealmAndServerDns "admin" "password"

# create user "cdec.im.test@gmail.com"
doPost "application/json" "{\"name\":\"cdec\",\"email\":\"cdec.im.test@gmail.com\",\"password\":\"pwd123ABC\"}" "http://${HOST_URL}/api/user" "${TOKEN}"
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

## test work with machine node behind the proxy
# remove default node
executeIMCommand "remove-node" "${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"${HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"0\".*"

# add node1.${HOST_URL}
executeIMCommand "add-node" "--codenvy-ip 192.168.56.110" "node1.${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"1\".*\[\" node1.${HOST_URL}\",\"node1.${HOST_URL}:2375\"].*"

# validate proxy settings in system config files on node
executeSshCommand "cat /etc/wgetrc" "node1.${HOST_URL}"
validateExpectedString ".*use_proxy=on.*"
validateExpectedString ".http_proxy=$HTTP_PROXY.*"
validateExpectedString ".https_proxy=$HTTPS_PROXY.*"
validateExpectedString ".no_proxy='$NO_PROXY'.*"

executeSshCommand "cat /etc/yum.conf" "node1.${HOST_URL}"
validateExpectedString ".*proxy=$PROXY_SERVER_WITHOUT_CREDENTIALS.*"
validateExpectedString ".*proxy_username=$PROXY_USERNAME.*"
validateExpectedString ".*proxy_password=$PROXY_PASSWORD.*"

authWithoutRealmAndServerDns "cdec" "pwd123ABC"

# run workspace "workspace-1"
doPost "application/json" "{}" "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}/runtime?token=${TOKEN}"

# obtain network ports
doSleep "10m"  "Wait until workspace starts to avoid 'java.lang.NullPointerException' error on verifying workspace state"
doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"
fetchJsonParameter "network.ports"
NETWORK_PORTS=${OUTPUT}

EXT_HOST_PORT_REGEX="4401/tcp=\[PortBinding\{hostIp='[0-9.]*', hostPort='([0-9]*)'\}\]"
EXT_HOST_PORT=$([[ "$NETWORK_PORTS" =~ $EXT_HOST_PORT_REGEX ]] && echo ${BASH_REMATCH[1]})
URL_OF_PROJECT_API="http://${HOST_URL}:81/${EXT_HOST_PORT}_node1.${HOST_URL}/wsagent/ext/project"

# obtain machine token
doGet "http://${HOST_URL}/api/machine/token/${WORKSPACE_ID}?token=${TOKEN}"
fetchJsonParameter "machineToken"
MACHINE_TOKEN=${OUTPUT}

# create project "project-1" of type "console-java" in workspace "workspace-1"
doPost "application/json" "{\"location\":\"https://github.com/che-samples/console-java-simple.git\",\"parameters\":{},\"type\":\"git\"}" "${URL_OF_PROJECT_API}/import/project-1?token=${MACHINE_TOKEN}"

doGet "http://${HOST_URL}/api/workspace/${WORKSPACE_ID}?token=${TOKEN}"
validateExpectedString ".*\"status\":\"RUNNING\".*"
validateExpectedString ".*\"path\":\"/project-1.*"

# remove node1.${HOST_URL}
executeIMCommand "remove-node" "node1.${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"0\".*"

printAndLog "RESULT: PASSED"
vagrantDestroy
