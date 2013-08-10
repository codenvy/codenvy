GOTO EndLicense

          CODENVY CONFIDENTIAL
          ________________

          [2012] - [2013] Codenvy, S.A.
          All Rights Reserved.
          NOTICE: All information contained herein is, and remains
          the property of Codenvy S.A. and its suppliers,
          if any. The intellectual and technical concepts contained
          herein are proprietary to Codenvy S.A.
          and its suppliers and may be covered by U.S. and Foreign Patents,
          patents in process, and are protected by trade secret or copyright law.
          Dissemination of this information or reproduction of this material
          is strictly forbidden unless prior written permission is obtained
          from Codenvy S.A..
:EndLicense

@ECHO OFF
REM Environment Variable Prerequisites

REM Global JAVA options
SET JAVA_OPTS=-Xms256m -Xmx1536m -XX:MaxPermSize=256m -XX:+UseCompressedOops

REM Sets some variables
SET SECURITY_OPTS=-Djava.security.auth.login.config=%CATALINA_HOME%/conf/jaas.conf

SET ANALYTICS_OPTS=-Danalytics.logs.directory=C:/cygwin/home/tolusha/pig/logs/logs-production
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.scripts.directory=%CATALINA_HOME%/scripts
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.result.directory=%CATALINA_HOME%/data/results
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.metrics.initial.values=%CATALINA_HOME%/analytics-conf/initial-values.xml
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Danalytics.acton.ftp.properties=%CATALINA_HOME%/analytics-conf/acton-ftp.properties
SET ANALYTICS_OPTS=%ANALYTICS_OPTS% -Dcom.codenvy.analytics.logpath=%CATALINA_HOME%/logs


SET QUARTZ_OPTS=-Dorg.terracotta.quartz.skipUpdateCheck=true

SET JMX_OPTS=-Dcom.sun.management.jmxremote.authenticate=true
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.password.file=%CATALINA_HOME%/conf/jmxremote.password
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.access.file=%CATALINA_HOME%/conf/jmxremote.access
SET JMX_OPTS=%JMX_OPTS% -Dcom.sun.management.jmxremote.ssl=false

REM uncomment if you want to debug app server
REM REMOTE_DEBUG=-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y
SET REMOTE_DEBUG=

SET JAVA_OPTS=%JAVA_OPTS% %SECURITY_OPTS% %ANALYTICS_OPTS% %JMX_OPTS% %REMOTE_DEBUG% %QUARTZ_OPTS%

SET CLASSPATH=%CATALINA_HOME%/conf/;%CATALINA_HOME%/lib/jul-to-slf4j.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/slf4j-api.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/logback-classic.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/logback-core.jar
SET CLASSPATH=%CLASSPATH%;%CATALINA_HOME%/lib/mail.jar

echo =======
echo %JAVA_OPT%
echo =======
