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
    printAndLog "TEST CASE: Add and remove Codenvy 4.x All-In-One nodes in RHEL OS"
    vagrantUp ${SINGLE_CODENVY4_RHEL_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
else
    printAndLog "TEST CASE: Add and remove Codenvy 4.x All-In-One nodes"
    vagrantUp ${SINGLE_CODENVY4_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
fi

# install Codenvy 4.x
installCodenvy ${LATEST_CODENVY4_VERSION}
validateInstalledCodenvyVersion ${LATEST_CODENVY4_VERSION}

# copy ssh key to codenvy node
scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key -P 2222 ~/.vagrant.d/insecure_private_key vagrant@127.0.0.1:./.ssh/id_rsa >> ${TEST_LOG}

# throw error if no --codenvy-ip is used
executeIMCommand "--valid-exit-code=1" "add-node" "node1.${HOST_URL}"
validateExpectedString ".*Use.the.following.syntax\:.add-node.--codenvy-ip.<CODENVY_IP_ADDRESS>.<NODE_DNS>.*"

# throw error if no Codenvy license
executeIMCommand "--valid-exit-code=1" "add-node" "--codenvy-ip 192.168.56.110" "node1.${HOST_URL}"
validateExpectedString ".*Your.Codenvy.subscription.only.allows.a.single.server.*\"status\".\:.\"ERROR\".*"

addCodenvyLicenseConfiguration
storeCodenvyLicense

# add node1.${HOST_URL}
executeIMCommand "add-node" "--codenvy-ip 192.168.56.110" "node1.${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${HOST_URL}\".*"

executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${HOST_URL}:2375\"\].*"

# throw error that node has been already used
executeIMCommand "--valid-exit-code=1" "add-node" "node1.codenvy"
validateExpectedString ".*Node..node1.${HOST_URL}..has.been.already.used.*"

# throw error that dns is incorrect
executeIMCommand "--valid-exit-code=1" "add-node" "bla-bla-bla"
validateExpectedString ".*Illegal.DNS.name.'bla-bla-bla'.of.additional.node..Correct.DNS.name.templates\:.\['node<number>.${HOST_URL}'\].*"

# throw error that host is not reachable
executeIMCommand "--valid-exit-code=1" "add-node" "node3.codenvy"
validateExpectedString ".*Can.t.connect.to.host..vagrant@node3.codenvy:22.*"

############# Start of change Codenvy hostname workflow
# change 'codenvy' hostname on 'test.codenvy' on puppet master
executeSshCommand "sudo hostname test.codenvy"
executeSshCommand "sudo sed -i 's/192.168.56.110 ${HOST_URL}//' /etc/hosts"
executeSshCommand "sudo sed -i 's/ ${HOST_URL}/ ${NEW_HOST_URL}/' /etc/hosts"
executeSshCommand "sudo sed -i 's/ ${HOST_URL}/ ${NEW_HOST_URL}/' /etc/hosts" "node1.${HOST_URL}"
executeSshCommand "sudo sed -i 's/ ${HOST_URL}/ ${NEW_HOST_URL}/' /etc/hosts" "node2.${NEW_HOST_URL}"

# change Codenvy host_url from 'codenvy' to 'test.codenvy'
executeIMCommand "config" "--hostname" "${NEW_HOST_URL}"

# change 'node1.codenvy' hostname on 'node1.test.codenvy' on node1
executeSshCommand "sudo sed -i 's/ node1.${HOST_URL}/ node1.${NEW_HOST_URL}/' /etc/hosts" "node1.${HOST_URL}"
executeSshCommand "sudo sed -i 's/192.168.56.15 node1.${HOST_URL}//' /etc/hosts" "node1.${HOST_URL}"
executeSshCommand "sudo hostname node1.test.codenvy" "node1.${HOST_URL}"
executeSshCommand "sudo sed -i 's/ node1.${HOST_URL}/ node1.${NEW_HOST_URL}/' /etc/hosts"
executeSshCommand "sudo sed -i 's/ node1.${HOST_URL}/ node1.${NEW_HOST_URL}/' /etc/hosts" "node2.${NEW_HOST_URL}"

# remove node1.${HOST_URL}
executeIMCommand "remove-node" "node1.${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"1\".*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

# add node1.${NEW_HOST_URL}
executeIMCommand "add-node" "node1.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" node1.${NEW_HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"
############# End of change Codenvy hostname workflow

# add node2
executeIMCommand "add-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"3\".*\[\" node1.${NEW_HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" node2.${NEW_HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

# remove node2
executeIMCommand "remove-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"

executeSshCommand "sudo find /var/lib/puppet/ssl -name node2.${NEW_HOST_URL}.pem -delete" "node2.${NEW_HOST_URL}"  # remove puppet agent certificate

doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750

doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" node1.${NEW_HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

# remove already removed node2
executeIMCommand "--valid-exit-code=1" "remove-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*Node..node2.${NEW_HOST_URL}..is.not.found.*"

# add node2 again
executeIMCommand "add-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"3\".*\[\" node1.${NEW_HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" node2.${NEW_HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

# remove node1.${NEW_HOST_URL}
executeIMCommand "remove-node" "node1.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${NEW_HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*[\" node2.${NEW_HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*\[\" ${NEW_HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

printAndLog "RESULT: PASSED"
vagrantDestroy
