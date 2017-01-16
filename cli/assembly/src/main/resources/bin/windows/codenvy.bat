@REM
@REM Copyright (c) [2012] - [2017] Codenvy, S.A.
@REM All rights reserved. This program and the accompanying materials
@REM are made available under the terms of the Eclipse Public License v1.0
@REM which accompanies this distribution, and is available at
@REM http://www.eclipse.org/legal/epl-v10.html
@REM
@REM Contributors:
@REM   Codenvy, S.A. - initial API and implementation
@REM

@echo off
rem
rem Copyright (c) 2012-2014 Codenvy, S.A.
rem All rights reserved. This program and the accompanying materials
rem are made available under the terms of the Eclipse Public License v1.0
rem which accompanies this distribution, and is available at
rem http://www.eclipse.org/legal/epl-v10.html
rem
rem Contributors:
rem   Codenvy, S.A. - initial API and implementation
rem

if not "%ECHO%" == "" echo %ECHO%

setlocal
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

rem Sourcing environment settings for karaf similar to tomcats setenv
SET KARAF_SCRIPT="shell.bat"
if exist "%DIRNAME%setenv.bat" (
  call "%DIRNAME%setenv.bat"
)

rem Check console window title. Set to Karaf by default
if not "%KARAF_TITLE%" == "" (
    title %KARAF_TITLE%
) else (
    title Karaf
)

rem Check/Set up some easily accessible MIN/MAX params for JVM mem usage
if "%JAVA_MIN_MEM%" == "" (
    set JAVA_MIN_MEM=128M
)
if "%JAVA_MAX_MEM%" == "" (
    set JAVA_MAX_MEM=512M
)

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

if not "%KARAF_HOME%" == "" (
    call :warn Ignoring predefined value for KARAF_HOME
)
set KARAF_HOME=%DIRNAME%..
if not exist "%KARAF_HOME%" (
    call :warn KARAF_HOME is not valid: "%KARAF_HOME%"
    goto END
)

if not "%KARAF_BASE%" == "" (
    if not exist "%KARAF_BASE%" (
       call :warn KARAF_BASE is not valid: "%KARAF_BASE%"
       goto END
    )
)
if "%KARAF_BASE%" == "" (
  set "KARAF_BASE=%KARAF_HOME%"
)

if not "%KARAF_DATA%" == "" (
    if not exist "%KARAF_DATA%" (
        call :warn KARAF_DATA is not valid: "%KARAF_DATA%"
        goto END
    )
)
if "%KARAF_DATA%" == "" (
    set "KARAF_DATA=%KARAF_BASE%\data"
)

if not "%KARAF_ETC%" == "" (
    if not exist "%KARAF_ETC%" (
        call :warn KARAF_ETC is not valid: "%KARAF_ETC%"
        goto END
    )
)
if "%KARAF_ETC%" == "" (
    set "KARAF_ETC=%KARAF_BASE%\etc"
)

set DEFAULT_JAVA_OPTS=
set DEFAULT_JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005

rem Support for loading native libraries
set PATH=%PATH%;%KARAF_BASE%\lib;%KARAF_HOME%\lib

rem Setup the Java Virtual Machine
if not "%JAVA%" == "" goto :Check_JAVA_END
    set JAVA=java
    if "%JAVA_HOME%" == "" call :warn JAVA_HOME not set; results may vary
    if not "%JAVA_HOME%" == "" set JAVA=%JAVA_HOME%\bin\java
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
:Check_JAVA_END

if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JAVA_OPTS%

if "%EXTRA_JAVA_OPTS%" == "" goto :KARAF_EXTRA_JAVA_OPTS_END
    set JAVA_OPTS="%JAVA_OPTS% %EXTRA_JAVA_OPTS%"
:KARAF_EXTRA_JAVA_OPTS_END

if "%KARAF_DEBUG%" == "" goto :KARAF_DEBUG_END
    rem Use the defaults if JAVA_DEBUG_OPTS was not set
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%

    set JAVA_OPTS="%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    call :warn Enabling Java debug options: %JAVA_DEBUG_OPTS%
:KARAF_DEBUG_END

set COMMAND=%1
set CLASSPATH=%KARAF_HOME%\system\org\apache\karaf\shell\org.apache.karaf.shell.console\3.0.2\org.apache.karaf.shell.console-3.0.2.jar
set CLASSPATH=%CLASSPATH%;%KARAF_HOME%\system\org\ops4j\pax\logging\pax-logging-api\1.7.4\pax-logging-api-1.7.4.jar
set CLASSPATH=%CLASSPATH%;%KARAF_HOME%\system\jline\jline\2.12\jline-2.12.jar

:EXECUTE
set CODENVY_HOME=%KARAF_HOME%
set FIRSTARG=%1%
SHIFT

set ARGS=%1 %2 %3 %4 %5 %6 %7
    if "%FIRSTARG%"=="" (
         set KARAF_HOME=
         set KARAF_BASE=
         set KARAF_ETC=
         set KARAF_DATA=
         set CLASSPATH=
       "%CODENVY_HOME%\bin\interactive-mode"
    ) else (
        "%JAVA%" %JAVA_OPTS% %OPTS% -classpath "%CLASSPATH%" -Dkaraf.instances="%KARAF_HOME%\instances" -Dkaraf.home="%KARAF_HOME%" -Dkaraf.base="%KARAF_BASE%" -Dkaraf.etc="%KARAF_ETC%" -Djava.io.tmpdir="%KARAF_DATA%\tmp" -Djava.util.logging.config.file="%KARAF_BASE%\etc\java.util.logging.properties" %KARAF_OPTS% com.codenvy.cli.shell.main.Main --classpath="%KARAF_HOME%\system" codenvy:%COMMAND% %ARGS%
    )

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal
