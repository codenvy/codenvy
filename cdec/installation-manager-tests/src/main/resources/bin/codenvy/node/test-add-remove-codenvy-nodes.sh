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

if [[ -n "$2" ]] && [[ "$2" == "rhel" ]]; then
    RHEL_OS=true
    INSTALL_OPTIONS="--advertise-network-interface=enp0s3"
    printAndLog "TEST CASE: Add and remove Codenvy All-In-One nodes in RHEL OS"
    vagrantUp ${SINGLE_RHEL_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
else
    printAndLog "TEST CASE: Add and remove Codenvy All-In-One nodes"
    vagrantUp ${SINGLE_WITH_ADDITIONAL_NODES_VAGRANT_FILE}
fi

# install Codenvy on-prem
installCodenvy ${LATEST_CODENVY_VERSION} ${INSTALL_OPTIONS}
validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

# throw error if no --codenvy-ip is used
executeIMCommand "--valid-exit-code=1" "add-node" "node1.${HOST_URL}"
validateExpectedString ".*Use.the.following.syntax\:.add-node.--codenvy-ip.<CODENVY_IP_ADDRESS>.<NODE_DNS>.*"

# add node1.${HOST_URL}
# Codenvy license error shouldn't be thrown
executeIMCommand "add-node" "--codenvy-ip 192.168.56.110" "node1.${HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${HOST_URL}\".*"

executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${HOST_URL}:2375\"\].*"

## throw error that node has been already used
executeIMCommand "--valid-exit-code=1" "add-node" "node1.${HOST_URL}"
validateExpectedString ".*Node..node1.${HOST_URL}..has.been.already.used.*"

# throw error that dns is incorrect
executeIMCommand "--valid-exit-code=1" "add-node" "bla-bla-bla"
validateExpectedString ".*Illegal.DNS.name.'bla-bla-bla'.of.node..Correct.DNS.name.templates\:.\['${HOST_URL}',.'node<number>.${HOST_URL}'\].*"

# throw error that host is not reachable
executeIMCommand "--valid-exit-code=1" "add-node" "node3.${HOST_URL}"
validateExpectedString ".*Can.t.connect.to.host..vagrant@node3.${HOST_URL}:22.*"

############# Start of change Codenvy hostname workflow
# throw error on changing Codenvy host_url from '${HOST_URL}' to '${NEW_HOST_URL}'
executeIMCommand "--valid-exit-code=1" "config" "--hostname" "${NEW_HOST_URL}"
validateExpectedString ".*There.is.a.problem.with.one.of.the.additional.nodes.with.new.hostname.*node1.${NEW_HOST_URL}.*"

# change '${HOST_URL}' hostname on '${NEW_HOST_URL}'
executeSshCommand "sudo sed -i 's/${HOST_URL}/${NEW_HOST_URL}/g' /etc/hosts" "node1.${HOST_URL}"
executeSshCommand "sudo sed -i 's/${HOST_URL}/${NEW_HOST_URL}/g' /etc/hosts"
executeSshCommand "sudo sed -i 's/${HOST_URL}/${NEW_HOST_URL}/g' /etc/hosts" "node2.${HOST_URL}"
doSleep "1m"  "Wait until network settings are used"

# change Codenvy host_url from '${HOST_URL}' to '${NEW_HOST_URL}'
executeIMCommand "config" "--hostname" "${NEW_HOST_URL}"

executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*"

# remove node1.${NEW_HOST_URL}
executeIMCommand "remove-node" "node1.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${NEW_HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"1\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*"

# add node1.${NEW_HOST_URL}
executeIMCommand "add-node" "node1.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*"
############# End of change Codenvy hostname workflow

addCodenvyLicenseConfiguration
storeCodenvyLicense

# add node2
executeIMCommand "add-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"3\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# remove node2
executeIMCommand "remove-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo find /var/lib/puppet/ssl -name node2.${NEW_HOST_URL}.pem -delete" "node2.${NEW_HOST_URL}"  # remove puppet agent certificate
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*"

# remove already removed node2
executeIMCommand "--valid-exit-code=1" "remove-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*Node..node2.${NEW_HOST_URL}..is.not.found.*"

# add node2 again
executeIMCommand "add-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"3\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node1.${HOST_URL}\",\"node1.${NEW_HOST_URL}:2375\"\].*\[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# remove node1.${NEW_HOST_URL}
executeIMCommand "remove-node" "node1.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node1.${NEW_HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*\[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# remove default node
executeIMCommand "remove-node" "${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"${NEW_HOST_URL}\".*"
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"1\".*[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# try to remove default node again and throw error
executeIMCommand "--valid-exit-code=1" "remove-node" "${NEW_HOST_URL}"
validateExpectedString ".*Node..${NEW_HOST_URL}..is.not.found.*"

# remove node2
executeIMCommand "remove-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo find /var/lib/puppet/ssl -name node2.${HOST_URL}.pem -delete" "node2.${NEW_HOST_URL}"  # remove puppet agent certificate
doSleep "1m"  "Wait until Docker machine takes into account /usr/local/swarm/node_list config"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"0\".*"

# add node2 again
executeIMCommand "add-node" "node2.${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"node2.${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"1\".*\[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# add default node
executeIMCommand "add-node" "${NEW_HOST_URL}"
validateExpectedString ".*\"type\".\:.\"MACHINE_NODE\".*\"host\".\:.\"${NEW_HOST_URL}\".*"
executeSshCommand "sudo systemctl stop iptables"  # open port 23750
doGet "http://${NEW_HOST_URL}:23750/info"
validateExpectedString ".*Nodes\",\"2\".*\[\" ${HOST_URL}\",\"${NEW_HOST_URL}:2375\"\].*[\" node2.${HOST_URL}\",\"node2.${NEW_HOST_URL}:2375\"].*"

# add default node again and throw error
executeIMCommand "--valid-exit-code=1" "add-node" "${NEW_HOST_URL}"
validateExpectedString ".*Node..${NEW_HOST_URL}..has.been.already.used.*"

printAndLog "RESULT: PASSED"
vagrantDestroy
