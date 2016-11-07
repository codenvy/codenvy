node default {
######################################################################################################
# CODENVY SYSTEM 
#
# Fundamental parameters that affect the initial system operation.
#
# CODENVY_VERSION  
#     Sets the version of Codenvy to download and use. We respect this variable
#     during 'codenvy init' and 'codenvy download'. Once a Codenvy instance has been
#     generated, CODENVY_VERSION is set to the value in codenvy.env, in the instance
#     folder and it locks the version for which Codenvy is running. If you set 
#     CODENVY_VERSION to a value that is diferent from what is set in codenvy.ver
#     Codenvy will not start until this conflict is realized. You can upgrade 
#     a Codenvy install from one version to another with 'codenvy upgrade'.
  $codenvy_version = getValue("CODENVY_VERSION","nightly")

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

###############################
#
# Environment definition, please set this to "production" for production use
  $env = getValue("CODENVY_ENVIRONMENT","production")

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
# Error reports
# Logback reports configuration
#
# email adress to send report
  $email_to = getValue("CODENVY_EMAIL_TO","admin@example.com")
  $email_from = getValue("CODENVY_EMAIL_FROM","noreply@codenvy.onprem")
  $email_subject = getValue("CODENVY_EMAIL_SUBJECT","Codenvy codenvy.onprem error: %logger{20} - %m")

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
# oAuth configurations
#
# (Optional) enter your oAuth client and secrets for integration with google, github, bitbucket and wso2.
# Please note that oAuth integration is optional, if you don't want to use oAuth leave this as it is.
# But it will affect on some functionality that depends on oAuth services like github integration.
#
# Google. Optional, but it can be used to log in / register an account
  $google_client_id = getValue("CODENVY_GOOGLE_CLIENT_ID","your_google_client_id")
  $google_secret = getValue("CODENVY_GOOGLE_SECRET","your_google_secret")
# Github. Optional, but it can be used to log in / register an account
  $github_client_id = getValue("CODENVY_GITHUB_CLIENT_ID","423531cf41d6c13e1b3b")
  $github_secret = getValue("CODENVY_GITHUB_SECRET","e708bfc28c541a8f25feac4466c93611d9018a3d")
# BitBucket. Leave is as is, unless you need to use BitBucket oAuth.
  $bitbucket_client_id = getValue("CODENVY_BITBUCKET_CLIENT_ID","your_bitbucket_client_id")
  $bitbucket_secret = getValue("CODENVY_BITBUCKET_SECRET","your_bitbucket_secret")
# WSO2. Leave is as is, unless you need to use WSO2 oAuth. Visit - https://cloud.wso2.com/
  $wso2_client_id = getValue("CODENVY_WSO2_CLIENT_ID","your_wso2_client_id")
  $wso2_secret = getValue("CODENVY_WSO2_SECRET","your_wso2_secret")
# ProjectLocker. Leave it as is, unless you need oAuth with ProjectLocker. Visit - http://projectlocker.com/
  $projectlocker_client_id = getValue("CODENVY_PROJECTLOCKER_CLIENT_ID","your_projectlocker_client_id")
  $projectlocker_secret = getValue("CODENVY_PROJECTLOCKER_SECRET","your_projectlocker_secret")
# Microsoft
  $microsoft_client_id = getValue("CODENVY_MICROSOFT_CLIENT_ID","your_microsoft_client_id")
  $microsoft_secret = getValue("CODENVY_MICROSOFT_SECRET","your_microsoft_secret")

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
  $limits_workspace_env_ram = getValue("CODENVY_LIMITS_WORKSPACE_ENV_RAM","16gb")

###############################
# Codenvy machine configurations
#
  $machine_extra_hosts = getValue("CODENVY_MACHINE_EXTRA_HOSTS","NULL")
  $machine_ws_agent_inactive_stop_timeout_ms = getValue("CODENVY_MACHINE_WS_AGENT_INACTIVE_STOP_TIMEOUT_MS","600000")
  $machine_default_mem_size_mb = getValue("CODENVY_MACHINE_DEFAULT_MEM_SIZE_MB","1024")
  $machine_ws_agent_max_start_time_ms = getValue("CODENVY_MACHINE_WS_AGENT_MAX_START_TIME_MS","300000")
  $machine_ws_agent_run_command = getValue("CODENVY_MACHINE_WS_AGENT_RUN_COMMAND","~/che/ws-agent/bin/catalina.sh run")
# Docker privilege mode, default false
  $machine_docker_privilege_mode = getValue("CODENVY_MACHINE_DOCKER_PRIVILEGE_MODE","false")
# Allows to adjust machine swap memory by multiplication current machnine memory on provided value.
# default is 0 which means disabled swap, if set multiplier value equal to 0.5 machine swap will be
# configured with size that equal to half of current machine memory.
  $machine_docker_memory_swap_multiplier = getValue("CODENVY_MACHINE_DOCKER_MEMORY_SWAP_MULTIPLIER","0")
# Semicolon separated extra volumes to mount in the machine.
# example: $machine_server_extra_volume = "/path/to/source1:/path/to/destination1:ro,Z;/path/to/source2:/path/to/destination2:ro,Z;"
  $machine_server_extra_volume = getValue("CODENVY_MACHINE_SERVER_EXTRA_VOLUME","")

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

#license configuration
  $license_manager_public_key = "30820122300d06092a864886f70d01010105000382010f00303032301006072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0004d244998b5e3b2ed908cb2eecab6f518c1e113711f1692cfa037d2bf4G82010a02820101009d399542c9b19f90d009554689f6a6ca2230e3587d41bd521281ea7b1e4f5820e2412605f6459ef974b2f41c4fec357090a75e490831cb906610ba7c842c52136ffe805959ff8bdd63cbb688412eae02f80bbf241a23492d8b80e89603RSA4204813SHA512withRSA24b668ad120ffe8ed2382c6e57e5a9ac3dce10679b5bfa9433cfac00b04905bf5c72759ce2d4ca8f2811367d113ef780e41b7a654b94ace166dada925fb64eed8a55fd5737f149f06c3216f90aa71e5862d4f530d599514fd92ec1361d6b65ea1183b8ffdb468500b5645d557105c84e7822f91c1d3b172b0cebe662ae94f5e1d1083549dd09efa595c54fb971d25f692e256bf56bd0b80e4469ab1b4126cc13df251e910203010001"

###############################
# Marketo configuration
#
  $marketo_host_url = getValue("CODENVY_MARKETO_HOST_URL","")
  $marketo_clientid = getValue("CODENVY_MARKETO_CLIENTID","")
  $marketo_client_secret = getValue("CODENVY_MARKETO_CLIENT_SECRET","")
  $marketo_send_lead_time_out_ms = getValue("CODENVY_MARKETO_SEND_LEAD_TIME_OUT_MS","")
  $marketo_batch_lead_size = getValue("CODENVY_MARKETO_BATCH_LEAD_SIZE","")
  $marketo_send_lead_amount_trying = getValue("CODENVY_MARKETO_SEND_LEAD_AMOUNT_TRYING","")
  $marketo_classic_codenvy_user_name = getValue("CODENVY_MARKETO_CLASSIC_CODENVY_USER_NAME","")
  $marketo_classic_codenvy_user_password = getValue("CODENVY_MARKETO_CLASSIC_CODENVY_USER_PASSWORD","")

###############################
#
# Codenvy folders on host machine
  $codenvy_folder = getValue("CODENVY_INSTANCE","/tmp/codenvy")

###############################
# Codenvy developmet mode
# path to codenvy puppet sources for development mode
  $puppet_src_folder = getValue("CODENVY_CONFIG","/path/to/codenvy/codenvy/puppet/sources")
# path to codenvy tomcat for development mode
  $codenvy_development_tomcat = getValue("CODENVY_DEVELOPMENT_TOMCAT","/path/to/codenvy_tomcat")

###############################
# Include base module
  include base
}
