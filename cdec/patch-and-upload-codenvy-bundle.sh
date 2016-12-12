#!/bin/bash
#
# CODENVY CONFIDENTIAL
# ________________
#
# [2012] - [2016] Codenvy, S.A.
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


# parameter 1 - server = [prod|stg|ngt]
# parameter 2 - PATH_TO_BUNDLE
# parameter 3 - BUNDLE_VERSION
# parameter 4 - PREV_CODENVY_VERSION
#
# For example: ./patch-and-upload-codenvy-bundle.sh ngt cdec-bundle-4.1.1.zip 4.1.1 4.0.1
# 1) repack "cdec-bundle-4.1.1.zip" into "/tmp/bundle/codenvy/4.1.1/codenvy-4.1.1.zip" with enclosed directory "/patches" with content from the directory "installation-manager-resources/src/main/resources/patches/4.1.1"
# 2) create "/tmp/bundle/codenvy/4.1.1/.properties" file where "previous-version=4.0.1"
# 3) upload "/tmp/bundle/codenvy/4.1.1/codenvy-4.1.1.zip" and "/tmp/bundle/codenvy/4.1.1/.properties" files into the directory "/home/codenvy/update-server-repository/codenvy/4.1.1" at the "ngt" server, that is Night Update Server with IP=updater.nightly4.codenvy-stg.com

SERVER=$1

PATH_TO_BUNDLE=$2
if [[ ! -n $PATH_TO_BUNDLE ]]; then
    echo "Local bundle destination is unknown";
    exit 1;
fi

BUNDLE_VERSION=$3
if [[ ! -n $BUNDLE_VERSION ]]; then
    echo "Bundle version is unknown";
    exit 1;
fi

PREV_CODENVY_VERSION=$4

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

uploadBundle() {
    local DESTINATION=update-server-repository/codenvy/${BUNDLE_VERSION}

    local BUNDLE=$TMP_DIR/$BUNDLE_NAME
    local PROPERTIES=$TMP_DIR/.properties

    local ARTIFACT="codenvy"
    local DESCRIPTION="Codenvy binaries"

    local MD5=`md5sum ${BUNDLE} | cut -d ' ' -f 1`
    local SIZE=`du -b ${BUNDLE} | cut -f1`
    local BUILD_TIME=`stat -c %y ${BUNDLE}`
    local BUILD_TIME=${BUILD_TIME:0:19}

    echo "file=codenvy-${BUNDLE_VERSION}.zip" > $PROPERTIES
    echo "artifact=${ARTIFACT}" >> $PROPERTIES
    echo "version=${BUNDLE_VERSION}" >> $PROPERTIES
    echo "label=STABLE" >> $PROPERTIES

    if [[ -n $PREV_CODENVY_VERSION ]]; then
        echo "previous-version="${PREV_CODENVY_VERSION} >> $PROPERTIES
    fi

    echo "description=${DESCRIPTION}" >> $PROPERTIES
    echo "authentication-required=false" >> $PROPERTIES
    echo "build-time="${BUILD_TIME} >> $PROPERTIES
    echo "md5=${MD5}" >> $PROPERTIES
    echo "size=${SIZE}" >> $PROPERTIES

    echo "ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} \"mkdir -p /home/${SSH_AS_USER_NAME}/${DESTINATION}\""

    ssh -i ${SSH_KEY_NAME} ${SSH_AS_USER_NAME}@${AS_IP} "mkdir -p /home/${SSH_AS_USER_NAME}/${DESTINATION}"
    scp -o StrictHostKeyChecking=no -i ${SSH_KEY_NAME} ${BUNDLE} ${SSH_AS_USER_NAME}@${AS_IP}:${DESTINATION}/
    scp -o StrictHostKeyChecking=no -i ${SSH_KEY_NAME} $TMP_DIR/.properties ${SSH_AS_USER_NAME}@${AS_IP}:${DESTINATION}/.properties
}

enclosePatches() {
    mkdir -p $TMP_DIR/tmp/patches
    cp -r $PATH_TO_PATCHES/* $TMP_DIR/tmp/patches

    unzip $TMP_DIR/$BUNDLE_NAME -d $TMP_DIR/tmp
    cd $TMP_DIR/tmp
    zip -r $BUNDLE_NAME *
    cd -
    mv -f $TMP_DIR/tmp/$BUNDLE_NAME $TMP_DIR
}

BUNDLE_NAME=codenvy-$BUNDLE_VERSION.zip
TMP_DIR=/tmp/bundle/codenvy/$BUNDLE_VERSION

rm -rf $TMP_DIR
mkdir -p $TMP_DIR
cp $PATH_TO_BUNDLE $TMP_DIR/$BUNDLE_NAME

PATH_TO_PATCHES=installation-manager-resources/src/main/resources/patches/$BUNDLE_VERSION
if [[ -d $PATH_TO_PATCHES ]]; then
    enclosePatches
fi

uploadBundle

