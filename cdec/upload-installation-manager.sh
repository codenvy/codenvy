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

SERVER=$1
if [ "${SERVER}" == "prod" ]; then
    echo "============[ Production will be updated ]=============="
    SSH_KEY_NAME=~/.ssh/cl-server-prod-20130219
    SSH_AS_USER_NAME=codenvy
    AS_IP=updater.codenvycorp.com
elif [ "${SERVER}" == "stg" ]; then
    echo "============[ Staging will be updated ]=============="
    SSH_KEY_NAME=~/.ssh/as1-cldide_cl-server.skey
    SSH_AS_USER_NAME=codenvy
    AS_IP=updater.codenvy-stg.com
elif [ "${SERVER}" == "ngt" ]; then
    echo "============[ Nightly will be updated ]=============="
    SSH_KEY_NAME=~/.ssh/as1-cldide_cl-server.skey
    SSH_AS_USER_NAME=codenvy
    AS_IP=updater.nightly4.codenvy-stg.com
else
    echo "Unknown server destination"
    exit 1
fi

if [ ! -z "$2" ]; then
    SSH_KEY_NAME=$2
fi

uploadInstallationManagerCli() {
    FILE="installation-manager"
    ARTIFACT=${FILE}"-cli"

    DESCRIPTION="Codenvy Installation manager"
    FILENAME=`ls ${ARTIFACT}-assembly/target | grep -G ${FILE}-.*-binary[.]tar.gz`
    VERSION=`ls ${ARTIFACT}-assembly/target | grep -G ${FILE}-.*[.]jar | grep -vE 'sources|original' | sed 's/'${FILE}'-//' | sed 's/.jar//'`
    SOURCE=${ARTIFACT}-assembly/target/${FILENAME}
    doUpload
}

uploadCodenvyServerInstallScript() {
    ARTIFACT=install-codenvy
    FILENAME=install-codenvy.sh
    SOURCE=installation-manager-resources/src/main/resources/im-scripts/${FILENAME}
    DESCRIPTION="Script to install Codenvy in single-server configuration"

    if [[ -f ${SOURCE} ]]; then
        doUpload
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/codenvy.com/c4.codenvy-stg.com/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/https:\/\/codenvy.com/http:\/\/updater.nightly4.codenvy-stg.com/g' ${DESTINATION}/${FILENAME}"
    fi
}

uploadCodenvyServerInstallMultiScript() {
    ARTIFACT=install-codenvy-multi
    FILENAME=install-codenvy-multi.sh
    SOURCE=installation-manager-resources/src/main/resources/im-scripts/${FILENAME}
    DESCRIPTION="Script to install Codenvy in multi-server configuration"

    if [[ -f ${SOURCE} ]]; then
        doUpload
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/codenvy.com/c4.codenvy-stg.com/g' ${DESTINATION}/${FILENAME}"
    fi
}

uploadCodenvyServerInstallInstallationManagerScript() {
    ARTIFACT=install-im-cli
    FILENAME=install-im-cli.sh
    SOURCE=installation-manager-resources/src/main/resources/im-scripts/${FILENAME}
    DESCRIPTION="Script to install Codenvy installation manager"

    if [[ -f ${SOURCE} ]]; then
        doUpload
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/codenvy.com/c4.codenvy-stg.com/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/https:\/\/start.codenvy.com\/install-single/http:\/\/updater.nightly4.codenvy-stg.com\/update\/repository\/public\/download\/install-codenvy/g' ${DESTINATION}/${FILENAME}"
    fi
}

uploadCodenvySingleServerInstallProperties() {
    ARTIFACT=codenvy-single-server-properties
    FILENAME=codenvy.properties
    VERSION=$1
    SOURCE=installation-manager-resources/src/main/resources/codenvy-properties/${VERSION}/codenvy-single-server.properties
    DESCRIPTION="Codenvy single-server installation properties"

    if [[ -f ${SOURCE} ]]; then
        doUpload
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/saas_api_endpoint=https:\/\/codenvy.com\/api/saas_api_endpoint=https:\/\/c4.codenvy-stg.com\/api/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/installation_manager_update_server_endpoint=https:\/\/codenvy.com\/update/installation_manager_update_server_endpoint=https:\/\/c4.codenvy-stg.com\/update/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/saas_api_endpoint=https:\/\/codenvy.com\/api/saas_api_endpoint=http:\/\/a1.codenvy-dev.com\/api/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/installation_manager_update_server_endpoint=https:\/\/codenvy.com\/update/installation_manager_update_server_endpoint=http:\/\/updater.nightly4.codenvy-stg.com\/update/g' ${DESTINATION}/${FILENAME}"
    fi
}

uploadCodenvyMultiServerInstallProperties() {
    ARTIFACT=codenvy-multi-server-properties
    FILENAME=codenvy.properties
    VERSION=$1
    SOURCE=installation-manager-resources/src/main/resources/codenvy-properties/${VERSION}/codenvy-multi-server.properties
    DESCRIPTION="Codenvy multi-server installation properties"

    if [[ -f ${SOURCE} ]]; then
        doUpload
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/saas_api_endpoint=https:\/\/codenvy.com\/api/saas_api_endpoint=https:\/\/c4.codenvy-stg.com\/api/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "stg" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/installation_manager_update_server_endpoint=https:\/\/codenvy.com\/update/installation_manager_update_server_endpoint=https:\/\/c4.codenvy-stg.com\/update/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/saas_api_endpoint=https:\/\/codenvy.com\/api/saas_api_endpoint=http:\/\/a1.codenvy-dev.com\/api/g' ${DESTINATION}/${FILENAME}"
        [ "${SERVER}" == "ngt" ] && ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "sed -i 's/installation_manager_update_server_endpoint=https:\/\/codenvy.com\/update/installation_manager_update_server_endpoint=http:\/\/updater.nightly4.codenvy-stg.com\/update/g' ${DESTINATION}/${FILENAME}"
    fi
}

doUpload() {
    DESTINATION=update-server-repository/${ARTIFACT}/${VERSION}

    MD5=`md5sum ${SOURCE} | cut -d ' ' -f 1`
    SIZE=`du -b ${SOURCE} | cut -f1`
    BUILD_TIME=`stat -c %y ${SOURCE}`
    BUILD_TIME=${BUILD_TIME:0:19}

    echo "file=${FILENAME}" > .properties
    echo "artifact=${ARTIFACT}" >> .properties
    echo "version=${VERSION}" >> .properties
    echo "description=${DESCRIPTION}" >> .properties
    echo "authentication-required=false" >> .properties
    echo "build-time="${BUILD_TIME} >> .properties
    echo "md5=${MD5}" >> .properties
    echo "size=${SIZE}" >> .properties

    ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "mkdir -p /home/${SSH_AS_USER_NAME}/${DESTINATION}"
    scp -o StrictHostKeyChecking=no -i ${SSH_KEY_NAME} ${SOURCE} ${SSH_AS_USER_NAME}@${AS_IP}:${DESTINATION}/${FILENAME}
    scp -o StrictHostKeyChecking=no -i ${SSH_KEY_NAME} .properties ${SSH_AS_USER_NAME}@${AS_IP}:${DESTINATION}/.properties

    rm .properties
}

uploadInstallationManagerCli
uploadCodenvyServerInstallMultiScript
uploadCodenvyServerInstallInstallationManagerScript
uploadCodenvyServerInstallScript

for DIR in installation-manager-resources/src/main/resources/codenvy-properties/*; do
    VERSION=`basename ${DIR}`
    uploadCodenvySingleServerInstallProperties ${VERSION}
done

for DIR in installation-manager-resources/src/main/resources/codenvy-properties/*; do
    VERSION=`basename ${DIR}`
    uploadCodenvyMultiServerInstallProperties ${VERSION}
done
