# Change Log

## [Unreleased](https://github.com/codenvy/codenvy/tree/HEAD)

[Full Changelog](https://github.com/codenvy/codenvy/compare/4.7.0...HEAD)

**Issues with no labels:**

- Cann  [\#651](https://github.com/codenvy/codenvy/issues/651)

## [4.7.0](https://github.com/codenvy/codenvy/tree/4.7.0) (2016-08-29)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.7.0-RC1...4.7.0)

**Issues with no labels:**

- Github push error [\#640](https://github.com/codenvy/codenvy/issues/640)
- Enhance GDB debugger to work with node.js [\#622](https://github.com/codenvy/codenvy/issues/622)
- Cannot ping api.nasa.gov at beta.codenvy.com [\#588](https://github.com/codenvy/codenvy/issues/588)
- \[dashboard\] Join profile and security tabs of the account view [\#577](https://github.com/codenvy/codenvy/issues/577)
- Make it possible to provide additional workspace mount [\#565](https://github.com/codenvy/codenvy/issues/565)
- Workspace refusing to start [\#557](https://github.com/codenvy/codenvy/issues/557)
- Good way to include Tomcat7 in my Java Stack [\#535](https://github.com/codenvy/codenvy/issues/535)
- Reorganize install.codenvycorp.com and start.codenvy.com servers [\#508](https://github.com/codenvy/codenvy/issues/508)
- inconsistent results on javascript [\#489](https://github.com/codenvy/codenvy/issues/489)
- Changing of hostname of multi-server Codenvy on-prem hung up [\#624](https://github.com/codenvy/codenvy/issues/624)
- Revamp list's header [\#613](https://github.com/codenvy/codenvy/issues/613)
- Impossible to restart any workspace on beta.codenvy.com [\#589](https://github.com/codenvy/codenvy/issues/589)
- Integration tests of installation manager failed because of lack of RHEL subscription [\#575](https://github.com/codenvy/codenvy/issues/575)
- NPE on some old accounts [\#564](https://github.com/codenvy/codenvy/issues/564)
- Container not cleaned up if it failed to start [\#562](https://github.com/codenvy/codenvy/issues/562)
- netstat deprecated and not available on some centos 7 distributions [\#560](https://github.com/codenvy/codenvy/issues/560)
- Make factory creation and edit screens identical [\#554](https://github.com/codenvy/codenvy/issues/554)
- Wrong behavior while creating new account [\#550](https://github.com/codenvy/codenvy/issues/550)
- Default admin name of zabbix is "Admin", not "admin" [\#547](https://github.com/codenvy/codenvy/issues/547)
- Issue with snapshots [\#541](https://github.com/codenvy/codenvy/issues/541)
- Rework MachineBackupManager multithreaded tests [\#537](https://github.com/codenvy/codenvy/issues/537)
- Installation manager doesn't store empty codenvy property [\#512](https://github.com/codenvy/codenvy/issues/512)
- Share a workspace: User not found message when sharing a workspace [\#509](https://github.com/codenvy/codenvy/issues/509)
- Factories with open file action do not get into the workspace [\#507](https://github.com/codenvy/codenvy/issues/507)
- Apply the new list component to factories list [\#494](https://github.com/codenvy/codenvy/issues/494)
- Add post-flight Codenvy on-prem installation check [\#479](https://github.com/codenvy/codenvy/issues/479)
- Can't checkout git repo in version 4.5.1 [\#476](https://github.com/codenvy/codenvy/issues/476)
- Support PostgreSQL DB when change admin password of Codenvy onprem by Installation Manager [\#466](https://github.com/codenvy/codenvy/issues/466)
- Add record '127.0.0.1 \<new-hostname\>' into /etc/hosts when changing Codenvy on-prem hostname [\#464](https://github.com/codenvy/codenvy/issues/464)
- Update codenvy config and update of Codenvy version hung up on https://\<hostname\> address [\#459](https://github.com/codenvy/codenvy/issues/459)
- Support of AWS ECR dynamic passwords [\#453](https://github.com/codenvy/codenvy/issues/453)
- Docker machine impl doesn't stop and remove containers if script that restores projects FS fails [\#447](https://github.com/codenvy/codenvy/issues/447)
- Create a page to be displayed after user is deleted [\#389](https://github.com/codenvy/codenvy/issues/389)

**Pull requests merged:**

- Fix empty factories message [\#639](https://github.com/codenvy/codenvy/pull/639) ([ashumilova](https://github.com/ashumilova))
- improve list of factories for UD [\#633](https://github.com/codenvy/codenvy/pull/633) ([olexii4](https://github.com/olexii4))
- \(\#624\) don't change /etc/hosts when changing host\_url of multi-server… [\#626](https://github.com/codenvy/codenvy/pull/626) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Remove change admin password capability from Installation Manager [\#625](https://github.com/codenvy/codenvy/pull/625) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Do not add null profile attributes [\#619](https://github.com/codenvy/codenvy/pull/619) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Update according to the IDE API changes [\#618](https://github.com/codenvy/codenvy/pull/618) ([azatsarynnyy](https://github.com/azatsarynnyy))
- rework che-lists  for UD [\#615](https://github.com/codenvy/codenvy/pull/615) ([olexii4](https://github.com/olexii4))
- \(\#479\) add post-flight Codenvy on-prem installation check [\#614](https://github.com/codenvy/codenvy/pull/614) ([dmytro-ndp](https://github.com/dmytro-ndp))
- add Codenvy on-prem 4.7.0-RC2-SNAPSHOT default properites [\#610](https://github.com/codenvy/codenvy/pull/610) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add Codenvy Admin User creator component [\#609](https://github.com/codenvy/codenvy/pull/609) ([akorneta](https://github.com/akorneta))
- Add terminal artifact to dependency management section [\#608](https://github.com/codenvy/codenvy/pull/608) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-597 Remove Mongo & LDAP DAO related code [\#605](https://github.com/codenvy/codenvy/pull/605) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-505 Rework permissions mechanism for stacks, recipes and system perms. [\#603](https://github.com/codenvy/codenvy/pull/603) ([mshaposhnik](https://github.com/mshaposhnik))
- Add needed RPC components to the Guice binding [\#598](https://github.com/codenvy/codenvy/pull/598) ([vparfonov](https://github.com/vparfonov))
- \[WIP\] Move AdminUserDao functionality to UserDao [\#596](https://github.com/codenvy/codenvy/pull/596) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Fixes related to adding foreign reference on account in Workspace [\#595](https://github.com/codenvy/codenvy/pull/595) ([akorneta](https://github.com/akorneta))
- CODENVY-537: Rework MachineBackupManager multithreaded tests [\#592](https://github.com/codenvy/codenvy/pull/592) ([mmorhun](https://github.com/mmorhun))
- Adapt Codenvy to changes in Che [\#571](https://github.com/codenvy/codenvy/pull/571) ([garagatyi](https://github.com/garagatyi))
- remove che-long-touch behaviour in navbar [\#648](https://github.com/codenvy/codenvy/pull/648) ([akurinnoy](https://github.com/akurinnoy))
- update dashboard view [\#647](https://github.com/codenvy/codenvy/pull/647) ([akurinnoy](https://github.com/akurinnoy))
- CHE-2158: simplify boxes' design for Dashboard page [\#630](https://github.com/codenvy/codenvy/pull/630) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-613: add factories page description [\#627](https://github.com/codenvy/codenvy/pull/627) ([ashumilova](https://github.com/ashumilova))
- CHE-1848: fix title in toolbar in list-factories view. [\#594](https://github.com/codenvy/codenvy/pull/594) ([akurinnoy](https://github.com/akurinnoy))
- Codenvy 507 [\#540](https://github.com/codenvy/codenvy/pull/540) ([olexii4](https://github.com/olexii4))
- CHE-1770: New layout for forms in UD [\#533](https://github.com/codenvy/codenvy/pull/533) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-509: add user not found message in share workspace widget [\#522](https://github.com/codenvy/codenvy/pull/522) ([ashumilova](https://github.com/ashumilova))
- Add redirect to account deleted page [\#504](https://github.com/codenvy/codenvy/pull/504) ([ashumilova](https://github.com/ashumilova))
- CODENVY-380 Create a page to be displayed after user is deleted [\#498](https://github.com/codenvy/codenvy/pull/498) ([vkuznyetsov](https://github.com/vkuznyetsov))
- \[WP\] Codenvy source code structural refactoring [\#322](https://github.com/codenvy/codenvy/pull/322) ([skabashnyuk](https://github.com/skabashnyuk))

## [4.7.0-RC1](https://github.com/codenvy/codenvy/tree/4.7.0-RC1) (2016-08-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.6.2...4.7.0-RC1)

**Issues with no labels:**

- Puppet doesn't change /etc/hosts [\#574](https://github.com/codenvy/codenvy/issues/574)
- Workspace Connection Error at creation [\#567](https://github.com/codenvy/codenvy/issues/567)
- API don't remove container when entrypoint is invalid and conatainer state is "Exited" [\#563](https://github.com/codenvy/codenvy/issues/563)
- Not able to connect with external database on 3306 port [\#534](https://github.com/codenvy/codenvy/issues/534)
- Populate a new Codenvy installation with some defaults [\#531](https://github.com/codenvy/codenvy/issues/531)
- Starting auxiliary machines inside workspace during the build [\#519](https://github.com/codenvy/codenvy/issues/519)
- Create a script for diagnostic and collect needed info for support in case of troubleshooting [\#413](https://github.com/codenvy/codenvy/issues/413)
- Create migration tool to migrate from LDAP/MongoDB to PostgreSQL [\#397](https://github.com/codenvy/codenvy/issues/397)

**Pull requests merged:**

- Fix codenvy wsagent groupId [\#585](https://github.com/codenvy/codenvy/pull/585) ([mkuznyetsov](https://github.com/mkuznyetsov))
- \(\#575\) fix unregistering the RHEL system in IM integration tests [\#576](https://github.com/codenvy/codenvy/pull/576) ([dmytro-ndp](https://github.com/dmytro-ndp))
- \(\#547\) fix zabbix admin name in default Codenvy on-prem properties [\#569](https://github.com/codenvy/codenvy/pull/569) ([dmytro-ndp](https://github.com/dmytro-ndp))
- \(\#560\) replace deprecated 'netstat' command by 'ss' [\#568](https://github.com/codenvy/codenvy/pull/568) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix config of installation manager integration tests [\#559](https://github.com/codenvy/codenvy/pull/559) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-550 Fix mail server groupId [\#558](https://github.com/codenvy/codenvy/pull/558) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Fix update.sh to use updater.nightly4.codenvy-stg.com [\#556](https://github.com/codenvy/codenvy/pull/556) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Installation manager [\#555](https://github.com/codenvy/codenvy/pull/555) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add default properties and migration script of Codenvy on-prem 4.6.2 [\#553](https://github.com/codenvy/codenvy/pull/553) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add integration tests for JPA DAOs on PostgreSQL database [\#551](https://github.com/codenvy/codenvy/pull/551) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Makes WorkspaceDAO to respect workers on get by user [\#542](https://github.com/codenvy/codenvy/pull/542) ([mshaposhnik](https://github.com/mshaposhnik))

## [4.6.2](https://github.com/codenvy/codenvy/tree/4.6.2) (2016-08-09)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.6.1...4.6.2)

**Issues with no labels:**

- Import from github [\#528](https://github.com/codenvy/codenvy/issues/528)
- Timeout for custome stack [\#493](https://github.com/codenvy/codenvy/issues/493)

**Pull requests merged:**

- refactoring of GitException [\#538](https://github.com/codenvy/codenvy/pull/538) ([vinokurig](https://github.com/vinokurig))
- \(\#459\) fix requesting to Codenvy API throught HTTPS [\#536](https://github.com/codenvy/codenvy/pull/536) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Create WorkerDao implementation on jpa [\#523](https://github.com/codenvy/codenvy/pull/523) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-453: Add support of AWS ECR dynamic passwords [\#521](https://github.com/codenvy/codenvy/pull/521) ([mmorhun](https://github.com/mmorhun))

## [4.6.1](https://github.com/codenvy/codenvy/tree/4.6.1) (2016-08-04)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.6.0...4.6.1)

**Issues with no labels:**

- FIND command will not scal [\#500](https://github.com/codenvy/codenvy/issues/500)
- Snapshot restore not happening [\#492](https://github.com/codenvy/codenvy/issues/492)
- PULL reports Failed to get private ssh key [\#491](https://github.com/codenvy/codenvy/issues/491)
- Check if workspace backup performed on docker service restart or stop [\#482](https://github.com/codenvy/codenvy/issues/482)
- Investigate possible ways to integrate support of AWS ECR dynamic passwords in Codenvy [\#463](https://github.com/codenvy/codenvy/issues/463)
- Describe configuration changes for 4.6 release [\#432](https://github.com/codenvy/codenvy/issues/432)
- Validate options of Codenvy bootstrap script [\#404](https://github.com/codenvy/codenvy/issues/404)
- Convert Chrome Store listing to beta.codenvy.com [\#402](https://github.com/codenvy/codenvy/issues/402)
- Create JPA based CommonPermissionsStorage [\#395](https://github.com/codenvy/codenvy/issues/395)
- Adding documentation to use alternative IDE [\#369](https://github.com/codenvy/codenvy/issues/369)

**Pull requests merged:**

- \(\#464\) update /etc/hosts when changing Codenvy hostname [\#517](https://github.com/codenvy/codenvy/pull/517) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-2033 Remove Che Git Provider module [\#513](https://github.com/codenvy/codenvy/pull/513) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Add ability to store empty codenvy properties [\#511](https://github.com/codenvy/codenvy/pull/511) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix tests to conform ErrorProne rules [\#510](https://github.com/codenvy/codenvy/pull/510) ([dmytro-ndp](https://github.com/dmytro-ndp))
- remove an old assets [\#506](https://github.com/codenvy/codenvy/pull/506) ([vkuznyetsov](https://github.com/vkuznyetsov))
- fixup! \(\#404\) validate options of Codenvy bootstrap script [\#503](https://github.com/codenvy/codenvy/pull/503) ([dmytro-ndp](https://github.com/dmytro-ndp))
- \(\#496\) add Codenvy on-prem 4.6.0 properies and migration script [\#497](https://github.com/codenvy/codenvy/pull/497) ([dmytro-ndp](https://github.com/dmytro-ndp))
- \(\#404\) validate options of Codenvy bootstrap script [\#495](https://github.com/codenvy/codenvy/pull/495) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1989 Fix permissions checking in according to new namespace concept [\#486](https://github.com/codenvy/codenvy/pull/486) ([sleshchenko](https://github.com/sleshchenko))
- corrected javadoc [\#363](https://github.com/codenvy/codenvy/pull/363) ([torzsmokus](https://github.com/torzsmokus))
- CHE-1369: rework UD to listen to environment channels [\#348](https://github.com/codenvy/codenvy/pull/348) ([ashumilova](https://github.com/ashumilova))

## [4.6.0](https://github.com/codenvy/codenvy/tree/4.6.0) (2016-08-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.5.1...4.6.0)

**Issues with no labels:**

- Impossibele to create pull request with plugin into own repository. [\#483](https://github.com/codenvy/codenvy/issues/483)
- Unexpected disappearense project from the project explorer [\#481](https://github.com/codenvy/codenvy/issues/481)
- Can't install version 4.4.0 [\#477](https://github.com/codenvy/codenvy/issues/477)
- upload file to the project [\#439](https://github.com/codenvy/codenvy/issues/439)
- hi new problem with my bot [\#438](https://github.com/codenvy/codenvy/issues/438)
- Remove beta designation on hosted C4 systems [\#436](https://github.com/codenvy/codenvy/issues/436)
- Control-C has stopped working [\#429](https://github.com/codenvy/codenvy/issues/429)
- Shared workspace doesn't refresh automatically [\#422](https://github.com/codenvy/codenvy/issues/422)
- codenvy config --hostname=${hostname} fails [\#420](https://github.com/codenvy/codenvy/issues/420)
- help plz cant find my data when added some git [\#417](https://github.com/codenvy/codenvy/issues/417)
- Sometimes machine.ws\_agent.run\_command can't execute properly [\#405](https://github.com/codenvy/codenvy/issues/405)
- PreferenceDao implementation on JPA [\#393](https://github.com/codenvy/codenvy/issues/393)
- Codenvy standalone server. Workspaces creation hanging. [\#390](https://github.com/codenvy/codenvy/issues/390)
- How to run python script? [\#385](https://github.com/codenvy/codenvy/issues/385)
- OAuth with github appears to be broken [\#357](https://github.com/codenvy/codenvy/issues/357)
- Maven dependencies not resolved by IDE [\#351](https://github.com/codenvy/codenvy/issues/351)
- Release and ship codenvy 4.6.0 [\#496](https://github.com/codenvy/codenvy/issues/496)
- Backup subs-system removes projects of workspace in some cases [\#488](https://github.com/codenvy/codenvy/issues/488)
- Milestone Summary 4.6.0 [\#478](https://github.com/codenvy/codenvy/issues/478)
- Missing pull request panel [\#458](https://github.com/codenvy/codenvy/issues/458)
- On beta in logs \[ERROR\] \[m.b.WorkspaceFsBackupScheduler 84\] - Machine machine???? is not found [\#454](https://github.com/codenvy/codenvy/issues/454)
- Codenvy 4.6.0 on-prem integration test of backup/restore failed [\#452](https://github.com/codenvy/codenvy/issues/452)
- Methods "updateAttributesById" and "updateAttributes" in the ProfileService clean up user lastName in some case of partial update [\#451](https://github.com/codenvy/codenvy/issues/451)
- VSTS projects does cloning failed [\#449](https://github.com/codenvy/codenvy/issues/449)
- Vagrant installer uses wrong proxy settings [\#440](https://github.com/codenvy/codenvy/issues/440)
- Unable to create user by Admin Dashbord [\#435](https://github.com/codenvy/codenvy/issues/435)
- Cloning a VSTS repo feature does not work [\#431](https://github.com/codenvy/codenvy/issues/431)
- Sourcegraph factory examples from getting-started page works wrong [\#430](https://github.com/codenvy/codenvy/issues/430)
- Marketo interceptors doesn't send data to the Marketo [\#427](https://github.com/codenvy/codenvy/issues/427)
- Bitbucket oAuth is broken [\#426](https://github.com/codenvy/codenvy/issues/426)
- Investigate exception  'Machine with name ws-machine already exists' [\#411](https://github.com/codenvy/codenvy/issues/411)
- Change default dns http://codenvy to any dns which contain dot symbol [\#407](https://github.com/codenvy/codenvy/issues/407)
- Make sure  "Delete this account" Button actually deletes this account [\#382](https://github.com/codenvy/codenvy/issues/382)

**Pull requests merged:**

- CODENVY-488: fix bug in backup manager, update tests [\#490](https://github.com/codenvy/codenvy/pull/490) ([garagatyi](https://github.com/garagatyi))
- \(\#407\) fix codenvy 3.x integration tests [\#487](https://github.com/codenvy/codenvy/pull/487) ([dmytro-ndp](https://github.com/dmytro-ndp))
- \(\#407\) change default dns 'codenvy' to 'codenvy.onprem' [\#485](https://github.com/codenvy/codenvy/pull/485) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-483; fix PR creation from own repo by sending name of origin [\#484](https://github.com/codenvy/codenvy/pull/484) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-458; fix plugin pullrequest appearing; [\#462](https://github.com/codenvy/codenvy/pull/462) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-454: Remove error log when machine was stopped while its backup task was in executor queue [\#461](https://github.com/codenvy/codenvy/pull/461) ([mmorhun](https://github.com/mmorhun))
- codenvy-451: Fix clean up 'lastName' profile attribute. [\#460](https://github.com/codenvy/codenvy/pull/460) ([AndrienkoAleksandr](https://github.com/AndrienkoAleksandr))
- CODENVY-449; fix VSTS project URL matching regexp [\#450](https://github.com/codenvy/codenvy/pull/450) ([mshaposhnik](https://github.com/mshaposhnik))
- CHE-1849 Fix according to changes in Workspace API [\#448](https://github.com/codenvy/codenvy/pull/448) ([vinokurig](https://github.com/vinokurig))
- CODENVY-411: Fix bug machine with name ws-machine already exists [\#446](https://github.com/codenvy/codenvy/pull/446) ([mmorhun](https://github.com/mmorhun))
- codenvy-435: Fix user creation by Admin dashboard [\#437](https://github.com/codenvy/codenvy/pull/437) ([AndrienkoAleksandr](https://github.com/AndrienkoAleksandr))
- CODENVY-601: Do not run new backup/restore process when another one is still running [\#425](https://github.com/codenvy/codenvy/pull/425) ([mmorhun](https://github.com/mmorhun))
- Fix test to not set workspaceId into environment context [\#424](https://github.com/codenvy/codenvy/pull/424) ([mshaposhnik](https://github.com/mshaposhnik))
- Fix update IM CLI message [\#423](https://github.com/codenvy/codenvy/pull/423) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-413: Make it possible to mark MachineNode as 'sheduled for maintenace' [\#418](https://github.com/codenvy/codenvy/pull/418) ([mmorhun](https://github.com/mmorhun))
- Use Model.gwt.xml see https://github.com/eclipse/che/pull/1869 [\#415](https://github.com/codenvy/codenvy/pull/415) ([vparfonov](https://github.com/vparfonov))
- CHE-1248 remove workspace information from EnvironmentContext [\#350](https://github.com/codenvy/codenvy/pull/350) ([mshaposhnik](https://github.com/mshaposhnik))
- Updated to support 4.4.x proxy installation [\#441](https://github.com/codenvy/codenvy/pull/441) ([TylerJewell](https://github.com/TylerJewell))
- CODENVY-572: Add exception to method signature [\#421](https://github.com/codenvy/codenvy/pull/421) ([vinokurig](https://github.com/vinokurig))
- CODENVY-509: remove current user instead of sending email [\#381](https://github.com/codenvy/codenvy/pull/381) ([ashumilova](https://github.com/ashumilova))
- CODENVY-651 Use 'dev' instead 'isDev' for machine configs in Mongo [\#356](https://github.com/codenvy/codenvy/pull/356) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Update accordingly to changes in everrest [\#341](https://github.com/codenvy/codenvy/pull/341) ([mkuznyetsov](https://github.com/mkuznyetsov))

## [4.5.1](https://github.com/codenvy/codenvy/tree/4.5.1) (2016-07-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.4.2...4.5.1)

**Issues with no labels:**

- Study how to integrate with customers ldap [\#399](https://github.com/codenvy/codenvy/issues/399)
- Create list of all entries that connected to user [\#387](https://github.com/codenvy/codenvy/issues/387)
- Workspace should update project by path instead by name [\#383](https://github.com/codenvy/codenvy/issues/383)
- Running a Java Applet on Codenvy's android emulator [\#366](https://github.com/codenvy/codenvy/issues/366)
- Cannot create a workspace nor a project [\#365](https://github.com/codenvy/codenvy/issues/365)
- Managing machine snapshots [\#349](https://github.com/codenvy/codenvy/issues/349)
- Existing workspaces taking 10+ minutes to load after being stopped [\#303](https://github.com/codenvy/codenvy/issues/303)

**Pull requests merged:**

- CODENVY-726: fix error of waiting on aliveness of Codenvy behind the proxy. [\#384](https://github.com/codenvy/codenvy/pull/384) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-702: check availability of Codenvy hostname [\#380](https://github.com/codenvy/codenvy/pull/380) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-718: add bootstrap script option to disable monitoring tools [\#379](https://github.com/codenvy/codenvy/pull/379) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-756: replace comma separator on pipeline in -Dhttp.nonProxyHosts [\#378](https://github.com/codenvy/codenvy/pull/378) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-726: fix checking if Codenvy on-prem is alive behind the proxy [\#377](https://github.com/codenvy/codenvy/pull/377) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-673: Fix IndexOutOfBoundsException [\#376](https://github.com/codenvy/codenvy/pull/376) ([vinokurig](https://github.com/vinokurig))
- Revert "CODENVY-413: Make it possible to mark MachineNode as 'sheduled for maintenace'" [\#375](https://github.com/codenvy/codenvy/pull/375) ([mmorhun](https://github.com/mmorhun))
- CODENVY-726: fix Codenvy on-prem apiEndpoint url [\#374](https://github.com/codenvy/codenvy/pull/374) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Update default Codenvy on-prem properties [\#373](https://github.com/codenvy/codenvy/pull/373) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-413: Make it possible to mark MachineNode as 'sheduled for maintenace' [\#372](https://github.com/codenvy/codenvy/pull/372) ([mmorhun](https://github.com/mmorhun))
- CODENVY-418: fix test to create project on node behind the proxy [\#371](https://github.com/codenvy/codenvy/pull/371) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-556 Add service for fetching recipe script [\#370](https://github.com/codenvy/codenvy/pull/370) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CHE-1397: change styles of error page [\#368](https://github.com/codenvy/codenvy/pull/368) ([ashumilova](https://github.com/ashumilova))
- Codenvy 361 [\#367](https://github.com/codenvy/codenvy/pull/367) ([garagatyi](https://github.com/garagatyi))
- Fix integration test of adding nodes behind the proxy [\#364](https://github.com/codenvy/codenvy/pull/364) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Restore user creation url in integration tests of Codenvy 3.x [\#362](https://github.com/codenvy/codenvy/pull/362) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add 'auth.sso.client\_allow\_anonymous=false' property [\#361](https://github.com/codenvy/codenvy/pull/361) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix user creation url in integration tests of IM CLI [\#360](https://github.com/codenvy/codenvy/pull/360) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-418: make Codenvy machine node add/remove behind the proxy [\#359](https://github.com/codenvy/codenvy/pull/359) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1390: remove projects from main menu [\#358](https://github.com/codenvy/codenvy/pull/358) ([ashumilova](https://github.com/ashumilova))
- Fix share workspace: getting workspace id, find user method, do not t… [\#355](https://github.com/codenvy/codenvy/pull/355) ([ashumilova](https://github.com/ashumilova))

## [4.4.2](https://github.com/codenvy/codenvy/tree/4.4.2) (2016-07-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.4.1...4.4.2)

**Issues with no labels:**

- Copy/paste not working in command configuration dialog \(Chrome only\) [\#354](https://github.com/codenvy/codenvy/issues/354)

**Pull requests merged:**

- CHE-1144: fix in accordance with changes in che [\#353](https://github.com/codenvy/codenvy/pull/353) ([garagatyi](https://github.com/garagatyi))
- CODENVY-720: Fix snapshot recovering [\#352](https://github.com/codenvy/codenvy/pull/352) ([garagatyi](https://github.com/garagatyi))

## [4.4.1](https://github.com/codenvy/codenvy/tree/4.4.1) (2016-07-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.4.0...4.4.1)

**Issues with no labels:**

- Some Enforcer rules have failed. Look above for specific messages explaining why the rule failed. [\#339](https://github.com/codenvy/codenvy/issues/339)

**Pull requests merged:**

- CODENVY-520 Remove allow\_anonymous from war configuration [\#347](https://github.com/codenvy/codenvy/pull/347) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CHE-1353: Add possibility to configure specific docker api version [\#346](https://github.com/codenvy/codenvy/pull/346) ([AndrienkoAleksandr](https://github.com/AndrienkoAleksandr))
- Resource management [\#345](https://github.com/codenvy/codenvy/pull/345) ([vzhukovskii](https://github.com/vzhukovskii))
- CHE-1365: remove usage of deprecated methods [\#344](https://github.com/codenvy/codenvy/pull/344) ([garagatyi](https://github.com/garagatyi))
- CHE-1395: fix browser titles [\#343](https://github.com/codenvy/codenvy/pull/343) ([ashumilova](https://github.com/ashumilova))
- CODENVY-706: check if there is subscription-manager command in RHEL OS [\#342](https://github.com/codenvy/codenvy/pull/342) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-575; cleanup environment context after each request lifecycle [\#340](https://github.com/codenvy/codenvy/pull/340) ([mshaposhnik](https://github.com/mshaposhnik))
- CHE-1256: Set Jgit implementation as default Git implementation [\#338](https://github.com/codenvy/codenvy/pull/338) ([vinokurig](https://github.com/vinokurig))
- CODENVY-84: work around default node of Codenvy on-prem [\#337](https://github.com/codenvy/codenvy/pull/337) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix update IM CLI message [\#336](https://github.com/codenvy/codenvy/pull/336) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-702: fix command which hangs up when changing Codenvy hostname [\#335](https://github.com/codenvy/codenvy/pull/335) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add default properties of Codenvy on-prem 4.4.0 [\#334](https://github.com/codenvy/codenvy/pull/334) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1298; remove war name from url schema [\#324](https://github.com/codenvy/codenvy/pull/324) ([mshaposhnik](https://github.com/mshaposhnik))
- CHE-1078: Adapt code to user service refactorings [\#243](https://github.com/codenvy/codenvy/pull/243) ([evoevodin](https://github.com/evoevodin))

## [4.4.0](https://github.com/codenvy/codenvy/tree/4.4.0) (2016-06-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.5...4.4.0)

**Issues with no labels:**

- Prompt message "Unable to get Profile" when I open IDE\(Codenvy \(Next-Generation Beta\)\) [\#333](https://github.com/codenvy/codenvy/issues/333)

**Pull requests merged:**

- CHE-1396 Double nav bar in some cases [\#332](https://github.com/codenvy/codenvy/pull/332) ([vitaliy-guliy](https://github.com/vitaliy-guliy))
- Fix tear down steps on exit in IM CLI integration tests [\#331](https://github.com/codenvy/codenvy/pull/331) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1276: Move DockerException to package exception [\#330](https://github.com/codenvy/codenvy/pull/330) ([akorneta](https://github.com/akorneta))
- CODENVY-309: simplify verification of RHEL subscription [\#329](https://github.com/codenvy/codenvy/pull/329) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-635: Create attachment files in separate temp directory [\#275](https://github.com/codenvy/codenvy/pull/275) ([vinokurig](https://github.com/vinokurig))

## [4.3.5](https://github.com/codenvy/codenvy/tree/4.3.5) (2016-06-24)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.4...4.3.5)

**Pull requests merged:**

- CHE-1277 Move machines output to project perpective [\#328](https://github.com/codenvy/codenvy/pull/328) ([vitaliy-guliy](https://github.com/vitaliy-guliy))
- Restore update server address for integration tests [\#327](https://github.com/codenvy/codenvy/pull/327) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-701: fix Codenvy version where the path is changed [\#326](https://github.com/codenvy/codenvy/pull/326) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Switch to temporary address updater-nightly2.codenvy-dev.com [\#325](https://github.com/codenvy/codenvy/pull/325) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-701: adapt IM CLI to use tomcat directory of Codenvy on-prem [\#323](https://github.com/codenvy/codenvy/pull/323) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix redirect to site login page on logout [\#321](https://github.com/codenvy/codenvy/pull/321) ([ashumilova](https://github.com/ashumilova))
- CODENVY-614: rename folder che-templates to templates [\#320](https://github.com/codenvy/codenvy/pull/320) ([riuvshin](https://github.com/riuvshin))
- Codenvy 694 Fix limits checking user on ws startup [\#319](https://github.com/codenvy/codenvy/pull/319) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-644: add --no-proxy-for-docker-daemon option to bootstrap script [\#318](https://github.com/codenvy/codenvy/pull/318) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Rework AdminUserServiceTests with everrest assured [\#317](https://github.com/codenvy/codenvy/pull/317) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-456: rename 'MACHINE' node type to 'MACHINE NODE' [\#316](https://github.com/codenvy/codenvy/pull/316) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1180: add ability to configure private docker registries [\#315](https://github.com/codenvy/codenvy/pull/315) ([olexii4](https://github.com/olexii4))
- Fix default properties of Codenvy 4.4.0 on-prem [\#314](https://github.com/codenvy/codenvy/pull/314) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-309: check subscription of RHEL when installing Codenvy on-prem [\#313](https://github.com/codenvy/codenvy/pull/313) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add debug information in log to track anonymous user creation [\#312](https://github.com/codenvy/codenvy/pull/312) ([skabashnyuk](https://github.com/skabashnyuk))
- CODENVY-677: fix factories loading [\#311](https://github.com/codenvy/codenvy/pull/311) ([akurinnoy](https://github.com/akurinnoy))
- CHE-1296: Use docker auth config saved in user preferences to start machine from private image [\#310](https://github.com/codenvy/codenvy/pull/310) ([mmorhun](https://github.com/mmorhun))
- Ship Codenvy on-prem 4.3.4 [\#308](https://github.com/codenvy/codenvy/pull/308) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1301: add not modified condition to the machine token response [\#307](https://github.com/codenvy/codenvy/pull/307) ([olexii4](https://github.com/olexii4))
- CHE-1342: improve navbar styles [\#301](https://github.com/codenvy/codenvy/pull/301) ([olexii4](https://github.com/olexii4))
- add che.lib.version property [\#291](https://github.com/codenvy/codenvy/pull/291) ([riuvshin](https://github.com/riuvshin))

## [4.3.4](https://github.com/codenvy/codenvy/tree/4.3.4) (2016-06-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.3...4.3.4)

## [4.3.3](https://github.com/codenvy/codenvy/tree/4.3.3) (2016-06-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.2...4.3.3)

**Pull requests merged:**

- fix UD transitive dependency of gulp-angular-templatecache [\#306](https://github.com/codenvy/codenvy/pull/306) ([olexii4](https://github.com/olexii4))
- CODENVY-667 Add 'search' action for stacks and recipes [\#305](https://github.com/codenvy/codenvy/pull/305) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-666: Fix accepting of the factories by the existing URLs [\#304](https://github.com/codenvy/codenvy/pull/304) ([akorneta](https://github.com/akorneta))
- CODENVY-446: fix script to migrate Codenvy 4.2.2 to 4.3.2 [\#302](https://github.com/codenvy/codenvy/pull/302) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1326: check recipe location exists before setting permissions [\#300](https://github.com/codenvy/codenvy/pull/300) ([ashumilova](https://github.com/ashumilova))
- CODENVY-547: fix error of changing of non-default admin password [\#299](https://github.com/codenvy/codenvy/pull/299) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-466: fix migrate snapshot script to work around incomplete data [\#298](https://github.com/codenvy/codenvy/pull/298) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-446: update codenvy migration script; fix integration tests. [\#296](https://github.com/codenvy/codenvy/pull/296) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1095: add new UD/IDE navigation experience [\#295](https://github.com/codenvy/codenvy/pull/295) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-446: add mongo script to update snapshots in time of migration [\#294](https://github.com/codenvy/codenvy/pull/294) ([dmytro-ndp](https://github.com/dmytro-ndp))

## [4.3.2](https://github.com/codenvy/codenvy/tree/4.3.2) (2016-06-14)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.1...4.3.2)

**Pull requests merged:**

- CODENVY-653: fix error of logging event by using Update Server [\#293](https://github.com/codenvy/codenvy/pull/293) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix creation of user in integration test of backup/restore Codenvy 4 [\#292](https://github.com/codenvy/codenvy/pull/292) ([dmytro-ndp](https://github.com/dmytro-ndp))

## [4.3.1](https://github.com/codenvy/codenvy/tree/4.3.1) (2016-06-14)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.0...4.3.1)

**Pull requests merged:**

- CODENVY-446: fix migration script and default codenvy properties [\#290](https://github.com/codenvy/codenvy/pull/290) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-648 Add swagger annotations for Permissions API [\#289](https://github.com/codenvy/codenvy/pull/289) ([sleshchenko](https://github.com/sleshchenko))

## [4.3.0](https://github.com/codenvy/codenvy/tree/4.3.0) (2016-06-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.3.0-RC1...4.3.0)

**Pull requests merged:**

- fix typo, make file not binary [\#288](https://github.com/codenvy/codenvy/pull/288) ([riuvshin](https://github.com/riuvshin))
- Add docker property into Codenvy on-prem config [\#287](https://github.com/codenvy/codenvy/pull/287) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix test of backup/restore Codenvy on-prem 4.x [\#286](https://github.com/codenvy/codenvy/pull/286) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-480 Fix creation of user [\#285](https://github.com/codenvy/codenvy/pull/285) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-619 Use UserNameValidator non-static methods [\#284](https://github.com/codenvy/codenvy/pull/284) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CHE-1315: remove 'Team' from dashboard [\#283](https://github.com/codenvy/codenvy/pull/283) ([olexii4](https://github.com/olexii4))
- CODENVY-631: divide bootstrap script proxy options [\#282](https://github.com/codenvy/codenvy/pull/282) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-446: add scripts and tools to update Codenvy 4.2.x to 4.3.0 [\#281](https://github.com/codenvy/codenvy/pull/281) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1245: Update docker client to use new format of X-Registry-Config header [\#280](https://github.com/codenvy/codenvy/pull/280) ([mmorhun](https://github.com/mmorhun))
- CODENVY-557: rework dashboard for using system permissions instead of… [\#279](https://github.com/codenvy/codenvy/pull/279) ([olexii4](https://github.com/olexii4))
- CODENVY-641: Add factory permission filter [\#278](https://github.com/codenvy/codenvy/pull/278) ([akorneta](https://github.com/akorneta))
- Replace missing exception [\#277](https://github.com/codenvy/codenvy/pull/277) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Revert machine token interceptor [\#276](https://github.com/codenvy/codenvy/pull/276) ([akorneta](https://github.com/akorneta))
- Replace codenvy-dev.com SaaS server on a1.codenvy-dev.com [\#274](https://github.com/codenvy/codenvy/pull/274) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix maven groupid for wsagent artifact [\#273](https://github.com/codenvy/codenvy/pull/273) ([akorneta](https://github.com/akorneta))
- CODENVY-27; remove interceptor as it is not needed anymore [\#272](https://github.com/codenvy/codenvy/pull/272) ([mshaposhnik](https://github.com/mshaposhnik))
- CHE-1222: add ability to share workspace with other users [\#271](https://github.com/codenvy/codenvy/pull/271) ([ashumilova](https://github.com/ashumilova))
- Fix IM test of update single node with Codenvy 4 [\#270](https://github.com/codenvy/codenvy/pull/270) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-27; allow to open workspaces by using /ws/namespace/ws\_name schema [\#269](https://github.com/codenvy/codenvy/pull/269) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-610: rename '--no-proxy' option on '--no-proxy-for-codenvy-wo… [\#266](https://github.com/codenvy/codenvy/pull/266) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-317: add ability to install IM CLI into the custom directory [\#265](https://github.com/codenvy/codenvy/pull/265) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Remove deperecated version of 3.x analytics [\#264](https://github.com/codenvy/codenvy/pull/264) ([skabashnyuk](https://github.com/skabashnyuk))
- CODENVY-619 Restrict usernames to letters and digits only [\#263](https://github.com/codenvy/codenvy/pull/263) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-610: add --no-proxy option to bootstrap script [\#262](https://github.com/codenvy/codenvy/pull/262) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-610: add possibility to config no\_proxy property [\#261](https://github.com/codenvy/codenvy/pull/261) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-583: Fix JavaDoc feature [\#260](https://github.com/codenvy/codenvy/pull/260) ([akorneta](https://github.com/akorneta))
- CODENVY-617: add property of docker privilege mode [\#259](https://github.com/codenvy/codenvy/pull/259) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-568: Fire FactoryAccepted Event then factory accepted: poject imported, actions perform    [\#258](https://github.com/codenvy/codenvy/pull/258) ([vparfonov](https://github.com/vparfonov))
- Update default codenvy on-prem properties to 4.3.0-R2-SNAPSHOT [\#257](https://github.com/codenvy/codenvy/pull/257) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-595: add possibility to adjust machine swap size [\#267](https://github.com/codenvy/codenvy/pull/267) ([riuvshin](https://github.com/riuvshin))
- CODENVY-566 restore \(again and again\) CORS on platform API [\#256](https://github.com/codenvy/codenvy/pull/256) ([benoitf](https://github.com/benoitf))
- CODENVY-603 CODENVY-604 allow to use .codenvy.json inside a repository [\#255](https://github.com/codenvy/codenvy/pull/255) ([benoitf](https://github.com/benoitf))
- CODENVY-445. Add ability to register an ssh target [\#254](https://github.com/codenvy/codenvy/pull/254) ([RomanNikitenko](https://github.com/RomanNikitenko))
- CODENVY-27; adapt machine sevice to workspace sharing [\#253](https://github.com/codenvy/codenvy/pull/253) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-513: Replace hardcoded email sender to property [\#252](https://github.com/codenvy/codenvy/pull/252) ([vinokurig](https://github.com/vinokurig))
- CODENVY-495: Replaced api token to machine token in sso principal [\#251](https://github.com/codenvy/codenvy/pull/251) ([akorneta](https://github.com/akorneta))
- CODENVY-480 Remove roles from User in EnvironmentContext [\#221](https://github.com/codenvy/codenvy/pull/221) ([sleshchenko](https://github.com/sleshchenko))

## [4.3.0-RC1](https://github.com/codenvy/codenvy/tree/4.3.0-RC1) (2016-06-04)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.2.3...4.3.0-RC1)

**Pull requests merged:**

- CHE-1269: Add check for machine runtime during links formation [\#250](https://github.com/codenvy/codenvy/pull/250) ([akorneta](https://github.com/akorneta))
- CHE-1260 Enable svg files license checking [\#249](https://github.com/codenvy/codenvy/pull/249) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-594 Log cleanup  [\#248](https://github.com/codenvy/codenvy/pull/248) ([skabashnyuk](https://github.com/skabashnyuk))
- Fix service which returns IM server config [\#247](https://github.com/codenvy/codenvy/pull/247) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-584: Add terminal link into each machine runtime [\#246](https://github.com/codenvy/codenvy/pull/246) ([akorneta](https://github.com/akorneta))
- Codenvy-27 Adopt token service to workspace sharing [\#245](https://github.com/codenvy/codenvy/pull/245) ([mshaposhnik](https://github.com/mshaposhnik))
- remove needless dependencies to site versions [\#244](https://github.com/codenvy/codenvy/pull/244) ([garagatyi](https://github.com/garagatyi))
- CODENVY-27; Adopt activity service to workspace sharing [\#242](https://github.com/codenvy/codenvy/pull/242) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-40: add pagination to list of users [\#240](https://github.com/codenvy/codenvy/pull/240) ([olexii4](https://github.com/olexii4))
- Fix 'email\_from' codenvy property value [\#239](https://github.com/codenvy/codenvy/pull/239) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-560 Adapt changes to new management of MachineSource in Che [\#238](https://github.com/codenvy/codenvy/pull/238) ([benoitf](https://github.com/benoitf))
- CODENVY-550 Remove nonencoded factory from LoginFilter configuration [\#237](https://github.com/codenvy/codenvy/pull/237) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Replace codenvy-stg.com on codenvy-dev.com in integration tests [\#236](https://github.com/codenvy/codenvy/pull/236) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-515: remove prefix 'im-' and hide outdated commands from IM CLI [\#235](https://github.com/codenvy/codenvy/pull/235) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-336: Refactoring of LocalGitUserResolver class [\#234](https://github.com/codenvy/codenvy/pull/234) ([vinokurig](https://github.com/vinokurig))
- CHE-336: Refactoring of GitUrl class [\#233](https://github.com/codenvy/codenvy/pull/233) ([vinokurig](https://github.com/vinokurig))
- CODENVY-511: update codenvy.properties file in case of --config option; add test [\#232](https://github.com/codenvy/codenvy/pull/232) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-348 Decode key in workspaceId init filter [\#231](https://github.com/codenvy/codenvy/pull/231) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Fix WorkspaceService to be able to share workspaces [\#230](https://github.com/codenvy/codenvy/pull/230) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-512: fix IM integration test of checking IM config [\#228](https://github.com/codenvy/codenvy/pull/228) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-511: add --config option to point custom codenvy.properties [\#227](https://github.com/codenvy/codenvy/pull/227) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-797: correct switch to mobile view for list of factories [\#214](https://github.com/codenvy/codenvy/pull/214) ([akurinnoy](https://github.com/akurinnoy))
- Add an authentication mechanism for workspace agent [\#188](https://github.com/codenvy/codenvy/pull/188) ([akorneta](https://github.com/akorneta))
- Added tests for WorkspaceActivityService [\#133](https://github.com/codenvy/codenvy/pull/133) ([mkuznyetsov](https://github.com/mkuznyetsov))

## [4.2.3](https://github.com/codenvy/codenvy/tree/4.2.3) (2016-05-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.2.2...4.2.3)

**Pull requests merged:**

- CODENVY-524: Update contribute button style [\#225](https://github.com/codenvy/codenvy/pull/225) ([slemeur](https://github.com/slemeur))
- CODENVY-512: add ability to view/update codenvy property by using IM CLI command [\#223](https://github.com/codenvy/codenvy/pull/223) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-164: add integrations tests of IM CLI under RHEL7 OS [\#222](https://github.com/codenvy/codenvy/pull/222) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-555: turn off reporting in Puppet in Codenvy on-prem [\#220](https://github.com/codenvy/codenvy/pull/220) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-554 if factory has an ID, no need to give again parameters as… [\#219](https://github.com/codenvy/codenvy/pull/219) ([benoitf](https://github.com/benoitf))
- CODENVY-522 Remove EnvironmentContext related classes [\#218](https://github.com/codenvy/codenvy/pull/218) ([skabashnyuk](https://github.com/skabashnyuk))
- CODENVY-541: Add needed dependency. Install DebugModule [\#217](https://github.com/codenvy/codenvy/pull/217) ([vparfonov](https://github.com/vparfonov))
- CODENVY-516: modify 'Job title' input with the 'Role' drop down list [\#216](https://github.com/codenvy/codenvy/pull/216) ([olexii4](https://github.com/olexii4))
- CODENVY-549 Remove invalid settings found in pom.xml [\#215](https://github.com/codenvy/codenvy/pull/215) ([benoitf](https://github.com/benoitf))
- CODENVY-543: replace '&' on '\&' in right part of sed command [\#213](https://github.com/codenvy/codenvy/pull/213) ([dmytro-ndp](https://github.com/dmytro-ndp))
- remove dependency on che-plugin-java-ext-lang-server [\#212](https://github.com/codenvy/codenvy/pull/212) ([evidolob](https://github.com/evidolob))
- CHE-956 remove dependency on che-jdt-ext-machine [\#211](https://github.com/codenvy/codenvy/pull/211) ([evidolob](https://github.com/evidolob))
- CHE-962: Add ability to store and use SSH keys for Subversion hosts [\#210](https://github.com/codenvy/codenvy/pull/210) ([tolusha](https://github.com/tolusha))
- Fix integration test of backup/restore Codenvy 4.x [\#209](https://github.com/codenvy/codenvy/pull/209) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-531: cleanup init.js [\#208](https://github.com/codenvy/codenvy/pull/208) ([vparfonov](https://github.com/vparfonov))
- Add binding for components separated in che [\#207](https://github.com/codenvy/codenvy/pull/207) ([akorneta](https://github.com/akorneta))
- CODENVY-357: place CLI logs into the same file \<cli\>/logs/cli.log [\#206](https://github.com/codenvy/codenvy/pull/206) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-510: support for Mirroring Docker Registry [\#205](https://github.com/codenvy/codenvy/pull/205) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1083: Refactor modules registration [\#204](https://github.com/codenvy/codenvy/pull/204) ([azatsarynnyy](https://github.com/azatsarynnyy))
- Add parameters for auto-snapshot and auto-restore of the workspace state [\#203](https://github.com/codenvy/codenvy/pull/203) ([akorneta](https://github.com/akorneta))
- CHE-1081: remove workspace id usage in wsagent services [\#202](https://github.com/codenvy/codenvy/pull/202) ([ashumilova](https://github.com/ashumilova))
- Codenvy 198 [\#201](https://github.com/codenvy/codenvy/pull/201) ([garagatyi](https://github.com/garagatyi))
- CODENVY-503: fix path to codenvy conf dir [\#200](https://github.com/codenvy/codenvy/pull/200) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-522 Remove roles context [\#199](https://github.com/codenvy/codenvy/pull/199) ([skabashnyuk](https://github.com/skabashnyuk))
- CODENVY-473: add IM unit tests; fix integration tests [\#198](https://github.com/codenvy/codenvy/pull/198) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-479 Rename User to Subject [\#197](https://github.com/codenvy/codenvy/pull/197) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-502 allow to create factories by providing github URL [\#195](https://github.com/codenvy/codenvy/pull/195) ([benoitf](https://github.com/benoitf))
- Codenvy 473: fix IM according to TIAA CREF requests [\#194](https://github.com/codenvy/codenvy/pull/194) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-292 Escape factory DB query attributes [\#193](https://github.com/codenvy/codenvy/pull/193) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Fix NPE and Removed unused code [\#192](https://github.com/codenvy/codenvy/pull/192) ([sleshchenko](https://github.com/sleshchenko))
- Fix tests [\#191](https://github.com/codenvy/codenvy/pull/191) ([sleshchenko](https://github.com/sleshchenko))
- CODENVY-473: add missed library for IM CLI [\#190](https://github.com/codenvy/codenvy/pull/190) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add Codenvy on-prem 4.2.2 default properties [\#189](https://github.com/codenvy/codenvy/pull/189) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-976: Make debugger API generic [\#180](https://github.com/codenvy/codenvy/pull/180) ([tolusha](https://github.com/tolusha))
- CODENVY-433 Implement permissions based authorization for Recipe and Stack APIs [\#163](https://github.com/codenvy/codenvy/pull/163) ([sleshchenko](https://github.com/sleshchenko))

## [4.2.2](https://github.com/codenvy/codenvy/tree/4.2.2) (2016-05-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.2.1...4.2.2)

**Pull requests merged:**

- CODENVY-478: fix installation manager to work without che-core-api-account [\#187](https://github.com/codenvy/codenvy/pull/187) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-781:Move all platform-api-client-gwt to the che-core-ide-api module [\#185](https://github.com/codenvy/codenvy/pull/185) ([vparfonov](https://github.com/vparfonov))
- Remove deprecated code from plugin-pullrequest [\#184](https://github.com/codenvy/codenvy/pull/184) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CHE-1026: rework wsagent usage [\#183](https://github.com/codenvy/codenvy/pull/183) ([ashumilova](https://github.com/ashumilova))
- CODENVY-308 remove references to status.codenvy.com [\#182](https://github.com/codenvy/codenvy/pull/182) ([vkuznyetsov](https://github.com/vkuznyetsov))
- CODENVY-478 remove che-core-api-account [\#181](https://github.com/codenvy/codenvy/pull/181) ([skabashnyuk](https://github.com/skabashnyuk))
- CHE-747:Move ProjectTemplateService to separate module [\#179](https://github.com/codenvy/codenvy/pull/179) ([vparfonov](https://github.com/vparfonov))
- CODENVY-473: IM: fix yum.conf, fix http\(s\)\_proxy settings [\#178](https://github.com/codenvy/codenvy/pull/178) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1061 Remove deprecations [\#177](https://github.com/codenvy/codenvy/pull/177) ([mkuznyetsov](https://github.com/mkuznyetsov))
- fixup! CODENVY-473: add proxy settings into 'user' section of puppet.conf [\#176](https://github.com/codenvy/codenvy/pull/176) ([dmytro-ndp](https://github.com/dmytro-ndp))
- fixup! CODENVY-473: add proxy settings into 'user' section of puppet.conf [\#175](https://github.com/codenvy/codenvy/pull/175) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-473: add proxy settings into 'user' section of puppet.conf [\#174](https://github.com/codenvy/codenvy/pull/174) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-473: fix install-im-cli script [\#173](https://github.com/codenvy/codenvy/pull/173) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Update code in accordance with Part API changes [\#172](https://github.com/codenvy/codenvy/pull/172) ([azatsarynnyy](https://github.com/azatsarynnyy))
- CODENVY-464: fixed fair source licens option and messages [\#171](https://github.com/codenvy/codenvy/pull/171) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix default properties of Codenvy 4.3.0-RC1-SNAPSHOT [\#170](https://github.com/codenvy/codenvy/pull/170) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Codenvy 473: fix Codenvy bootstrap script for TIAA CREF [\#169](https://github.com/codenvy/codenvy/pull/169) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Update in accordance with IDE API changes [\#168](https://github.com/codenvy/codenvy/pull/168) ([azatsarynnyy](https://github.com/azatsarynnyy))
- CODENVY-426 show dedicated message when clone fails due to lack of SSH key [\#167](https://github.com/codenvy/codenvy/pull/167) ([mshaposhnik](https://github.com/mshaposhnik))
- Fix eclipse jdt dependency, codenvy repacked one should be used [\#166](https://github.com/codenvy/codenvy/pull/166) ([mshaposhnik](https://github.com/mshaposhnik))
- Fix url for unregistering client [\#165](https://github.com/codenvy/codenvy/pull/165) ([sleshchenko](https://github.com/sleshchenko))
- Codenvy 455 - Add mouse & keyboard activity listener.  [\#164](https://github.com/codenvy/codenvy/pull/164) ([mshaposhnik](https://github.com/mshaposhnik))
- Navbar menu improvements [\#162](https://github.com/codenvy/codenvy/pull/162) ([akurinnoy](https://github.com/akurinnoy))
- CHE-253: Delete SwarmContainerInfo [\#160](https://github.com/codenvy/codenvy/pull/160) ([mmorhun](https://github.com/mmorhun))
- Move mvn config from assembly to main dir [\#159](https://github.com/codenvy/codenvy/pull/159) ([garagatyi](https://github.com/garagatyi))
- Fix test-backup-restore-single-node-with-codenvy4 [\#158](https://github.com/codenvy/codenvy/pull/158) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1076 Used commond source code from che wsagent [\#157](https://github.com/codenvy/codenvy/pull/157) ([skabashnyuk](https://github.com/skabashnyuk))
- Create Codenvy Terminal packaging [\#156](https://github.com/codenvy/codenvy/pull/156) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-273: fix TestSecureShellAgent [\#155](https://github.com/codenvy/codenvy/pull/155) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-353: fix integration test [\#154](https://github.com/codenvy/codenvy/pull/154) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Codenvy-275: improve validation of node to add [\#153](https://github.com/codenvy/codenvy/pull/153) ([dmytro-ndp](https://github.com/dmytro-ndp))
- add maven server to the assembly [\#152](https://github.com/codenvy/codenvy/pull/152) ([vparfonov](https://github.com/vparfonov))
- Decouple shared modules or user, machine, workspace, factory [\#150](https://github.com/codenvy/codenvy/pull/150) ([mshaposhnik](https://github.com/mshaposhnik))
- CHE-362. Changes according to adding display modes in  StatusNotification [\#109](https://github.com/codenvy/codenvy/pull/109) ([RomanNikitenko](https://github.com/RomanNikitenko))

## [4.2.1](https://github.com/codenvy/codenvy/tree/4.2.1) (2016-04-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.2.0...4.2.1)

**Pull requests merged:**

- CODENVY-353: improve checking sudo rights before installing Codenvy [\#151](https://github.com/codenvy/codenvy/pull/151) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-1070: Decouple server and shared part of che-core-api-git [\#149](https://github.com/codenvy/codenvy/pull/149) ([vinokurig](https://github.com/vinokurig))
- CHE-1069: Decouple server and shared part of che-core-api-ssh [\#148](https://github.com/codenvy/codenvy/pull/148) ([vinokurig](https://github.com/vinokurig))
- Add quotes around machine\_ws\_agent\_max\_start\_time\_ms value [\#147](https://github.com/codenvy/codenvy/pull/147) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix machine\_ws\_agent\_max\_start\_time\_ms codenvy property [\#146](https://github.com/codenvy/codenvy/pull/146) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-323: improve readability of stacks config [\#145](https://github.com/codenvy/codenvy/pull/145) ([ashumilova](https://github.com/ashumilova))
- Add default codenvy properties for 4.3.0-RC1-SNAPSHOT [\#144](https://github.com/codenvy/codenvy/pull/144) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add permissions authorization [\#138](https://github.com/codenvy/codenvy/pull/138) ([sleshchenko](https://github.com/sleshchenko))
- Codenvy 214 [\#137](https://github.com/codenvy/codenvy/pull/137) ([olexii4](https://github.com/olexii4))
- CODENVY-349 Rename workspace agent packaged war name [\#108](https://github.com/codenvy/codenvy/pull/108) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CHE-769: remove icon from factories [\#102](https://github.com/codenvy/codenvy/pull/102) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-272: change UI for password fields [\#100](https://github.com/codenvy/codenvy/pull/100) ([akurinnoy](https://github.com/akurinnoy))

## [4.2.0](https://github.com/codenvy/codenvy/tree/4.2.0) (2016-04-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.1.1...4.2.0)

**Pull requests merged:**

- Add login filter on workspace activity service [\#143](https://github.com/codenvy/codenvy/pull/143) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Add script to  migrate from  Codenvy 4.1.1 to 4.2.0 [\#142](https://github.com/codenvy/codenvy/pull/142) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-434 Adapt Che URL and port config [\#141](https://github.com/codenvy/codenvy/pull/141) ([stour](https://github.com/stour))
- Apply token manually on activity update request [\#140](https://github.com/codenvy/codenvy/pull/140) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Update IM CLI help command output [\#139](https://github.com/codenvy/codenvy/pull/139) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix wait workspace timeout in IM test [\#136](https://github.com/codenvy/codenvy/pull/136) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-417: fix export http\(s\) proxy settings to 'yum install puppet… command' [\#135](https://github.com/codenvy/codenvy/pull/135) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-296: remove wrong 'local' prefix [\#134](https://github.com/codenvy/codenvy/pull/134) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-350: add support of http\(s\)-proxy-for-codenvy options of bootstrap script. [\#132](https://github.com/codenvy/codenvy/pull/132) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-427 Update workspace activity only for running workspaces [\#131](https://github.com/codenvy/codenvy/pull/131) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-428 Improve workspace activity notification [\#130](https://github.com/codenvy/codenvy/pull/130) ([mkuznyetsov](https://github.com/mkuznyetsov))
- Codenvy 417: refactoring; add unit tests. [\#129](https://github.com/codenvy/codenvy/pull/129) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-771: change not configured oAuth message [\#128](https://github.com/codenvy/codenvy/pull/128) ([ashumilova](https://github.com/ashumilova))
- use che-steps-container directive [\#127](https://github.com/codenvy/codenvy/pull/127) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-417: fix error of installing puppet as a root user [\#126](https://github.com/codenvy/codenvy/pull/126) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-423: fixed swarm nodes info parsing [\#125](https://github.com/codenvy/codenvy/pull/125) ([garagatyi](https://github.com/garagatyi))
- CODENVY-363: modify error messages for dashboard [\#124](https://github.com/codenvy/codenvy/pull/124) ([olexii4](https://github.com/olexii4))
- Fix codenvy.properties; backup manifest before update Codenvy [\#123](https://github.com/codenvy/codenvy/pull/123) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-401: move a product info provider to plugin-product-info [\#122](https://github.com/codenvy/codenvy/pull/122) ([olexii4](https://github.com/olexii4))
- CODENVY-1938: Add error code & attributes for limit exception [\#121](https://github.com/codenvy/codenvy/pull/121) ([evoevodin](https://github.com/evoevodin))
- Fix test of backup/restore Codenvy on-prem 4.x [\#120](https://github.com/codenvy/codenvy/pull/120) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Revert "CHE-243: Return link header describing page links" [\#119](https://github.com/codenvy/codenvy/pull/119) ([evoevodin](https://github.com/evoevodin))
- CODENVY-350: Set up workspaces to work behind the proxy [\#118](https://github.com/codenvy/codenvy/pull/118) ([mmorhun](https://github.com/mmorhun))
- Fixed updating of runtime workspace in activity manager and formatting [\#117](https://github.com/codenvy/codenvy/pull/117) ([akorneta](https://github.com/akorneta))
- CODENVY-370: dashboard for OnPrem must not include the button "Codenv… [\#116](https://github.com/codenvy/codenvy/pull/116) ([akurinnoy](https://github.com/akurinnoy))
- CHE-243: Return link header describing page links [\#115](https://github.com/codenvy/codenvy/pull/115) ([evoevodin](https://github.com/evoevodin))
- CODENVY-409; project without sources must not clone separately [\#114](https://github.com/codenvy/codenvy/pull/114) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-408: fix setting codenvy properties from bootstrap script [\#113](https://github.com/codenvy/codenvy/pull/113) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-995: discover ssh machines architecture automaticaly [\#112](https://github.com/codenvy/codenvy/pull/112) ([garagatyi](https://github.com/garagatyi))
- Remove unused classes and code [\#111](https://github.com/codenvy/codenvy/pull/111) ([skabashnyuk](https://github.com/skabashnyuk))
- Fix PullRequest Plugin according chnages in GitServiceClient [\#110](https://github.com/codenvy/codenvy/pull/110) ([vparfonov](https://github.com/vparfonov))
- CODENVY-368: fix $java\_naming\_security\_principal when update from binary [\#107](https://github.com/codenvy/codenvy/pull/107) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Replace existed proxy settings; bootstrap script improvements. [\#106](https://github.com/codenvy/codenvy/pull/106) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-708: Remove usage of MachineExtensionProxyServlet [\#105](https://github.com/codenvy/codenvy/pull/105) ([vparfonov](https://github.com/vparfonov))

## [4.1.1](https://github.com/codenvy/codenvy/tree/4.1.1) (2016-04-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.1.0...4.1.1)

**Pull requests merged:**

- Skip importing project without sources in factory [\#104](https://github.com/codenvy/codenvy/pull/104) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-368: fix merge of properties, add scripts for migration [\#103](https://github.com/codenvy/codenvy/pull/103) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix java\_naming\_security\_principal property; add properties for 4.1.1 [\#101](https://github.com/codenvy/codenvy/pull/101) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-313: work around http\[s\]-proxy options of bootstrap script [\#99](https://github.com/codenvy/codenvy/pull/99) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-373 update saas, onprem email templates [\#98](https://github.com/codenvy/codenvy/pull/98) ([vkuznyetsov](https://github.com/vkuznyetsov))
- CODENY-367 restore openWelcomePage action and remove default one [\#97](https://github.com/codenvy/codenvy/pull/97) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-363: add error messages when user reach the RAM limits [\#96](https://github.com/codenvy/codenvy/pull/96) ([olexii4](https://github.com/olexii4))
- Che 889 [\#95](https://github.com/codenvy/codenvy/pull/95) ([garagatyi](https://github.com/garagatyi))
- CHE-930: fix build after changes in terminal [\#94](https://github.com/codenvy/codenvy/pull/94) ([garagatyi](https://github.com/garagatyi))
- CODENVY-257: Add Java and GDB debugger services [\#93](https://github.com/codenvy/codenvy/pull/93) ([tolusha](https://github.com/tolusha))
- CHE-975: remove using analytics api [\#92](https://github.com/codenvy/codenvy/pull/92) ([ashumilova](https://github.com/ashumilova))
- CODENVY-370: dashboard for OnPrem must not include the button "Codenv… [\#91](https://github.com/codenvy/codenvy/pull/91) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-276: get logo URL from branding component [\#90](https://github.com/codenvy/codenvy/pull/90) ([akurinnoy](https://github.com/akurinnoy))
- Use end HTML markup to avoid some missing tags [\#89](https://github.com/codenvy/codenvy/pull/89) ([benoitf](https://github.com/benoitf))
- CODENVY-368: add test of update Codenvy 4 on-prem from binary [\#88](https://github.com/codenvy/codenvy/pull/88) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Encrease default codenvy machine inactivity timeout to 8 hours [\#87](https://github.com/codenvy/codenvy/pull/87) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add default properties for Codenvy 4.1.0, 4.2.0-RC1-SNAPSHOT [\#86](https://github.com/codenvy/codenvy/pull/86) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-209: Change title of loading page of IDE. [\#79](https://github.com/codenvy/codenvy/pull/79) ([AndrienkoAleksandr](https://github.com/AndrienkoAleksandr))
- CODENVY-276: fix inconsistent size of Codenvy's logo while loading a … [\#75](https://github.com/codenvy/codenvy/pull/75) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-289 Rename plugin-contribution to plugin-pullrequest [\#22](https://github.com/codenvy/codenvy/pull/22) ([mkuznyetsov](https://github.com/mkuznyetsov))

## [4.1.0](https://github.com/codenvy/codenvy/tree/4.1.0) (2016-04-08)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.1...4.1.0)

**Pull requests merged:**

- CODENVY-141 show beta message in login/create site pages [\#85](https://github.com/codenvy/codenvy/pull/85) ([vkuznyetsov](https://github.com/vkuznyetsov))
- CODENVY-313: fix requesting through authenticated proxy [\#83](https://github.com/codenvy/codenvy/pull/83) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix workspace updating on stop by timeout [\#82](https://github.com/codenvy/codenvy/pull/82) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY-135 open source factories [\#81](https://github.com/codenvy/codenvy/pull/81) ([benoitf](https://github.com/benoitf))
- Add new default properties to Codenvy on-prem 4.1.0 [\#80](https://github.com/codenvy/codenvy/pull/80) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Add hosts into the /etc/hosts of Codenvy on-prem from the new line [\#78](https://github.com/codenvy/codenvy/pull/78) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix IM integration test of backup-restore codenvy4 [\#77](https://github.com/codenvy/codenvy/pull/77) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-257: Fix packaging [\#76](https://github.com/codenvy/codenvy/pull/76) ([tolusha](https://github.com/tolusha))
- CODENVY-345: don't create storage in time of reading it [\#74](https://github.com/codenvy/codenvy/pull/74) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-124: setup proxy variables in the IM CLI [\#73](https://github.com/codenvy/codenvy/pull/73) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-316: update reference to factory documentation in UD [\#72](https://github.com/codenvy/codenvy/pull/72) ([akurinnoy](https://github.com/akurinnoy))
- Fix IM CLI messages [\#71](https://github.com/codenvy/codenvy/pull/71) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-356 rename odyssey to site [\#70](https://github.com/codenvy/codenvy/pull/70) ([vkuznyetsov](https://github.com/vkuznyetsov))
- CHE-793: replace button style to open in IDE [\#69](https://github.com/codenvy/codenvy/pull/69) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-273: improvements to handle Factory's pretty name [\#68](https://github.com/codenvy/codenvy/pull/68) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-236: fix test-add-remove-codenvy-nodes-with-codenvy4 [\#67](https://github.com/codenvy/codenvy/pull/67) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-334 Handle hosted workspace stop dialogs [\#66](https://github.com/codenvy/codenvy/pull/66) ([mkuznyetsov](https://github.com/mkuznyetsov))
- CODENVY 2nd sentence in error message must have a period at the end [\#65](https://github.com/codenvy/codenvy/pull/65) ([vkuznyetsov](https://github.com/vkuznyetsov))
- CODENVY-326: Allow to dissable limits [\#63](https://github.com/codenvy/codenvy/pull/63) ([evoevodin](https://github.com/evoevodin))
- CODENVY-236: disconnect removing node from the puppet master [\#62](https://github.com/codenvy/codenvy/pull/62) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-141: fix codenvy classic url [\#61](https://github.com/codenvy/codenvy/pull/61) ([riuvshin](https://github.com/riuvshin))
- add repomanagement [\#60](https://github.com/codenvy/codenvy/pull/60) ([eivantsov](https://github.com/eivantsov))
- CHE-718: Adapt hosted infrastructure to the model & API changes [\#59](https://github.com/codenvy/codenvy/pull/59) ([evoevodin](https://github.com/evoevodin))
- CODENVY-235: check write access to install directory; code cleanup [\#58](https://github.com/codenvy/codenvy/pull/58) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-266: fix header align, fix overriding branding assets [\#57](https://github.com/codenvy/codenvy/pull/57) ([ashumilova](https://github.com/ashumilova))
- Update selenium version [\#56](https://github.com/codenvy/codenvy/pull/56) ([riuvshin](https://github.com/riuvshin))
- Restore generation of BuildInfo.properties file by default [\#55](https://github.com/codenvy/codenvy/pull/55) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-335: fix style for license message [\#53](https://github.com/codenvy/codenvy/pull/53) ([olexii4](https://github.com/olexii4))
- fix style for license message [\#52](https://github.com/codenvy/codenvy/pull/52) ([olexii4](https://github.com/olexii4))
- Restore generation of BuildInfo.properties file by default [\#51](https://github.com/codenvy/codenvy/pull/51) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-716 workspace id environment filter regex fix [\#50](https://github.com/codenvy/codenvy/pull/50) ([mshaposhnik](https://github.com/mshaposhnik))
- fix info message for download widget [\#49](https://github.com/codenvy/codenvy/pull/49) ([olexii4](https://github.com/olexii4))
- Add properties of Codenvy on-pren 4.0.1 and 4.1.0-SNAPSHOT [\#48](https://github.com/codenvy/codenvy/pull/48) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-299: improve fields title for add user widget [\#47](https://github.com/codenvy/codenvy/pull/47) ([olexii4](https://github.com/olexii4))
- CODENVY-141: add link to Codenvy Classic  [\#46](https://github.com/codenvy/codenvy/pull/46) ([ashumilova](https://github.com/ashumilova))
- CODENVY-233: fix error of executing IM CLI command after update [\#45](https://github.com/codenvy/codenvy/pull/45) ([dmytro-ndp](https://github.com/dmytro-ndp))
- rework logs output [\#43](https://github.com/codenvy/codenvy/pull/43) ([akurinnoy](https://github.com/akurinnoy))
- CHE-746. Changes corresponding to spliting github plugin to several modules [\#39](https://github.com/codenvy/codenvy/pull/39) ([RomanNikitenko](https://github.com/RomanNikitenko))
- CODENVY-286: fix styles for config widget in administration section [\#35](https://github.com/codenvy/codenvy/pull/35) ([olexii4](https://github.com/olexii4))
- CHE-716 getting workspaces via complex key [\#32](https://github.com/codenvy/codenvy/pull/32) ([mshaposhnik](https://github.com/mshaposhnik))
- CODENVY-224: Enable workspace shutdown wait timeout [\#23](https://github.com/codenvy/codenvy/pull/23) ([akorneta](https://github.com/akorneta))

## [4.0.1](https://github.com/codenvy/codenvy/tree/4.0.1) (2016-03-29)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC14...4.0.1)

**Pull requests merged:**

- fix factory configuration widget [\#44](https://github.com/codenvy/codenvy/pull/44) ([olexii4](https://github.com/olexii4))
- Fix test of backup and restore of Codenvy 4.x AIO [\#42](https://github.com/codenvy/codenvy/pull/42) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Fix prop name whisch used on about window [\#41](https://github.com/codenvy/codenvy/pull/41) ([vparfonov](https://github.com/vparfonov))
- remove codenvy-depmgt, move external dependency management to codenvy [\#40](https://github.com/codenvy/codenvy/pull/40) ([riuvshin](https://github.com/riuvshin))
- fix logs output on factory loading [\#38](https://github.com/codenvy/codenvy/pull/38) ([akurinnoy](https://github.com/akurinnoy))
- CODENVY-318: remove codenvy analytic info from factory details [\#37](https://github.com/codenvy/codenvy/pull/37) ([olexii4](https://github.com/olexii4))
- CODENVY-306: parse user's credentials of proxy in 'codenvy' script [\#34](https://github.com/codenvy/codenvy/pull/34) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CODENVY-290: add Google Analytics script [\#33](https://github.com/codenvy/codenvy/pull/33) ([olexii4](https://github.com/olexii4))
- CODENVY-312: add support of artifact version with GA [\#31](https://github.com/codenvy/codenvy/pull/31) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Remove GA label [\#30](https://github.com/codenvy/codenvy/pull/30) ([riuvshin](https://github.com/riuvshin))
- CODENVY-296: fix IM CLI command of changing Codenvy on-prem hostname [\#29](https://github.com/codenvy/codenvy/pull/29) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-871: Add popup notifier for bad connection [\#28](https://github.com/codenvy/codenvy/pull/28) ([akorneta](https://github.com/akorneta))
- Revert "CHE-708: Add CORS Filter" [\#27](https://github.com/codenvy/codenvy/pull/27) ([dimasnurenko](https://github.com/dimasnurenko))
- Do not send customer's password in plain text in Welcome to Codenvy email [\#26](https://github.com/codenvy/codenvy/pull/26) ([mmorhun](https://github.com/mmorhun))
- Fix checking on the minimal disk space in Codenvy install bootstrap script [\#25](https://github.com/codenvy/codenvy/pull/25) ([dmytro-ndp](https://github.com/dmytro-ndp))
- CHE-794 Improve the UI of the PR panel [\#15](https://github.com/codenvy/codenvy/pull/15) ([vitaliy-guliy](https://github.com/vitaliy-guliy))
- CHE-708: Add CORS Filter [\#13](https://github.com/codenvy/codenvy/pull/13) ([dimasnurenko](https://github.com/dimasnurenko))

## [4.0.0-RC14](https://github.com/codenvy/codenvy/tree/4.0.0-RC14) (2016-03-23)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC13...4.0.0-RC14)

**Pull requests merged:**

- Rework copy methhod. [\#24](https://github.com/codenvy/codenvy/pull/24) ([vparfonov](https://github.com/vparfonov))
- CODENVY-261: use copy of object because it can be cahanged it other thread [\#21](https://github.com/codenvy/codenvy/pull/21) ([vparfonov](https://github.com/vparfonov))
- fix license [\#20](https://github.com/codenvy/codenvy/pull/20) ([garagatyi](https://github.com/garagatyi))
- ODENVY-296: fix changing of Codenvy on-prem hostname [\#19](https://github.com/codenvy/codenvy/pull/19) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Remove useless for Codenvy on-prem 4.x checkins [\#18](https://github.com/codenvy/codenvy/pull/18) ([dmytro-ndp](https://github.com/dmytro-ndp))
- fix license [\#17](https://github.com/codenvy/codenvy/pull/17) ([garagatyi](https://github.com/garagatyi))
- CHE-781 remove analytics [\#16](https://github.com/codenvy/codenvy/pull/16) ([skabashnyuk](https://github.com/skabashnyuk))
- CODENVY-302: fix workspace FS backup/restore [\#14](https://github.com/codenvy/codenvy/pull/14) ([garagatyi](https://github.com/garagatyi))
- CODENVY-287: set clean VM image for tests [\#12](https://github.com/codenvy/codenvy/pull/12) ([dmytro-ndp](https://github.com/dmytro-ndp))
- Move mongo factory storage to platform-api impl [\#11](https://github.com/codenvy/codenvy/pull/11) ([mshaposhnik](https://github.com/mshaposhnik))
- Make changes accroding to the new project API [\#10](https://github.com/codenvy/codenvy/pull/10) ([vparfonov](https://github.com/vparfonov))
- CODENVY-291: temporary remove factory routing section [\#9](https://github.com/codenvy/codenvy/pull/9) ([ashumilova](https://github.com/ashumilova))
- CODENVY-102 Handle 'branch merged' event from VSTS [\#8](https://github.com/codenvy/codenvy/pull/8) ([stour](https://github.com/stour))
- Rename com.codenvy.client.version to project.version [\#7](https://github.com/codenvy/codenvy/pull/7) ([skabashnyuk](https://github.com/skabashnyuk))
- Codenvy 37 [\#6](https://github.com/codenvy/codenvy/pull/6) ([olexii4](https://github.com/olexii4))
- Fix increasing dashboard war size. [\#5](https://github.com/codenvy/codenvy/pull/5) ([ashumilova](https://github.com/ashumilova))
- update vagrant.sh according to structure changes [\#4](https://github.com/codenvy/codenvy/pull/4) ([riuvshin](https://github.com/riuvshin))
- CODENVY-252 Used dependency management directly from che parent [\#3](https://github.com/codenvy/codenvy/pull/3) ([skabashnyuk](https://github.com/skabashnyuk))
- Add properties for Codenvy on-prem 3.14.1.1 and 3.14.2-SNAPSHOT [\#2](https://github.com/codenvy/codenvy/pull/2) ([dmytro-ndp](https://github.com/dmytro-ndp))
- add node\_modules to .gitignore [\#1](https://github.com/codenvy/codenvy/pull/1) ([riuvshin](https://github.com/riuvshin))

## [4.0.0-RC13](https://github.com/codenvy/codenvy/tree/4.0.0-RC13) (2016-03-09)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC12...4.0.0-RC13)

## [4.0.0-RC12](https://github.com/codenvy/codenvy/tree/4.0.0-RC12) (2016-03-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC11...4.0.0-RC12)

## [4.0.0-RC11](https://github.com/codenvy/codenvy/tree/4.0.0-RC11) (2016-03-03)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC10...4.0.0-RC11)

## [4.0.0-RC10](https://github.com/codenvy/codenvy/tree/4.0.0-RC10) (2016-03-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC9...4.0.0-RC10)

## [4.0.0-RC9](https://github.com/codenvy/codenvy/tree/4.0.0-RC9) (2016-03-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC8...4.0.0-RC9)

## [4.0.0-RC8](https://github.com/codenvy/codenvy/tree/4.0.0-RC8) (2016-02-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC7...4.0.0-RC8)

## [4.0.0-RC7](https://github.com/codenvy/codenvy/tree/4.0.0-RC7) (2016-02-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC6...4.0.0-RC7)

## [4.0.0-RC6](https://github.com/codenvy/codenvy/tree/4.0.0-RC6) (2016-02-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC5...4.0.0-RC6)

## [4.0.0-RC5](https://github.com/codenvy/codenvy/tree/4.0.0-RC5) (2016-02-18)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.14.1.1...4.0.0-RC5)

## [3.14.1.1](https://github.com/codenvy/codenvy/tree/3.14.1.1) (2016-02-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC4...3.14.1.1)

## [4.0.0-RC4](https://github.com/codenvy/codenvy/tree/4.0.0-RC4) (2016-02-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC3...4.0.0-RC4)

## [4.0.0-RC3](https://github.com/codenvy/codenvy/tree/4.0.0-RC3) (2016-02-08)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-RC2...4.0.0-RC3)

## [4.0.0-RC2](https://github.com/codenvy/codenvy/tree/4.0.0-RC2) (2016-02-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.14.1...4.0.0-RC2)

## [3.14.1](https://github.com/codenvy/codenvy/tree/3.14.1) (2016-01-31)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-13...3.14.1)

## [4.0.0-beta-13](https://github.com/codenvy/codenvy/tree/4.0.0-beta-13) (2016-01-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-12...4.0.0-beta-13)

## [4.0.0-beta-12](https://github.com/codenvy/codenvy/tree/4.0.0-beta-12) (2016-01-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-11...4.0.0-beta-12)

## [4.0.0-beta-11](https://github.com/codenvy/codenvy/tree/4.0.0-beta-11) (2016-01-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-10...4.0.0-beta-11)

## [4.0.0-beta-10](https://github.com/codenvy/codenvy/tree/4.0.0-beta-10) (2016-01-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-9...4.0.0-beta-10)

## [4.0.0-beta-9](https://github.com/codenvy/codenvy/tree/4.0.0-beta-9) (2016-01-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-8...4.0.0-beta-9)

## [4.0.0-beta-8](https://github.com/codenvy/codenvy/tree/4.0.0-beta-8) (2016-01-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-7...4.0.0-beta-8)

## [4.0.0-beta-7](https://github.com/codenvy/codenvy/tree/4.0.0-beta-7) (2016-01-09)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-6...4.0.0-beta-7)

## [4.0.0-beta-6](https://github.com/codenvy/codenvy/tree/4.0.0-beta-6) (2016-01-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-5...4.0.0-beta-6)

## [4.0.0-beta-5](https://github.com/codenvy/codenvy/tree/4.0.0-beta-5) (2016-01-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-4...4.0.0-beta-5)

## [4.0.0-beta-4](https://github.com/codenvy/codenvy/tree/4.0.0-beta-4) (2016-01-04)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-2...4.0.0-beta-4)

## [4.0.0-beta-2](https://github.com/codenvy/codenvy/tree/4.0.0-beta-2) (2015-12-30)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.14.0...4.0.0-beta-2)

## [3.14.0](https://github.com/codenvy/codenvy/tree/3.14.0) (2015-12-29)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-beta-1...3.14.0)

## [4.0.0-beta-1](https://github.com/codenvy/codenvy/tree/4.0.0-beta-1) (2015-12-23)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-M5-1...4.0.0-beta-1)

## [4.0.0-M5-1](https://github.com/codenvy/codenvy/tree/4.0.0-M5-1) (2015-12-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-M5...4.0.0-M5-1)

## [4.0.0-M5](https://github.com/codenvy/codenvy/tree/4.0.0-M5) (2015-12-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.4.4...4.0.0-M5)

## [3.13.4.4](https://github.com/codenvy/codenvy/tree/3.13.4.4) (2015-12-04)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.4.3...3.13.4.4)

## [3.13.4.3](https://github.com/codenvy/codenvy/tree/3.13.4.3) (2015-12-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.4.2...3.13.4.3)

## [3.13.4.2](https://github.com/codenvy/codenvy/tree/3.13.4.2) (2015-12-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.4.1...3.13.4.2)

## [3.13.4.1](https://github.com/codenvy/codenvy/tree/3.13.4.1) (2015-11-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.4...3.13.4.1)

## [3.13.4](https://github.com/codenvy/codenvy/tree/3.13.4) (2015-11-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.3...3.13.4)

## [3.13.3](https://github.com/codenvy/codenvy/tree/3.13.3) (2015-11-24)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.2...3.13.3)

## [3.13.2](https://github.com/codenvy/codenvy/tree/3.13.2) (2015-11-22)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-M4...3.13.2)

## [4.0.0-M4](https://github.com/codenvy/codenvy/tree/4.0.0-M4) (2015-11-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.1...4.0.0-M4)

## [3.13.1](https://github.com/codenvy/codenvy/tree/3.13.1) (2015-10-24)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-M3...3.13.1)

## [4.0.0-M3](https://github.com/codenvy/codenvy/tree/4.0.0-M3) (2015-10-14)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.13.0...4.0.0-M3)

## [3.13.0](https://github.com/codenvy/codenvy/tree/3.13.0) (2015-10-08)
[Full Changelog](https://github.com/codenvy/codenvy/compare/4.0.0-M2...3.13.0)

## [4.0.0-M2](https://github.com/codenvy/codenvy/tree/4.0.0-M2) (2015-10-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.5.2...4.0.0-M2)

## [3.12.5.2](https://github.com/codenvy/codenvy/tree/3.12.5.2) (2015-10-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.6...3.12.5.2)

## [3.12.6](https://github.com/codenvy/codenvy/tree/3.12.6) (2015-10-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.5.1...3.12.6)

## [3.12.5.1](https://github.com/codenvy/codenvy/tree/3.12.5.1) (2015-09-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.5...3.12.5.1)

## [3.12.5](https://github.com/codenvy/codenvy/tree/3.12.5) (2015-09-18)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.4...3.12.5)

## [3.12.4](https://github.com/codenvy/codenvy/tree/3.12.4) (2015-09-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.3...3.12.4)

## [3.12.3](https://github.com/codenvy/codenvy/tree/3.12.3) (2015-09-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.2...3.12.3)

## [3.12.2](https://github.com/codenvy/codenvy/tree/3.12.2) (2015-09-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.1...3.12.2)

## [3.12.1](https://github.com/codenvy/codenvy/tree/3.12.1) (2015-08-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.9.2...3.12.1)

## [3.11.9.2](https://github.com/codenvy/codenvy/tree/3.11.9.2) (2015-08-26)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.12.0...3.11.9.2)

## [3.12.0](https://github.com/codenvy/codenvy/tree/3.12.0) (2015-08-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.9.1...3.12.0)

## [3.11.9.1](https://github.com/codenvy/codenvy/tree/3.11.9.1) (2015-08-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.9...3.11.9.1)

## [3.11.9](https://github.com/codenvy/codenvy/tree/3.11.9) (2015-08-07)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.8...3.11.9)

## [3.11.8](https://github.com/codenvy/codenvy/tree/3.11.8) (2015-08-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.7...3.11.8)

## [3.11.7](https://github.com/codenvy/codenvy/tree/3.11.7) (2015-08-04)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.6...3.11.7)

## [3.11.6](https://github.com/codenvy/codenvy/tree/3.11.6) (2015-07-30)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.5...3.11.6)

## [3.11.5](https://github.com/codenvy/codenvy/tree/3.11.5) (2015-07-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.4...3.11.5)

## [3.11.4](https://github.com/codenvy/codenvy/tree/3.11.4) (2015-07-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.3...3.11.4)

## [3.11.3](https://github.com/codenvy/codenvy/tree/3.11.3) (2015-07-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.2...3.11.3)

## [3.11.2](https://github.com/codenvy/codenvy/tree/3.11.2) (2015-07-14)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.1...3.11.2)

## [3.11.1](https://github.com/codenvy/codenvy/tree/3.11.1) (2015-07-09)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.11.0...3.11.1)

## [3.11.0](https://github.com/codenvy/codenvy/tree/3.11.0) (2015-07-08)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.10.3.2...3.11.0)

## [3.10.3.2](https://github.com/codenvy/codenvy/tree/3.10.3.2) (2015-07-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.10.3.1...3.10.3.2)

## [3.10.3.1](https://github.com/codenvy/codenvy/tree/3.10.3.1) (2015-07-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.10.3...3.10.3.1)

## [3.10.3](https://github.com/codenvy/codenvy/tree/3.10.3) (2015-07-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.10.2...3.10.3)

## [3.10.2](https://github.com/codenvy/codenvy/tree/3.10.2) (2015-06-30)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.9.2...3.10.2)

## [3.9.2](https://github.com/codenvy/codenvy/tree/3.9.2) (2015-06-26)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.9.1...3.9.2)

## [3.9.1](https://github.com/codenvy/codenvy/tree/3.9.1) (2015-06-18)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.6...3.9.1)

## [0.16.6](https://github.com/codenvy/codenvy/tree/0.16.6) (2015-06-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.30.3...0.16.6)

## [0.30.3](https://github.com/codenvy/codenvy/tree/0.30.3) (2015-06-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.10.0...0.30.3)

## [2.10.0](https://github.com/codenvy/codenvy/tree/2.10.0) (2015-05-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.10.0...2.10.0)

## [1.10.0](https://github.com/codenvy/codenvy/tree/1.10.0) (2015-05-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.25.0...1.10.0)

## [0.25.0](https://github.com/codenvy/codenvy/tree/0.25.0) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.31.0...0.25.0)

## [0.31.0](https://github.com/codenvy/codenvy/tree/0.31.0) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.17.0...0.31.0)

## [0.17.0](https://github.com/codenvy/codenvy/tree/0.17.0) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.10.0...0.17.0)

## [3.10.0](https://github.com/codenvy/codenvy/tree/3.10.0) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.30.2...3.10.0)

## [0.30.2](https://github.com/codenvy/codenvy/tree/0.30.2) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.5...0.30.2)

## [0.16.5](https://github.com/codenvy/codenvy/tree/0.16.5) (2015-05-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.4...0.16.5)

## [0.16.4](https://github.com/codenvy/codenvy/tree/0.16.4) (2015-05-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.30.1...0.16.4)

## [0.30.1](https://github.com/codenvy/codenvy/tree/0.30.1) (2015-05-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.24.1...0.30.1)

## [0.24.1](https://github.com/codenvy/codenvy/tree/0.24.1) (2015-05-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.3...0.24.1)

## [0.16.3](https://github.com/codenvy/codenvy/tree/0.16.3) (2015-05-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.2...0.16.3)

## [0.16.2](https://github.com/codenvy/codenvy/tree/0.16.2) (2015-04-29)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.1...0.16.2)

## [0.16.1](https://github.com/codenvy/codenvy/tree/0.16.1) (2015-04-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.9.0...0.16.1)

## [2.9.0](https://github.com/codenvy/codenvy/tree/2.9.0) (2015-04-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.9.0...2.9.0)

## [1.9.0](https://github.com/codenvy/codenvy/tree/1.9.0) (2015-04-27)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.24.0...1.9.0)

## [0.24.0](https://github.com/codenvy/codenvy/tree/0.24.0) (2015-04-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.30.0...0.24.0)

## [0.30.0](https://github.com/codenvy/codenvy/tree/0.30.0) (2015-04-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.16.0...0.30.0)

## [0.16.0](https://github.com/codenvy/codenvy/tree/0.16.0) (2015-04-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.9.0...0.16.0)

## [3.9.0](https://github.com/codenvy/codenvy/tree/3.9.0) (2015-04-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.15.CCCIS.1...3.9.0)

## [0.15.CCCIS.1](https://github.com/codenvy/codenvy/tree/0.15.CCCIS.1) (2015-04-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.8.3...0.15.CCCIS.1)

## [3.8.3](https://github.com/codenvy/codenvy/tree/3.8.3) (2015-04-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.8.2...3.8.3)

## [3.8.2](https://github.com/codenvy/codenvy/tree/3.8.2) (2015-04-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.8.1...3.8.2)

## [3.8.1](https://github.com/codenvy/codenvy/tree/3.8.1) (2015-04-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.8.0...3.8.1)

## [3.8.0](https://github.com/codenvy/codenvy/tree/3.8.0) (2015-03-30)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.8.0...3.8.0)

## [1.8.0](https://github.com/codenvy/codenvy/tree/1.8.0) (2015-03-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.23.0...1.8.0)

## [0.23.0](https://github.com/codenvy/codenvy/tree/0.23.0) (2015-03-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.29.0...0.23.0)

## [0.29.0](https://github.com/codenvy/codenvy/tree/0.29.0) (2015-03-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.15.0...0.29.0)

## [0.15.0](https://github.com/codenvy/codenvy/tree/0.15.0) (2015-03-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.7.3...0.15.0)

## [3.7.3](https://github.com/codenvy/codenvy/tree/3.7.3) (2015-03-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.14.1...3.7.3)

## [0.14.1](https://github.com/codenvy/codenvy/tree/0.14.1) (2015-03-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.7.2...0.14.1)

## [3.7.2](https://github.com/codenvy/codenvy/tree/3.7.2) (2015-03-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.7.1...3.7.2)

## [3.7.1](https://github.com/codenvy/codenvy/tree/3.7.1) (2015-03-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.7.0...3.7.1)

## [1.7.0](https://github.com/codenvy/codenvy/tree/1.7.0) (2015-03-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.22.0...1.7.0)

## [0.22.0](https://github.com/codenvy/codenvy/tree/0.22.0) (2015-03-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.28.0...0.22.0)

## [0.28.0](https://github.com/codenvy/codenvy/tree/0.28.0) (2015-03-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.14.0...0.28.0)

## [0.14.0](https://github.com/codenvy/codenvy/tree/0.14.0) (2015-03-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.7.0...0.14.0)

## [3.7.0](https://github.com/codenvy/codenvy/tree/3.7.0) (2015-03-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.5.3...3.7.0)

## [3.5.3](https://github.com/codenvy/codenvy/tree/3.5.3) (2015-02-20)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.5.2...3.5.3)

## [3.5.2](https://github.com/codenvy/codenvy/tree/3.5.2) (2015-02-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.21.1...3.5.2)

## [0.21.1](https://github.com/codenvy/codenvy/tree/0.21.1) (2015-02-03)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.6.0...0.21.1)

## [1.6.0](https://github.com/codenvy/codenvy/tree/1.6.0) (2015-01-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.21.0...1.6.0)

## [0.21.0](https://github.com/codenvy/codenvy/tree/0.21.0) (2015-01-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.27.0...0.21.0)

## [0.27.0](https://github.com/codenvy/codenvy/tree/0.27.0) (2015-01-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.13.0...0.27.0)

## [0.13.0](https://github.com/codenvy/codenvy/tree/0.13.0) (2015-01-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.5.1...0.13.0)

## [3.5.1](https://github.com/codenvy/codenvy/tree/3.5.1) (2015-01-17)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.5.0...3.5.1)

## [3.5.0](https://github.com/codenvy/codenvy/tree/3.5.0) (2015-01-16)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.5.0...3.5.0)

## [1.5.0](https://github.com/codenvy/codenvy/tree/1.5.0) (2015-01-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.20.0...1.5.0)

## [0.20.0](https://github.com/codenvy/codenvy/tree/0.20.0) (2015-01-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.26.0...0.20.0)

## [0.26.0](https://github.com/codenvy/codenvy/tree/0.26.0) (2015-01-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.12.0...0.26.0)

## [0.12.0](https://github.com/codenvy/codenvy/tree/0.12.0) (2015-01-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.4.0...0.12.0)

## [1.4.0](https://github.com/codenvy/codenvy/tree/1.4.0) (2014-12-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.19.0...1.4.0)

## [0.19.0](https://github.com/codenvy/codenvy/tree/0.19.0) (2014-12-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.11.0...0.19.0)

## [0.11.0](https://github.com/codenvy/codenvy/tree/0.11.0) (2014-12-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.3.2...0.11.0)

## [3.3.2](https://github.com/codenvy/codenvy/tree/3.3.2) (2014-12-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.3.1...3.3.2)

## [3.3.1](https://github.com/codenvy/codenvy/tree/3.3.1) (2014-12-09)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.3.0...3.3.1)

## [3.3.0](https://github.com/codenvy/codenvy/tree/3.3.0) (2014-12-07)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.3.0...3.3.0)

## [1.3.0](https://github.com/codenvy/codenvy/tree/1.3.0) (2014-12-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.18.0...1.3.0)

## [0.18.0](https://github.com/codenvy/codenvy/tree/0.18.0) (2014-12-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.10.0...0.18.0)

## [0.10.0](https://github.com/codenvy/codenvy/tree/0.10.0) (2014-12-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.2.0...0.10.0)

## [1.2.0](https://github.com/codenvy/codenvy/tree/1.2.0) (2014-11-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.9.0...1.2.0)

## [0.9.0](https://github.com/codenvy/codenvy/tree/0.9.0) (2014-11-05)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.1.1...0.9.0)

## [2.1.1](https://github.com/codenvy/codenvy/tree/2.1.1) (2014-10-24)
[Full Changelog](https://github.com/codenvy/codenvy/compare/3.1.0...2.1.1)

## [3.1.0](https://github.com/codenvy/codenvy/tree/3.1.0) (2014-10-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.1.0...3.1.0)

## [1.1.0](https://github.com/codenvy/codenvy/tree/1.1.0) (2014-10-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.8.0...1.1.0)

## [0.8.0](https://github.com/codenvy/codenvy/tree/0.8.0) (2014-10-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.0.3...0.8.0)

## [1.0.3](https://github.com/codenvy/codenvy/tree/1.0.3) (2014-08-22)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.6.0...1.0.3)

## [0.6.0](https://github.com/codenvy/codenvy/tree/0.6.0) (2014-08-21)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.0.2...0.6.0)

## [1.0.2](https://github.com/codenvy/codenvy/tree/1.0.2) (2014-08-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.8.2...1.0.2)

## [2.8.2](https://github.com/codenvy/codenvy/tree/2.8.2) (2014-08-18)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.8.1...2.8.2)

## [2.8.1](https://github.com/codenvy/codenvy/tree/2.8.1) (2014-08-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.0.0-M1...2.8.1)

## [2.0.0-M1](https://github.com/codenvy/codenvy/tree/2.0.0-M1) (2014-08-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.0.1...2.0.0-M1)

## [1.0.1](https://github.com/codenvy/codenvy/tree/1.0.1) (2014-08-11)
[Full Changelog](https://github.com/codenvy/codenvy/compare/1.0.0...1.0.1)

## [1.0.0](https://github.com/codenvy/codenvy/tree/1.0.0) (2014-08-07)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.8.0...1.0.0)

## [2.8.0](https://github.com/codenvy/codenvy/tree/2.8.0) (2014-08-06)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.5.0...2.8.0)

## [0.5.0](https://github.com/codenvy/codenvy/tree/0.5.0) (2014-08-01)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.4.0...0.5.0)

## [0.4.0](https://github.com/codenvy/codenvy/tree/0.4.0) (2014-06-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.7.0...0.4.0)

## [2.7.0](https://github.com/codenvy/codenvy/tree/2.7.0) (2014-06-12)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.3.0...2.7.0)

## [0.3.0](https://github.com/codenvy/codenvy/tree/0.3.0) (2014-05-29)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.6.0...0.3.0)

## [2.6.0](https://github.com/codenvy/codenvy/tree/2.6.0) (2014-05-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.5.0...2.6.0)

## [2.5.0](https://github.com/codenvy/codenvy/tree/2.5.0) (2014-04-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.4.0...2.5.0)

## [2.4.0](https://github.com/codenvy/codenvy/tree/2.4.0) (2014-03-28)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.1...2.4.0)

## [0.1](https://github.com/codenvy/codenvy/tree/0.1) (2014-03-14)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.3.0...0.1)

## [2.3.0](https://github.com/codenvy/codenvy/tree/2.3.0) (2014-03-13)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.2.0...2.3.0)

## [2.2.0](https://github.com/codenvy/codenvy/tree/2.2.0) (2014-03-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.1.0...2.2.0)

## [2.1.0](https://github.com/codenvy/codenvy/tree/2.1.0) (2014-02-03)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.0.1...2.1.0)

## [2.0.1](https://github.com/codenvy/codenvy/tree/2.0.1) (2013-12-25)
[Full Changelog](https://github.com/codenvy/codenvy/compare/2.0.0...2.0.1)

## [2.0.0](https://github.com/codenvy/codenvy/tree/2.0.0) (2013-12-02)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.4.2...2.0.0)

## [0.4.2](https://github.com/codenvy/codenvy/tree/0.4.2) (2013-06-19)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.4.1...0.4.2)

## [0.4.1](https://github.com/codenvy/codenvy/tree/0.4.1) (2013-06-18)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.3.1...0.4.1)

## [0.3.1](https://github.com/codenvy/codenvy/tree/0.3.1) (2013-05-15)
[Full Changelog](https://github.com/codenvy/codenvy/compare/0.2.0...0.3.1)

## [0.2.0](https://github.com/codenvy/codenvy/tree/0.2.0) (2013-04-23)
[Full Changelog](https://github.com/codenvy/codenvy/compare/v0.1...0.2.0)

## [v0.1](https://github.com/codenvy/codenvy/tree/v0.1) (2013-02-14)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*