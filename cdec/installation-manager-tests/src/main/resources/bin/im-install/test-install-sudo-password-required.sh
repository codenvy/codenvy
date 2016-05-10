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

[ -f "./lib.sh" ] && . ./lib.sh
[ -f "../lib.sh" ] && . ../lib.sh

printAndLog "TEST CASE: Install when sudo password is required"
vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

# 1) description without password: "vagrant ALL=(ALL) NOPASSWD:ALL"
# set password: "passwd vagrant"
# change sudo password timeout:
# 2) execute "mcedit /etc/sudoers"
# 3) add "Defaults timestamp_timeout=0" to ask password every time
# or add "Defaults timestamp_timeout=-1" to turn off asking password
# 4) save
executeSshCommand "sudo sed -i -e 's/vagrant.*/vagrant ALL=\(ALL\) ALL/g' /etc/sudoers"
executeSshCommand "--valid-exit-code=1" "sudo -n -k true"

executeSshCommand "--valid-exit-code=1" "bash <(curl -L -s ${UPDATE_SERVICE}/repository/public/download/install-codenvy) --suppress --license=accept"
validateExpectedString ".*ERROR\:.This.account.'vagrant'.does.not.have.sufficient.sudo.rights.to.perform.an.installation.*NOTE\:.Grant.privileges.to.run.sudo.without.password.to.'vagrant'.user.and.restart.installation.*"

printAndLog "RESULT: PASSED"
vagrantDestroy
