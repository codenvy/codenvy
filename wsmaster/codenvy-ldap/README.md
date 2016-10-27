LDAP Integration
---
Ldap integration model has two major components: synchronization and authentication.
A successfully login requires prior syncronization to be completed at
least once. Only then authentication is possible:

* synchronization module gets all users according to configured filters, grabs necessary fields, transforms them to Codenvy User and persists into the database
* users that are not returned in the search results will be removed; users that have been identified by the search will be updated, and new users will added
* when a user enters name and password, the system will authenticate it against a remote ldap
* if authentication has successfuly completed then LDAP entry will be transformed according to synchronization configuration to Codenvy User
* the user will be authenticated in Codenvy

LDAP Authentication
---

Authentication.

User authentication is implemented in the following way:

1. Search for  for user DN according to the provided name. It can be performed in two ways: either by 
   a given DN format or user search by given query
2. To verify user password two functions can be used: `ldap bind` or `ldap compare`
3. If username and password match, Ldap entry is taken and transformed to obtain UserID (this is where synchronization configuration mechanism is applied) 
4. Checks if the user with a given id exists in Codenvy database. If all goes well a user is successfully authenticated

| Authentication type  | 1. Dn resolution   | 3.Password check |  3 EntryResolver | Mandatory properties  |
|---|---|---|---|---|
| AD | Format  | Bind  | User filter search  | ldap.auth.dn_format  |
| AUTHENTICATED  | Search  | Bind or Compare if ldap.auth.user_password_attribute is set  | User filter search  | ldap.auth.user.filter   |
| ANONYMOUS  | Search  | Bind or Compare if ldap.auth.user_password_attribute is set  |  User filter search | ldap.auth.user.filter  |
| DIRECT  | Format  | Bind  | DN format search  | ldap.auth.dn_format  |
| SASL  | Search  |  Bind |    User filter search| ldap.auth.user.filter  |



#### Authentication configuration
- __ldap.auth.authentication_type__ - Type of authentication to use:
     *  AD - Active Directory. Users authenticate with `sAMAccountName`. Requires the `ldap.auth.dn_format` property to be correctly configured. 
     *  AUTHENTICATED - Authenticated Search.  Manager bind/search followed by user simple bind.
     *  ANONYMOUS -  Anonymous search followed by user simple bind.
     Both AUTHENTICATED and ANONYMOUS types are depends on following set of properties: `ldap.base_dn`, `ldap.auth.subtree_search`, `dap.auth.allow_multiple_dns`, `ldap.auth.user.filter`, `ldap.auth.user_password_attribute`.
     *  DIRECT -  Direct Bind. Compute user DN from format string and perform simple bind. Requires `ldap.base_dn` property to be correctly configured.
     *  SASL - SASL bind search. Depends on following set of properties: `ldap.base_dn`, `ldap.auth.subtree_search`, `ldap.auth.allow_multiple_dns` and `ldap.auth.user.filter`.
- __ldap.auth.dn_format__ - Resolves an entry DN by using String#format. This resolver is typically used when an entry DN can be formatted directly from the user identifier. For instance, entry DNs of the form  uid=dfisher,ou=people,dc=ldaptive,dc=org could be formatted from uid=%s,ou=people,dc=ldaptive,dc=org. 

     Typical examples:  
     * _CN=%1$s,CN=Users,DC=ad,DC=codenvy-dev,DC=com_     

     Parameters:
     *  first parameter - user name provided for password validation.
- __ldap.auth.subtree_search__ - Indicates whether subtree search will be used. When set to true, allows to search authenticating DN out of the `base_dn` tree.
- __ldap.auth.allow_multiple_dns__ - Indicates whether DN resolution should fail if multiple DNs are found. When false, exception will be thrown if multiple DNs is found during search. When true, the first entry will be used for authentication attempt.
- __ldap.auth.user.filter__ - Defines the LDAP search filter (https://docs.oracle.com/cd/E19693-01/819-0997/gdxpo/index.html) parameters applied during search for the user.
    It is required to  contain an `{user}` variable and, unlike similar property from synchronization, cannot contain wildcard ('*') values (because it is supposed to search for single entity).

     Typical examples: 
     *  OpenLDAP: _cn={user}_ 
     *  ActiveDirectory: _(&(objectCategory=Person)(sAMAccountName={user}))_      

    Variables:
     *  user  - user name provided for password validation.
- __ldap.auth.user_password_attribute__ - Defines the LDAP attribute name, which value will be interpreted as the password during authentication. 

#### Connection configuration

- __ldap.url__ - the url of the directory server.
The example: _ldap://codenvy.com:389_
- __ldap.connection.connect_timeout_ms__ - the time to wait for a connection to be
established, the value must be specified in milliseconds.
The example: _30000_
- __ldap.connection.response_timeout_ms__ - restricts all the connection to
wait for a response not more than specified value, the value MUST be specified
in milliseconds. The example: _60000_
- __ldap.connection.pool.min_size__ - the size of minimum available connections in
the pool. The example: _3_
- __ldap.connection.pool.max_size__ - the size of maximum available connections in
the pool. The example: _10_
- __ldap.connection.pool.validate.on_checkout__ - Indicates whether connections will be validated before being picked from the pool. Connections that fail validation are evicted from the pool.  
- __ldap.connection.pool.validate.on_checkin__ - Indicates whether connections will be validated before being returned to the pool. Connections that fail validation are evicted from the pool.
- __ldap.connection.pool.validate.periodically__ - Indicates whether connections should be validated periodically when the pool is idle. Connections that fail validation are evicted from the pool.
- __ldap.connection.pool.validate.period_ms__ - Period in milliseconds at which pool should be validated. Default value is 30 min.
- __ldap.connection.pool.idle_ms__ - Time in milliseconds at which a connection should be considered idle and become a candidate for removal from the pool
- __ldap.connection.pool.prune_ms__ - Period in milliseconds between connection pool prunes (e.g. idle connections are removed).
- __ldap.connection.pool.fail_fast__ - Indicates whether exception should be thrown during pool initialization when pool does not contain at least one connection and it's minimum size is greater than zero
- __ldap.connection.pool.block_wait_ms__ - Period in milliseconds during which an pool which is reached the maximum size will block new requests. BlockingTimeoutException will be thrown when time is exceeded. Default is _infinite_.
- __ldap.connection.bind.dn__ - Since connections are initialized by performing a bind operation, this property indicates DN to make this bind with. The example: _userX_
- __ldap.connection.bind.password__ - Credential for the initial connection bind. The example: _password_
   On Active Directory, a special mode called FastBind can be activated by setting both  `ldap.connection.bind.dn` and `ldap.connection.bind.password` to a value of "*".  In this mode, no group evaluation is done, so it can be only used to verify a client's credentials. 
   See http://msdn.microsoft.com/en-us/library/cc223503(v=prot.20).aspx
   

#### SSL configuration
   SSL can be configured in two ways - using trust certificate or using secure keystore. 
   Certificates from trusted CA does not need any additional actions like manual import. It's enough to just turn SSL on. For the self-signed certificates, it is required to import it into java keystore or use separately.  
   See https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html for keystore import instructions.
- __ldap.connection.use_ssl__ - Indicates whether the secured protocol will be used for connections.
- __ldap.connection.use_start_tls__ - Indicates whether TLS (Transport Layer Security) should be established on connections.
- __ldap.connection.ssl.trust_certificates__ - Path to the certificates file. Example: `file:///etc/ssl/mycertificate.cer`
- __ldap.connection.ssl.keystore.name__  - Defines name of the keystore to use. Example: `file:///usr/local/jdk/jre/lib/security/mycerts`
- __ldap.connection.ssl.keystore.password__ - Defines keystore password.
- __ldap.connection.ssl.keystore.type__ - Defines keystore type.

#### SASL configuration
   The Simple Authentication and Security Layer (SASL) is a method for adding authentication support to connection-based protocols. To use this specification, a protocol includes a command
   for identifying and authenticating a user to a server and for optionally negotiating a security layer for subsequent protocol interactions. 

   As an example, if the client and server both uses TLS, and have trusted certificates, they may use the SASL/EXTERNAL, and for client requests the server derive its identity from credentials provided at a lower (TLS) level.
- __ldap.connection.sasl.mechanism__ - Defines SASL mechanism. Supported values are `DIGEST_MD5`, `CRAM_MD5`, `GSSAPI` and `EXTERNAL`.
    See https://msdn.microsoft.com/en-us/library/cc223371.aspx for AD,  or http://www.openldap.org/doc/admin24/sasl.html for OpenLdap mechanisms explanation.
- __ldap.connection.sasl.realm__ - SASL realm value. Example: `example.com`
- __ldap.connection.sasl.authorization_id__ - Defines the SASL authorization id.
- __ldap.connection.sasl.security_strength__ - Specifies the client's preferred privacy protection strength (ciphers and key lengths used for encryption). The value of this property is a comma-separated list of strength values, the order of which specifies the preference order. The three possible strength values are "low", "medium", and "high". If you do not specify this property, then it defaults to "high,medium,low".
- __ldap.connection.sasl.mutual_auth__ - SASL mutual authentication on supported mechanisms. For some applications, it is equally important that the LDAP server's identity be verified. The process by which both parties participating in the exchange authenticate each other is referred to as mutual authentication. Defaults to false.
- __ldap.connection.sasl.quality_of_protection__ - Defines integrity and privacy protection of the communication channel.It is negotiated between during the authentication phase of the SASL exchange. Possible values are `auth` (default),`auth-inf` and `auth-conf`. 


LDAP Synchronizer
---

Service for synchronizing third party LDAP users with Codenvy database.

##### Terminology
- LDAP storage - third party directory server considered as primary users storage
- LDAP cache - a storage in Codenvy database, which basically is a mirror of LDAP storage
- Synchronized user - a user who is present in LDAP cache
- Synchronization candidate - a user present in LDAP storage matching all the filters and groups, the user who is going to be synchronized
- Codenvy User - entity in Codenvy API. A user is stored in Codenvy database (PosgreSQL)

##### Synchronization strategy/behaviour

The data in LDAP cache is considered to be eventually consistent as long
as the synchronizer does its job.
Synchronization itself is unidirectional, which basically means that
READ restricted connection to ldap-server is all that needed.

- If the synchronizer can't retrieve users from LDAP storage, it fails
- If the synchronizer can't store/update a user in LDAP cache it prints
a warning with a reason and continues synchronization
- If synchronization candidate is missing from LDAP cache, an appropriate
User and Profile will be created
- If synchronization candidate is present in LDAP cache, an appropriate
User and Profile will be refreshed with data from LDAP storage(replacing
  the entity in LDAP cache)
- If LDAP cache contains synchronized users who are missing from LDAP storage
those users will be removed by next synchronization iteration

There are 2 possible strategies for synchronization
- Synchronization period is configured then synchronization is periodical
- Synchronization period is set to _-1_ then synchronization executed once
per server start after configured initial delay

Along with that synchronization can be enforced by REST API call,
`POST <host>/api/sync/ldap` will do that, this won't reestimate periodical
synchronization, but it is guaranteed that 2 parallel synchronizations won't
be executed.


Configuration
---

#### Synchronizer configuration

- __ldap.sync.period_ms__ _(optional)_ - how often to synchronize users/profiles.
The period property must be specified in milliseconds e.g. _86400000_ is one day.
If the synchronization shouldn't be periodical set the value of this
configuration property to _-1_ then it will be done once each time
server starts.

- __ldap.sync.initial_delay_ms__ - when to synchronize first time. The delay
property must be specified in milliseconds. Unlike period, delay MUST be a non-negative
integer value, if it is set to _0_ then synchronization will be performed immediately
on sever startup.

- __ldap.sync.user_linking_attribute__ _(optional)_ - what attribute to use
  for linking ldap users and database users. Possible value are: _id_, _email_.
If this attribute is not configured _id_ will be used

- __ldap.sync.remove_if_missing__ - whether to remove those users who are present
 in LDAP cache but missing from LDAP storage

- __ldap.sync.update_if_exists__ - whether to update those users who are present in LDAP cache
and were changed in LDAP storage

#### Users selection configuration

- __ldap.base_dn__ - the root distinguished name to search LDAP entries,
serves as a base point for searching users.
The example: _dc=codenvy,dc=com_

- __ldap.sync.user.additional_dn__ _(optional)_ - if set will be used
in addition to <i>ldap.base_dn</i> for searching users.
The example: _ou=CodenvyUsers_

- __ldap.sync.user.filter__ - the filter used to search users, only those users
who match the filter will be synchronized.
The example: _(objectClass=inetOrgPerson)_

- __ldap.sync.page.size__ _(optional)_  - how many LDAP entry retrieve per-page,
if set to <= 0 then <i>1000</i> is used by default.

- __ldap.sync.page.read_timeout_ms__ _(optional)_ - how much time to wait for
a page, the default value is 30000ms, the value of this property MUST be
set in milliseconds.

#### Groups configuration

- __ldap.sync.group.additional_dn__(optional) - if set will be used
in addition to <i>ldap.base_dn</i> for searching groups.
The example: _ou=groups_

- __ldap.sync.group.filter__ (optional) - the filter used to search groups.
The synchronizer will use this filter to find all the groups and then
<i>ldap.sync.group.attr.members</i> attribute for retrieving DNs of those users
who should be synchronized, please note that if this parameter is set
then <i>ldap.sync.group.attr.members</i> must be also set.
All the users who are members of found groups will be filtered by
<i>ldap.sync.user.filter</i>.
The example: _(&(objectClass=groupOfNames)(cn=CodenvyMembers))_

- __ldap.sync.group.attr.members__ (optional) - the name of the attribute
which identifies group members distinguished names. The synchronizer considers
that this attribute is multi-value attribute and values are user DNs.
This attribute is ignored if <i>ldap.sync.group.filter</i> is not set.
The example: _member_



#### Data to synchronize configuration

- __ldap.sync.user.attr.id__ - LDAP attribute name which defines unique mandatory
user identifier, the value of this attribute will be used as Codenvy User/Profile identifier.
All the characters which are not in `a-zA-Z0-9-_` will be removed from user identifier during synchronization, for instance
if the ide of the user is _{0-1-2-3-4-5}_ he will be synchronized as a user with id _0-1-2-3-4-5_.
Common values for this property : _cn_, _uid_, _objectGUID_.

- __ldap.sync.user.attr.name__ - LDAP attribute name which defines unique
user name, this attribute will be used as Condevy User name.
Common values for this property : _cn_.

- __ldap.sync.user.attr.email__ - LDAP attribute name which defines unique user email,
the value of this attribute will be used as Codenvy User email. If there is no such analogue you can
simply use the same attribute used for name.
Common values for this property: _mail_.

- __ldap.sync.profile.attrs__ _(optional)_ - comma separated application-to-LDAP
attribute mapping pairs. Available application attributes:
  - firstName
  - phone
  - lastName
  - employer
  - country
  - jobtitle

  Common values for the attributes above in the described format:  _firstName=givenName,phone=telephoneNumber,lastName=sn,employer=o,country=st,jobtitle=title_.



#### AD example

Properties to be configured in `/etc/puppet/manifests/nodes/codenvy/codenvy.pp`(this is an example, take a look at comments)
```
ldap.url=ldap://???? <--- Change this 

ldap.base_dn=DC=ad,DC=codenvy-dev,DC=com <--- Change this 
ldap.auth.user.filter=(&(objectCategory=Person)(sAMAccountName=*)) <--- Change this 
ldap.auth.authentication_type=AD <--- Change this 

ldap.auth.dn_format=CN=%1$s,CN=Users,DC=ad,DC=codenvy-dev,DC=com <--- Change this 
ldap.auth.user_password_attribute=NULL
ldap.auth.allow_multiple_dns=false
ldap.auth.subtree_search=true

ldap.connection.provider=NULL
ldap.connection.bind.dn=CN=skryzhny,CN=Users,DC=ad,DC=codenvy-dev,DC=com <--- Change this 
ldap.connection.bind.password=????? <--- Change this 
ldap.connection.use_ssl=false
ldap.connection.use_start_tls=false
ldap.connection.pool.min_size=3
ldap.connection.pool.max_size=10
ldap.connection.pool.validate.on_checkout=false
ldap.connection.pool.validate.on_checkin=false
ldap.connection.pool.validate.period_ms=180000
ldap.connection.pool.validate.periodically=true
ldap.connection.pool.fail_fast=true
ldap.connection.pool.idle_ms=5000
ldap.connection.pool.prune_ms=10000
ldap.connection.pool.block_wait_ms=30000
ldap.connection.connect_timeout_ms=30000
ldap.connection.response_timeout_ms=120000

ldap.connection.ssl.trust_certificates=NULL
ldap.connection.ssl.keystore.name=NULL
ldap.connection.ssl.keystore.password=NULL
ldap.connection.ssl.keystore.type=NULL

ldap.connection.sasl.realm=NULL
ldap.connection.sasl.mechanism=NULL
ldap.connection.sasl.authorization_id=NULL
ldap.connection.sasl.security_strength=NULL
ldap.connection.sasl.mutual_auth=false
ldap.connection.sasl.quality_of_protection=NULL


ldap.sync.initial_delay_ms=10000
ldap.sync.period_ms=-1
ldap.sync.remove_if_missing=true
ldap.sync.update_if_exists=true
ldap.sync.page.size=1000
ldap.sync.page.read_timeout_ms=30000
ldap.sync.user.additional_dn=NULL
ldap.sync.user.filter=(&(objectCategory=Person)(sAMAccountName=*)) <--- Change this 
ldap.sync.user.attr.email=cn <--- Change this 
ldap.sync.user.attr.id=objectGUID <--- Change this 
ldap.sync.user.attr.name=cn <--- Change this 
ldap.sync.profile.attrs=firstName=sAMAccountName <--- Change this 
ldap.sync.group.additional_dn=NULL
ldap.sync.group.filter=NULL
ldap.sync.group.attr.members=NULL
```
