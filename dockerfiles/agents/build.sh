#!/bin/sh
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

IMAGE_NAME="codenvy/agents"
. $(cd "$(dirname "$0")"; pwd)/../build.include

# prepare
DIR=$(cd "$(dirname "$0")"; pwd)
DEPENDENCY_DIR="${DIR}/dependency"
POM_VERSION=$(cat ${DIR}/../../pom.xml | grep "^        <version>.*</version>$" | awk -F'[><]' '{print $3}')

rm -rf ${DEPENDENCY_DIR}

TERMINAL_AGENTS=("org.eclipse.che:exec-agent:${POM_VERSION}:tar.gz:linux_amd64"
                 "org.eclipse.che:exec-agent:${POM_VERSION}:tar.gz:linux_arm7");

for artifact in ${TERMINAL_AGENTS[@]}; do
    prefix=$(echo ${artifact} | cut -d ':' -f 5)
    mvn dependency:copy -Dartifact=${artifact} -DoutputDirectory=${DEPENDENCY_DIR}/${prefix}/terminal -Dmdep.stripVersion=true
    mv ${DEPENDENCY_DIR}/${prefix}/terminal/exec-agent-${prefix}.tar.gz ${DEPENDENCY_DIR}/${prefix}/terminal/websocket-terminal-${prefix}.tar.gz
done

mvn dependency:copy -Dartifact=com.codenvy.onpremises:onpremises-ide-packaging-tomcat-ext-server:${POM_VERSION}:tar.gz \
                    -DoutputDirectory=${DEPENDENCY_DIR} \
                    -Dmdep.stripVersion=true

mv ${DEPENDENCY_DIR}/onpremises-ide-packaging-tomcat-ext-server.tar.gz ${DEPENDENCY_DIR}/ws-agent.tar.gz

# build
init
build

# clean up
rm -rf ${DEPENDENCY_DIR}

