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
detect_user_for_upload() {
    case "${SERVER}" in
      "a9" | "a10" | "a11" | "machines" | "nightly4" )
          SSH_KEY_NAME=upl-dev
          SSH_USER_NAME=upl-dev
      ;;
      "prod")
          SSH_KEY_NAME=upl-all
          SSH_USER_NAME=upl-all
      ;;
    esac
}

doUploadTomcat() {
echo 'Uploading:'
    FILE_LOCATION=$1
    UPLOAD_DESTINATION=$2
    FILE_NAME=`echo ${FILE_LOCATION} | cut -d "/" -f 3`
    FILE_NAME_WITH_POSTFIX=${FILE_NAME}_${DATE}
    scp -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${CLOUD_IDE_HOME}/${FILE_LOCATION} ${SSH_USER_NAME}@${PUPPET_DNS}:/tmp/${FILE_NAME_WITH_POSTFIX}

    # override package name without version for dev servers
    if [ ${SERVER} != "prod" ]; then
        FILE_NAME=`echo ${FILE_NAME} | sed 's/-[0-9].*\.zip/.zip/g'`
    fi
    ssh -i ~/.ssh/${SSH_KEY_NAME} -o StrictHostKeyChecking=no ${SSH_USER_NAME}@${PUPPET_DNS} "mv -f /tmp/${FILE_NAME_WITH_POSTFIX} /mnt/u03/puppet/files/servers/${UPLOAD_DESTINATION}/${FILE_NAME}"
}

selectTomcatToUpload() {
   case "$SCOPE" in
      "aio")
         AIO=`ls onpremises-ide-packaging-tomcat-codenvy-allinone/target/onpremises-ide-packaging-tomcat-codenvy-allinone-*.zip`
         EXT_SERVER=`ls onpremises-ide-packaging-tomcat-ext-server/target/onpremises-ide-packaging-tomcat-ext-server-*.tar.gz`
         TERMINAL=`ls onpremises-ide-packaging-zip-terminal/target/onpremises-ide-packaging-zip-terminal-*.tar.gz`
         doUploadTomcat ${AIO} ${SERVER}/${SCOPE}
         doUploadTomcat ${EXT_SERVER} ${SERVER}/${SCOPE}
         doUploadTomcat ${TERMINAL} ${SERVER}/${SCOPE}
      ;;
     "api")
         API=`ls onpremises-ide-packaging-tomcat-api/target/onpremises-ide-packaging-tomcat-api-*.zip`
         doUploadTomcat ${API} ${SERVER}/${SCOPE}
      ;;
     "machine")
         EXT_SERVER=`ls onpremises-ide-packaging-tomcat-ext-server/target/onpremises-ide-packaging-tomcat-ext-server-*.tar.gz`
         TERMINAL=`ls onpremises-ide-packaging-zip-terminal/target/onpremises-ide-packaging-zip-terminal-*.tar.gz`
         doUploadTomcat ${EXT_SERVER} ${SERVER}/${SCOPE}
         doUploadTomcat ${TERMINAL} ${SERVER}/${SCOPE}
      ;;
     "site")
         SITE=`ls onpremises-ide-packaging-tomcat-site/target/onpremises-ide-packaging-tomcat-site-*.zip`
         doUploadTomcat ${SITE} ${SERVER}/${SCOPE}
      ;;
     "all")
         API=`ls onpremises-ide-packaging-tomcat-api/target/onpremises-ide-packaging-tomcat-api-*.zip`
         SITE=`ls onpremises-ide-packaging-tomcat-site/target/onpremises-ide-packaging-tomcat-site-*.zip`
         EXT_SERVER=`ls onpremises-ide-packaging-tomcat-ext-server/target/onpremises-ide-packaging-tomcat-ext-server-*.tar.gz`
         TERMINAL=`ls onpremises-ide-packaging-zip-terminal/target/onpremises-ide-packaging-zip-terminal-*.tar.gz`
         doUploadTomcat ${API} ${SERVER}/api
         doUploadTomcat ${SITE} ${SERVER}/site
         doUploadTomcat ${EXT_SERVER} ${SERVER}/machine
         doUploadTomcat ${TERMINAL} ${SERVER}/machine
      ;;
   esac
}

doBuild() {
    if [ "${NO_BUILD}" == false ]; then
        if [ "${TOMCATS_ONLY}" == true ]; then
            if [ "${SCOPE}" == "aio" ]; then
                cd onpremises-ide-packaging-tomcat-codenvy-allinone
                mvn clean install ${MAVEN_PARAMS}
                cd ..
                cd onpremises-ide-packaging-tomcat-ext-server
                mvn clean install ${MAVEN_PARAMS}
                cd ..
            else
                dirlist=(`ls | grep packaging-tomcat`)
                for i in ${dirlist[@]}; do
                    cd ${i}
                    mvn clean install ${MAVEN_PARAMS}
                    cd ..
                done
            fi
        else
            mvn clean install ${MAVEN_PARAMS}
        fi
    fi
}

PUPPET_DNS=puppet.codenvycorp.com
DATE=`date '+%y%m%d-%H%M%S'`
CLOUD_IDE_HOME=`pwd`
SCOPE_HELP="\033[31mNeed to select target to upload as first argument.\npossible values: aio, api, site, machine, all\e[0m"
SERVER_HELP="\033[31mNeed to select server where to upload as second argument.\npossible values: a9, a10, a11, nightly4, machines\e[0m"
#checking possible scope values
case "$1" in
   "prod" | "a9" | "a10" | "a11" | "nightly4" | "machines" )
      echo "Selected '$1' as cloud for update"
      SERVER=$1
   ;;
   *)
      echo -e ${SERVER_HELP}
      exit 1
   ;;
esac

case "${SERVER}" in
    "a9" | "a10" | "a11" )
      echo "Selected 'aio' as scope for update"
      SCOPE='aio'
      shift
   ;;
   *)
      case "$2" in
         "site" | "api" | "all" | "aio" | "machine")
            if [[ "$2" != "aio" || "$SERVER" == "prod" ]]; then
                echo "Selected '$2' as scope for update"
                SCOPE=$2
                shift
                shift
            else
                echo "aio scope is not supported for $1 server"
                exit 1
            fi
         ;;
         *)
            echo  -e ${SCOPE_HELP}
            exit 1
         ;;
      esac
      ;;
esac

NO_BUILD=false
TOMCATS_ONLY=false
MAVEN_PARAMS=""

for i in "$@"
do
  case "$i" in
    --notests | --t)
      MAVEN_PARAMS=${MAVEN_PARAMS}"  -DskipTests=true"
      ;;
    --nobuild | --b)
      NO_BUILD=true
      ;;
    --U)
      MAVEN_PARAMS=${MAVEN_PARAMS}"  --U"
      ;;
    --tomcatsonly | --to)
      TOMCATS_ONLY=true
      ;;
    -D*)
      MAVEN_PARAMS="${MAVEN_PARAMS} $i"
      ;;
    *)
      echo "Unknown parameter $i."
      exit 1
      ;;
  esac
done

# Any subsequent commands which fail will cause the shell script to exit immediately
set -e

doBuild

detect_user_for_upload

selectTomcatToUpload

echo '================================================'
echo '                Uploading completed.'
echo '================================================'
