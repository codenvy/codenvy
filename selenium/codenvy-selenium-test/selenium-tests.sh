#!/bin/bash
#
# Copyright (c) 2012-2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - initial API and implementation
#
export CUR_DIR=$(cd "$(dirname "$0")"; pwd)
export BASE_ACTUAL_RESULTS_URL="https://ci.codenvycorp.com/view/qa/job/nightly-onprem-selenium-tests/"
export API_SUFFIX="/api/"
export CALLER=$(basename $0)

defineProfileConfiguration() {
    for var in "$@"; do
        if [[ "$var" =~ -P.* ]]; then
            PROFILE=$(echo "$var" | sed -e "s/-P//g")

            if [[ ${PROFILE} == "nightly" ]]; then
                PROFILE_ARGS="--https --host=nightly-onprem.codenvy-stg.com"

            elif [[ ${PROFILE} == "a1" ]]; then
                PROFILE_ARGS="--https --host=a1.codenvy-stg.com"

            elif [[ ${PROFILE} == "a2" ]]; then
                PROFILE_ARGS="--https --host=a2.codenvy-stg.com"

            elif [[ ${PROFILE} == "a3" ]]; then
                PROFILE_ARGS="--https --host=a3.codenvy-stg.com"

            elif [[ ${PROFILE} == "a4" ]]; then
                PROFILE_ARGS="--https --host=a4.codenvy-stg.com"

            elif [[ ${PROFILE} == "a5" ]]; then
                PROFILE_ARGS="--https --host=a5.codenvy-stg.com"

            else
                echo "[TEST] Unrecognized profile "${PROFILE}
                echo "[TEST] Available profiles: -P[nightly|a1|a2|a3|a4|a5]"
                exit 1
            fi

            return
        fi
    done
}

defineProfileConfiguration $@

cd ${CUR_DIR}

TESTS_SCOPE="--suite=CodenvyOnpremSuite.xml"
CLEAN_TARGET_COMMAND="clean"
for var in "$@"; do
    if [[ "$var" =~ --test=.* ]] || [[ "$var" =~ --suite=.* ]]; then
        TESTS_SCOPE=
        break
    fi

    if [[ "$var" == "--compare-with-ci" ]] \
        || [[ "$var" == "--failed-tests" ]] \
        || [[ "$var" == "--regression-tests" ]]; then
        TESTS_SCOPE=
        CLEAN_TARGET_COMMAND=
        break
    fi
done

mvn $CLEAN_TARGET_COMMAND dependency:unpack-dependencies \
    -DincludeArtifactIds=che-selenium-core \
    -DincludeGroupIds=org.eclipse.che.selenium \
    -Dmdep.unpack.includes=webdriver.sh \
    -DoutputDirectory=${CUR_DIR}/target/bin
chmod +x ${CUR_DIR}/target/bin/webdriver.sh

(target/bin/webdriver.sh "$TESTS_SCOPE" $@ "$PROFILE_ARGS")
