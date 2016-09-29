#!/bin/bash

set -e  # tells bash that it should exit the script if any statement returns value > 0

dollar_symbol='$'

CURRENT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
LOG_FILE="$CODENVY_IM_BASE/migration.log"
rm -f $LOG_FILE

# merge docker registry properties
echo "Merge docker AWS registry properties into the ${dollar_symbol}docker_registry_aws_ecr_credentials" >> $LOG_FILE
if [ "${OLD_docker_registry_aws_id}" != "" ] && [ "${OLD_docker_registry_aws_region}" != "" ] && [ "${OLD_docker_registry_aws_access_key_id}" != "" ] && [ "${OLD_docker_registry_aws_secret_access_key}" != "" ]; then
    sudo sed -i "s/^  ${dollar_symbol}docker_registry_aws_ecr_credentials.*=.*/  ${dollar_symbol}docker_registry_aws_ecr_credentials = \"registry1.id=${OLD_docker_registry_aws_id}\nregistry1.region=${OLD_docker_registry_aws_region}\nregistry1.access_key_id=${OLD_docker_registry_aws_access_key_id}\nregistry1.secret_access_key=${OLD_docker_registry_aws_secret_access_key}\"/" "${PATH_TO_MANIFEST}" &>> $LOG_FILE
fi

# migrate admin credentials
echo >> $LOG_FILE
echo "Migrate admin credentials..." >> $LOG_FILE

sudo sed -i "s/^  ${dollar_symbol}codenvy_admin_name.*=.*/  ${dollar_symbol}codenvy_admin_name = \"${OLD_admin_ldap_user_name}\"/" "${PATH_TO_MANIFEST}" &>> $LOG_FILE
sudo sed -i "s/^  ${dollar_symbol}codenvy_admin_initial_password.*=.*/  ${dollar_symbol}codenvy_admin_initial_password = \"${OLD_admin_ldap_password}\"/" "${PATH_TO_MANIFEST}" &>> $LOG_FILE
sudo sed -i "s/^  ${dollar_symbol}codenvy_admin_email.*=.*/  ${dollar_symbol}codenvy_admin_email = \"${OLD_admin_ldap_mail}\"/" "${PATH_TO_MANIFEST}" &>> $LOG_FILE

# JPA integration migration
echo >> $LOG_FILE
echo "Start JPA integration migration..." >> $LOG_FILE

MIGRATION_CONF="${CURRENT_DIR}/configuration.properties"

updateMigrationConfig() {
    sed -i "s/${dollar_symbol}{$1}/$2/g" "$MIGRATION_CONF" &>> $LOG_FILE
}

updateMigrationConfig ldap_connect_pool "${OLD_ldap_connect_pool}"
updateMigrationConfig ldap_connect_pool_initsize "${OLD_ldap_connect_pool_initsize}"
updateMigrationConfig ldap_connect_pool_maxsize "${OLD_ldap_connect_pool_maxsize}"
updateMigrationConfig ldap_connect_pool_prefsize "${OLD_ldap_connect_pool_prefsize}"
updateMigrationConfig ldap_connect_pool_timeout "${OLD_ldap_connect_pool_timeout}"
updateMigrationConfig ldap_protocol "${OLD_ldap_protocol}"
updateMigrationConfig ldap_host "${OLD_ldap_host}"
updateMigrationConfig ldap_port "${OLD_ldap_port}"
updateMigrationConfig java_naming_security_authentication "${OLD_java_naming_security_authentication}"
updateMigrationConfig java_naming_security_principal "${OLD_java_naming_security_principal}"
updateMigrationConfig user_ldap_password "${OLD_user_ldap_password}"
updateMigrationConfig user_ldap_user_container_dn "${OLD_user_ldap_user_container_dn}"
updateMigrationConfig user_ldap_object_classes "${OLD_user_ldap_object_classes}"
updateMigrationConfig user_ldap_attr_aliases "${OLD_user_ldap_attr_aliases}"
updateMigrationConfig user_ldap_attr_email "${OLD_user_ldap_attr_email}"
updateMigrationConfig user_ldap_attr_name "${OLD_user_ldap_attr_name}"
updateMigrationConfig user_ldap_attr_id "${OLD_user_ldap_attr_id}"
updateMigrationConfig user_ldap_users_ou "${OLD_user_ldap_users_ou}"
updateMigrationConfig user_ldap_dn "${OLD_user_ldap_dn}"
updateMigrationConfig profile_ldap_attr_id "${OLD_profile_ldap_attr_id}"
updateMigrationConfig user_ldap_object_classes "${OLD_user_ldap_object_classes}"
updateMigrationConfig profile_ldap_allowed_attributes "${OLD_profile_ldap_allowed_attributes}"

updateMigrationConfig mongo_orgservice_db_name "${OLD_mongo_orgservice_db_name}"
updateMigrationConfig mongo_orgservice_user_name "${OLD_mongo_orgservice_user_name}"
updateMigrationConfig mongo_orgservice_user_pwd "${OLD_mongo_orgservice_user_pwd}"
updateMigrationConfig mongo_user_pass "${OLD_mongo_user_pass}"

updateMigrationConfig pgsql_port "${OLD_pgsql_port}"
updateMigrationConfig pgsql_database_name "${OLD_pgsql_database_name}"
updateMigrationConfig pgsql_username "${OLD_pgsql_username}"
updateMigrationConfig pgsql_pass "${OLD_pgsql_pass}"

updateMigrationConfig admin_ldap_user_name "${OLD_admin_ldap_user_name}"
updateMigrationConfig admin_ldap_password "${OLD_admin_ldap_password}"
updateMigrationConfig admin_ldap_mail "${OLD_admin_ldap_mail}"

# https://github.com/codenvy/deployment/tree/master/automation/jpa-migration-tool
${CODENVY_IM_BASE}/jre/bin/java -jar ${CURRENT_DIR}/jpa-migration-tool.jar migrate -config-file $MIGRATION_CONF &>> $LOG_FILE
