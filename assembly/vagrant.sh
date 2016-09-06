#!/bin/bash
#
# CODENVY CONFIDENTIAL
# __________________
#
#  [2012] - [2013] Codenvy, S.A.
#  All Rights Reserved.
#
# NOTICE:  All information contained herein is, and remains
# the property of Codenvy S.A. and its suppliers,
# if any.  The intellectual and technical concepts contained
# herein are proprietary to Codenvy S.A.
# and its suppliers and may be covered by U.S. and Foreign Patents,
# patents in process, and are protected by trade secret or copyright law.
# Dissemination of this information or reproduction of this material
# is strictly forbidden unless prior written permission is obtained
# from Codenvy S.A..
#

# To use vagrant you should have next lines in /etc/hosts file:
# 192.168.56.99 onprem.box.com


getUserHelp() {
  echo "usage: ./vagrant.sh  [--nobuild|--b] [--nogwt|--g] [--notests|--t] [--U] [--o] [-h|--h|-help|--help] [--destroy|--d] [--multi] [--dp] [--centos6|--c6]"
}

getOptionsHelp() {
    echo "Options:"
    echo "         --destroy, --d"
    echo "             Destroy an old vagrant before starting update."
    echo ""
    echo "         --nogwt, --g"
    echo "             Do not build gwt client."
    echo ""
    echo "         --multi"
    echo "             Use multiserver instead of single server environment."
    echo ""
    echo "         --dp"
    echo "             Do not make pull in deployment project."
    echo ""
    echo "         --dc"
    echo "             Do not make checkout deployment project."
    echo ""
    echo "         --notests, --t"
    echo "             Do not run tests."
    echo ""
    echo "         --o"
    echo "             Use maven offline option."
    echo ""
    echo "         --U"
    echo "             Add -U to maven command."
    echo ""
    echo "         --nobuild, --b"
    echo "             Do not build onpremises project."
    echo ""
    echo "         --centos6, --c6"
    echo "             Provision Centos6"
    echo ""
    echo "         -h, --h, -help, --help"
    echo "             Show user help"
}

deleteFileIfExists() {
  if [ -f $1 ]; then
    echo $1
    rm -rf $1
  fi
}

cloneDeploymentProject() {
  # if deployment project does not exist, clone it from github
  if [ ! -d "../../deployment" ]; then
    git clone git@github.com:codenvy/deployment.git ../../deployment
  fi
}

pullDeploymentProject() {
  cd ../../deployment
  git pull
  cd ../codenvy/assembly
}

parseParameters() {
  for i in "$@"
  do
    case "$i" in
      --help | -h | -help | --h)
        getUserHelp
        getOptionsHelp
        exit 0
        ;;
      --notests | --t)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -Dmaven.test.skip=true"
        ;;
      --nobuild | --b)
        SKIP_BUILD=true
        ;;
      --U)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -U"
        ;;
      --o)
        MAVEN_PARAMS=${MAVEN_PARAMS}"  -o"
        ;;
      --destroy | --d)
        DESTROY_VM=true
        ;;
      --centos6 | --c6)
        OS_CENTOS6=true
        ;;
      --multi)
        MULTI_SERVER=true
        ;;
      --scalable-aio)
        SCALABLE_AIO=true
        ;;
      --dp)
        MAKE_PULL_IN_DEPLOYMENT=false
        ;;
      --nogwt)
        mvn_version=`mvn -v | grep "Apache Maven" | sed 's/Apache Maven //g' | sed 's/ .*//g'`
        if [[ ${mvn_version} < "3.2.1" ]]; then
          echo "'--nogwt' is supported for maven 3.2.1 or later"
        else
          MAVEN_PARAMS=${MAVEN_PARAMS}" -pl \"!onpremises-ide-compiling-war-ide\""
        fi
        ;;
      *)
        echo "Unknown parameter $i."
        exit 1
        ;;
    esac
  done
}

# Check tomcat logs for errors
checkThatTomcatStartsWithoutErrors() {
  [[ ${1} == "" ]] && tomcat_name="aio" || tomcat_name=${1}
  echo -e "\n########  Check tomcat logs for errors | " ${tomcat_name} "\t  ########"

  SERVER_STATE='Starting'
  COUNTER=0
  local testfile=/tmp/catalin.out
  local remoteLogsFile=../codenvy/tomcat/logs/catalina.out
  local fromLine=$(vagrant ssh $1 -c "sudo grep -n 'o.a.c.s.VersionLoggerListener .* Server version:        Apache Tomca' ${remoteLogsFile}  | tail -1" | sed 's/:.*//g')
  while [[ ${SERVER_STATE} != "Started" ]]; do
    deleteFileIfExists ${testfile}

    vagrant ssh ${1} -c "sudo tail -n +${fromLine:-1} ${remoteLogsFile}" > ${testfile}

    if grep -Fq "[ERROR]" ${testfile}
    then
      # set error occurs to true and do not exit because other server should be checked in multi server setup
      TOMCAT_ERRORS=true
      echo "Exeption occured during tomcat start. Take a look ${testfile}. Exiting"
      cat ${testfile}
      # Set state started to exit from loop
      SERVER_STATE=Started
    else
      if grep -Fq "Server startup" ${testfile}
      then
        echo "Server ${1} started"
        SERVER_STATE=Started
      fi

      echo "standalone build state = ${SERVER_STATE}  Attempt ${COUNTER}"
      sleep 5
      let COUNTER=COUNTER+1
    fi
  done
}

startNotificationsHandling() {
    # send notification if build fails
    onAbort() {
        if (($? != 0)); then
            type notify-send >/dev/null 2>&1 && notify-send -t 100000 --urgency=normal -i "error" "Update failed"
            exit 1
        fi
        exit 0
    }

    # catch stopping over exit command
    trap 'onAbort' 0
}

endNotificationsHandling() {
    # unset catch stopping over exit command
    trap : 0
}

startNotificationsHandling $@

# set mode that halt script execution on error status codes from commands
set -e

# set default parameters of script
MAVEN_PARAMS=""
SKIP_BUILD=false
DESTROY_VM=false
MULTI_SERVER=false
MAKE_PULL_IN_DEPLOYMENT=true
OS_CENTOS6=false
SCALABLE_AIO=false
#

# parse parameters specified by user
parseParameters "$@"

cloneDeploymentProject

if [ ${MAKE_PULL_IN_DEPLOYMENT} == true ]; then
  pullDeploymentProject
fi

# Build project
if [ ${SKIP_BUILD} == false ]; then
  mvn clean install ${MAVEN_PARAMS}
fi

# Use Centos6 OS
if [ ${OS_CENTOS6} == true ]; then
  export OS_CENTOS6="true"
fi

if [ ${MULTI_SERVER} == true ]; then
  # Copy tomcats for multi-server environment
  cp -f onpremises-ide-packaging-tomcat-api/target/*.zip ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-tomcat-api.zip
  cp -f onpremises-ide-packaging-tomcat-site/target/*.zip ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-tomcat-site.zip
  cp -f ../analytics/analytics-tomcat-pkg/target/*.zip ../../deployment/puppet/modules/multi_server/files/analytics-tomcat-pkg.zip
  cp -f onpremises-ide-packaging-tomcat-ext-server/target/*.tar.gz ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-tomcat-ext-server.tar.gz
  cp -f onpremises-ide-packaging-zip-terminal/target/*.tar.gz ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-zip-terminal.tar.gz

  # Open folder for multi server env
  cd ../../deployment/puppet/vagrant-multi-vm-env

elif [ ${SCALABLE_AIO} == true ]; then
  # Copy all-in-one tomcat zip to puppet folder for subsequent update
  cp -f onpremises-ide-packaging-tomcat-codenvy-allinone/target/*.zip ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-tomcat-codenvy-allinone.zip
  cp -f onpremises-ide-packaging-tomcat-ext-server/target/*.tar.gz ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-tomcat-ext-server.tar.gz
  cp -f onpremises-ide-packaging-zip-terminal/target/*.tar.gz ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-zip-terminal.tar.gz
  cp -f onpremises-ide-packaging-tomcat-im/target/*.zip ../../deployment/puppet/modules/codenvy_im/files/onpremises-ide-packaging-tomcat-im.zip
  cp -f onpremises-ide-packaging-tomcat-ext-server/target/*.tar.gz ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-tomcat-ext-server.tar.gz
  cp -f onpremises-ide-packaging-zip-terminal/target/*.tar.gz ../../deployment/puppet/modules/multi_server/files/onpremises-ide-packaging-zip-terminal.tar.gz

  # Open folder for AIO env
  cd ../../deployment/puppet/vagrant-scalable-aio

else
  # Copy all-in-one tomcat zip to puppet folder for subsequent update
  cp -f onpremises-ide-packaging-tomcat-codenvy-allinone/target/*.zip ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-tomcat-codenvy-allinone.zip
  cp -f onpremises-ide-packaging-tomcat-ext-server/target/*.tar.gz ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-tomcat-ext-server.tar.gz
  cp -f onpremises-ide-packaging-zip-terminal/target/*.tar.gz ../../deployment/puppet/modules/all_in_one/files/onpremises-ide-packaging-zip-terminal.tar.gz
  cp -f onpremises-ide-packaging-tomcat-im/target/*.zip ../../deployment/puppet/modules/codenvy_im/files/onpremises-ide-packaging-tomcat-im.zip

  # Open folder for AIO env
  cd ../../deployment/puppet
fi

# Destroy existing VM if user set corresponding parameter
if [ $DESTROY_VM == true ]; then
  vagrant destroy -f
fi

# Run vagrant. Don't do it if vagrant is runing already
vagrant up --no-provision #to avoid duplicated provisioning at next command
# Update changed configuration
vagrant provision

TOMCAT_ERRORS=false
# Check tomcat logs for errors
if [ ${MULTI_SERVER} == true ]; then
  checkThatTomcatStartsWithoutErrors "api"
  checkThatTomcatStartsWithoutErrors "site"
  checkThatTomcatStartsWithoutErrors "analytics"
elif [ ${SCALABLE_AIO} == true ]; then
  checkThatTomcatStartsWithoutErrors "devbox"
else
  checkThatTomcatStartsWithoutErrors ""
fi

if [ ${TOMCAT_ERRORS} == true ]; then
  exit 1
fi

type notify-send >/dev/null 2>&1 && notify-send -t 100000 --urgency=low -i "terminal" "Updated successfully"

endNotificationsHandling

echo '================================================'
echo '                Update completed'
echo '================================================'
