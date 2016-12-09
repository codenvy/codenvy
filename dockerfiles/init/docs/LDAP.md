## LDAP Integration
The Codenvy LDAP integration has two major roles: synchronization and authentication.

For a user to successfully login, they must first be synchronized with Codenvy. After syncing authorization is possible:

*Synchronization*  

- Synchronizer gets all users based on the configured groups/filters.
- Synchronizer creates a Codenvy User and persists the necessary fields from LDAP into the Codenvy database (passwords are not persisted).
- Each time the synchronizer runs the groups/filters are re-evaluated: Users that no longer match the group/filters are removed; Users that match are updated or added as needed.

*Authentication*  

- When a user enters their name and password, the system authenticates them against the remote LDAP.
- If authentication is successful the user gains access to Codenvy.

##### LDAP AUTHENTICATION

User authentication is implemented as follows:

1. Search for for user DN according to the provided name. It can be performed in two ways: either by
a given DN format, or based on a user search query.
2. To verify the user's password two functions can be used: `ldap bind` or `ldap compare`.
3. If username and password match, the LDAP entry is taken and transformed to obtain UserID (this is where synchronization configuration mechanism is applied).
4. Checks if the user with a given ID already exists in the Codenvy database. If it doesn't user is authenticated.

##### CONFIGURATION
There are several types of configuration covered in the tables below:

- Authentication configuration
- Connection configuration
- SSL configuration
- SASL configuration

*Authentication Configuration*  

TODO: Add table

##### Connection Configuration

TODO: Add table

##### SSL Configuration

SSL can be configured in two ways - using trust certificate or using secure keystore.

Certificates from a trusted certificate authority (CA) do not need any additional actions like manual import. It's enough to just turn SSL on.

Self-signed certificates must be imported into the Java keystore or used separately. See [https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html](https://docs.oracle.com/javase/tutorial/security/toolsign/rstep2.html) for keystore import instructions.

TODO: Add table

##### SASL Configuration
The Simple Authentication and Security Layer (SASL) is a method for adding authentication support to connection-based protocols. To use this specification, a protocol includes a command for identifying and authenticating a user to a server and for optionally negotiating a security layer for subsequent protocol interactions.

As an example, if the client and server both uses TLS, and have trusted certificates, they may use SASL / EXTERNAL, and for client requests the server can derive its identity from credentials provided at a lower (TLS) level.

TODO: Add table

##### LDAP SYNCHRONIZER
This service synchronizes third party LDAP users with the Codenvy database.

*Terminology*  

LDAP storage - third party directory server considered as primary users storage.
LDAP cache - a storage in Codenvy database, which basically is a mirror of LDAP storage.
Synchronized user - a user who is present in LDAP cache.
Synchronization candidate - a user present in LDAP storage matching all the filters and groups, the user who is going to be synchronized.
Codenvy User - entity in Codenvy API. A user is stored in Codenvy database (PosgreSQL).

*Synchronization Strategy*  

The data in the LDAP cache is considered to be consistent as long as the synchronizer does its job. Synchronization itself is unidirectional, requiring only a READ restricted connection to LDAP server.

If the synchronizer can't retrieve users from LDAP storage, it fails.
If the synchronizer can't store/update a user in LDAP cache it prints a warning with a reason and continues synchronization.
If synchronization candidate is missing from LDAP cache, an appropriate User and Profile will be created.
If synchronization candidate is present in LDAP cache, the User and Profile will be refreshed with data from LDAP storage(replacing the entity in the LDAP cache).
If LDAP cache contains synchronized users who are missing from LDAP storage those users will be removed by the next synchronization iteration.
There are 2 possible strategies for synchronization:

Synchronization period is configured and synchronization is periodical.
Synchronization period is set to `-1` then synchronization is executed once
per server start after the configured initial delay.
Synchronization can also be triggered by a REST API call:

`POST <host>/api/sync/ldap`

This won't change the execution of a periodical synchronization, but it is guaranteed that 2 parallel synchronizations won't be executed.

*Configuration*   

TODO: Add table

* Users Selection Configuration *

TODO: Add table

*Group Configuration*  

TODO: Add table

*Synchronized Data Configuration*  

TODO: Add table

*ACTIVE DIRECTORY EXAMPLE*  
Properties to be configured in `/etc/puppet/manifests/nodes/codenvy/codenvy.pp`

Commented items must be changed.

TODO: Add table
