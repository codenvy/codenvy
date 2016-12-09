---
title: User Management
excerpt: "Managing users and accounts."
layout: docs
overview: true
permalink: /docs/user/
---
Users can create accounts with the self-service portal that is at the root URL.  Admins can also create and destroy accounts. You must use the Codenvy API with an admin token to execute these tasks.
# Login As Admin  

```shell  
curl -v 'http://dev.box.com/api/auth/login?' 
     -H 'Content-Type: application/json' 
     -d '{"username":"admin@codenvy.onprem"password":"codenvyadmin"}'
```

# Create User  
Replace `{admintoken}` with the token returned from the login.
```shell  
curl 'http://dev.box.com/api/user/create' 
     -H 'Authorization:{admintoken}' 
     -H 'Content-Type: application/json' 
     -d '{"name":"newusername"password":"somepassword"}'
```
If successful, it will return a JSON response with additional helper method and a user ID. The user can then login with the user name and password you provided.
```json  
{
  "password" : "<none>\n  "aliases"  : [],
  "links"    : [
    { 
      "href":"http://dev.box.com/api/profile\n      "parameters":[],
      "rel":"current user profile\n      "method":"GET\n      "produces":"application/json"
    },
    {
      "href":"http://dev.box.com/api/user\n      "parameters":[],
      "rel":"get current\n      "method":"GET\n      "produces":"application/json"
    },
    {
      "href":"http://dev.box.com/api/user/password\n      "parameters":[],
      "rel":"update password\n      "method":"POST\n      "consumes":"application/x-www-form-urlencoded"
    },
    {
      "href":"http://dev.box.com/api/user/userp3yy3nw3anpp7zrl\n      "parameters":[],
      "rel":"get user by id\n      "method":"GET\n      "produces":"application/json"
    },
    {
      "href":"http://dev.box.com/api/profile/userp3yy3nw3anpp7zrl\n      "parameters":[],
      "rel":"user profile by id\n      "method":"GET\n      "produces":"application/json"
    },
    {
      "href":"http://dev.box.com/api/user/find?email=someuser@codenvy.com\n      "parameters":[],
      "rel":"get user by email\n      "method":"GET\n      "produces":"application/json"
    },
    {
      "href":"http://dev.box.com/api/user/userp3yy3nw3anpp7zrl\n      "parameters":[],
      "rel":"remove user by id\n      "method":"DELETE"
    }
  ],
  "name":"newusername\n  "email":"someuser\n  "id":"userp3yy3nw3anpp7zrl"
}
```

# Delete User  
Take the unique user ID and use it as part of the URL syntax to delete a user account. 
```shell  
curl -v -X DELETE 'http://dev.box.com/api/user/{userid}' 
     -H 'Authorization:{admintoken}'\
```

# Get User Count  
You can query the LDAP database to see how many users are being used to count against your license.
```shell  
sudo slapcat 2>&1 | grep -c "^dn: uid=user"
```

# Generate User List  
You can also use the interaction with LDAP to generate a list of all users within the system.
```shell  
#!/bin/bash

HOST=192.168.56.99
PASS=CodenvyAdmin
BASE=dc=a,dc=codenvy-dev,dc=com

ldapsearch -x -h $HOST -w$PASS -Dcn=Admin,$BASE -b ou=users,$BASE '(objectClass=inetOrgPerson)' uid cn givenname sn \
| grep -v '^#' | grep -v '^dn: uid=' | grep -v 'search: ' | grep -v 'result: '\
```
and it will generate a list of users like:
```text  
givenName: Admin
sn: Codenvy
cn: prodadmin
uid: prodadmin@codenvy.onprem

uid: userirhx2223a39or9lp
cn: skryzhny
sn: Kryzhnii
givenName: Serhii

uid: userfajbbuvmhliuxhyb
cn: sergk
sn: Kryzhny
givenName: Sergey\
```
