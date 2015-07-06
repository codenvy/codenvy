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

if [ -z "${CODENVY_LOCAL_CONF_DIR}" ]; then
    echo "Need to set CODENVY_LOCAL_CONF_DIR"
    exit 1
fi

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms128m -Xmx512M -XX:MaxPermSize=128m -XX:+UseCompressedOops"

#Global LOGS DIR
[ -z "${CODENVY_LOGS_DIR}" ]  && CODENVY_LOGS_DIR="$CATALINA_HOME/logs"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="8000"

#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dcom.sun.management.jmxremote  \
                                             -Dcom.sun.management.jmxremote.ssl=false \
                                             -Dcom.sun.management.jmxremote.authenticate=false \
                                             -Dcodenvy.local.conf.dir=${CODENVY_LOCAL_CONF_DIR} \
                                             -Dcodenvy.logback.smtp.appender=${CODENVY_LOCAL_CONF_DIR}/logback-smtp-appender.xml \
                                             -Dcodenvy.syslog.appender=${CODENVY_LOCAL_CONF_DIR}/syslog-appender.xml"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"

export JAVA_OPTS="$JAVA_OPTS  -Dcodenvy.logs.dir=${CODENVY_LOGS_DIR}"

echo "Using LOCAL_CONF_DIR:  $CODENVY_LOCAL_CONF_DIR"



