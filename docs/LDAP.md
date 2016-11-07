## LDAP
Codenvy is compatible with an InetOrgPerson.schema. For other schemas please contact us at info@codenvy.com.

#### LDAP AUTHENTICATION
User authentication is implemented as follows:

1. Search for for user DN according to the provided name. It can be performed in two ways: either by a given DN format, or based on a user search query.
2. To verify the user's password two functions can be used: ldap bind or ldap compare.
3. If username and password match, the LDAP entry is taken and transformed to obtain UserID (this is where synchronization configuration mechanism is applied).
4. Checks if the user with a given ID already exists in the Codenvy database. If it doesn't user is authenticated.

TODO: Add table.

#### Authentication Configuration
TODO: Add table.

#### Connection Configuration
TODO: Add table.

#### SSL Configuration
TODO: Add table.

#### SASL Configuration
TODO: Add table.

#### Synchronizer Terminology
- LDAP storage - third party directory server considered as primary users storage.
- LDAP cache - a storage in Codenvy database, which basically is a mirror of LDAP storage.
- Synchronized user - a user who is present in LDAP cache.
- Synchronization candidate - a user present in LDAP storage matching all the filters and groups, the user who is going to be synchronized.
- Codenvy User - entity in Codenvy API. A user is stored in Codenvy database (PosgreSQL).

#### Synchronization Strategy
The data in the LDAP cache is considered to be consistent as long as the synchronizer does its job. Synchronization itself is unidirectional, requiring only a READ restricted connection to LDAP server.

- If the synchronizer can't retrieve users from LDAP storage, it fails.
- If the synchronizer can't store/update a user in LDAP cache it prints a warning with a reason and continues synchronization.
- If synchronization candidate is missing from LDAP cache, an appropriate User and Profile will be created.
- If synchronization candidate is present in LDAP cache, the User and Profile will be refreshed with data from LDAP storage(replacing the entity in the LDAP cache).
- If LDAP cache contains synchronized users who are missing from LDAP storage those users will be removed by the next synchronization iteration.

There are 2 possible strategies for synchronization:

1. Synchronization period is configured and synchronization is periodical.
2. Synchronization period is set to -1 then synchronization is executed once per server start after the configured initial delay. 

Synchronization can also be triggered by a REST API call:

POST <host>/api/sync/ldap

This won't change the execution of a periodical synchronization, but it is guaranteed that 2 parallel synchronizations won't be executed.

#### Synchronization Configuration
TODO: Add table.

#### Synchronization User Selection Configuration
TODO: Add table.

#### Synchronization Group Configuration
TODO: Add table.

#### Synchronization Data Configuration
TODO: Add table.

#### ACTIVE DIRECTORY EXAMPLE
TODO: Add content.
