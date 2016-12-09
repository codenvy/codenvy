---
title: Authentication
excerpt: "Add your oAuth keys and integrate your corporate LDAP."
layout: docs
overview: true
permalink: /docs/authentication/
---
# Essential Files  


| File   | Role   
| --- | --- 
| `/etc/puppet/manifests/nodes/codenvy/codenvy.pp`   | Codenvy system properties configuration file for LDAP and oAuth.   

If you modify these files, puppet will detect the changes on a 300 second interval. You can also run `puppet agent -t` to initiate an update.
# oAuth  
The Codenvy account creation and login screens offer optional oAuth buttons for users. These are disabled by default.  You must add your secret keys and client IDs to the configuration file. There is an oAuth section commented out.
```text  
###############################
# oAuth 

# Google. 
  $google_client_id = "your_google_client_id"
  $google_secret = "your_google_secret"
  
# Github. 
  $github_client_id = "your_github_client_id"
  $github_secret = "your_github_secret"
  
# BitBucket.
  $bitbucket_client_id = "your_bitbucket_client_id"
  $bitbucket_secret = "your_bitbucket_secret"
###############################
```
### Generate ID & Secrets for oAuth
You need to register your Codenvy installation at different oAuth providers. Each provider has a different location for configuring oAuth. Each site will ask for two URLs. After providing these details, the site will give you a unique client ID and client secret.

| Name   | URL   
| --- | --- 
| Site, Homepage, or Hostname URL   | `http://<your_hostname>`   
| Authorized JavaScript Origins URL   | `http://<your_hostname>`   
| Callback or Authorization URL   | `http://<your_hostname>/api/oauth/callback`   

### GitHub
1. **Account Settings** > **Applications**.
2. Register a new application.

### Google
1. **Google Developer Console** > **APIS & Auth** > **Credentials**
2. You will be presented with an option to create a new client ID.

### BitBucket
1. **Integrated Applications** > **Add Consumer** 


# LDAP  
You can configure parameters to the details of your organization.
```json  
  ###############################
  # LDAP configurations
  #
  # LDAP server address
  $ldap_protocol = "ldap"
  $ldap_host = "localhost"
  $ldap_port = "389"
  # (Mandatory) replace placeholder with some password
  # That pass will be used for $java_naming_security_principal for access to LDAP
  $user_ldap_password = "password"
  $java_naming_security_principal = "cn=Admin,dc=codenvy-enterprise,dc=com"
  $java_naming_security_authentication = "simple"
  #
  # connection pool configurations
  $ldap_connect_pool = "true"
  $ldap_connect_pool_initsize = "10"
  $ldap_connect_pool_maxsize = "20"
  $ldap_connect_pool_prefsize = "10"
  $ldap_connect_pool_timeout = "300000"
  #
  # ldap dn configurations
  $user_ldap_dn = "dc=codenvy-enterprise,dc=com"
  $user_ldap_users_ou = "users"
  $user_ldap_user_container_dn = "ou=$user_ldap_users_ou,$user_ldap_dn"
  $user_ldap_user_dn = "uid"
  $user_ldap_old_user_dn = "cn"
  $user_ldap_object_classes = "inetOrgPerson"
  $user_ldap_attr_name = "cn"
  $user_ldap_attr_id = "uid"
  $user_ldap_attr_password = "userPassword"
  $user_ldap_attr_email = "mail"
  $user_ldap_attr_aliases = "initials"
  # each user should have defined $user_ldap_attr_role_name property
  $user_ldap_attr_role_name = "employeeType"
  $user_ldap_allowed_role = "NULL"
  #
  # user profile mappings
  $profile_ldap_profile_container_dn = "ou=$user_ldap_users_ou,$user_ldap_dn"
  $profile_ldap_profile_dn = "uid"
  $profile_ldap_attr_id = "uid"
  $profile_ldap_allowed_attributes = "givenName=firstName,telephoneNumber=phone,mail=email,sn=lastName,o=employer,st=country,title=jobtitle"
  #
  # Pre-installed admin user in default codenvy LDAP
  # (Mandatory) Codenvy admin user name
  $admin_ldap_user_name = "admin"
  # (Mandatory) Codenvy admin mail
  $admin_ldap_mail = "$admin_ldap_user_name@codenvy.onprem"
  # (Mandatory) Codenvy admin password
  $admin_ldap_password = "password"
```
