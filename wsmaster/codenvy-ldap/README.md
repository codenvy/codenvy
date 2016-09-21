LDAP Authentication
---
#### Authentication configuration
- __ldap.auth.user.filter__ - Search filter to execute. The example: _(&(objectCategory=Person)(sAMAccountName=*))_
- __ldap.auth.authentication_type__ - Type authentication to use:
     *  AD - Active Directory. Users authenticate with sAMAccountName.
     *  AUTHENTICATED - Authenticated Search.  Manager bind/search followed by user simple bind.
     *  ANONYMOUS -  Anonymous search followed by user simple bind.
     *  DIRECT -  Direct Bind. Compute user DN from format string and perform simple bind.
     *  SASL - SASL bind search.
- __ldap.auth.dn_format__ - Resolves an entry DN by using String#format. This resolver is typically used when an entry DN
can be formatted directly from the user identifier. For instance, entry DNs of the form  uid=dfisher,ou=people,dc=ldaptive,dc=org could be formattedfrom uid=%s,ou=people,dc=ldaptive,dc=org. The example: _CN=%1$s,CN=Users,DC=ad,DC=codenvy-dev,DC=com_

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
- __ldap.connection.bind.dn__ -  DN to bind as before performing operations. The example: _userX_
- __ldap.connection.bind.password__ - Credential for the bind DN. The example: _password_


LDAP Synchronizer
---

Service for synchronizing third party LDAP users with Codenvy database.

##### Terminology
- LDAP storage - third party directory server considered as primary users storage
- LDAP cache - a storage in Codenvy database, which basically is a mirror of LDAP storage
- Synchronized user - a user who is present in LDAP cache
- Synchronization candidate - a user present in LDAP storage matching all the filters and
groups, the user who is going to be synchronized

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

#### Users selection configuration

- __ldap.base_dn__ - the root distinguished name to search LDAP entries,
seves as a base point for searching users.
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
