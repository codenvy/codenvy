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

. ./config.sh

trap cleanUp EXIT

cleanUp() {
    log "clean up on exit"
    vagrantDestroy
}

printAndLog() {
    echo $@
    log $@
}

logStartCommand() {
    log
    log "=== [ "`date`" ] COMMAND STARTED: "$@
}

logEndCommand() {
    log "=================================== COMMAND COMPLETED: "$@
    log
}

log() {
    echo "TEST: "$@ >> ${TEST_LOG}
}

validateExitCode() {
    EXIT_CODE=$1
    local validCode=$2
    IS_INSTALL_CODENVY=$3

    if [[ ! -z ${validCode} ]]; then
        if [[ ${EXIT_CODE} == ${validCode} ]]; then
            return
        fi
    else
        if [[ ${EXIT_CODE} == "0" ]]; then
            return
        fi
    fi

    printAndLog "RESULT: FAILED"

    $(retrieveTestLogs)

    if [[ ! -z ${IS_INSTALL_CODENVY} ]]; then
        installLogContent=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@$(detectMasterNode) "cat install.log | sed 's/-\\\|\\///g'" | sed 's/\r//')
        printAndLog "============= Install.log file content ========="
        printAndLog "${installLogContent}"
        printAndLog "================================================="
    fi

    exit 1
}

retrieveTestLogs() {
    INSTALL_ON_NODE=$(detectMasterNode)
    if [[ -z $INSTALL_ON_NODE ]]; then
        exit
    fi

    logDirName="logs/`basename "$0" | sed 's/\\.sh//g'`"
    log "Name of directory with logs: ${logDirName}"
    [[ -d "${logDirName}" ]] && exit

    mkdir --parent ${logDirName}

    if [[ ${INSTALL_ON_NODE} == "master.${HOST_URL}" ]]; then
        for HOST in master api analytics data site runner1 builder1 datasource; do
            # store puppet logs
            executeSshCommand --bypass-validation "sudo chown -R root:root /var/log/puppet" ${HOST}.codenvy
            executeSshCommand --bypass-validation "sudo chmod 777 /var/log/puppet" ${HOST}.codenvy
            scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:/var/log/puppet/puppet-agent.log ${logDirName}/puppet-agent-${HOST}.log

            # store messages log
            executeSshCommand --bypass-validation "sudo cp /var/log/messages /home/vagrant/messages" ${HOST}.codenvy
            executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/messages" ${HOST}.codenvy
            scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:messages ${logDirName}/messages-${HOST}

            if [[ ${HOST} == "api" || ${HOST} == "analytics" || ${HOST} == "site" || ${HOST} == "runner1" || ${HOST} == "builder1" ]]; then
                # store Codenvy log
                executeSshCommand --bypass-validation "sudo cp /home/codenvy/codenvy-tomcat/logs/catalina.out /home/vagrant/codenvy-catalina.out" ${HOST}.codenvy   # for codenvy 3.x
                executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/codenvy-catalina.out" ${HOST}.codenvy
                scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:codenvy-catalina.out ${logDirName}/codenvy-catalina-${HOST}.out
            fi

            if [[ ${HOST} == "master" ]]; then
                # store IM Server log
                executeSshCommand --bypass-validation "sudo cp /home/codenvy-im/codenvy-im-tomcat/logs/catalina.out /home/vagrant/im-server-catalina.out" ${HOST}.codenvy
                executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/im-server-catalina.out" ${HOST}.codenvy
                scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${HOST}.codenvy:im-server-catalina-${HOST}.out ${logDirName}/
            fi
        done
    else
        # store puppet logs
        executeSshCommand --bypass-validation "sudo chown -R root:root /var/log/puppet"
        executeSshCommand --bypass-validation "sudo chmod 777 /var/log/puppet"
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/var/log/puppet/puppet-agent.log ${logDirName}/

        # store Codenvy log
        executeSshCommand --bypass-validation "sudo cp /home/codenvy/codenvy-tomcat/logs/catalina.out /home/vagrant/codenvy-catalina.out"   # for codenvy 3.x
        executeSshCommand --bypass-validation "sudo cp /home/codenvy/tomcat/logs/catalina.out /home/vagrant/codenvy-catalina.out"           # for codenvy 4.x
        executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/codenvy-catalina.out"
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:codenvy-catalina.out ${logDirName}/

        # store IM Server log
        executeSshCommand --bypass-validation "sudo cp /home/codenvy-im/codenvy-im-tomcat/logs/catalina.out /home/vagrant/im-server-catalina.out"
        executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/im-server-catalina.out"
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:im-server-catalina.out ${logDirName}/

        # store messages log
        executeSshCommand --bypass-validation "sudo cp /var/log/messages /home/vagrant/messages"
        executeSshCommand --bypass-validation "sudo chmod 777 /home/vagrant/messages"
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:messages ${logDirName}/
    fi

    scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/home/vagrant/codenvy/install.log ${logDirName}/install.log
    scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE}:/home/vagrant/codenvy/cli/logs/cli.log ${logDirName}/cli.log
}

vagrantDestroy() {
    if [[ $RHEL_OS == true ]]; then
        # remove RHEL OS subscription
        executeSshCommand --bypass-validation "sudo subscription-manager remove --all"
        executeSshCommand --bypass-validation "sudo subscription-manager unregister"
    fi

    vagrant destroy -f >> ${TEST_LOG}
}

validateInstalledCodenvyVersion() {
    VERSION=$1

    [[ -z ${VERSION} ]] && VERSION=${LATEST_CODENVY3_VERSION}
    logStartCommand "validateInstalledCodenvyVersion "${VERSION}

    executeIMCommand "install" "--list"
    validateExpectedString ".*\"artifact\".*\:.*\"codenvy\".*\"version\".*\:.*\"${VERSION}\".*\"status\".*\:.*\"SUCCESS\".*"

    logEndCommand "validateInstalledCodenvyVersion"
}

validateInstalledImCliClientVersion() {
    VERSION=$1

    [[ -z ${VERSION} ]] && VERSION=${LATEST_IM_CLI_CLIENT_VERSION}

    logStartCommand "validateInstalledImCliClientVersion "${VERSION}

    executeIMCommand "install" "--list"
    validateExpectedString ".*\"artifact\".*\:.*\"installation-manager-cli\".*\"version\".*\:.*\"${VERSION}\".*\"status\".*\:.*\"SUCCESS\".*"

    logEndCommand "validateInstalledImCliClientVersion"
}

installCodenvy() {
    MULTI_OPTION=""
    VERSION_OPTION=""
    INSTALL_ON_NODE=$(detectMasterNode)

    validCode=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        validCode=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi

    if [[ ${INSTALL_ON_NODE} == "master.${HOST_URL}" ]]; then
        scp -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key -P 2222 ~/.vagrant.d/insecure_private_key vagrant@127.0.0.1:./.ssh/id_rsa >> ${TEST_LOG}
        MULTI_OPTION="--multi"
    fi

    VERSION=$1
    if [[ ! -z ${VERSION} ]]; then
        VERSION_OPTION="--version="${VERSION}
        shift
    fi

    logStartCommand "installCodenvy $VERSION_OPTION $@"

    ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "export TERM='xterm' && bash <(curl -L -s ${UPDATE_SERVICE}/repository/public/download/install-codenvy) --suppress --license=accept ${MULTI_OPTION} ${VERSION_OPTION} $@" >> ${TEST_LOG}
    EXIT_CODE=$?

    # to get last several rows from test log file
    OUTPUT=$(tail ${TEST_LOG})

    validateExitCode ${EXIT_CODE} ${validCode} --installCodenvy

    logEndCommand "installCodenvy"
}

installImCliClient() {
    logStartCommand "installImCliClient "$@
    INSTALL_ON_NODE=$(detectMasterNode)

    local validCode=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        validCode=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi

    VERSION=$1
    VERSION_OPTION=""
    if [[ ! -z ${VERSION} ]]; then
        VERSION_OPTION="--version="${VERSION}
        shift
    fi

    ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${INSTALL_ON_NODE} "export TERM='xterm' && bash <(curl -L -s ${UPDATE_SERVICE}/repository/public/download/install-im-cli) --license=accept ${VERSION_OPTION} $@" >> ${TEST_LOG}
    EXIT_CODE=$?

    # to get last several rows from test log file
    OUTPUT=$(tail ${TEST_LOG})

    validateExitCode ${EXIT_CODE} ${validCode}

    logEndCommand "installImCliClient"
}

vagrantUp() {
    VAGRANT_FILE=$1

    cp ${VAGRANT_FILE} Vagrantfile

    vagrant up >> ${TEST_LOG}
    validateExitCode $?
}

auth() {
    doAuth $1 $2 "sysldap" $3
}

authOnSite() {
    doAuth $1 $2 "org" $3
}

authWithoutRealmAndServerDns() {
    doAuth $1 $2
}

doAuth() {
    logStartCommand "auth "$@

    USERNAME=$1
    PASSWORD=$2
    REALM=$3
    SERVER_DNS=$4

    [[ -z ${SERVER_DNS} ]] && SERVER_DNS="http://${HOST_URL}"

    if [[ -n ${REALM} ]]; then
        local REALM_PARAMETER=", \"realm\":\"${REALM}\""
    fi

    OUTPUT=$(curl -s -X POST -H "Content-Type: application/json" -d "{\"username\":\"${USERNAME}\", \"password\":\"${PASSWORD}\"${REALM_PARAMETER}}" ${SERVER_DNS}/api/auth/login)

    EXIT_CODE=$?

    log ${OUTPUT}
    validateExitCode $?

    fetchJsonParameter "value"
    TOKEN=${OUTPUT}

    logEndCommand "auth"
}

executeIMCommand() {
    logStartCommand "executeIMCommand "$@

    local validCode=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        validCode=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi
    EXECUTE_ON_NODE=$(detectMasterNode)

    OUTPUT=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${EXECUTE_ON_NODE} "codenvy $@")
    EXIT_CODE=$?

    log ${OUTPUT}
    validateExitCode ${EXIT_CODE} ${validCode}

    logEndCommand "executeIMCommand"
}

# --valid-exit-code=N
# --bypass-validation
executeSshCommand() {
    local validCode=0
    if [[ $1 =~ --valid-exit-code=.* ]]; then
        validCode=`echo "$1" | sed -e "s/--valid-exit-code=//g"`
        shift
    fi

    bypassValidation=false
    if [[ $1 =~ --bypass-validation ]]; then
        bypassValidation=true
        shift
    fi

    if [[ $bypassValidation == false ]]; then
        logStartCommand "executeSshCommand "$@
    fi

    COMMAND=$1

    EXECUTE_ON_NODE=$2
    [[ -z ${EXECUTE_ON_NODE} ]] && EXECUTE_ON_NODE=$(detectMasterNode)

    OUTPUT=$(ssh -o StrictHostKeyChecking=no -i ~/.vagrant.d/insecure_private_key vagrant@${EXECUTE_ON_NODE} "${COMMAND}")
    EXIT_CODE=$?

    if [[ $bypassValidation == false ]]; then
        log ${OUTPUT}
        validateExitCode ${EXIT_CODE} ${validCode}
        logEndCommand "executeSshCommand"
    fi
}

detectMasterNode() {
    ping -c1 -q "master.${HOST_URL}" >> /dev/null
    if [[ $? == 0 ]]; then
        echo "master.${HOST_URL}"
    else
        ping -c1 -q ${HOST_URL} >> /dev/null
        if [[ $? == 0 ]]; then
            echo ${HOST_URL}
        fi
    fi
}

fetchJsonParameter() {
    validateExpectedString ".*\"$1\".*"
    OUTPUT=`echo ${OUTPUT} | sed 's/.*"'$1'"\s*:\s*"\([^"]*\)*".*/\1/'`
}

# --method={POST|GET|...}
# --content-type=...
# --cookie=...
# --body=...
# --url=...
# --output-http-code
# --verbose
doHttpRequest() {
    for var in "$@"; do
        if [[ "$var" =~ --content-type=.* ]]; then
            CONTENT_TYPE_OPTION=`echo "-H \"Content-Type: $var\"" | sed -e "s/--content-type=//g"`

        elif [[ "$var" =~ --body=.* ]]; then
            local BODY_OPTION=`echo "-d '$var'" | sed -e "s/--body=//g"`

        elif [[ "$var" =~ --url=.* ]]; then
            local URL=`echo "'$var'" | sed -e "s/--url=//g"`

        elif [[ "$var" =~ --method=.* ]]; then
            local METHOD_OPTION=`echo "-X $var" | sed -e "s/--method=//g"`
            
        elif [[ "$var" == "--output-http-code" ]]; then
            local OUTPUT_HTTP_CODE_OPTION="-o /dev/null -w \"%{http_code}\""

        elif [[ "$var" == "--verbose" ]]; then
            local VERBOSE_OPTION="-v"

        elif [[ "$var" =~ --cookie=.* ]]; then
            local COOKIE_OPTION=$(echo "-H \"Cookie: session-access-key=$var\"" | sed -e "s/--cookie=//g")

        fi
    done

    local COMMAND="curl -s $VERBOSE_OPTION $OUTPUT_HTTP_CODE_OPTION $CONTENT_TYPE_OPTION $COOKIE_OPTION $BODY_OPTION $METHOD_OPTION $URL"
    
    logStartCommand $COMMAND
    
    OUTPUT=$(eval $COMMAND)
    EXIT_CODE=$?
    log ${OUTPUT}

    validateExitCode ${EXIT_CODE}

    logEndCommand "curl"
}

doPost() {
    doHttpRequest --method=POST \
                  --content-type=$1 \
                  --body="$2" \
                  --url=$3 \
                  --cookie=$4
}

doGet() {
    doHttpRequest --method=GET \
                  --url=$1
}

createDefaultFactory() {
    logStartCommand "createDefaultFactory"

    TOKEN=$1

    OUTPUT=$(curl "http://${HOST_URL}/api/factory/?token="${TOKEN} -H 'Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7yqwdS1Jq8TWiUAE'  --data-binary $'------WebKitFormBoundary7yqwdS1Jq8TWiUAE\r\nContent-Disposition: form-data; name="factoryUrl"\r\n\r\n{\r\n  "v": "2.1",\r\n  "project": {\r\n    "name": "my-minimalistic-factory",\r\n    "description": "Minimalistic Template"\r\n  },\r\n  "source": {\r\n    "project": {\r\n      "location": "https://github.com/codenvy/sdk",\r\n      "type": "git"\r\n    }\r\n  }\r\n}\r\n------WebKitFormBoundary7yqwdS1Jq8TWiUAE--\r\n')
    EXIT_CODE=$?
    log ${OUTPUT}

    validateExitCode ${EXIT_CODE}

    logEndCommand "createDefaultFactory"
}

validateExpectedString() {
    logStartCommand "validateRegex "$@

    [[ ${OUTPUT} =~ $1 ]] || validateExitCode 1

    logEndCommand "validateRegex"
}

validateErrorString() {
    logStartCommand "validateErrorRegex "$@

    [[ ${OUTPUT} =~ $1 ]] && validateExitCode 1

    logEndCommand "validateRegex"
}

# $1 - NUMBER[SUFFIX]: Pause for NUMBER seconds.  SUFFIX may be 's' for seconds (the default), 'm' for minutes, 'h' for hours or 'd' for days.
# $2 - description to log
doSleep() {
    local TIME_TO_WAIT=$1

    local DESCRIPTION=$2
    [[ ! -z ${DESCRIPTION} ]] && log ${DESCRIPTION}

    executeSshCommand "sleep $TIME_TO_WAIT"
}

addCodenvyLicenseConfiguration() {
    local CONF_DIR='$HOME/che_conf'
    executeSshCommand "sed -i '1iexport CHE_LOCAL_CONF_DIR=$CONF_DIR' ~/.bashrc"
    executeSshCommand "if [ ! -d $CONF_DIR ]; then mkdir $CONF_DIR; fi"
    executeSshCommand "echo license-manager.public_key=$CODENVY_LICENSE_PUBLIC_KEY >> $CONF_DIR/installation-manager.properties"
}

storeCodenvyLicense() {
    storeStorageProperty "codenvy-license-key" "$CODENVY_LICENSE"
}

storeStorageProperty() {
    local property=$1
    local value=$2
    executeSshCommand "if [ ! -d $STORAGE_DIR ]; then sudo mkdir -p $STORAGE_DIR; fi"
    executeSshCommand "if [ ! -f $STORAGE_FILE ]; then sudo touch $STORAGE_FILE; fi"

    # add some text. the following grep command won't fail
    executeSshCommand "sudo bash -c 'echo ${property}= >> ${STORAGE_FILE}'"
    executeSshCommand "sudo sed -i '1 a #####' ${STORAGE_FILE}"

    executeSshCommand "sudo cat $STORAGE_FILE | grep -v ${property}= >> /tmp/tmp.tmp"
    executeSshCommand "sudo mv /tmp/tmp.tmp $STORAGE_FILE"
    executeSshCommand "sudo bash -c 'echo ${property}=${value} >> ${STORAGE_FILE}'"
}
