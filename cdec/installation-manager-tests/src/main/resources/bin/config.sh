#!/bin/bash
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

UPDATE_SERVER="http://updater.nightly4.codenvy-stg.com"
UPDATE_SERVICE="${UPDATE_SERVER}/update"
SAAS_SERVER="http://a1.codenvy-dev.com"
TEST_LOG="installation-manager-test.log"
HOST_URL="codenvy.onprem"
NEW_HOST_URL="test.${HOST_URL}"

AVAILABLE_CODENVY_VERSIONS=$(curl -s -X GET ${UPDATE_SERVICE}/repository/updates/codenvy)

PREV_CODENVY3_VERSION=$(echo ${AVAILABLE_CODENVY_VERSIONS} | sed 's/.*"\(3[^"]*\)","3[^"]*".*\]/\1/')
LATEST_CODENVY3_VERSION=$(echo ${AVAILABLE_CODENVY_VERSIONS} | sed 's/.*"\(3[^"]*\)".*/\1/')

PREV_CODENVY_VERSION=$(echo ${AVAILABLE_CODENVY_VERSIONS} | sed 's/.*"\([4-5][^"]*\)","[4-5][^"]*".*\]/\1/')
LATEST_CODENVY_VERSION=$(echo ${AVAILABLE_CODENVY_VERSIONS} | sed 's/.*"\([4-5][^"]*\)".*/\1/')

LATEST_STABLE_CODENVY_VERSION=$(echo ${AVAILABLE_CODENVY_VERSIONS} | sed 's/.*"\([^"]*[0-9]\)".*\]/\1/')

AVAILABLE_IM_CLI_CLIENT_VERSIONS=$(curl -s -X GET ${UPDATE_SERVICE}/repository/updates/installation-manager-cli)
PREV_IM_CLI_CLIENT_VERSION=$(echo ${AVAILABLE_IM_CLI_CLIENT_VERSIONS} | sed 's/.*"\([^"]*\)","[^"]*"\]/\1/')
LATEST_IM_CLI_CLIENT_VERSION=$(echo ${AVAILABLE_IM_CLI_CLIENT_VERSIONS} | sed 's/.*"\([^"]*\)".*/\1/')

SINGLE_NODE_VAGRANT_FILE="../vagrant/single/CentOS7/Vagrantfile"
SINGLE_NODE_RHEL_VAGRANT_FILE="../vagrant/single/RHEL7/Vagrantfile"
MULTI_CODENVY3_NODE_VAGRANT_FILE="../vagrant/multi-codenvy3/CentOS7/Vagrantfile"
MULTI_CODENVY3_NODE_WITH_ADDITIONAL_NODES_VAGRANT_FILE="../vagrant/multi-codenvy3-with-additional-nodes/CentOS7/Vagrantfile"
SINGLE_WITH_ADDITIONAL_NODES_VAGRANT_FILE="../vagrant/single-with-additional-nodes/CentOS7/Vagrantfile"
SINGLE_RHEL_WITH_ADDITIONAL_NODES_VAGRANT_FILE="../vagrant/single-with-additional-nodes/RHEL7/Vagrantfile"

CODENVY_LICENSE_PUBLIC_KEY=30820122300d06092a864886f70d01010105000382010f00303032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e000441e6665f703ef0d6dfa670a5a83609b8b7e187e3870bcfb969a65323G82010a0282010100b426e684e6ffa90e3a21e5c10a05b763b1fa35451e057d5c2e7e728a390a32c9b5f7448c89a791db8b78fe6f1f997b9b6bba0f1661c6f840178eb4e928b11aebb5347ada52aeb6eed8a34c3af9f8466b3240ed52cf6ec565d229e4d003RSA4204813SHA512withRSAdac0901280df8d78f3aab96da4694201aad99cc17834feafb825bb33a3cd24c0f53ed660e4ec8d57a29aaa0e6c4745c6930272bc66ff69e516d9bea13fbbf3a2e54ce862c11282e41996e811814829b004988f09a6fc4198145eeef60517d71c61d4dafd8cbfc7e4804e6ae516617809d893fe872385ef3763a9f9847362cf4f33b2cf0b243743a98ddc3b951296f62d611d1e590a713392533e3bb59b1a854b9f8f303f0203010001
CODENVY_LICENSE=d9a53efe56c19eaaae2d14be98de42ce76cfeaf8bc63c00c6f40651d08b9c8147840126f517bb72313bb6f8b63b40d288b4e8fb7f94695ab9b6eed931057cdd7169351b7a945eacab48cf341d584a5cace29410ef91e08d544e0b7b3ddeda4a572163c6d5678204453615490795c308dd0cbff96d47d62d23cd08c811f4ab8ef7cdcccc5375df27497daedbc086759d682d940a7162e1e4c980c887137700aa5f5b585bcba0c2f0f082397bdc2e01059accff3f3f65552ca6c9104d58b039015201339fb32798a102c1192757fd6af4a828fffbe547fc9efaec3675418d273d49c2babe8881476200bb05c6da02a4e08e591f064f4b93d32e4a68d9ca0f23e2d1116175514fe47a6be54f595a83d5f3138152101c8cae1653ce8256b5acac2c4ec1ddecd02624243ca2210c87eeb2f8aadc979d5d6b118b5b43cdc01cac0cdb055b8f9e9fa33afc1aede10ba423ba9abeedff8dfde0943c063c7fbd815378ccfb35e4e20363605c2efb3c7e1f21abd47f9ba7f7d15322db8bfba4b70739d56d536e7407aeadfaca0fe9a873962d46383e119807270d1273d033dd251db6c7598fc8c136cedddb3180642d7d4f9a2a6a7b75f44aeb25f5be5cfeb9ddb1d73dfe144fa91ac6235b176d453863204e1c146aced340c49207bbe3c201391165b23fb2b100073e21ab2d6df227016b4b4f4c8f6fedfcbcadc8ff6c14c1188fb93c7448bdc3f0d43644f17caad767c6ac7a60113e82083eff81245473847bcf700ccbcea0aafd76eb2b6676b3a5f6111ec90bef2efc036152e088b4b7ad689279788b55a75efb11b940c13ef62832cdcdad8634e839e7bffcfa15be139108bdd9a241b9c80c709a7164faf67433a3d67b7068bd3f295fdae0e01ec04c35ac23183460effb15c08f04c2bc40ec0b868cb2cfd21b368e1da7d7247df4a08fe745ed1c4ad996d0885a6f406aab66beb13d76c17d5596cf68a81e8caccb64e5a09741727489e9174bc6434bb53bcea4a755b60264cdf671e3168648c88

STORAGE_DIR=/usr/local/codenvy/im/storage
STORAGE_FILE=/usr/local/codenvy/im/storage/config.properties

CODENVY_DATA_DIR=/home/codenvy/codenvy-data

HTTP_PROXY=http://testuser:testpassword@proxy.ua.codenvy-dev.com:3129/
HTTPS_PROXY=http://testuser:testpassword@proxy.ua.codenvy-dev.com:3129/
PROXY_SERVER=$(echo $HTTP_PROXY | sed 's|http://.*@\(.*\):.*|\1|')
PROXY_SERVER_WITHOUT_CREDENTIALS="http://$(echo $HTTP_PROXY | sed 's|http://.*@\(.*\)|\1|')"
PROXY_USERNAME=testuser
PROXY_PASSWORD=testpassword
PROXY_IP=172.19.11.156
HTTP_PASSWORDLESS_PROXY=http://proxy.ua.codenvy-dev.com:3128/
HTTPS_PASSWORDLESS_PROXY=http://proxy.ua.codenvy-dev.com:3128/
HTTPS_PASSWORDLESS_PROXY_WITHOUT_TRAILING_SLASH=${HTTPS_PASSWORDLESS_PROXY%?}  # remove trailing slash of $HTTPS_PASSWORDLESS_PROXY

PATH_TO_CODENVY_PUPPET_MANIFEST="/etc/puppet/manifests/nodes/codenvy/codenvy.pp"

CUSTOM_SINGLE_NODE_LATEST_VERSION_CONFIG_URL="${UPDATE_SERVICE}/repository/public/download/codenvy-single-server-properties/${LATEST_CODENVY_VERSION}"
CUSTOM_SINGLE_NODE_PREV_VERSION_CONFIG_URL="${UPDATE_SERVICE}/repository/public/download/codenvy-single-server-properties/${PREV_CODENVY_VERSION}"
