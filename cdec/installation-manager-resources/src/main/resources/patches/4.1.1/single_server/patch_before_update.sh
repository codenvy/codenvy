#!/bin/bash

#### fix manifest
dollar_symbol='$'

createFileBackup() {
    if [[ -n $1 ]]; then
        local currentTimeInMillis=$(($(date +%s%N)/1000000))
        sudo cp -f $1 $1.$currentTimeInMillis
    fi
}

createFileBackup "$PATH_TO_MANIFEST"

if [[ -n "$user_ldap_dn_value" ]]; then
    sudo sed -i "s/${dollar_symbol}java_naming_security_principal.*=.*\"cn=Admin,${dollar_symbol}user_ldap_dn\"/${dollar_symbol}java_naming_security_principal = \"cn=Admin,$user_ldap_dn\"/" "$PATH_TO_MANIFEST" &> update_manifest.log
fi

#### fix mongoDB
CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

mongo -u$mongo_admin_user_name -p$mongo_admin_pass --authenticationDatabase admin "${CURRENT_DIR}/update_mongo.js" &> update_mongo.log
