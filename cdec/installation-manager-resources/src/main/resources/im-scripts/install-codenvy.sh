#!/bin/bash

# bash <(curl -L -s https://start.codenvy.com/install-single)
#
# allowed options:
# --multi
# --silent
# --suppress
# --version=<VERSION TO INSTALL>
# --hostname=<CODENVY HOSTNAME>
# --license=accept
# --install-directory=<PATH TO SINGLE DIRECTORY FOR ALL RESOURCES>
# --im-cli: to install IM CLI client only
# --config=<PATH/URL TO CUSTOM CODENVY CONFIG>
# --docker-registry-mirror=<URL>

# --http-proxy-for-installation=<HTTP PROXY URL>
# --https-proxy-for-installation=<HTTPS PROXY URL>
# --https-no-proxy-for-installation=<NO_PROXY SETTING>

# --http-proxy-for-codenvy=<HTTP PROXY URL>
# --https-proxy-for-codenvy=<HTTPS PROXY URL>
# --no-proxy-for-codenvy=<NO_PROXY_FOR_CODENVY PROPERTY>

# --http-proxy-for-codenvy-workspaces=<HTTP PROXY URL>
# --https-proxy-for-codenvy-workspaces=<HTTPS PROXY URL>
# --no-proxy-for-codenvy-workspaces=<NO_PROXY_FOR_CODENVY_WORKSPACES PROPERTY>

# --http-proxy-for-docker-daemon=<HTTP PROXY URL>
# --https-proxy-for-docker-daemon=<HTTPS PROXY URL>
# --no-proxy-for-docker-daemon=<NO_PROXY_FOR_DOCKER_DAEMON>

# --disable-monitoring-tools

# --skip-post-flight-check

# --advertise-network-interface=<NETWORK_INTERFACE_WHICH_IS_USED_BY_THE_DOCKER_FOR_ADVERTISING>

trap cleanUp EXIT

unset HOST_NAME
unset HTTP_PROXY_FOR_INSTALLATION
unset HTTPS_PROXY_FOR_INSTALLATION
unset NO_PROXY_FOR_INSTALLATION
unset ADVERTISE_NETWORK_INTERFACE

JDK_URL=http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jdk-8u45-linux-x64.tar.gz
JRE_URL=http://download.oracle.com/otn-pub/java/jdk/8u45-b14/jre-8u45-linux-x64.tar.gz

PUPPET_AGENT_PACKAGE=puppet-3.8.6-1.el7.noarch
PUPPET_SERVER_PACKAGE=puppet-server-3.8.6-1.el7.noarch

EXTERNAL_DEPENDENCIES=("https://codenvy.com||0"
                       "https://install.codenvycorp.com||0"
                       "http://dl.fedoraproject.org/pub/epel/||1"
                       "http://nginx.org/packages/centos/||1"
                       "http://yum.postgresql.org/||0"
                       "https://yum.puppetlabs.com/||1"
                       "http://repo.zabbix.com/zabbix/||0"
                       "${JDK_URL}|Cookie:oraclelicense=accept-securebackup-cookie|0"
                       "http://mirror.centos.org/centos||0"
                       "http://mirrorlist.centos.org||0");

RHEL_REPOS=("rhel-7-server-optional-rpms"
            "rhel-7-server-extras-rpms");

CURRENT_STEP=0
INSTALLATION_STEPS=("Configuring system..."
                    "Installing required packages... [java]"
                    "Install the Codenvy installation manager..."
                    "Downloading Codenvy binaries... "
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Installing Codenvy... ~10 mins"
                    "Booting Codenvy... "
                    "");

PUPPET_MASTER_PORTS=("tcp:8140");
SITE_PORTS=("tcp:80" "tcp:443" "tcp:10050" "tcp:32001" "tcp:32101");
API_PORTS=("tcp:8080" "tcp:8180" "tcp:10050" "tcp:32001" "tcp:32101" "tcp:32201" "tcp:32301");
DATA_PORTS=("tcp:389" "tcp:5432" "tcp:10050" "tcp:27017" "tcp:28017");
DATASOURCE_PORTS=("tcp:8080" "tcp:10050" "tcp:32001" "tcp:32101");
RUNNER_PORTS=("tcp:80" "tcp:8080" "tcp:10050" "tcp:32001" "tcp:32101");
BUILDER_PORTS=("tcp:8080" "tcp:10050" "tcp:32001" "tcp:32101");
ANALYTICS_PORTS=("tcp:7777" "tcp:8080" "udp:5140" "tcp:9763" "tcp:10050" "tcp:32001" "tcp:32101");

INTERNET_CHECKER_PID=
TIMER_PID=
PRINTER_PID=
LINE_UPDATER_PID=
DOWNLOAD_PROGRESS_UPDATER_PID=
LOADER_PID=

STEP_LINE=
PUPPET_LINE=
PROGRESS_LINE=
TIMER_LINE=
CURL_PROXY_OPTION=

DEPENDENCIES_STATUS_OFFSET=85  # fit screen width = 100 cols
PROGRESS_FACTOR=2
URL_REGEX="^https?:\/\/[\da-zA-Z_\-\.\/?&#+]+"

MIN_OS_VERSION=7.1

UNRECOGNIZED_PARAMETERS=()

cleanUp() {
    setterm -cursor on
    killTimer
    killPuppetInfoPrinter
    killInternetAccessChecker
    killFooterUpdater
    killDownloadProgressUpdater
    killLoader
}

validateExitCode() {
    local exitCode=$1
    if [[ -n "${exitCode}" ]] && [[ ! "${exitCode}" == "0" ]]; then
        pauseTimer
        pausePuppetInfoPrinter
        pauseInternetAccessChecker
        pauseFooterUpdater
        pauseDownloadProgressUpdater
        println
        println $(printError "Unexpected error occurred. See ${INSTALL_LOG} for more details")
        exit ${exitCode}
    fi
}

setRunOptions() {
    ARTIFACT="codenvy"
    CODENVY_TYPE="single"
    SILENT=false
    SUPPRESS=false
    LICENSE_ACCEPTED=false
    INSTALL_DIR=./codenvy
    DISABLE_MONITORING_TOOLS=false
    SKIP_POST_FLIGHT_CHECK=false

    local i=0

    for var in "$@"; do
        if [[ "$var" == "--multi" ]]; then
            CODENVY_TYPE="multi"

        elif [[ "$var" == "--silent" ]]; then
            SILENT=true

        elif [[ "$var" == "--suppress" ]]; then
            SUPPRESS=true

        elif [[ "$var" == "--im-cli" ]]; then
            ARTIFACT="installation-manager-cli"

        elif [[ "$var" =~ --version=.* ]]; then
            VERSION=$(echo "$var" | sed -e "s/--version=//g")

        elif [[ "$var" =~ --hostname=.* ]]; then
            HOST_NAME=$(echo "$var" | sed -e "s/--hostname=//g")

        elif [[ "$var" =~ --license=accept ]]; then
            LICENSE_ACCEPTED=true

        elif [[ "$var" =~ --install-directory=.* ]]; then
            INSTALL_DIR=$(echo "$var" | sed -e "s/--install-directory=//g")

        elif [[ "$var" =~ --docker-registry-mirror=.* ]]; then
            DOCKER_REGISTRY_MIRROR=$(echo "$var" | sed -e "s/--docker-registry-mirror=//g")

        elif [[ "$var" =~ --http-proxy-for-installation=.* ]]; then
            HTTP_PROXY_FOR_INSTALLATION=$(echo "$var" | sed -e "s/--http-proxy-for-installation=//g")
            CURL_PROXY_OPTION="--proxy $HTTP_PROXY_FOR_INSTALLATION"
        elif [[ "$var" =~ --https-proxy-for-installation=.* ]]; then
            HTTPS_PROXY_FOR_INSTALLATION=$(echo "$var" | sed -e "s/--https-proxy-for-installation=//g")
            CURL_PROXY_OPTION="--proxy $HTTPS_PROXY_FOR_INSTALLATION"
        elif [[ "$var" =~ --no-proxy-for-installation=.* ]]; then
            NO_PROXY_FOR_INSTALLATION=$(echo "$var" | sed -e "s/--no-proxy-for-installation=//g")

        elif [[ "$var" =~ --http-proxy-for-codenvy=.* ]]; then
            HTTP_PROXY_FOR_CODENVY=$(echo "$var" | sed -e "s/--http-proxy-for-codenvy=//g")
        elif [[ "$var" =~ --https-proxy-for-codenvy=.* ]]; then
            HTTPS_PROXY_FOR_CODENVY=$(echo "$var" | sed -e "s/--https-proxy-for-codenvy=//g")
        elif [[ "$var" =~ --no-proxy-for-codenvy=.* ]]; then
            NO_PROXY_FOR_CODENVY=$(echo "$var" | sed -e "s/--no-proxy-for-codenvy=//g")

        elif [[ "$var" =~ --http-proxy-for-codenvy-workspaces=.* ]]; then
            HTTP_PROXY_FOR_CODENVY_WORKSPACES=$(echo "$var" | sed -e "s/--http-proxy-for-codenvy-workspaces=//g")
        elif [[ "$var" =~ --https-proxy-for-codenvy-workspaces=.* ]]; then
            HTTPS_PROXY_FOR_CODENVY_WORKSPACES=$(echo "$var" | sed -e "s/--https-proxy-for-codenvy-workspaces=//g")
        elif [[ "$var" =~ --no-proxy-for-codenvy-workspaces=.* ]]; then
            NO_PROXY_FOR_CODENVY_WORKSPACES=$(echo "$var" | sed -e "s/--no-proxy-for-codenvy-workspaces=//g")

        elif [[ "$var" =~ --http-proxy-for-docker-daemon=.* ]]; then
            HTTP_PROXY_FOR_DOCKER_DAEMON=$(echo "$var" | sed -e "s/--http-proxy-for-docker-daemon=//g")
        elif [[ "$var" =~ --https-proxy-for-docker-daemon=.* ]]; then
            HTTPS_PROXY_FOR_DOCKER_DAEMON=$(echo "$var" | sed -e "s/--https-proxy-for-docker-daemon=//g")
        elif [[ "$var" =~ --no-proxy-for-docker-daemon=.* ]]; then
            NO_PROXY_FOR_DOCKER_DAEMON=$(echo "$var" | sed -e "s/--no-proxy-for-docker-daemon=//g")

        elif [[ "$var" =~ --config=.* ]]; then
            CUSTOM_CONFIG=$(echo "$var" | sed -e "s/--config=//g")

        elif [[ "$var" == "--disable-monitoring-tools" ]]; then
            DISABLE_MONITORING_TOOLS=true

        elif [[ "$var" == "--skip-post-flight-check" ]]; then
            SKIP_POST_FLIGHT_CHECK=true

        elif [[ "$var" =~ --advertise-network-interface=.* ]]; then
            ADVERTISE_NETWORK_INTERFACE=$(echo "$var" | sed -e "s/--advertise-network-interface=//g")

        else
            UNRECOGNIZED_PARAMETERS[$((i++))]="$var"

        fi
    done

    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        LAST_INSTALLATION_STEP=13
        ARTIFACT_DISPLAY="Codenvy"
        if [[ -z "${VERSION}" ]]; then
            VERSION=$(fetchProperty "https://codenvy.com/update/repository/properties/${ARTIFACT}?label=stable" "version")
        fi
    else
        LAST_INSTALLATION_STEP=3
        ARTIFACT_DISPLAY="Installation Manager CLI"
        CODENVY_TYPE="single"
        INSTALLATION_STEPS=("Configuring system..."
                            "Installing required packages... [java]"
                            "Install the Codenvy installation manager..."
                            "");
        if [[ -z "${VERSION}" ]]; then
            VERSION=$(fetchProperty "https://codenvy.com/update/repository/properties/${ARTIFACT}" "version")
        fi
    fi

    EXTERNAL_DEPENDENCIES[0]="https://codenvy.com/update/repository/public/download/${ARTIFACT}/${VERSION}||0"

    if [[ "${CODENVY_TYPE}" == "single" ]] && [[ ! -z "${HOST_NAME}" ]]; then
        SUPPRESS=true
    fi

    mkdir --parents "${INSTALL_DIR}"
    INSTALL_DIR=$(readlink -f $INSTALL_DIR)

    INSTALL_LOG="$INSTALL_DIR/install.log"
    CONFIG="$INSTALL_DIR/codenvy.properties"
}

# $1 - url
# $2 - property
fetchProperty() {
    local url=$1
    local property=$2
    local seq="s/.*\"${property}\":\"\([^\"]*\)\".*/\1/"
    echo $(curl -s $CURL_PROXY_OPTION ${url} | sed ${seq})
}

# run specific function and don't break installation if connection lost
doEvalWaitReconnection() {
    local func=$1
    shift

    for ((;;)); do
        eval ${func} $@
        local exitCode=$?

        if [[ ${exitCode} == 0 ]]; then
            break
        else
            doUpdateInternetAccessChecker
            local checkFailed=$?

            if [[ ${checkFailed} == 0 ]]; then
                return ${exitCode} # Internet connection is OK, probably another error
            else
                sleep 1m # wait reconnection
            fi
        fi
    done
}

doConfigureSystem() {
    setStepIndicator 0

    doEvalWaitReconnection installPackageIfNeed tar
    validateExitCode $?

    doEvalWaitReconnection installPackageIfNeed unzip
    validateExitCode $?
}

configureProxySettings() {
    if [[ -n "$HTTP_PROXY_FOR_INSTALLATION" || -n "$HTTPS_PROXY_FOR_INSTALLATION" || -n "$NO_PROXY_FOR_INSTALLATION" ]]; then

        local PROXY_WITH_USER_AUTH_REGEXP="https?://([^:]*):?(.*)@.*"
        local proxyUserName
        local proxyPassword
        local proxyWithoutAuth

        local bashrcToDisplay
        local yumConfToDisplay
        local wgetrcToDisplay

        if [[ "$HTTP_PROXY_FOR_INSTALLATION" =~ $PROXY_WITH_USER_AUTH_REGEXP ]]; then
            proxyUser=$([[ "$HTTP_PROXY_FOR_INSTALLATION" =~ $PROXY_WITH_USER_AUTH_REGEXP ]] && echo ${BASH_REMATCH[1]})
            proxyPassword=$([[ "$HTTP_PROXY_FOR_INSTALLATION" =~ $PROXY_WITH_USER_AUTH_REGEXP ]] && echo ${BASH_REMATCH[2]})
        fi

        proxyWithoutAuth=$(echo "$HTTP_PROXY_FOR_INSTALLATION" | sed "s/@//")
        if [[ -n "$proxyUser" ]]; then
            # remove "<proxyUser>:<proxyPassword>@" token from the proxy URL
            proxyWithoutAuth=$(echo "$proxyWithoutAuth" | sed "s/$proxyUser//")

            if [[ -n "$proxyPassword" ]]; then
                proxyWithoutAuth=$(echo "$proxyWithoutAuth" | sed "s/:$proxyPassword//")
            fi
        fi

        wgetrcToDisplay="${wgetrcToDisplay}$(print "use_proxy=on")\n"

        if [[ -n "$HTTP_PROXY_FOR_INSTALLATION" ]]; then
            bashrcToDisplay="${bashrcToDisplay}$(print "export http_proxy=$HTTP_PROXY_FOR_INSTALLATION")\n"

            yumConfToDisplay="${yumConfToDisplay}$(print "proxy=$proxyWithoutAuth")\n"
            if [[ -n "$proxyUser" ]]; then
                yumConfToDisplay="${yumConfToDisplay}$(print "proxy_username=$proxyUser")\n"
            fi

            if [[ -n "$proxyPassword" ]]; then
                yumConfToDisplay="${yumConfToDisplay}$(print "proxy_password=$proxyPassword")\n"
            fi

            wgetrcToDisplay="${wgetrcToDisplay}$(print "http_proxy=$HTTP_PROXY_FOR_INSTALLATION")\n"
        fi

        if [[ -n "$HTTPS_PROXY_FOR_INSTALLATION" ]]; then
            bashrcToDisplay="${bashrcToDisplay}$(print "export https_proxy=$HTTPS_PROXY_FOR_INSTALLATION")\n"
            wgetrcToDisplay="${wgetrcToDisplay}$(print "https_proxy=$HTTPS_PROXY_FOR_INSTALLATION")\n"
        fi

        if [[ -n "$NO_PROXY_FOR_INSTALLATION" ]]; then
            bashrcToDisplay="${bashrcToDisplay}$(print "export no_proxy='$NO_PROXY_FOR_INSTALLATION'")\n"

            # https://www.gnu.org/software/wget/manual/html_node/Wgetrc-Commands.html
            wgetrcToDisplay="${wgetrcToDisplay}$(print "no_proxy='$NO_PROXY_FOR_INSTALLATION'")\n"
        fi

        if [[ ${SUPPRESS} == false ]]; then
            println "Proxy options found! This server needs the following to install:"
            println "================================"
            println $(printImportantInfo "# In ~/.bashrc:")
            echo -en "${bashrcToDisplay}"
            println

            if [[ -n "$yumConfToDisplay" ]]; then
                println $(printImportantInfo "# In /etc/yum.conf:")
                echo -en  "${yumConfToDisplay}"
                println
            fi

            println $(printImportantInfo "# In /etc/wgetrc:")
            echo -en  "${wgetrcToDisplay}"
            println "================================"
            println

            print -n "Can Codenvy configure your system with these properties? [y/N]: "
            read ANSWER
            if [[ ! "${ANSWER}" == "y" ]]; then
                exit 1
            fi

            println
        fi

        # setup /etc/wgetrc
        putLineIntoFile /etc/wgetrc "use_proxy=on" "^use_proxy=.*$"

        if [[ -n "$HTTP_PROXY_FOR_INSTALLATION" ]]; then
            # setup ~/.bashrc
            putLineIntoFile ~/.bashrc "export http_proxy=$HTTP_PROXY_FOR_INSTALLATION" "^export.*http_proxy=.*$"
            source ~/.bashrc

            # setup /etc/yum.conf
            putLineIntoFile /etc/yum.conf "proxy=$proxyWithoutAuth" "^proxy=.*$"
            if [[ -n "$proxyUser" ]]; then
                putLineIntoFile /etc/yum.conf "proxy_username=$proxyUser" "^proxy_username=.*$"
            fi

            if [[ -n "$proxyPassword" ]]; then
                putLineIntoFile /etc/yum.conf "proxy_password=$proxyPassword" "^proxy_password=.*$"
            fi

            # setup /etc/wgetrc
            putLineIntoFile /etc/wgetrc "http_proxy=$HTTP_PROXY_FOR_INSTALLATION" "^http_proxy=.*$"
        fi

        if [[ -n "$HTTPS_PROXY_FOR_INSTALLATION" ]]; then
            # setup ~/.bashrc
            putLineIntoFile ~/.bashrc "export https_proxy=$HTTPS_PROXY_FOR_INSTALLATION" "^export.*https_proxy=.*$"
            source ~/.bashrc

            # setup /etc/wgetrc
            putLineIntoFile /etc/wgetrc "https_proxy=$HTTPS_PROXY_FOR_INSTALLATION" "^https_proxy=.*$"
        fi

        if [[ -n "$NO_PROXY_FOR_INSTALLATION" ]]; then
            # setup ~/.bashrc
            putLineIntoFile ~/.bashrc "export no_proxy='$NO_PROXY_FOR_INSTALLATION'" "^export.*no_proxy=.*$"
            source ~/.bashrc

            # setup /etc/wgetrc
            putLineIntoFile /etc/wgetrc "no_proxy='$NO_PROXY_FOR_INSTALLATION'" "^no_proxy=.*$"
        fi
    fi
}

# Safe update: replace token by replacement to if regexForReplacement matches pathToFile, or insert replacement to the end of file as a seperate line otherwise.
# Create backup file "<path_to_file>.<time>" before replacing.
# parameter $1 - pathToFile
# parameter $2 - replacement
# parameter $3 - regexForReplacement (optional)
putLineIntoFile() {
    local pathToFile=$1
    local replacement=$2
    local regexForReplacement=$3

    if [[ -n "$pathToFile" && -n "$replacement" ]]; then
        if test -n "$regexForReplacement" && sudo grep -Eq "$regexForReplacement" "$pathToFile"; then
            createFileBackup "$pathToFile"
            sudo sed -i "s|$regexForReplacement|$replacement|" "$pathToFile" &> /dev/null
            return
        fi

        if ! sudo grep -Eq "^${replacement}$" "$pathToFile"; then
            createFileBackup "$pathToFile"
            echo "$replacement" | sudo tee --append "$pathToFile" &> /dev/null
            return
        fi
    fi
}

# parameter $1 - path to file which should be copied into the $1.<time>
createFileBackup() {
    local pathToFile=$1
    if [[ -a "$pathToFile" ]]; then
        local currentTimeInMillis=$(($(date +%s%N)/1000000))
        sudo cp -f "$pathToFile" "${pathToFile}.$currentTimeInMillis"
    fi
}

doInstallJava() {
    setStepIndicator 1
    doEvalWaitReconnection installJava
    validateExitCode $?
}

doInstallImCli() {
    setStepIndicator 2
    doEvalWaitReconnection installIm
    validateExitCode $?
}

# Download binaries. If file is corrupted due to unexpected errors then it will be redownloaded
doDownloadBinaries() {
    setStepIndicator 3

    for ((;;)); do
        OUTPUT=$(doEvalWaitReconnection executeIMCommand download ${ARTIFACT} ${VERSION})
        local exitCode=$?
        echo "${OUTPUT}" | sed 's/\[[=> ]*\]//g'  >> ${INSTALL_LOG}

        if [[ ${exitCode} == 0 ]]; then
            break
        fi

        if [[ "${OUTPUT}" =~ .*File.corrupted.* ]]; then
            echo "Codenvy binaries will be redownloaded" >> ${INSTALL_LOG}
            continue
        else
            validateExitCode ${exitCode}
        fi
    done
    doUpdateDownloadProgress 100

    executeIMCommand download --list-local >> ${INSTALL_LOG}
    validateExitCode $?
}

runDownloadProgressUpdater() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    updateDownloadProgress &
    DOWNLOAD_PROGRESS_UPDATER_PID=$!
}

killDownloadProgressUpdater() {
    if [ -n "${DOWNLOAD_PROGRESS_UPDATER_PID}" ]; then
        kill -KILL ${DOWNLOAD_PROGRESS_UPDATER_PID}
    fi
}

continueDownloadProgressUpdater() {
    if [ -n "${DOWNLOAD_PROGRESS_UPDATER_PID}" ]; then
        kill -SIGCONT ${DOWNLOAD_PROGRESS_UPDATER_PID}
    fi
}

pauseDownloadProgressUpdater() {
    if [ -n "${DOWNLOAD_PROGRESS_UPDATER_PID}" ]; then
        kill -SIGSTOP ${DOWNLOAD_PROGRESS_UPDATER_PID}
    fi
}

updateDownloadProgress() {
    local totalSize=$(fetchProperty "https://codenvy.com/update/repository/properties/${ARTIFACT}/${VERSION}" "size")
    local file=$(fetchProperty "https://codenvy.com/update/repository/properties/${ARTIFACT}/${VERSION}" "file")

    for ((;;)); do
        local size
        local localFile="${INSTALL_DIR}/updates/${ARTIFACT}/${VERSION}/${file}"
        if [[ -f "${localFile}" ]]; then
            size=$(du -b "${localFile}" | cut -f1)
        else
            size=0
        fi
        local percent=$(( ${size}*100/${totalSize} ))

        doUpdateDownloadProgress ${percent}
        sleep 1 &>/dev/null
    done
}

doUpdateDownloadProgress() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    local percent=$1
    local bars=$(( ${LAST_INSTALLATION_STEP}*${PROGRESS_FACTOR} ))
    local progress_field=
    for ((i=1; i<=$(( ${bars}*${percent}/100 )); i++));  do
       progress_field="${progress_field}="
    done
    progress_field=$(printf "[%-${bars}s]" ${progress_field})
    local message="Downloading  ${progress_field} ${percent}%"

    updateLine ${PUPPET_LINE} "${message}"
}

doInstallCodenvy() {
    if [[ ${DISABLE_MONITORING_TOOLS} == true ]]; then
        insertProperty "install_monitoring_tools" "false"
    fi

    for ((STEP=1; STEP<=9; STEP++));  do
        if [ ${STEP} == 9 ]; then
            setStepIndicator $(( $STEP+3 ))
        else
            setStepIndicator $(( $STEP+3 ))
        fi

        for ((;;)); do
            local exitCode
            if [ ${CODENVY_TYPE} == "multi" ]; then
                doEvalWaitReconnection executeIMCommand install --step ${STEP} --forceInstall --multi --config ${CONFIG} ${ARTIFACT} ${VERSION} >> ${INSTALL_LOG}
                exitCode=$?
            else
                doEvalWaitReconnection executeIMCommand install --step ${STEP} --forceInstall --config ${CONFIG} ${ARTIFACT} ${VERSION} >> ${INSTALL_LOG}
                exitCode=$?
            fi

            if [[ ${exitCode} == 0 ]]; then
                break;
            fi

            # if error occurred not because of internet access lost then break installation
            # it prevents breaking installation due to a lot of puppet errors
            local checkFailed=$(cat /tmp/im_internet_access_lost 2>/dev/null)
            if [[ ! ${STEP} == 9 ]] && [[ ${checkFailed} == 1 ]]; then
                echo "Repeating installation step "${STEP} >> ${INSTALL_LOG}
                continue;
            else
                validateExitCode ${exitCode}
            fi

            break;
        done
    done
}

downloadConfig() {
    # load external config into the ${CONFIG} file
    if [[ -n "$CUSTOM_CONFIG" ]]; then
        if [[ "$CUSTOM_CONFIG" =~ $URL_REGEX ]]; then
            # check url to config on http error
            http_code=$(curl $CURL_PROXY_OPTION --silent --write-out '%{http_code}' --output /dev/null "$CUSTOM_CONFIG")
            if [[ ! ${http_code} -eq 200 ]]; then    # if response code != "200 OK"
                println $(printError "ERROR: custom codenvy configuration '$CUSTOM_CONFIG' not found or downloadable.")
                exit 1
            fi

            curl $CURL_PROXY_OPTION --silent --output "$CONFIG" "$CUSTOM_CONFIG"
            return
        else
            cp "$CUSTOM_CONFIG" "$CONFIG"
            return
        fi
    fi

    # load config from Update Server
    local url="https://codenvy.com/update/repository/public/download/codenvy-${CODENVY_TYPE}-server-properties/${VERSION}"

    # check url to config on http error
    http_code=$(curl $CURL_PROXY_OPTION --silent --write-out '%{http_code}' --output /dev/null "${url}")
    if [[ ! ${http_code} -eq 200 ]]; then    # if response code != "200 OK"
        local updates=$(curl $CURL_PROXY_OPTION --silent "https://codenvy.com/update/repository/updates/${ARTIFACT}")
        println $(printError "ERROR: Version '${VERSION}' is not available")
        println
        if [[ -n "${VERSION}" ]] && [[ ! "${updates}" =~ .*\"${VERSION}\".* ]]; then
            println $(printWarning "NOTE: You've used '--version' flag to install a specific version.")
            println $(printWarning "NOTE: We could not find this version in the repository. Versions found:")
            println $(printWarning "NOTE: ${updates}")
            println $(printWarning "NOTE: Installing without '--version' will use latest version.")
        else
            println $(printWarning "NOTE: codenvy.properties not found or downloadable.")
        fi

        exit 1
    fi

    # load config into the ${CONFIG} file
    curl $CURL_PROXY_OPTION --silent --output "${CONFIG}" "${url}"
}

# $1 - command name
installPackageIfNeed() {
    local exitCode
    rpm -qa | grep "^$1-" &> /dev/null || { # check if required package already has been installed earlier
        echo -n "Install package '$1'... " >> ${INSTALL_LOG}

        exitCode=$(sudo yum install $1 -y -q --errorlevel=0 >> ${INSTALL_LOG} 2>&1; echo $?)
        if [[ ! ${exitCode} == 0 ]]; then
            echo " [FAILED]" >> ${INSTALL_LOG}
            return ${exitCode}
        else
            echo " [OK]" >> ${INSTALL_LOG}
        fi
    }
}

preConfigureSystem() {
    doCheckSystemManager

    doCheckInstalledPuppet

    sudo yum clean all &> /dev/null

    installPackageIfNeed curl
    validateExitCode $?

    installPackageIfNeed wget
    validateExitCode $?

    # back up file existed CONFIG file to prevent installation codenvy with wrong configuration
    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        if [[ -f "${CONFIG}" ]]; then
            if [[ -n "${CUSTOM_CONFIG}" ||  ! $(cat "${CONFIG}") =~ .*${VERSION}.* ]]; then
                mv "${CONFIG}" "${CONFIG}.back"
            fi
        fi

        if [[ ! -f "${CONFIG}" ]]; then
            downloadConfig
        fi
    fi
}

installJava() {
    echo -n "Install java package from '${JRE_URL}' into the directory '${INSTALL_DIR}/jre' ... " >> ${INSTALL_LOG}

    wget -q --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "${JRE_URL}" --output-document=${INSTALL_DIR}/jre.tar.gz >> ${INSTALL_LOG} 2>&1 || return 1
    tar -xf ${INSTALL_DIR}/jre.tar.gz -C ${INSTALL_DIR} >> ${INSTALL_LOG} 2>&1

    rm -fr ${INSTALL_DIR}/jre >> ${INSTALL_LOG} 2>&1
    mv -f ${INSTALL_DIR}/jre1.8.0_45 ${INSTALL_DIR}/jre >> ${INSTALL_LOG} 2>&1
    rm ${INSTALL_DIR}/jre.tar.gz >> ${INSTALL_LOG} 2>&1

    echo " [OK]" >> ${INSTALL_LOG}
}

installIm() {
    local imUrl="https://codenvy.com/update/repository/public/download/installation-manager-cli"

    if [[ "${ARTIFACT}" == "installation-manager-cli" ]]; then
        imUrl=${imUrl}"/"${VERSION}
    fi
    echo "${imUrl}" >> ${INSTALL_LOG}

    local imFilePath="${INSTALL_DIR}/$(curl -sI "${imUrl}" | grep -o -E 'filename=(.*)[.]tar.gz' | sed -e 's/filename=//')"
    if [[ ! $? == 0 ]]; then
        return 1
    fi

    curl -s --output "${imFilePath}" -L "${imUrl}" || return 1

    local imCliDir="${INSTALL_DIR}/cli"
    if [ -d ${imCliDir} ]; then rm -rf ${imCliDir}; fi
    mkdir "${imCliDir}"

    tar -xf "${imFilePath}" -C "${imCliDir}"
    rm "${imFilePath}"

    sed -i "2iJAVA_HOME=${INSTALL_DIR}/jre" ${imCliDir}/bin/codenvy
    printf "\nexport CODENVY_IM_BASE=${INSTALL_DIR}\n" >> ${HOME}/.bashrc
    printf '\nexport PATH=$PATH:$CODENVY_IM_BASE/cli/bin\n' >> ${HOME}/.bashrc
}

clearLine() {
    echo -en "\033[2K"
}

cursorUp() {
    echo -en "\e[1A"
}

cursorDown() {
    echo -en "\e[1B"
}

cursorSave() {
    echo -en "\e[s"
}

cursorRestore() {
    echo -en "\e[u"
}

# $1 - line number which is starting backward from 1 - the last bottom row.
# $2, $3,.. - messages to display in line number $1
# replace spaces with "_" character not to loose them
updateLine() {
    local lineNumber=$1
    shift
    echo "$@" > /tmp/im_line_${lineNumber} 2>/dev/null
}

updateFooter() {
    for ((;;)); do
        cursorUp
        cursorUp
        cursorUp
        cursorUp

        for ((line=4; line>=1; line--));  do
            local prev_text=$(cat /tmp/im_prev_line_${line} 2>/dev/null)
            local text=$(cat /tmp/im_line_${line} 2>/dev/null | tail -1)

            if [[ ! "${prev_text}" == "${text}" ]]; then
                clearLine
                println "${text}"
                echo "${text}" > /tmp/im_prev_line_${line} 2>/dev/null
            else
                cursorDown
            fi
        done

        sleep 1 &>/dev/null
    done
}

# https://wiki.archlinux.org/index.php/Color_Bash_Prompt
printError() {
    echo -en "\e[91m$1\e[0m" # with High Intensity RED color
}

printSuccess() {
    echo -en "\e[32m$1\e[0m" # with Underline GREEN color
}

printWarning() {
    echo -en "\e[93m$1\e[0m" # with High Intensity YELLOW color
}

printImportantInfo() {
    echo -en "\e[92m$1\e[0m" # with Underline GREEN color
}

printImportantLink() {
    echo -en "\e[94m$1\e[0m" # with High Intensity blue color
}

printPrompt() {
    clearLine
    echo -en "\e[34m[CODENVY] \e[0m" # with Underline blue color
}

print() {
    printPrompt; echo -n "$@"
}

println() {
    printPrompt; echo "$@"
}

askProperty() {
    read VALUE
    echo "${VALUE}"
}

insertProperty() {
    local value=$(echo $2 | sed -r 's/[|]/\\|/g')  # replace "|" on "\|" for sed command
    sed -i "s|$1=.*|$1=$value|g" "${CONFIG}"
}

# read value of property from the ${CONFIG} file where properties are stored in view of "<name> = <value>"
# $1 - property name
readProperty() {
    local property=$1
    local value=$(sed '/^\#/d' "${CONFIG}" | grep "^\s*$property\s*="  | tail -n 1 | cut -d "=" -f2- | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
    echo $value
}


validateHostname() {
    DNS=$1
    OUTPUT=$(ping -c 1 ${DNS} &> /dev/null && echo success || echo fail)
    echo ${OUTPUT}
}

askHostnameAndInsertProperty() {
    PROMPT=$1
    VARIABLE=$2

    FIRST_ATTEMPT=true

    while :
    do
        if [[ ${FIRST_ATTEMPT} == false ]]; then
            cursorUp
            cursorUp
            clearLine
        else
            FIRST_ATTEMPT=false
        fi

        print "$(printf "%-35s" "${PROMPT}:") "

        read VALUE
    done

    insertProperty "${VARIABLE}" ${VALUE}
}

executeIMCommand() {
    ${INSTALL_DIR}/cli/bin/codenvy $@
}

pressAnyKeyToContinueAndClearConsole() {
    if [[ ${SUPPRESS} == false ]]; then
        println  "Press any key to continue"
        read -n1 -s
        clear
    fi
}

pressAnyKeyToContinue() {
    if [[ ${SUPPRESS} == false ]]; then
        println  "Press any key to continue"
        read -n1 -s
    fi
}

pressYKeyToContinue() {
    if [[ ${SUPPRESS} == false ]]; then
        if [[ ! -z "$1" ]]; then
            print $@
        else
            print "Continue installation"
        fi
        echo -n " [y/N]: "

        read ANSWER
        if [[ ! "${ANSWER}" == "y" ]]; then
            exit 1
        fi
    fi
}

doCheckPortRemote() {
    local protocol=$1
    local port=$2
    local host=$3
    OUTPUT=$(ssh -o LogLevel=quiet -o StrictHostKeyChecking=no -t ${host} "ss -ano | egrep LISTEN | egrep ${protocol} | egrep ':${host}\s'")
    echo ${OUTPUT}
}

doCheckPortLocal() {
    local protocol=$1
    local port=$2
    OUTPUT=$(ss -ano | egrep LISTEN | egrep ${protocol} | egrep ":${port}\s")
    echo ${OUTPUT}
}

validatePortLocal() {
    local protocol=$1
    local port=$2
    local host="localhost"
    doValidatePort doCheckPortLocal ${protocol} ${port} ${host}
}

validatePortRemote() {
    local protocol=$1
    local port=$2
    local host=$3
    doValidatePort doCheckPortRemote ${protocol} ${port} ${host}
}

doValidatePort() {
    local func=$1
    local protocol=$2
    local port=$3
    local host=$4
    local output=$(eval ${func} ${protocol} ${port} ${host})

    if [ "${output}" != "" ]; then
        installPackageIfNeed lsof
        println $(printError "ERROR: The port ${protocol}:${port} on '${host}' is busy.")
        println $(printError "ERROR: The installation cannot proceed.")
        println
        println $(printWarning "NOTE: Codenvy uses this port internally. All required ports are listed in docs.")
        println $(printWarning "NOTE: The problem might occur if some services required by Codenvy are")
        println $(printWarning "NOTE: already running. Run 'sudo lsof -i ${protocol}:${port} | grep LISTEN' on '${host}' to identify")
        println $(printWarning "NOTE: the running process. We recommend restarting installation on a bare system.")
        exit 1
    fi
}

doGetHostsVariables() {
    HOST_NAME=$(grep host_url\\s*=\\s*.* ${CONFIG} | sed 's/host_url\s*=\s*\(.*\)/\1/')
    PUPPET_MASTER_HOST_NAME=$(grep puppet_master_host_name=.* ${CONFIG} | cut -f2 -d '=')
    DATA_HOST_NAME=$(grep data_host_name=.* ${CONFIG} | cut -f2 -d '=')
    API_HOST_NAME=$(grep api_host_name=.* ${CONFIG} | cut -f2 -d '=')
    BUILDER_HOST_NAME=$(grep builder_host_name=.* ${CONFIG} | cut -f2 -d '=')
    RUNNER_HOST_NAME=$(grep runner_host_name=.* ${CONFIG} | cut -f2 -d '=')
    DATASOURCE_HOST_NAME=$(grep datasource_host_name=.* ${CONFIG} | cut -f2 -d '=')
    ANALYTICS_HOST_NAME=$(grep analytics_host_name=.* ${CONFIG} | cut -f2 -d '=')
    SITE_HOST_NAME=$(grep site_host_name=.* ${CONFIG} | cut -f2 -d '=')
}

doCheckAvailablePorts_single() {
    for PORT in ${SITE_PORTS[@]} ${API_PORTS[@]} ${DATA_PORTS[@]} ${DATASOURCE_PORTS[@]} ${RUNNER_PORTS[@]} ${BUILDER_PORTS[@]}; do
        PROTOCOL=$(echo ${PORT}|awk -F':' '{print $1}');
        PORT_ONLY=$(echo ${PORT}|awk -F':' '{print $2}');

        validatePortLocal "${PROTOCOL}" "${PORT_ONLY}"
    done
}

doCheckInstalledPuppet() {
    # check puppet agent
    rpm -qa | grep "^puppet-[0-9]" &> /dev/null;
    if [ $? -eq 0 ]; then
        rpm -qa | grep "$PUPPET_AGENT_PACKAGE" &> /dev/null;
        if [ $? -ne 0 ]; then
            println $(printError "ERROR: Your system has the wrong puppet agent version!")
            println $(printWarning "NOTE: Please, uninstall it or update to package '$PUPPET_AGENT_PACKAGE', and then start installation again.")
            exit 1;
        fi
    fi

    # check puppet server
    rpm -qa | grep "^puppet-server-[0-9]" &> /dev/null;
    if [ $? -eq 0 ]; then
        rpm -qa | grep "$PUPPET_SERVER_PACKAGE" &> /dev/null;
        if [ $? -ne 0 ]; then
            println $(printError "ERROR: Your system has the wrong puppet server version!")
            println $(printWarning "NOTE: Please, uninstall it or update to package '$PUPPET_SERVER_PACKAGE', and then start installation again.")
            exit 1;
        fi
    else
        # check if puppet master ports has been already opened
        for PORT in ${PUPPET_MASTER_PORTS[@]}; do
            PROTOCOL=$(echo ${PORT}|awk -F':' '{print $1}');
            PORT_ONLY=$(echo ${PORT}|awk -F':' '{print $2}');

            validatePortLocal "${PROTOCOL}" "${PORT_ONLY}"
        done
    fi
}

doCheckSystemManager() {
    # we need to provide full path /sbin/pidof to avoid ssh error "bash: pidof: command not found" in integration tests
    /sbin/pidof systemd &> /dev/null;
    if [ $? -ne 0 ]; then
        println $(printError "ERROR: Your system doesn't use required system manager 'systemd'.")
        exit 1;
    fi
}

doCheckAvailablePorts_multi() {
    doGetHostsVariables

    for HOST in ${PUPPET_MASTER_HOST_NAME} ${DATA_HOST_NAME} ${API_HOST_NAME} ${BUILDER_HOST_NAME} ${DATASOURCE_HOST_NAME} ${ANALYTICS_HOST_NAME} ${SITE_HOST_NAME} ${RUNNER_HOST_NAME}; do
        if [[ "${HOST}" == "${PUPPET_MASTER_HOST_NAME}" ]]; then
            PORTS=${PUPPET_MASTER_PORTS[@]}
        elif [[ "${HOST}" == "${DATA_HOST_NAME}" ]]; then
            PORTS=${DATA_PORTS[@]}
        elif [[ "${HOST}" == "${API_HOST_NAME}" ]]; then
            PORTS=${API_PORTS[@]}
        elif [[ "${HOST}" == "${BUILDER_HOST_NAME}" ]]; then
            PORTS=${BUILDER_PORTS[@]}
        elif [[ "${HOST}" == "${DATASOURCE_HOST_NAME}" ]]; then
            PORTS=${DATASOURCE_PORTS[@]}
        elif [[ "${HOST}" == "${ANALYTICS_HOST_NAME}" ]]; then
            PORTS=${ANALYTICS_PORTS[@]}
        elif [[ "${HOST}" == "${SITE_HOST_NAME}" ]]; then
            PORTS=${SITE_PORTS[@]}
        elif [[ "${HOST}" == "${RUNNER_HOST_NAME}" ]]; then
            PORTS=${RUNNER_PORTS[@]}
        fi

        for PORT in ${PORTS[@]}; do
            PROTOCOL=$(echo ${PORT}|awk -F':' '{print $1}');
            PORT_ONLY=$(echo ${PORT}|awk -F':' '{print $2}');

            if [[ "${HOST}" == "${PUPPET_MASTER_HOST_NAME}" ]]; then
                validatePortLocal "${PROTOCOL}" "${PORT_ONLY}"
            else
                validatePortRemote "${PROTOCOL}" "${PORT_ONLY}" "${HOST}"
            fi
        done
    done
}

printPreInstallInfo_single() {
    doEnsureLicenseAgreement

    println "Checking system pre-requisites..."
    println

    doCheckAvailableResourcesLocally 2500000 1 1000000 8000000 4 300000000

    preConfigureSystem

    configureProxySettings

    println "Checking installation repositories..."

    checkResourceAccess

    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        println "Configuring system properties with file://${CONFIG}..."
        println
    fi

    if [[ ${DISABLE_MONITORING_TOOLS} == true ]]; then
        insertProperty "install_monitoring_tools" "false"
    fi

    if [ -n "${HOST_NAME}" ]; then
        insertProperty "host_url" "${HOST_NAME}"
    fi

    if [ -n "${HTTP_PROXY_FOR_DOCKER_DAEMON}" ]; then
        insertProperty "http_proxy_for_docker_daemon" "${HTTP_PROXY_FOR_DOCKER_DAEMON}"
    fi
    if [ -n "${HTTPS_PROXY_FOR_DOCKER_DAEMON}" ]; then
        insertProperty "https_proxy_for_docker_daemon" "${HTTPS_PROXY_FOR_DOCKER_DAEMON}"
    fi
    if [ -n "${NO_PROXY_FOR_DOCKER_DAEMON}" ]; then
        insertProperty "no_proxy_for_docker_daemon" "${NO_PROXY_FOR_DOCKER_DAEMON}"
    fi

    if [ -n "${DOCKER_REGISTRY_MIRROR}" ]; then
        insertProperty "docker_registry_mirror" "${DOCKER_REGISTRY_MIRROR}"
    fi

    if [ -n "${HTTP_PROXY_FOR_CODENVY}" ]; then
        insertProperty "http_proxy_for_codenvy" "${HTTP_PROXY_FOR_CODENVY}"
    fi
    if [ -n "${HTTPS_PROXY_FOR_CODENVY}" ]; then
        insertProperty "https_proxy_for_codenvy" "${HTTPS_PROXY_FOR_CODENVY}"
    fi
    if [ -n "${NO_PROXY_FOR_CODENVY}" ]; then
        insertProperty "no_proxy_for_codenvy" "${NO_PROXY_FOR_CODENVY}"
    fi

    if [ -n "${HTTP_PROXY_FOR_CODENVY_WORKSPACES}" ]; then
        insertProperty "http_proxy_for_codenvy_workspaces" "${HTTP_PROXY_FOR_CODENVY_WORKSPACES}"
    fi
    if [ -n "${HTTPS_PROXY_FOR_CODENVY_WORKSPACES}" ]; then
        insertProperty "https_proxy_for_codenvy_workspaces" "${HTTPS_PROXY_FOR_CODENVY_WORKSPACES}"
    fi
    if [ -n "${NO_PROXY_FOR_CODENVY_WORKSPACES}" ]; then
        insertProperty "no_proxy_for_codenvy_workspaces" "${NO_PROXY_FOR_CODENVY_WORKSPACES}"
    fi

    checkNetworkInterface

    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        doCheckAvailablePorts_single
    fi
}

doEnsureLicenseAgreement() {
    if [[ ${LICENSE_ACCEPTED} == true ]]; then
        println "You have accepted the Codenvy license agreement"
        println "at https://codenvy.com/docs/terms-of-service.pdf"
        println
        return
    fi

    println "You must accept the Codenvy license agreement to install"
    println "this software: https://codenvy.com/docs/terms-of-service.pdf"
    println
    print "Accept? [y/N]: "
    read VALUE

    if [[ "${VALUE}" != "y" && "${VALUE}" != "Y" ]]; then
        exit 1;
    fi

    println
    println "You are awesome. Let's do this."
}

# parameter 1 - MIN_RAM_KB
# parameter 2 - MIN_CORES
# parameter 3 - MIN_DISK_SPACE_KB
# parameter 4 - REC_RAM_KB
# parameter 5 - REC_CORES
# parameter 6 - REC_DISK_SPACE_KB
doCheckAvailableResourcesLocally() {
    local MIN_RAM_KB=$1
    local MIN_CORES=$2
    local MIN_DISK_SPACE_KB=$3
    local REC_RAM_KB=$4
    local REC_CORES=$5
    local REC_DISK_SPACE_KB=$6


    local osIssueFound=false
    local osType=""
    local osVersion=""
    local osInfo=""

    case $(uname) in
        Linux )
            if [ -f /etc/redhat-release ] ; then
                if grep 'Red Hat Enterprise Linux Server' /etc/redhat-release &> /dev/null; then
                    # RHEL
                    osType="RHEL"
                    doCheckRedhatSubscription
                else
                    # CentOS
                    osType="CentOS"
                fi

                osVersion=$(cat /etc/redhat-release | sed 's/.* \([0-9.]*\) .*/\1/')
                osInfo=$(cat /etc/redhat-release | sed 's/Linux release //')

            # SuSE
            elif [ -f /etc/SuSE-release ] ; then
                osInfo="SuSE"

            # debian
            elif [ -f /etc/debian_version ]; then
                osInfo=$(cat /etc/issue.net)

            # other linux OS
            elif [ -f /etc/lsb-release ]; then
                osInfo=$(cat /etc/lsb-release | grep '^DISTRIB_ID' | awk -F=  '{ print $2 }')
            fi
            ;;

        * )
            osInfo=$(uname);
            ;;
    esac

    # check on CentOS or RHEL OS of version >= 7.1
    if [[ "${osType}" != "CentOS" && "${osType}" != "RHEL" || "${osVersion}" < "${MIN_OS_VERSION}" ]]; then
        osIssueFound=true
    fi

    local osInfoToDisplay=$(printf "%-47s" "${osInfo}")
    local osStateToDisplay=$([ ${osIssueFound} == false ] && printSuccess "[OK]" || printError "[NOT OK]")
    println "DETECTED OS: ${osInfoToDisplay} ${osStateToDisplay}"

    local resourceIssueFound="none"

    local availableRAM=$(cat /proc/meminfo | grep MemTotal | awk '{print $2}')
    local availableRAMIssue=false
    local RAMStateToDisplay=$(printSuccess "[OK]")

    local availableDiskSpace=$(df /home | tail -1 | awk '{print $4}')  # available
    local availableDiskSpaceIssue=false
    local diskStateToDisplay=$(printSuccess "[OK]")

    local availableCores=$(grep -c ^processor /proc/cpuinfo)
    local availableCoresIssue=false
    local coresStateToDisplay=$(printSuccess "[OK]")

    if (( ${availableRAM} < ${MIN_RAM_KB} )); then
        resourceIssueFound="blocker"
        RAMStateToDisplay=$(printError "[NOT OK]")
    elif (( ${availableRAM} < ${REC_RAM_KB} )); then
        [ ${resourceIssueFound} == "none" ] && resourceIssueFound="warning"
        RAMStateToDisplay=$(printWarning "[WARNING]")
    fi

    if (( ${availableCores} < ${MIN_CORES})); then
        resourceIssueFound="blocker"
        coresStateToDisplay=$(printError "[NOT OK]")
    elif (( ${availableCores} < ${REC_CORES} )); then
        [ ${resourceIssueFound} == "none" ] && resourceIssueFound="warning"
        coresStateToDisplay=$(printWarning "[WARNING]")
    fi

    if (( ${availableDiskSpace} < ${MIN_DISK_SPACE_KB})); then
        resourceIssueFound="blocker"
        diskStateToDisplay=$(printError "[NOT OK]")
    elif (( ${availableDiskSpace} < ${REC_DISK_SPACE_KB} )); then
        [ ${resourceIssueFound} == "none" ] && resourceIssueFound="warning"
        diskStateToDisplay=$(printWarning "[WARNING]")
    fi

    local minRAMToDisplay=$(printf "%-15s" "$(echo ${MIN_RAM_KB} | awk '{tmp = $1/1000/1000; printf"%0.2f",tmp}') GB")
    local recRAMToDisplay=$(printf "%-15s" "$(echo ${REC_RAM_KB} | awk '{tmp = $1/1000/1000; printf"%0.2f",tmp}') GB")
    local availableRAMToDisplay=$(cat /proc/meminfo | grep MemTotal | awk '{tmp = $2/1000/1000; printf"%0.2f",tmp}')
    local availableRAMToDisplay=$(printf "%-11s" "${availableRAMToDisplay} GB")

    local minCoresToDisplay=$(printf "%-15s" "${MIN_CORES} cores")
    local recCoresToDisplay=$(printf "%-15s" "${REC_CORES} cores")
    local availableCoresToDisplay=$(printf "%-11s" "${availableCores} cores")

    local minDiskSpaceToDisplay=$(printf "%-15s" "$(( ${MIN_DISK_SPACE_KB} /1000/1000 )) GB")
    local recDiskSpaceToDisplay=$(printf "%-15s" "$(( ${REC_DISK_SPACE_KB} /1000/1000 )) GB")
    local availableDiskSpaceToDisplay=$(( availableDiskSpace /1000/1000 ))
    local availableDiskSpaceToDisplay=$(printf "%-11s" "${availableDiskSpaceToDisplay} GB")

    local writePermIssueFound=false
    local writePermStateToDisplay=$(printf "%-44s" && printSuccess "[OK]")

    if (( $(touch "$INSTALL_DIR/tmp_file" &>/dev/null; echo $?; rm "$INSTALL_DIR/tmp_file" &>/dev/null) == 1 )); then
        writePermIssueFound=true
        writePermStateToDisplay=$(printf "%-44s" && printError "[NOT OK]")
    fi

    local sudoerRightsIssueFound="none"
    local sudoerRightsToDisplay=$(printf "%-44s" && printSuccess "[OK]")

    # validate sudo rights without password
    sudo -k -n true 2> /dev/null
    if [[ ! $? == 0 ]]; then
        sudoerRightsIssueFound="blocker"
        sudoerRightsToDisplay=$(printf "%-44s" && printError "[NOT OK]")
    else
        if ! sudo grep "^#includedir.*/etc/sudoers.d" /etc/sudoers &>/dev/null; then
            [ ${sudoerRightsIssueFound} == "none" ] && sudoerRightsIssueFound="warning"
            sudoerRightsToDisplay=$(printf "%-44s" && printWarning "[WARNING]")
        fi
    fi

    println
    println "                 MINIMUM        RECOMMENDED     AVAILABLE"
    println "RAM              $minRAMToDisplay $recRAMToDisplay $availableRAMToDisplay $RAMStateToDisplay"
    println "CPU              $minCoresToDisplay $recCoresToDisplay $availableCoresToDisplay $coresStateToDisplay"
    println "Disk Space       $minDiskSpaceToDisplay $recDiskSpaceToDisplay $availableDiskSpaceToDisplay $diskStateToDisplay"
    println "Write Permission $writePermStateToDisplay"
    println "Sudoer Rights    $sudoerRightsToDisplay"
    println

    if [[ ${osIssueFound} == true ]]; then
        println $(printError "ERROR: The OS version doesn't match requirements.")
        println $(printWarning "NOTE: You need a server with CentOS or RHEL OS of $MIN_OS_VERSION version at least.")
        println
    fi

    if [[ ${resourceIssueFound} == "blocker" ]]; then
        println $(printError "ERROR: The resources available are lower than minimum.")
        println
    fi

    if [[ ${resourceIssueFound} == "warning" ]]; then
        println $(printWarning "!!! The resources available are lower than recommended.")
        println
    fi

    if [[ ${writePermIssueFound} == true ]]; then
        println "$(printError "ERROR: Installation directory \"")$(printImportantLink $INSTALL_DIR)$(printError "\" cannot be written to.")"
        println
    fi

    if [[ ${sudoerRightsIssueFound} == "blocker" ]]; then
        println $(printError "ERROR: This account '${USER}' does not have sufficient sudo rights to perform an installation.")
        println $(printWarning "NOTE: Grant privileges to run sudo without password to '${USER}' user and restart installation.")
        println
    fi

    if [[ ${sudoerRightsIssueFound} == "warning" ]]; then
        println $(printWarning "We could not find '#includedir /etc/sudoers.d' in /etc/sudoers.")
        println $(printWarning "This entry can be removed by security teams.")
        println $(printWarning "Codenvy will install but workspaces may fail to start.")
        println $(printWarning "During installation we will create a new user named 'codenvy',")
        println $(printWarning "which will be granted passwordless sudoer rights.")
        println $(printWarning "These rights are provided in the '/etc/sudoers.d' directory.")
        println $(printWarning "Contact your system administrator to discuss security options.")
        println
    fi

    if [[ ${osIssueFound} == true ]] || [[ ${resourceIssueFound} == "blocker" ]] || [[ ${writePermIssueFound} == true ]] || [[ ${sudoerRightsIssueFound} == "blocker" ]]; then
        exit 1;
    fi

    if [[ ${sudoerRightsIssueFound} == "warning" || ${resourceIssueFound} == "warning" ]] && [[ ${SUPPRESS} == false ]]; then
        pressYKeyToContinue "Proceed?"
        println
    fi
}

checkResourceAccess() {
    local resourceIssueFound=false
    local printStatus=true

    for resource in ${EXTERNAL_DEPENDENCIES[@]}; do
        doCheckResourceAccess ${resource} ${printStatus} || resourceIssueFound=true
    done

    println

    if [[ ${resourceIssueFound} == true ]]; then
        println $(printError "ERROR: Some external repositories are not accessible.")
        println
        println $(printWarning "NOTE: This is probably a temporary issue.")
        println $(printWarning "NOTE: Run 'wget --spider <url>' to check for access.")
        println $(printWarning "NOTE: Restart installation once access is restored.")
        println $(printWarning "NOTE: You may consider setting up a proxy server if access is blocked.")
        exit 1
    fi
}

doCheckResourceAccess() {
    local resource=$1
    local printStatus=$2
    local url=$(echo ${resource} | awk -F'|' '{print $1}');
    local cookie=$(echo ${resource} | awk -F'|' '{print $2}');
    local checkFailed=0

    if [[ "${cookie}" == "" ]]; then
        wget --timeout=20 --tries=5 --quiet --spider ${url} || checkFailed=1
    else
        wget --timeout=20 --tries=5 --quiet --spider --no-cookies --no-check-certificate --header "${cookie}" ${url} || checkFailed=1
    fi

    if [[ ${printStatus} == true ]]; then
        local checkStatus=$([[ ${checkFailed} == 0 ]] && echo $(printSuccess "[OK]") || echo $(printError "[NOT OK]"))
        println "$(printf "%-${DEPENDENCIES_STATUS_OFFSET}s" ${url}) ${checkStatus}"
    fi

    return ${checkFailed}
}

doCheckRedhatSubscription() {
    # check if subscription-manager exists in system
    if ! sudo subscription-manager &> /dev/null; then
        return
    fi

    # check validity of subscription
    if ! sudo subscription-manager status &> /dev/null; then
        local reposToDisplay=$(printf "'%s', " "${RHEL_REPOS[@]}")
        reposToDisplay=${reposToDisplay::-2}

        println $(printError "RHEL OS subscription is invalid.")
        println $(printWarning "NOTE: Please, make sure that this system is register and next repositories are enabled:")
        println $(printWarning "NOTE: ${reposToDisplay}")
        exit 1
    fi

    # check if all required repos are enabled
    local disabledRequiredRepoToDisplay
    local listOfEnabledRepo=$(sudo subscription-manager repos --list-enabled)
    for repo in ${RHEL_REPOS[@]}; do
        if ! echo $listOfEnabledRepo | grep "$repo" &> /dev/null; then
            disabledRequiredRepoToDisplay="${disabledRequiredRepoToDisplay}'$repo', "
        fi
    done

    if [[ -n "$disabledRequiredRepoToDisplay" ]]; then
        println $(printError "Next required repositories aren't enabled: ${disabledRequiredRepoToDisplay::-2}")
        println $(printWarning "NOTE: You could use command 'sudo subscription-manager repos --enable=<repo-name>' to enable them.")
        exit 1
    fi
}

# Checks if network interface defined in --advertise-network-interface variable or in the "docker_cluster_advertise" codenvy property is present in system.
# If not, ask user to type correct interface name in console.
checkNetworkInterface() {
    if [[ "${ARTIFACT}" == "codenvy" ]] && [[ "${VERSION}" =~ ^(5).* ]]; then
        if [[ -z "${ADVERTISE_NETWORK_INTERFACE}" ]]; then
            ADVERTISE_NETWORK_INTERFACE=$(readProperty "docker_cluster_advertise")
        fi

        # check if ADVERTISE_NETWORK_INTERFACE network interface is present in system
        if ! ls /sys/class/net | grep "${ADVERTISE_NETWORK_INTERFACE}" &> /dev/null; then
            println $(printWarning "Codenvy needs to advertise itself on a network interface. By default we use '${ADVERTISE_NETWORK_INTERFACE}' but can't find that interface on this machine.")
            print $(printWarning "Please enable '${ADVERTISE_NETWORK_INTERFACE}' or enter the name of an available interface: ")

            local newValueOfDockerClusterAdvertise=$(askProperty)

            println
        fi

        if [ -n "${newValueOfDockerClusterAdvertise}" ]; then
            insertProperty "docker_cluster_advertise" "${newValueOfDockerClusterAdvertise}"
        else
            insertProperty "docker_cluster_advertise" "${ADVERTISE_NETWORK_INTERFACE}"
        fi
    fi
}

printPreInstallInfo_multi() {
    doEnsureLicenseAgreement

    println "Checking system pre-requisites..."
    println

    doCheckAvailableResourcesLocally 1000000 1 1000000 2000000 2 50000000

    preConfigureSystem

    configureProxySettings

    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        println "Configuring system properties with file://${CONFIG}..."
        println
    fi

    if [[ ${DISABLE_MONITORING_TOOLS} == true ]]; then
        insertProperty "install_monitoring_tools" "false"
    fi

    if [[ ${SUPPRESS} == true ]]; then
        if [ -n "${HOST_NAME}" ]; then
            insertProperty "host_url" ${HOST_NAME}
        fi

        doGetHostsVariables

        println "Hostname of Codenvy              : "${HOST_NAME}
        println "Hostname of Puppet master node   : "${PUPPET_MASTER_HOST_NAME}
        println "Hostname of data node            : "${DATA_HOST_NAME}
        println "Hostname of API node             : "${API_HOST_NAME}
        println "Hostname of builder node         : "${BUILDER_HOST_NAME}
        println "Hostname of runner node          : "${RUNNER_HOST_NAME}
        println "Hostname of datasource node      : "${DATASOURCE_HOST_NAME}
        println "Hostname of analytics node       : "${ANALYTICS_HOST_NAME}
        println "Hostname of site node            : "${SITE_HOST_NAME}
        println
    else
        println "Codenvy hostnames:       will prompt for entry"
        println

        askHostnameAndInsertProperty "Set hostname of Codenvy" "host_url"
        askHostnameAndInsertProperty "Set hostname of Puppet master node" "puppet_master_host_name"
        askHostnameAndInsertProperty "Set hostname of data node" "data_host_name"
        askHostnameAndInsertProperty "Set hostname of API node" "api_host_name"
        askHostnameAndInsertProperty "Set hostname of builder node" "builder_host_name"
        askHostnameAndInsertProperty "Set hostname of runner node" "runner_host_name"
        askHostnameAndInsertProperty "Set hostname of datasource node" "datasource_host_name"
        askHostnameAndInsertProperty "Set hostname of analytics node" "analytics_host_name"
        askHostnameAndInsertProperty "Set hostname of site node" "site_host_name"

        clearLine

        println
        pressYKeyToContinue "Proceed?"
        println
    fi

    println "Checking access to Codenvy nodes..."
    println

    doCheckAvailableResourcesOnNodes

    if [[ "${ARTIFACT}" == "codenvy" ]]; then
        doCheckAvailablePorts_multi
    fi

    println "Checking access to external dependencies..."
    println

    checkResourceAccess
}

doCheckAvailableResourcesOnNodes() {
    local globalNodeIssueFound=false
    local globalOsIssueFound=false

    doGetHostsVariables

    local output=$(validateHostname ${PUPPET_MASTER_HOST_NAME})
    if [ "${output}" != "success" ]; then
        println $(printError "ERROR: The hostname '${PUPPET_MASTER_HOST_NAME}' is not available.")
        println
        println $(printWarning "NOTE: This might happen when the node is down or not accessible")
        println $(printWarning "NOTE: by a pre-configured DNS host name. Make sure you have")
        println $(printWarning "NOTE: an appropriate entry in '/etc/hosts' file.")
        exit 1
    fi
    println "$(printf "%-43s" "${PUPPET_MASTER_HOST_NAME}" && printSuccess "[OK]")"

    for HOST in ${DATA_HOST_NAME} ${API_HOST_NAME} ${BUILDER_HOST_NAME} ${DATASOURCE_HOST_NAME} ${ANALYTICS_HOST_NAME} ${SITE_HOST_NAME} ${RUNNER_HOST_NAME}; do
        # check if host available
        local output=$(validateHostname ${HOST})
        if [ "${output}" != "success" ]; then
            println $(printError "ERROR: The hostname '${HOST}' is not available.")
            println
            println $(printWarning "NOTE: This might happen when the node is down or not accessible")
            println $(printWarning "NOTE: by a pre-configured DNS host name. Make sure you have")
            println $(printWarning "NOTE: an appropriate entry in '/etc/hosts' file.")
            exit 1
        fi

        local sshPrefix="ssh -o BatchMode=yes -o LogLevel=quiet -o StrictHostKeyChecking=no -t ${HOST}"

        # validate ssh access
        ${sshPrefix} "exit"
        if [[ ! $? == 0 ]]; then
            println $(printError "ERROR: There is no ssh access to '${HOST}'")
            println
            println $(printWarning "NOTE: Put public part of ssh key (id_rsa.pub) onto '${HOST}' node")
            println $(printWarning "NOTE: into ~/.ssh folder of '${USER}' user and restart installation")
            exit 1
        fi

        # valid sudo rights
        ${sshPrefix} "sudo -n true 2> /dev/null"
        if [[ ! $? == 0 ]]; then
            println $(printError "ERROR: User '${USER}' doesn't have sudo rights on '${HOST}'")
            println
            println $(printWarning "NOTE: Grant sudo privileges to '${USER}' user and restart installation")
            exit 1
        fi

        if [[ "${HOST}" == "${RUNNER_HOST_NAME}" ]]; then
            MIN_RAM_KB=1500000
            MIN_DISK_SPACE_KB=40000000
        else
            MIN_RAM_KB=1000000
            MIN_DISK_SPACE_KB=20000000
        fi

        local osIssueFound=false

        local osType=""
        local osVersion=""
        local osInfo=""

        case $(${sshPrefix} "uname" | sed 's/\r//') in
            Linux )
                if [[ $(${sshPrefix} "if [[ -f /etc/redhat-release ]]; then echo 1; fi" | sed 's/\r//') == 1 ]]; then
                    osType="RHEL";
                    osVersion=$(${sshPrefix} "cat /etc/redhat-release" | sed 's/.* \([0-9.]*\) .*/\1/')
                    osInfo=$(${sshPrefix} "cat /etc/redhat-release" | sed 's/Linux release //' | sed 's/\r//')

                # SuSE
                elif [[ $(${sshPrefix} "if [[ -f /etc/SuSE-release ]]; then echo 1; fi" | sed 's/\r//') == 1 ]]; then
                    osInfo="SuSE"

                # debian
                elif [[ $(${sshPrefix} "if [[ -f /etc/debian_version ]]; then echo 1; fi" | sed 's/\r//') == 1 ]]; then
                    osInfo=$(${sshPrefix} "cat /etc/issue.net" | sed 's/\r//')

                # other linux OS
                elif [[ $(${sshPrefix} "if [[ -f /etc/lsb-release ]]; then echo 1; fi" | sed 's/\r//') == 1 ]]; then
                    osInfo=$(${sshPrefix} "$(cat /etc/lsb-release | grep '^DISTRIB_ID' | awk -F=  '{ print $2 }')" | sed 's/\r//')
                fi
                ;;

            * )
                osInfo=$(${sshPrefix} "uname" | sed 's/\r//');
                ;;
        esac

        # check on RHEL OS or CentOS of version >= 7.1
        if [[ "${osType}" != "RHEL" || "${osVersion}" < "${MIN_OS_VERSION}" ]]; then
            osIssueFound=true
            globalOsIssueFound=true
        fi

        local availableRAM=$(${sshPrefix} "cat /proc/meminfo | grep MemTotal" | awk '{print $2}')
        local availableRAMIssue=false

        local availableDiskSpace=$(${sshPrefix} "df ${HOME} | tail -1" | awk '{print $4}') # available
        local availableDiskSpaceIssue=false

        if [[ -z "${availableRAM}" || ${availableRAM} < ${MIN_RAM_KB} ]]; then
            availableRAMIssue=true
        fi

        if [[ -z "${availableDiskSpace}" || ${availableDiskSpace} < ${MIN_DISK_SPACE_KB} ]]; then
            availableDiskSpaceIssue=true
        fi

        if [[ ${osIssueFound} == true || ${availableRAMIssue} == true || ${availableDiskSpaceIssue} == true ]]; then
            globalNodeIssueFound=true

            local nodeStateToDisplay=$([ ${osIssueFound} == true ] && echo $(printError "[NOT OK]") || echo $(printWarning "[WARNING]"))
            println "$(printf "%-43s" "${HOST}" ${nodeStateToDisplay})"

            osInfoToDisplay=$(printf "%-47s" "${osInfo}")
            if [[ ${osIssueFound} == true ]]; then
                println "> DETECTED OS: ${osInfoToDisplay} $(printError "[NOT OK]")"
                println
            fi

            if [[ ${availableRAMIssue} == true || ${availableDiskSpaceIssue} == true ]]; then
                println ">                 RECOMMENDED     AVAILABLE"

                if [[ ${availableRAMIssue} == true ]]; then
                    local minRAMToDisplay=$(printf "%-15s" "$(printf "%0.2f" "$( m=34; awk -v m=${MIN_RAM_KB} 'BEGIN { print m/1000/1000 }' )") GB")
                    local availableRAMToDisplay=$(${sshPrefix} "cat /proc/meminfo | grep MemTotal" | awk '{tmp = $2/1000/1000; printf"%0.2f",tmp}')
                    local availableRAMToDisplay=$(printf "%-11s" "${availableRAMToDisplay} GB")

                    println "> RAM             $minRAMToDisplay $availableRAMToDisplay $(printWarning "[WARNING]")"
                fi

                if [[ ${availableDiskSpaceIssue} == true ]]; then
                    local minDiskSpaceToDisplay=$(printf "%-15s" "$(( ${MIN_DISK_SPACE_KB}/1000/1000 )) GB")
                    local availableDiskSpaceToDisplay=$(( availableDiskSpace /1000/1000 ))
                    local availableDiskSpaceToDisplay=$(printf "%-11s" "${availableDiskSpaceToDisplay} GB")

                    println "> Disk Space      $minDiskSpaceToDisplay $availableDiskSpaceToDisplay $(printWarning "[WARNING]")"
                fi

                println
            fi
        else
            println "$(printf "%-43s" "${HOST}" && printSuccess "[OK]")"
        fi
    done

    println

    if [[ ${globalNodeIssueFound} == true ]]; then
        if [[ ${globalOsIssueFound} == true ]]; then
            println $(printError "ERROR: The OS version doesn't match requirements.")
            println
            println $(printWarning "NOTE: You need a node with CentOS or RHEL OS of $MIN_OS_VERSION version at least.")
            exit 1;
        fi

        println $(printWarning "!!! Some nodes do not match recommended.")
        println
        if [[ ${SUPPRESS} == false ]]; then
            pressYKeyToContinue "Proceed?"
            println
        fi
    fi
}

setStepIndicator() {
    CURRENT_STEP=$1
    echo ${CURRENT_STEP} > /tmp/im_current_step 2>/dev/null
    shift

    updateLine ${STEP_LINE} ${INSTALLATION_STEPS[${CURRENT_STEP}]}
    updateProgress ${CURRENT_STEP}
}

################ Timer
initTimer() {
    START_TIME=$(date +%s)
}

runTimer() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    updateTimer &
    TIMER_PID=$!
}

killTimer() {
    if [ -n "${TIMER_PID}" ]; then
        kill -KILL ${TIMER_PID}
    fi
}

continueTimer() {
    if [ -n "${TIMER_PID}" ]; then
        kill -SIGCONT ${TIMER_PID}
    fi
}

pauseTimer() {
    if [ -n "${TIMER_PID}" ]; then
        kill -SIGSTOP ${TIMER_PID}
    fi
}

updateTimer() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    for ((;;)); do
        END_TIME=$(date +%s)
        DURATION=$(( $END_TIME-$START_TIME))
        M=$(( $DURATION/60 ))
        S=$(( $DURATION%60 ))

        updateLine ${TIMER_LINE} "Elapsed time: "${M}"m "${S}"s"

        sleep 1 &>/dev/null
    done
}

################ Puppet Info Printer
updatePuppetInfo() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    for ((;;)); do
        local line=$(sudo tail -n 1 /var/log/puppet/puppet-agent.log 2>/dev/null)
        if [[ -n "$line" ]]; then
            updateLine ${PUPPET_LINE} "[PUPPET: ${line:0:$(( ${DEPENDENCIES_STATUS_OFFSET}-8 ))}...]"     # print first N symbols of line
        else
            updateLine ${PUPPET_LINE} ""
        fi
        sleep 1 &>/dev/null
    done
}

runPuppetInfoPrinter() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    updatePuppetInfo &
    PRINTER_PID=$!
}

killPuppetInfoPrinter() {
    if [ -n "${PRINTER_PID}" ]; then
        kill -KILL ${PRINTER_PID}
    fi
}

continuePuppetInfoPrinter() {
    if [ -n "${PRINTER_PID}" ]; then
        kill -SIGCONT ${PRINTER_PID}
    fi
}

pausePuppetInfoPrinter() {
    if [ -n "${PRINTER_PID}" ]; then
        kill -SIGSTOP ${PRINTER_PID}
    fi
}

updateProgress() {
    if [[ ${SILENT} == true ]]; then
        return
    fi

    local current_step=$1
    local last_step=${LAST_INSTALLATION_STEP}

    local progress_number=$(( ${current_step}*100/${last_step} ))

    local progress_field=
    for ((i=1; i<=${current_step}*${PROGRESS_FACTOR}; i++));  do
       progress_field="${progress_field}="
    done

    progress_field=$(printf "[%-$(( ${last_step}*${PROGRESS_FACTOR} ))s]" ${progress_field})

    local message="Full install ${progress_field} ${progress_number}%"

    updateLine ${PROGRESS_LINE} "${message}"
}

runFooterUpdater() {
    updateFooter &
    LINE_UPDATER_PID=$!
}

killFooterUpdater() {
    if [ -n "${LINE_UPDATER_PID}" ]; then
        kill -KILL ${LINE_UPDATER_PID}
    fi
}

continueFooterUpdater() {
    if [ -n "${LINE_UPDATER_PID}" ]; then
        kill -SIGCONT ${LINE_UPDATER_PID}
    fi
}

pauseFooterUpdater() {
    if [ -n "${LINE_UPDATER_PID}" ]; then
        kill -SIGSTOP ${LINE_UPDATER_PID}
    fi
}

# footer lines count descendently
initFooterPosition() {
    println
    println
    println
    println

    STEP_LINE=4
    PUPPET_LINE=3
    PROGRESS_LINE=2
    TIMER_LINE=1

    echo "" > /tmp/im_line_1 2>/dev/null
    echo "" > /tmp/im_line_2 2>/dev/null
    echo "" > /tmp/im_line_3 2>/dev/null
    echo "" > /tmp/im_line_4 2>/dev/null
    echo "" > /tmp/im_prev_line_1 2>/dev/null
    echo "" > /tmp/im_prev_line_2 2>/dev/null
    echo "" > /tmp/im_prev_line_3 2>/dev/null
    echo "" > /tmp/im_prev_line_4 2>/dev/null
}

runInternetAccessChecker() {
    echo "0" > /tmp/im_internet_access_lost 2>/dev/null
    updateInternetAccessChecker &
    INTERNET_CHECKER_PID=$!
}

killInternetAccessChecker() {
    if [ -n "${INTERNET_CHECKER_PID}" ]; then
        kill -KILL ${INTERNET_CHECKER_PID}
    fi
}

continueInternetAccessChecker() {
    if [ -n "${INTERNET_CHECKER_PID}" ]; then
        kill -SIGCONT ${INTERNET_CHECKER_PID}
    fi
}

pauseInternetAccessChecker() {
    if [ -n "${INTERNET_CHECKER_PID}" ]; then
        kill -SIGSTOP ${INTERNET_CHECKER_PID}
    fi
}

updateInternetAccessChecker() {
    for ((;;)); do
        doUpdateInternetAccessChecker
        local checkFailed=$?
        local tmp=$(cat /tmp/im_current_step 2>/dev/null)

        if [[ "${tmp}" =~ ^[0-9]*$ ]]; then
            CURRENT_STEP=${tmp}
        fi

        if [[ ${checkFailed} == 1 ]]; then
            updateLine ${STEP_LINE} "${INSTALLATION_STEPS[${CURRENT_STEP}]} $(printError " Internet connection lost... reconnecting...")"
        else
            updateLine ${STEP_LINE} "${INSTALLATION_STEPS[${CURRENT_STEP}]}"
        fi

        sleep 1m &>/dev/null
    done
}

doUpdateInternetAccessChecker() {
    local printStatus=false
    local checkFailed=0

    for resource in ${EXTERNAL_DEPENDENCIES[@]}; do
        local isRequiredToCheck=$(echo ${resource} | awk -F'|' '{print $3}');

        if [[ ${isRequiredToCheck} == 1 ]]; then
            doCheckResourceAccess ${resource} ${printStatus} || checkFailed=1
        fi

        if [[ ${checkFailed} == 1 ]]; then
            echo ${checkFailed} > /tmp/im_internet_access_lost 2>/dev/null
        fi
    done

    return ${checkFailed}
}

################ Loader
runLoader() {
    updateLoader &
    LOADER_PID=$!
}

killLoader() {
    if [ -n "${LOADER_PID}" ]; then
        kill -KILL ${LOADER_PID} &>/dev/null
    fi
}

continueLoader() {
    if [ -n "${LOADER_PID}" ]; then
        cursorSave
        kill -SIGCONT ${LOADER_PID} &>/dev/null
    fi
}

pauseLoader() {
    if [ -n "${LOADER_PID}" ]; then
        kill -SIGSTOP ${LOADER_PID} &>/dev/null
    fi
}

updateLoader() {
    CHAR_CHANGE_TIMEOUT_SEC=0.25
    LOADER_CHARS=('-' '\\' '|' '/')

    for ((;;)) do
        for char in ${LOADER_CHARS[@]}; do
            cursorSave
            sleep ${CHAR_CHANGE_TIMEOUT_SEC}
            echo -en ${char}
            cursorRestore
        done
    done
}


printPostInstallInfo_codenvy() {
    local systemAdminName=$(readProperty "admin_ldap_user_name")
    if [ -z "$systemAdminName" ]; then
        systemAdminName=$(readProperty "codenvy_admin_name")  # property name starting from version 5.0.0-M4
    fi


    if [[ "${VERSION}" =~ ^([4-5]).* ]]; then
        local systemAdminPassword=$(readProperty "admin_ldap_password")
        if [ -z "$systemAdminPassword" ]; then
            systemAdminPassword=$(readProperty "codenvy_admin_initial_password")  # property name starting from version 5.0.0-M4
        fi
    else
        local systemAdminPassword=$(readProperty "system_ldap_password")
    fi

    if [ -z ${HOST_NAME} ]; then
        HOST_NAME=$(readProperty "host_url")
    fi

    println
    println "Codenvy at:       $(printImportantLink "http://$HOST_NAME")"
    println "Admin user name:  $(printImportantInfo $systemAdminName)"
    println "Admin password:   $(printImportantInfo $systemAdminPassword)"
    println
    println "$(printWarning "!!! Set up DNS or add a hosts rule on your clients to reach this hostname.")"
}

printPostInstallInfo_installation-manager-cli() {
    println
    println "Codenvy Installation Manager is installed into ${INSTALL_DIR}/cli directory"
}


# $1 - admin name
# $2 - admin password
# $3 - version of check tool
#
# return 1 on error
postFlightCheckOfCodenvy() {
    local adminName="$1"
    local adminPassword="$2"
    local imageVersion="$3"

    if [[ $SKIP_POST_FLIGHT_CHECK == true ]]; then
        return
    fi

    echo "Verifying installation..." >> ${INSTALL_LOG}

    println
    printPrompt
    echo -en "$(printf "%-73s" "Verifying installation...")"

    runLoader

    # load che-test tool
    pullDockerImage "codenvy/che-test:${imageVersion}"
    if [[ $? != 0 ]]; then
        return 1
    fi

    # load che-ip tool
    pullDockerImage "codenvy/che-ip:${imageVersion}"
    if [[ $? != 0 ]]; then
        return 1
    fi

    # run che-test tool
    runDockerTool "codenvy/che-test:${imageVersion}" \
                  "post-flight-check" \
                  "$adminName" \
                  "$adminPassword" \
                  "Codenvy is installed, but basic post-flight API tests failed. We have this error message:"

    if [[ $? != 0 ]]; then
        return 1
    fi
}

# $1 - docker image
#
# return 1 on error
pullDockerImage() {
    local image=$1
    local errorMessage

    continueLoader
    errorMessage=$(sudo docker pull "$image" 2>&1)

    if [[ $? != 0 ]]; then
        pauseLoader

        echo "Error of loading 'codenvy/che-test' docker image: '$errorMessage'" >> ${INSTALL_LOG}

        echo -e "$(printError "[NOT OK]")"
        println
        println $(printError "Unexpected error occurred. See ${INSTALL_LOG} for more details")

        return 1
    fi

    pauseLoader
}

# $1 - docker image
# $2 - command
# $3 - user
# $4 - password
# $5 - error message prefix
#
# return 1 on error
runDockerTool() {
    local image=$1
    local command=$2
    local user=$3
    local password=$4
    local errorMessagePrefix=$5

    continueLoader
    local errorMessage=$( { sudo docker run --rm -v /var/run/docker.sock:/var/run/docker.sock "$image" "$command" \
                                                                                                       --user="$user" \
                                                                                                       --password="$password" \
                                                                                                       --logger-prefix-off \
                                                                                                       --quiet \
                                                                                                       --port=80 2>&3 1>&1 3>&- | {
       i=1
       while IFS= read -r line; do
          pauseLoader

          if [[ $i > 1 ]]; then
             echo -e "$(printSuccess "[OK]")"

             echo -e " [OK]" >> ${INSTALL_LOG}
          else
             echo -e ' ' # clean up loader
          fi

          echo -en "$line" >> ${INSTALL_LOG}

          echo -en "\e[34m[CODENVY] \e[0m$((i++)). "
          echo -en "$(printf "%-70s" "$line")"

          continueLoader
       done;
    } } 3>&1 1>&2 | {
       read -r error;
       echo "$error"
    } )

    pauseLoader

    if [[ -n "$errorMessage" ]]; then
        echo -e " [NOT OK]" >> ${INSTALL_LOG}
        echo "$errorMessage" >> ${INSTALL_LOG}

        echo -e "$(printError "[NOT OK]")"
        println
        println $(printError "$errorMessagePrefix")
        println $(printError "'$errorMessage'")

        return 1
    else
        echo -e $(printSuccess "[OK]")
        echo -e " [OK]" >> ${INSTALL_LOG}
    fi
}


clear

setRunOptions "$@"

println "Welcome. This program installs ${ARTIFACT_DISPLAY} ${VERSION}."
println

if [[ ${#UNRECOGNIZED_PARAMETERS[@]} != 0 ]]; then
    println $(printWarning "!!! You passed unrecognized parameters:")
    for var in "${UNRECOGNIZED_PARAMETERS[@]}"; do
        println $(printWarning "'$var'")
    done

    if [[ ${SUPPRESS} == false ]]; then
        pressYKeyToContinue "Proceed?"
    fi

    println
fi

printPreInstallInfo_${CODENVY_TYPE}

setterm -cursor off

initFooterPosition
initTimer

runFooterUpdater
runTimer
runInternetAccessChecker

doConfigureSystem
doInstallJava
doInstallImCli

if [[ "${ARTIFACT}" == "codenvy" ]]; then
    runDownloadProgressUpdater
    doDownloadBinaries
    pauseDownloadProgressUpdater

    runPuppetInfoPrinter
    doInstallCodenvy
    pausePuppetInfoPrinter
fi

setStepIndicator ${LAST_INSTALLATION_STEP}
updateLine ${PUPPET_LINE} " "

sleep 2

if [[ ${SILENT} == true ]]; then
    cursorUp
    cursorUp
    cursorUp
fi

pauseTimer
pauseInternetAccessChecker
pauseFooterUpdater

if [[ "${ARTIFACT}" == "codenvy" ]] && [[ "${VERSION}" =~ ^([4-5]).* ]]; then
    systemAdminName=$(readProperty "admin_ldap_user_name")
    if [ -z "$systemAdminName" ]; then
        systemAdminName=$(readProperty "codenvy_admin_name")  # property name starting from version 5.0.0-M4
    fi

    systemAdminPassword=$(readProperty "admin_ldap_password")
    if [ -z "$systemAdminPassword" ]; then
        systemAdminPassword=$(readProperty "codenvy_admin_initial_password")  # property name starting from version 5.0.0-M4
    fi

    toolVersion=$(if [[ "${VERSION}" =~ .*SNAPSHOT$ ]]; then echo "nightly"; else echo ${VERSION}; fi)

    postFlightCheckOfCodenvy "$systemAdminName" "$systemAdminPassword" "$toolVersion"
    println
fi

printPostInstallInfo_${ARTIFACT}
