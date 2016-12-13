#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

DIR=$(cd "$(dirname "$0")"; pwd)
DEPENDENCY_DIR="${DIR}/dependency"
POM_VERSION=$(cat ${DIR}/../../pom.xml | grep "^        <version>.*</version>$" | awk -F'[><]' '{print $3}')

rm -rf ${DEPENDENCY_DIR}
mkdir -p ${DEPENDENCY_DIR}

ARTIFACTS=("org.eclipse.che:exec-agent:${POM_VERSION}:tar.gz:linux_amd64"
           "org.eclipse.che:exec-agent:${POM_VERSION}:tar.gz:linux_arm7"
           "com.codenvy.onpremises:onpremises-ide-packaging-tomcat-ext-server:${POM_VERSION}:tar.gz");

for artifact in ${ARTIFACTS[@]}; do
    mvn dependency:copy -Dartifact=${artifact} -DoutputDirectory=${DEPENDENCY_DIR} -Dmdep.stripVersion=true
done
mv ${DEPENDENCY_DIR}/onpremises-ide-packaging-tomcat-ext-server.tar.gz ${DEPENDENCY_DIR}/ws-agent.tar.gz
