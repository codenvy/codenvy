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

printAndLog "TEST CASE: Setup and update Codenvy On Premise configuration"

vagrantUp ${SINGLE_NODE_VAGRANT_FILE}

installCodenvy ${LATEST_CODENVY_VERSION} --config=${CUSTOM_SINGLE_NODE_LATEST_VERSION_CONFIG_URL}
validateInstalledCodenvyVersion ${LATEST_CODENVY_VERSION}

PROPERTY_TO_TEST=zabbix_admin_email
VALUE_TO_TEST=root@localhost
VALUE_TO_UPDATE=user@localhost

executeIMCommand "config"
validateExpectedString ".*codenvy_admin_initial_password=*****.*installation_manager_update_server_endpoint=$UPDATE_SERVICE.*version=$LATEST_CODENVY_VERSION.*$PROPERTY_TO_TEST=$VALUE_TO_TEST.*"

executeIMCommand "config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_TEST.*"

executeSshCommand "echo y | codenvy config $PROPERTY_TO_TEST $VALUE_TO_UPDATE"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_UPDATE.*"

executeIMCommand "config $PROPERTY_TO_TEST"
validateExpectedString ".*$PROPERTY_TO_TEST=$VALUE_TO_UPDATE.*"

# shouldn't fail on unknown host url
executeIMCommand "config --hostname=unknown890934857203489520.com"
executeSshCommand "sudo grep \"127.0.0.1 unknown890934857203489520.com\" /etc/hosts"

executeSshCommand "echo y | codenvy config 'host_url' 685029345127unknown.com"
executeSshCommand "sudo grep \"127.0.0.1 685029345127unknown.com\" /etc/hosts"

# validate using custom config file
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

printAndLog "RESULT: PASSED"

vagrantDestroy
