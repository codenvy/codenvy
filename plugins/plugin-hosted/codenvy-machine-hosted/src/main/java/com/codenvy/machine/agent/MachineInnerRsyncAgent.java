/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.machine.agent;


import org.eclipse.che.api.agent.shared.model.impl.AgentImpl;

import java.util.Collections;

/**
 * Agent that runs inside of dev machine container and ensures that rsync is available and
 * public key for SSH access is added to authorized keys.
 *
 * @author Alexander Garagatyi
 */
public class MachineInnerRsyncAgent extends AgentImpl {
    public MachineInnerRsyncAgent() {
        super("com.codenvy.rsync_in_machine",
              "Rsync sync agent",
              null,
              "Sync support",
              Collections.singletonList("org.eclipse.che.ssh"),
              Collections.emptyMap(),
              "#\n# Copyright (c) 2012-2016 Codenvy, S.A.\n" +
              "# All rights reserved. This program and the accompanying materials\n" +
              "# are made available under the terms of the Eclipse Public License v1.0\n" +
              "# which accompanies this distribution, and is available at\n" +
              "# http://www.eclipse.org/legal/epl-v10.html\n" +
              "#\n" +
              "# Contributors:\n" +
              "# Codenvy, S.A. - initial API and implementation\n" +
              "#\n\n" +
              "unset SUDO\n" +
              "unset PACKAGES\n" +
              "test \"$(id -u)\" = 0 || SUDO=\"sudo -E\"\n\n" +
              "LINUX_TYPE=$(cat /etc/os-release | grep ^ID= | tr '[:upper:]' '[:lower:]')\n" +
              "LINUX_VERSION=$(cat /etc/os-release | grep ^VERSION_ID=)\n\n" +
              "###############################\n" +
              "### Install Needed packaged ###\n" +
              "###############################\n\n" +
              "# Red Hat Enterprise Linux 7 \n" +
              "############################\n" +
              "if echo ${LINUX_TYPE} | grep -qi \"rhel\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} yum -y install ${PACKAGES};\n" +
              "    }\n" +
              "# Ubuntu 14.04 16.04 / Linux Mint 17 \n" +
              "####################################\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"ubuntu\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} apt-get update;\n" +
              "        ${SUDO} apt-get -y install ${PACKAGES};\n" +
              "    }\n" +
              "# Debian 8\n" +
              "##########\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"debian\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} apt-get update;\n" +
              "        ${SUDO} apt-get -y install ${PACKAGES};\n" +
              "    }\n" +
              "# Fedora 23\n" +
              "###########\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"fedora\"; then\n" +
              "    PACKAGES=${PACKAGES}\" procps-ng\"\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} dnf -y install ${PACKAGES};\n" +
              "    }\n" +
              "# CentOS 7.1 & Oracle Linux 7.1\n" +
              "###############################\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"centos\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} yum -y install ${PACKAGES};\n" +
              "    }\n" +
              "# openSUSE 13.2\n" +
              "###############\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"opensuse\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} zypper install -y ${PACKAGES};\n" +
              "    }\n" +
              "# Alpine 3.3\n" +
              "############$$\n" +
              "elif echo ${LINUX_TYPE} | grep -qi \"alpine\"; then\n" +
              "    command -v rsync >/dev/null 2>&1 || { PACKAGES=${PACKAGES}\" rsync\"; }\n" +
              "    test \"${PACKAGES}\" = \"\" || {\n" +
              "        ${SUDO} apk update;\n" +
              "        ${SUDO} apk add rsync ${PACKAGES};\n" +
              "    }\n\n" +
              "else\n" +
              "    >&2 echo \"Unrecognized Linux Type\"\n" +
              "    >&2 cat /etc/os-release\n" +
              "    exit 1\n" +
              "fi\n\n" +
              "mkdir -p ~/.ssh\n" +
              "echo ${CODENVY_SYNC_PUB_KEY} >> ~/.ssh/authorized_keys\n" +
              "echo \"Rsync agent installed\"",
              null);
    }
}
