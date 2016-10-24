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

printAndLog "TEST CASE: Install the latest single-node Codenvy and update its configuration"

vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${LATEST_CODENVY_VERSION} --config=${CUSTOM_SINGLE_NODE_LATEST_VERSION_CONFIG_URL}
validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

PROPERTY_TO_TEST="docker_registry_credentials"
VALUE_TO_TEST=

executeIMCommand "config"
validateExpectedString ".*codenvy_admin_initial_password=*****.*$PROPERTY_TO_TEST=$VALUE_TO_TEST.*installation_manager_update_server_endpoint=$UPDATE_SERVICE.*version=$LATEST_CODENVY_VERSION.*"

executeIMCommand "config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_TEST"

### test setup multi-line properties
# to set multiline value of Puppet config "registry1.url=url\nregistry1.username=user1\nregistry1.password=passwd"
MULTILINE_VALUE_TO_UPDATE="registry1.url=url\\\nregistry1.username=user1\\\nregistry1.password=passwd"

executeSshCommand "echo y | codenvy config $PROPERTY_TO_TEST $MULTILINE_VALUE_TO_UPDATE"
validateExpectedString ".*Do.you.want.to.update.Codenvy.property.'$PROPERTY_TO_TEST'.with.new.value.'registry1.url=url.nregistry1.username=user1.nregistry1.password=passwd'?.*"

executeSshCommand "sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay"

executeIMCommand "config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=registry1\.url=url.registry1\.username=user1.registry1\.password=passwd"

# restore initial value of multiline property
executeSshCommand "echo y | codenvy config $PROPERTY_TO_TEST '$VALUE_TO_TEST'"
validateExpectedString ".*Do.you.want.to.update.Codenvy.property.'$PROPERTY_TO_TEST'.with.new.value.'$VALUE_TO_TEST'?.*"

executeSshCommand "sudo puppet agent --onetime --ignorecache --no-daemonize --no-usecacheonfailure --no-splay"

executeIMCommand "config $PROPERTY_TO_TEST"
validateExpectedString "$PROPERTY_TO_TEST=$VALUE_TO_TEST"

### change Codenvy hostname
executeIMCommand "config" "--hostname" "${NEW_HOST_URL}"

# verify changes on api node
executeSshCommand "sudo cat /home/codenvy/codenvy-data/conf/general.properties"
executeSshCommand "sudo grep \"api.endpoint=http://${NEW_HOST_URL}/api\" /home/codenvy/codenvy-data/conf/general.properties"

# verify changes on installation-manager service
executeSshCommand "sudo cat /home/codenvy-im/codenvy-im-data/conf/installation-manager.properties"
executeSshCommand "sudo grep \"api.endpoint=http://${NEW_HOST_URL}/api\" /home/codenvy-im/codenvy-im-data/conf/installation-manager.properties"

authWithoutRealmAndServerDns "admin" "password" "http://${NEW_HOST_URL}"


### shouldn't fail on unknown host url
executeIMCommand "config --hostname=unknown890934857203489520.com"
executeSshCommand "sudo grep \"127.0.0.1 unknown890934857203489520.com\" /etc/hosts"

executeSshCommand "echo y | codenvy config 'host_url' 685029345127unknown.com"
executeSshCommand "sudo grep \"127.0.0.1 685029345127unknown.com\" /etc/hosts"

### validate using custom config file
PATH_TO_INSTALL_DIR=codenvy
PATH_TO_CUSTOM_CONFIG="${PATH_TO_INSTALL_DIR}/codenvy.properties.${LATEST_CODENVY_VERSION}"
executeSshCommand "mv ${PATH_TO_INSTALL_DIR}/codenvy.properties $PATH_TO_CUSTOM_CONFIG"

installCodenvy --valid-exit-code=1 ${LATEST_CODENVY_VERSION}
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties | grep ${LATEST_CODENVY_VERSION}"
executeSshCommand --valid-exit-code=1 "test -f ${PATH_TO_INSTALL_DIR}/codenvy.properties.back"

# custom config file defined by url
installCodenvy --valid-exit-code=1 ${PREV_CODENVY_VERSION} --config=${CUSTOM_SINGLE_NODE_PREV_VERSION_CONFIG_URL}
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties | grep ${PREV_CODENVY_VERSION}"
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties.back | grep ${LATEST_CODENVY_VERSION}"

# custom config file defined by path
installCodenvy --valid-exit-code=1 ${LATEST_CODENVY_VERSION} --config=${PATH_TO_CUSTOM_CONFIG}
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties | grep ${LATEST_CODENVY_VERSION}"
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties.back | grep ${PREV_CODENVY_VERSION}"

installCodenvy --valid-exit-code=1 ${PREV_CODENVY_VERSION}
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties | grep ${PREV_CODENVY_VERSION}"
executeSshCommand "cat ${PATH_TO_INSTALL_DIR}/codenvy.properties.back | grep ${LATEST_CODENVY_VERSION}"

### test re-install
# remove codenvy binary
executeSshCommand "sudo rm -rf /home/codenvy/tomcat/webapps"
executeSshCommand "sudo rm -rf /home/codenvy-im/codenvy-im-tomcat/webapps"

# preform re-install
executeIMCommand "install" "--reinstall" "codenvy"
validateExpectedString ".*\"artifact\".\:.\"codenvy\".*\"status\".\:.\"SUCCESS\".*\"status\".\:.\"OK\".*"

validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

printAndLog "RESULT: PASSED"

vagrantDestroy
