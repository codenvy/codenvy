node default {
######################################################################################################
# CODENVY SYSTEM
#
# Fundamental parameters that affect the initial system operation.
#
# CODENVY_HOST
#    The IP address or DNS name of where the Codenvy endpoint will service your users.
#    If you are running this on a local system, we auto-detect this value as the IP
#    address of your Docker daemon. On many systems, especially those from cloud hosters
#    like DigitalOcean, you must explicitly set this to the external IP address or
#    DNS entry provided by the provider.
  $host_url = getValue("CODENVY_HOST","codenvy.onprem")

###############################
# Docker IP adress on host
  $docker_ip = getValue("CODENVY_DOCKER_IP","172.17.0.1")

###############################
# swarm nodes management
# coma separated list of <IP>:<PORT>
  $swarm_nodes = getValue("CODENVY_SWARM_NODES","172.17.0.1:2375")
  $swarm_log_level = getValue("CODENVY_SWARM_LOG_LEVEL","info")

###############################
#
# Environment definition, please set this to "production" for production use
  $env = getValue("CHE_ENVIRONMENT","production")

##############################
# default admin credentials
#
  $codenvy_admin_name = getValue("CODENVY_ADMIN_NAME","admin")
  $codenvy_admin_initial_password = getValue("CODENVY_ADMIN_INITIAL_PASSWORD","password")
  $codenvy_admin_email = getValue("CODENVY_ADMIN_EMAIL","admin@codenvy.onprem")

##############################
# http / https configuration
#
  $host_protocol = getValue("CODENVY_HOST_PROTOCOL","http")
# Values below will be used only if $host_protocol = "https" is used
#
# Path to ssl cert
# NOTE: cert should be installed manually
  $path_to_haproxy_ssl_certificate = getValue("CODENVY_PATH_TO_HAPROXY_SSL_CERTIFICATE","/etc/codenvy_dev_cert.pem")
# haproxy additional ssl config
  $haproxy_https_config = getValue("CODENVY_HAPROXY_HTTPS_CONFIG","no-sslv3 no-tls-tickets ciphers ALL:-ADH:+HIGH:+MEDIUM:-LOW:-SSLv2:-EXP:!RC4:!AECDH")

################################
# LDAP integration
#
# auth handler, to enable integration with LDAP set this value to "ldap"
# example: $auth_handler_default = "ldap"
  $auth_handler_default = getValue("CODENVY_AUTH_HANDLER_DEFAULT","org")
#
# LDAP auth settings
  $ldap_url = getValue("CODENVY_LDAP_URL","")
  $ldap_base_dn = getValue("CODENVY_LDAP_BASE_DN","")
  $ldap_auth_user_filter = getValue("CODENVY_LDAP_AUTH_USER_FILTER","")
  $ldap_auth_authentication_type  = getValue("CODENVY_LDAP_AUTH_AUTHENTICATION_TYPE","AD")
  $ldap_auth_dn_format = getValue("CODENVY_LDAP_AUTH_DN_FORMAT","")
  $ldap_auth_user_password_attribute = getValue("CODENVY_LDAP_AUTH_USER_PASSWORD_ATTRIBUTE","NULL")
  $ldap_auth_allow_multiple_dns = getValue("CODENVY_LDAP_AUTH_ALLOW_MULTIPLE_DNS","false")
  $ldap_auth_subtree_search = getValue("CODENVY_LDAP_AUTH_SUBTREE_SEARCH","true")
# LDAP connection settings
  $ldap_connection_provider= getValue("CODENVY_LDAP_CONNECTION_PROVIDER","NULL")
  $ldap_connection_bind_dn = getValue("CODENVY_LDAP_CONNECTION_BIND_DN","")
  $ldap_connection_bind_password = getValue("CODENVY_LDAP_CONNECTION_BIND_PASSWORD","")
  $ldap_connection_use_ssl = getValue("CODENVY_LDAP_CONNECTION_USE_SSL","false")
  $ldap_connection_use_start_tls = getValue("CODENVY_LDAP_CONNECTION_USE_START_TLS","false")
  $ldap_connection_pool_min_size = getValue("CODENVY_LDAP_CONNECTION_POOL_MIN_SIZE","3")
  $ldap_connection_pool_max_size = getValue("CODENVY_LDAP_CONNECTION_POOL_MAX_SIZE","10")
  $ldap_connection_pool_validate_on_checkout = getValue("CODENVY_LDAP_CONNECTION_POOL_VALIDATE_ON_CHECKOUT","false")
  $ldap_connection_pool_validate_on_checkin = getValue("CODENVY_LDAP_CONNECTION_POOL_VALIDATE_ON_CHECKIN","false")
  $ldap_connection_pool_validate_period_ms = getValue("CODENVY_LDAP_CONNECTION_POOL_VALIDATE_PERIOD_MS","180000")
  $ldap_connection_pool_validate_periodically = getValue("CODENVY_LDAP_CONNECTION_POOL_VALIDATE_PERIODICALLY","true")
  $ldap_connection_pool_fail_fast = getValue("CODENVY_LDAP_CONNECTION_POOL_FAIL_FAST","true")
  $ldap_connection_pool_idle_ms = getValue("CODENVY_LDAP_CONNECTION_POOL_IDLE_MS","5000")
  $ldap_connection_pool_prune_ms = getValue("CODENVY_LDAP_CONNECTION_POOL_PRUNE_MS","10000")
  $ldap_connection_pool_block_wait_ms = getValue("CODENVY_LDAP_CONNECTION_POOL_BLOCK_WAIT_MS","30000")
  $ldap_connection_connect_timeout_ms = getValue("CODENVY_LDAP_CONNECTION_CONNECT_TIMEOUT_MS","30000")
  $ldap_connection_response_timeout_ms = getValue("CODENVY_LDAP_CONNECTION_RESPONSE_TIMEOUT_MS","120000")
  $ldap_connection_ssl_trust_certificates = getValue("CODENVY_LDAP_CONNECTION_SSL_TRUST_CERTIFICATES","NULL")
  $ldap_connection_ssl_keystore_name = getValue("CODENVY_LDAP_CONNECTION_SSL_KEYSTORE_NAME","NULL")
  $ldap_connection_ssl_keystore_password = getValue("CODENVY_LDAP_CONNECTION_SSL_KEYSTORE_PASSWORD","NULL")
  $ldap_connection_ssl_keystore_type = getValue("CODENVY_LDAP_CONNECTION_SSL_KEYSTORE_TYPE","NULL")
  $ldap_connection_sasl_realm = getValue("CODENVY_LDAP_CONNECTION_SASL_REALM","NULL")
  $ldap_connection_sasl_mechanism = getValue("CODENVY_LDAP_CONNECTION_SASL_MECHANISM","NULL")
  $ldap_connection_sasl_authorization_id = getValue("CODENVY_LDAP_CONNECTION_SASL_AUTHORIZATION_ID","NULL")
  $ldap_connection_sasl_security_strength = getValue("CODENVY_LDAP_CONNECTION_SASL_SECURITY_STRENGTH","NULL")
  $ldap_connection_sasl_mutual_auth = getValue("CODENVY_LDAP_CONNECTION_SASL_MUTUAL_AUTH","false")
  $ldap_connection_sasl_quality_of_protection = getValue("CODENVY_LDAP_CONNECTION_SASL_QUALITY_OF_PROTECTION","NULL")
# LDAP Synchronization settings
  $ldap_sync_initial_delay_ms = getValue("CODENVY_LDAP_SYNC_INITIAL_DELAY_MS","10000")
  $ldap_sync_period_ms = getValue("CODENVY_LDAP_SYNC_PERIOD_MS","-1")
  $ldap_sync_page_size = getValue("CODENVY_LDAP_SYNC_PAGE_SIZE","1000")
  $ldap_sync_page_read_timeout_ms = getValue("CODENVY_LDAP_SYNC_PAGE_READ_TIMEOUT_MS","30000")
  $ldap_sync_user_additional_dn = getValue("CODENVY_LDAP_SYNC_USER_ADDITIONAL_DN","NULL")
  $ldap_sync_user_filter = getValue("CODENVY_LDAP_SYNC_USER_FILTER","")
  $ldap_sync_user_attr_email = getValue("CODENVY_LDAP_SYNC_USER_ATTR_EMAIL","cn")
  $ldap_sync_user_attr_id = getValue("CODENVY_LDAP_SYNC_USER_ATTR_ID","objectGUID")
  $ldap_sync_user_attr_name = getValue("CODENVY_LDAP_SYNC_USER_ATTR_NAME","cn")
  $ldap_sync_profile_attrs = getValue("CODENVY_LDAP_SYNC_PROFILE_ATTRS","")
  $ldap_sync_group_additional_dn = getValue("CODENVY_LDAP_SYNC_GROUP_ADDITIONAL_DN","NULL")
  $ldap_sync_group_filter = getValue("CODENVY_LDAP_SYNC_GROUP_FILTER","NULL")
  $ldap_sync_group_attr_members = getValue("CODENVY_LDAP_SYNC_GROUP_ATTR_MEMBERS","NULL")

################################
# Mail server configuration
#
  $mail_host = getValue("CODENVY_MAIL_HOST","smtp.example.com")
  $mail_host_port = getValue("CODENVY_MAIL_HOST_PORT","465")
  $mail_use_ssl = getValue("CODENVY_MAIL_USE_SSL","true")
  $mail_transport_protocol = getValue("CODENVY_MAIL_TRANSPORT_PROTOCOL","smtp")
  $mail_smtp_auth = getValue("CODENVY_MAIL_SMTP_AUTH","true")
  $mail_smtp_socketFactory_class = getValue("CODENVY_MAIL_SMTP_SOCKETFACTORY_CLASS","javax.net.ssl.SSLSocketFactory")
  $mail_smtp_socketFactory_fallback = getValue("CODENVY_MAIL_SMTP_SOCKETFACTORY_FALLBACK","false")
  $mail_smtp_socketFactory_port = getValue("CODENVY_MAIL_SMTP_SOCKETFACTORY_PORT","465")
  $mail_smtp_auth_username = getValue("CODENVY_MAIL_SMTP_AUTH_USERNAME","smtp_username")
  $mail_smtp_auth_password = getValue("CODENVY_MAIL_SMTP_AUTH_PASSWORD","smtp_password")

################################
# Email properties
#
  $workspace_email_invite_subject = getValue("CODENVY_WORKSPACE_EMAIL_INVITE_SUBJECT","Join My Workspace")
  $organization_email_invite_subject = getValue("CODENVY_ORGANIZATION_EMAIL_INVITE_SUBJECT","Join My Organization")
  $account_verify_mail_subject = getValue("CODENVY_ACCOUNT_VERIFY_MAIL_SUBJECT","Verify Your Codenvy Account")
  $account_password_recovery_mail_subject = getValue("CODENVY_ACCOUNT_PASSWORD_RECOVERY_MAIL_SUBJECT","Codenvy Password Recovery")
  $account_created_byadmin_mail_subject = getValue("CODENVY_ACCOUNT_CREATED_BYADMIN_MAIL_SUBJECT","Welcome To Codenvy")
  $account_created_byuser_mail_subject = getValue("CODENVY_ACCOUNT_CREATED_BYUSER_MAIL_SUBJECT","Welcome To Codenvy")

################################
# Error reports
# Logback reports configuration
#
# email adress to send report
  $email_to = getValue("CODENVY_EMAIL_TO","admin@example.com")
  $email_from = getValue("CODENVY_EMAIL_FROM","noreply@codenvy.onprem")
  $email_subject = getValue("CODENVY_EMAIL_SUBJECT","Codenvy codenvy.onprem error: %logger{20} - %m")
  $codenvy_logback_smtp_host = getValue("CODENVY_LOGBACK_SMTP_HOST","localhost")
  $codenvy_logback_smtp_port = getValue("CODENVY_LOGBACK_SMTP_PORT","25")

# Configuration of organization email notifications
#
  $organization_email_member_added_subject=getValue("CODENVY_ORGANIZATION_EMAIL_MEMBER_ADDED_SUBJECT","You’ve been added to a Codenvy Organization")
  $organization_email_member_removed_subject=getValue("CODENVY_ORGANIZATION_EMAIL_MEMBER_REMOVED_SUBJECT","You’ve been removed from a Codenvy Organization")
  $organization_email_renamed_subject=getValue("CODENVY_ORGANIZATION_EMAIL_RENAMED_SUBJECT","Codenvy Organization renamed")
  $organization_email_removed_subject=getValue("CODENVY_ORGANIZATION_EMAIL_REMOVED_SUBJECT","Codenvy Organization deleted")


###############################
# PGSQL Server, used as back-end for billing
#
# (Mandatory) replace placeholder with some password
  $pgsql_pass = getValue("CODENVY_PGSQL_PASS","codenvy")
#
  $pgsql_username = getValue("CODENVY_PGSQL_USERNAME","pgcodenvy")
  $pgsql_database_name = getValue("CODENVY_PGSQL_DATABASE_NAME","dbcodenvy")
  $pgsql_listen_addresses = getValue("CODENVY_PGSQL_LISTEN_ADDRESSES","*")
  $pgsql_port = getValue("CODENVY_PGSQL_PORT","5432")
  $pgsql_max_connections = getValue("CODENVY_PGSQL_MAX_CONNECTIONS","200")
  $pgsql_shared_buffers = getValue("CODENVY_PGSQL_SHARED_BUFFERS","256MB")
  $pgsql_work_mem = getValue("CODENVY_PGSQL_WORK_MEM","6553kB")
  $pgsql_maintenance_work_mem = getValue("CODENVY_PGSQL_MAINTENANCE_WORK_MEM","64MB")
  $pgsql_wal_buffers = getValue("CODENVY_PGSQL_WAL_BUFFERS","7864kB")
  $pgsql_checkpoint_segments = getValue("CODENVY_PGSQL_CHECKPOINT_SEGMENTS","32")
  $pgsql_checkpoint_completion_target = getValue("CODENVY_PGSQL_CHECKPOINT_COMPLETION_TARGET","0.9")
  $pgsql_effective_cache_size = getValue("CODENVY_PGSQL_EFFECTIVE_CACHE_SIZE","768MB")
  $pgsql_default_statistics_target = getValue("CODENVY_PGSQL_DEFAULT_STATISTICS_TARGET","100")
  $pgsql_log_min_duration_statement = getValue("CODENVY_PGSQL_LOG_MIN_DURATION_STATEMENT","2000")

###############################
# JMX credentials
#
# (Mandatory) replace placeholders with some username and password
  $jmx_username = getValue("CODENVY_JMX_USERNAME","admin")
  $jmx_password = getValue("CODENVY_JMX_PASSWORD","codenvy")

###############################
# XMX JAVA_OPTS
#
# (Optional) enter custom xmx value, default value is 1g
  $codenvy_server_xmx = getValue("CODENVY_SERVER_XMX","2048")

###############################
# Auth configurations
#
  $auth_sso_access_ticket_lifetime_seconds = getValue("CODENVY_AUTH_SSO_ACCESS_TICKET_LIFETIME","259200")
  $auth_sso_bearer_ticket_lifetime_seconds = getValue("CODENVY_AUTH_SSO_BEARER_TICKET_LIFETIME","3600")

###############################
# oAuth configurations
#
# (Optional) enter your oAuth client and secrets for integration with google, github, bitbucket and wso2.
# Please note that oAuth integration is optional, if you don't want to use oAuth leave this as it is.
# But it will affect on some functionality that depends on oAuth services like github integration.
#
# Google. Optional, but it can be used to log in / register an account
  $google_client_id = getValue("CODENVY_GOOGLE_CLIENT_ID","NULL")
  $google_secret = getValue("CODENVY_GOOGLE_SECRET","NULL")
# Github. Optional, but it can be used to log in / register an account
  $github_client_id = getValue("CODENVY_GITHUB_CLIENT_ID","NULL")
  $github_secret = getValue("CODENVY_GITHUB_SECRET","NULL")
# BitBucket. Leave is as is, unless you need to use BitBucket oAuth.
  $bitbucket_client_id = getValue("CODENVY_BITBUCKET_CLIENT_ID","NULL")
  $bitbucket_secret = getValue("CODENVY_BITBUCKET_SECRET","NULL")
  $bitbucket_endpoint = getValue("CODENVY_BITBUCKET_ENDPOINT","https://bitbucket.org")
  $bitbucket_consumer_key = getValue("CODENVY_BITBUCKET_CONSUMER_KEY","")
  $bitbucket_private_key = getValue("CODENVY_BITBUCKET_PRIVATE_KEY","")
# WSO2. Leave is as is, unless you need to use WSO2 oAuth. Visit - https://cloud.wso2.com/
  $wso2_client_id = getValue("CODENVY_WSO2_CLIENT_ID","NULL")
  $wso2_secret = getValue("CODENVY_WSO2_SECRET","NULL")
# ProjectLocker. Leave it as is, unless you need oAuth with ProjectLocker. Visit - http://projectlocker.com/
  $projectlocker_client_id = getValue("CODENVY_PROJECTLOCKER_CLIENT_ID","NULL")
  $projectlocker_secret = getValue("CODENVY_PROJECTLOCKER_SECRET","NULL")
# Microsoft
  $microsoft_client_id = getValue("CODENVY_MICROSOFT_CLIENT_ID","NULL")
  $microsoft_secret = getValue("CODENVY_MICROSOFT_SECRET","NULL")
# RedHat
  $redhat_client_id = getValue("CODENVY_REDHAT_CLIENT_ID","NULL")
  $redhat_secret = getValue("CODENVY_REDHAT_SECRET","NULL")
# LinkedIn
  $linkedin_client_id = getValue("CODENVY_LINKEDIN_CLIENT_ID","NULL")
  $linkedin_secret = getValue("CODENVY_LINKEDIN_SECRET","NULL")
  
# Integration
  $integration_factory_owner_username = getValue("CODENVY_INTEGRATION_FACTORY_OWNER_USERNAME","")
  $integration_factory_owner_password = getValue("CODENVY_INTEGRATION_FACTORY_OWNER_PASSWORD","")

###############################
# Codenvy Workspace configurations
#
# Allow users self registration, if false only admin will be allowed to create new users.
  $user_self_creation_allowed = getValue("CODENVY_USER_SELF_CREATION_ALLOWED","true")
# Limits
  $limits_user_workspaces_count = getValue("CODENVY_LIMITS_USER_WORKSPACES_COUNT","30")
  $limits_user_workspaces_run_count = getValue("CODENVY_LIMITS_USER_WORKSPACES_RUN_COUNT","10")
  $limits_user_workspaces_ram = getValue("CODENVY_LIMITS_USER_WORKSPACES_RAM","100gb")
  $limits_organization_workspaces_ram = getValue("CODENVY_LIMITS_ORGANIZATION_WORKSPACES_RAM","100gb")
  $limits_organization_workspaces_count = getValue("CODENVY_LIMITS_ORGANIZATION_WORKSPACES_COUNT","30")
  $limits_organization_workspaces_run_count = getValue("CODENVY_LIMITS_ORGANIZATION_WORKSPACES_RUN_COUNT","10")
  $limits_workspace_idle_timeout = getValue("CODENVY_MACHINE_WS_AGENT_INACTIVE_STOP_TIMEOUT_MS","14400000")
  $limits_workspace_env_ram = getValue("CODENVY_LIMITS_WORKSPACE_ENV_RAM","16gb")
# workspace snapshots
  $docker_registry_for_workspace_snapshots = getValue("CODENVY_DOCKER_REGISTRY_FOR_WORKSPACE_SNAPSHOTS","$host_url:5000")
  $docker_registry_namespace = getValue("CODENVY_DOCKER_REGISTRY_NAMESPACE","NULL")
  $workspace_auto_snapshot = getValue("CODENVY_WORKSPACE_AUTO_SNAPSHOT","false")
  $workspace_auto_restore = getValue("CODENVY_WORKSPACE_AUTO_RESTORE","false")
  $workspace_auto_start = getValue("CODENVY_WORKSPACE_AUTO_START","true")
  $workspace_java_options= getValue("CHE_WORKSPACE_JAVA_OPTIONS", "-Xms256m -Xmx2048m -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom")
  $workspace_maven_options = getValue("CHE_WORKSPACE_MAVEN_OPTIONS", "NULL")

###############################
# Codenvy machine configurations
#
  $machine_extra_hosts = getValue("CODENVY_MACHINE_EXTRA_HOSTS","NULL")
  $machine_default_mem_size_mb = getValue("CODENVY_MACHINE_DEFAULT_MEM_SIZE_MB","1024")
  $machine_ws_agent_max_start_time_ms = getValue("CODENVY_MACHINE_WS_AGENT_MAX_START_TIME_MS","300000")
  $machine_ws_agent_run_command = getValue("CODENVY_MACHINE_WS_AGENT_RUN_COMMAND","~/che/ws-agent/bin/catalina.sh run")
# Docker privilege mode, default false
  $machine_docker_privileged = getValue("CODENVY_MACHINE_DOCKER_PRIVILEGED","false")
# Allows to adjust machine swap memory by multiplication current machnine memory on provided value.
# default is 0 which means disabled swap, if set multiplier value equal to 0.5 machine swap will be
# configured with size that equal to half of current machine memory.
  $machine_docker_memory_swap_multiplier = getValue("CODENVY_MACHINE_DOCKER_MEMORY_SWAP_MULTIPLIER","0")
# Semicolon separated extra volumes to mount in the machine.
# example: $machine_server_extra_volume = "/path/to/source1:/path/to/destination1:ro,Z;/path/to/source2:/path/to/destination2:ro,Z;"
  $machine_server_extra_volume = getValue("CODENVY_MACHINE_SERVER_EXTRA_VOLUME","")
# Docker network driver for machines.
  $che_machine_docker_network_driver = getValue("CODENVY_MACHINE_DOCKER_NETWORK_DRIVER","bridge")
# Next 2 values set limits on CPU consumption by containers of started workspaces.
# Period sets amount of units per CPU core.
# Quota sets amount of units available for container per whole CPU.
# Max value of quota could be period * number of CPU cores in a system.
# Example:
# period = 5000
# quota = 10000
  $machine_docker_cpu_period = getValue("CODENVY_DOCKER_CPU_PERIOD","0")
  $machine_docker_cpu_quota = getValue("CODENVY_DOCKER_CPU_QUOTA","0")
# Sets set of CPUs that can be used by each container of started workspace.
# Example:
# 0-3
# 1,4
$machine_docker_cpuset_cpus = getValue("CODENVY_DOCKER_CPUSET_CPUS","NULL")
# Sets parent cgroup for cgroups of containers created by workspaces.
# This allows an admin to set custom cgroup limitations to all containers of workspaces by configuring cgroups.
# Example:
# /my_group
# my_another_group
$machine_docker_parent_cgroup = getValue("CODENVY_DOCKER_PARENT_CGROUP","NULL")

###############################
# Http proxy configuration
# leave those fields empty if no configuration needed
#
# http proxy for codenvy
  $http_proxy_for_codenvy = getValue("CODENVY_HTTP_PROXY_FOR_CODENVY","")
  $https_proxy_for_codenvy = getValue("CODENVY_HTTPS_PROXY_FOR_CODENVY","")
# provide dns which proxy should not be used for.
# please leave this empty if you don't need no_proxy configuration
  $no_proxy_for_codenvy = getValue("CODENVY_NO_PROXY_FOR_CODENVY","")
#
# http proxy for codenvy workspaces
  $http_proxy_for_codenvy_workspaces = getValue("CODENVY_HTTP_PROXY_FOR_CODENVY_WORKSPACES","")
  $https_proxy_for_codenvy_workspaces = getValue("CODENVY_HTTPS_PROXY_FOR_CODENVY_WORKSPACES","")
# provide dns which proxy should not be used for.
# please leave this as it is if you don't need no_proxy configuration
  $no_proxy_for_codenvy_workspaces = getValue("CODENVY_NO_PROXY_FOR_CODENVY_WORKSPACES","")

################################
# DNS resolver configuration
  $dns_resolvers = getValue("CODENVY_DNS_RESOLVERS","")

###############################
#
# System super privileged mode
  $system_super_privileged_mode = getValue("CODENVY_SYSTEM_SUPER_PRIVILEGED_MODE","false")

###############################
#
# Blocked countries
  $auth_blocked_country_names = getValue("CODENVY_AUTH_BLOCKED_COUNTRY_NAMES","")


###############################
#
# Codenvy folders on host machine
  $codenvy_folder = getValue("CHE_INSTANCE","/tmp/codenvy")

###############################
# Codenvy developmet mode
# path to codenvy puppet sources for development mode
  $puppet_src_folder = getValue("CHE_CONFIG","/path/to/codenvy/codenvy/puppet/sources")
# path to codenvy tomcat for development mode
  $codenvy_development_tomcat = getValue("PATH_TO_CHE_ASSEMBLY","NULL")
# path to codenvy ws agent for development mode
  $codenvy_development_ws_agent = getValue("PATH_TO_WS_AGENT_ASSEMBLY","NULL")
# codenvy debug port
  $codenvy_debug_port = getValue("CODENVY_DEBUG_PORT","8000")
# codenvy debug suspend
  $codenvy_debug_suspend = getValue("CODENVY_DEBUG_SUSPEND","false")
# use local repository flag on / off
  $codenvy_repo = getValue("CHE_REPO","off")

###############################
# Codenvy system extra hosts
  $codenvy_extra_hosts = getValue("CODENVY_EXTRA_HOSTS","NULL")

###############################
# DB initialization and migration configuration
  $db_schema_flyway_scripts_locations=getValue("DB_SCHEMA_FLYWAY_SCRIPTS_LOCATIONS","classpath:che-schema,classpath:codenvy-schema")

###############################
# Rsync backup/restore configuration
# Throughput of rsync connection for workspace files restoring in KiB. Value may include dot, unit suffix, e.g. 1.5m.
  $rsync_restore_bwlimit=getValue("RSYNC_RESTORE_BWLIMIT","7000")
# Throughput of rsync connection for workspace files backup in KiB.
  $rsync_backup_bwlimit=getValue("RSYNC_BACKUP_BWLIMIT","7000")
# Sets log level of ssh connection used by rsync for workspace syncing. Value may include dot, unit suffix, e.g. 1.5m.
# Default value INFO. Possible values: QUIET, FATAL, ERROR, INFO, VERBOSE, DEBUG1, DEBUG2, and DEBUG3
  $rsync_ssh_log_level=getValue("RSYNC_SSH_LOG_LEVEL","INFO")

###############################
# User mail blacklist
  $user_mail_blacklist = getValue("CODENVY_USER_MAIL_BLACKLIST","")
  $emailvalidator_blacklistfile = getValue("CODENVY_BLACKLISTFILE_PATH","/opt/codenvy-data/conf/user-mail-blacklist.txt")

###############################
# Defines stack loading policy. Allowed values true/false.
  $che_predefined_stacks_reload_on_start = getValue("CHE_PREDEFINED_STACKS_RELOAD_ON_START","false")

###############################
# Include base module
  include base
}
