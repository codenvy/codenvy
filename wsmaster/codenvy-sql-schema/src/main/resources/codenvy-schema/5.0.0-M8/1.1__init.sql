--
--  [2012] - [2016] Codenvy, S.A.
--  All Rights Reserved.
--
-- NOTICE:  All information contained herein is, and remains
-- the property of Codenvy S.A. and its suppliers,
-- if any.  The intellectual and technical concepts contained
-- herein are proprietary to Codenvy S.A.
-- and its suppliers and may be covered by U.S. and Foreign Patents,
-- patents in process, and are protected by trade secret or copyright law.
-- Dissemination of this information or reproduction of this material
-- is strictly forbidden unless prior written permission is obtained
-- from Codenvy S.A..
--

-- Factory Button --------------------------------------------------------------
CREATE TABLE button (
    id          BIGINT          NOT NULL,
    type        VARCHAR(255),
    color       VARCHAR(255),
    counter     BOOLEAN,
    logo        VARCHAR(255),
    style       VARCHAR(255),

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Action ----------------------------------------------------------------------
CREATE TABLE action (
    entityid        BIGINT          NOT NULL,
    id              VARCHAR(255),

    PRIMARY KEY (entityid)
);
--------------------------------------------------------------------------------


-- Action properties -----------------------------------------------------------
CREATE TABLE action_properties (
    action_entityid         BIGINT,
    properties              VARCHAR(255),
    properties_key          VARCHAR(255)
);
-- constraints
ALTER TABLE action_properties ADD CONSTRAINT fk_action_properties_action_entityid FOREIGN KEY (action_entityid) REFERENCES ACTION (entityid);
--------------------------------------------------------------------------------


-- On app closed action --------------------------------------------------------
CREATE TABLE onappclosed (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- On projects loaded action ---------------------------------------------------
CREATE TABLE onprojectsloaded (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- On app loaded action --------------------------------------------------------
CREATE TABLE onapploaded (
    id      BIGINT      NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- On app closed action --------------------------------------------------------
CREATE TABLE onappclosed_action (
    onappclosed_id      BIGINT      NOT NULL,
    actions_entityid    BIGINT      NOT NULL,

    PRIMARY KEY (onappclosed_id, actions_entityid)
);
-- constraints
ALTER TABLE onappclosed_action ADD CONSTRAINT fk_onappclosed_action_actions_entityid FOREIGN KEY (actions_entityid) REFERENCES ACTION (entityid);
ALTER TABLE onappclosed_action ADD CONSTRAINT fk_onappclosed_action_onappclosed_id FOREIGN KEY (onappclosed_id) REFERENCES onappclosed (id);
--------------------------------------------------------------------------------


-- On project loaded action ----------------------------------------------------
CREATE TABLE onprojectsloaded_action (
    onprojectsloaded_id         BIGINT      NOT NULL,
    actions_entityid            BIGINT      NOT NULL,

    PRIMARY KEY (onprojectsloaded_id, actions_entityid)
);
-- constraints
ALTER TABLE onprojectsloaded_action ADD CONSTRAINT fk_onprojectsloaded_action_onprojectsloaded_id FOREIGN KEY (onprojectsloaded_id) REFERENCES onprojectsloaded (id);
ALTER TABLE onprojectsloaded_action ADD CONSTRAINT fk_onprojectsloaded_action_actions_entityid FOREIGN KEY (actions_entityid) REFERENCES ACTION (entityid);
--------------------------------------------------------------------------------


-- On app loaded action --------------------------------------------------------
CREATE TABLE onapploaded_action (
    onapploaded_id          BIGINT      NOT NULL,
    actions_entityid        BIGINT      NOT NULL,

    PRIMARY KEY (onapploaded_id, actions_entityid)
);
-- constraints
ALTER TABLE onapploaded_action ADD CONSTRAINT fk_onapploaded_action_actions_entityid FOREIGN KEY (actions_entityid) REFERENCES ACTION (entityid);
ALTER TABLE onapploaded_action ADD CONSTRAINT fk_onapploaded_action_onapploaded_id FOREIGN KEY (onapploaded_id) REFERENCES onapploaded (id);
--------------------------------------------------------------------------------


-- Ide -------------------------------------------------------------------------
CREATE TABLE ide (
    id                      BIGINT      NOT NULL,
    onappclosed_id          BIGINT,
    onapploaded_id          BIGINT,
    onprojectsloaded_id     BIGINT,

    PRIMARY KEY (id)
);
-- constraints
ALTER TABLE ide ADD CONSTRAINT fk_ide_onappclosed_id FOREIGN KEY (onappclosed_id) REFERENCES onappclosed (id);
ALTER TABLE ide ADD CONSTRAINT fk_ide_onprojectsloaded_id FOREIGN KEY (onprojectsloaded_id) REFERENCES onprojectsloaded (id);
ALTER TABLE ide ADD CONSTRAINT fk_ide_onapploaded_id FOREIGN KEY (onapploaded_id) REFERENCES onapploaded (id);
--------------------------------------------------------------------------------


-- Factory ---------------------------------------------------------------------
CREATE TABLE factory (
    id                  VARCHAR(255)         NOT NULL,
    name                VARCHAR(255),
    version             VARCHAR(255)         NOT NULL,
    created             BIGINT,
    userid              VARCHAR(255),
    creation_strategy   VARCHAR(255),
    match_reopen        VARCHAR(255),
    referer             VARCHAR(255),
    since               BIGINT,
    until               BIGINT,
    button_id           BIGINT,
    ide_id              BIGINT,
    workspace_id        BIGINT,

    PRIMARY KEY (id)
);
-- constraints
ALTER TABLE factory ADD CONSTRAINT fk_factory_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE factory ADD CONSTRAINT fk_factory_ide_id FOREIGN KEY (ide_id) REFERENCES ide (id);
ALTER TABLE factory ADD CONSTRAINT fk_factory_button_id FOREIGN KEY (button_id) REFERENCES button (id);
ALTER TABLE factory ADD CONSTRAINT fk_factory_workspace_id FOREIGN KEY (workspace_id) REFERENCES workspaceconfig (id);
--------------------------------------------------------------------------------


-- Factory Images --------------------------------------------------------------
CREATE TABLE factory_images (
    imagedata   BYTEA,
    mediatype   VARCHAR(255),
    name        VARCHAR(255),
    factory_id  VARCHAR(255)
);
-- constraints
ALTER TABLE factory_images ADD CONSTRAINT fk_factory_images_factory_id FOREIGN KEY (factory_id) REFERENCES factory (id);
--------------------------------------------------------------------------------


-- System permissions ----------------------------------------------------------
CREATE TABLE systempermissions (
    id      VARCHAR(255)         NOT NULL,
    userid  VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_systempermissions_userid ON systempermissions (userid);
-- constraints
ALTER TABLE systempermissions ADD CONSTRAINT fk_systempermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


-- System permissions actions --------------------------------------------------
CREATE TABLE systempermissions_actions (
    systempermissions_id    VARCHAR(255),
    actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX index_systempermissions_actions_actions ON systempermissions_actions (actions);
-- constraints
ALTER TABLE systempermissions_actions ADD CONSTRAINT fk_systempermissions_actions_systempermissions_id FOREIGN KEY (systempermissions_id) REFERENCES systempermissions (id);
--------------------------------------------------------------------------------


-- Workspace workers -----------------------------------------------------------
CREATE TABLE worker (
    id              VARCHAR(255)         NOT NULL,
    userid          VARCHAR(255),
    workspaceid     VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_worker_userid_workspaceid ON worker (userid, workspaceid);
CREATE INDEX index_worker_workspaceid ON worker (workspaceid);
-- constraints
ALTER TABLE worker ADD CONSTRAINT fk_worker_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE worker ADD CONSTRAINT fk_worker_workspaceid FOREIGN KEY (workspaceid) REFERENCES workspace (id);
--------------------------------------------------------------------------------


-- Worker actions --------------------------------------------------------------
CREATE TABLE worker_actions (
    worker_id       VARCHAR(255),
    actions         VARCHAR(255)
);
-- indexes
CREATE INDEX index_worker_actions_actions ON worker_actions (actions);
-- constraints
ALTER TABLE worker_actions ADD CONSTRAINT fk_worker_actions_worker_id FOREIGN KEY (worker_id) REFERENCES worker (id);
--------------------------------------------------------------------------------


-- Stack permissions -----------------------------------------------------------
CREATE TABLE stackpermissions (
    id          VARCHAR(255)         NOT NULL,
    stackid     VARCHAR(255),
    userid      VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_stackpermissions_userid_stackid ON stackpermissions (userid, stackid);
CREATE INDEX index_stackpermissions_stackid ON stackpermissions (stackid);
-- constraints
ALTER TABLE stackpermissions ADD CONSTRAINT fk_stackpermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE stackpermissions ADD CONSTRAINT fk_stackpermissions_stackid FOREIGN KEY (stackid) REFERENCES stack (id);
--------------------------------------------------------------------------------


-- Stack permissions actions ---------------------------------------------------
CREATE TABLE stackpermissions_actions (
    stackpermissions_id     VARCHAR(255),
    actions                 VARCHAR(255)
);
-- indexes
CREATE INDEX index_stackpermissions_actions_actions ON stackpermissions_actions (actions);
-- constraints
ALTER TABLE stackpermissions_actions ADD CONSTRAINT fk_stackpermissions_actions_stackpermissions_id FOREIGN KEY (stackpermissions_id) REFERENCES stackpermissions (id);
--------------------------------------------------------------------------------


-- Recipe permissions ----------------------------------------------------------
CREATE TABLE recipepermissions (
    id          VARCHAR(255)         NOT NULL,
    recipeid    VARCHAR(255),
    userid      VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_recipepermissions_userid_recipeid ON recipepermissions (userid, recipeid);
CREATE INDEX index_recipepermissions_recipeid ON recipepermissions (recipeid);
-- constraints
ALTER TABLE recipepermissions ADD CONSTRAINT fk_recipepermissions_userid FOREIGN KEY (userid) REFERENCES usr (id);
ALTER TABLE recipepermissions ADD CONSTRAINT fk_recipepermissions_recipeid FOREIGN KEY (recipeid) REFERENCES recipe (id);
--------------------------------------------------------------------------------


-- Recipe permissions actions --------------------------------------------------
create table recipepermissions_actions (
    recipepermissions_id    varchar(255),
    actions                 VARCHAR(255)
);
-- indexes
create index index_recipepermissions_actions_actions on recipepermissions_actions (actions);
-- constraints
ALTER TABLE recipepermissions_actions ADD CONSTRAINT fk_recipepermissions_actions_recipepermissions_id FOREIGN KEY (recipepermissions_id) REFERENCES recipepermissions (id);
--------------------------------------------------------------------------------


-- Organization ----------------------------------------------------------------
CREATE TABLE organization (
    id          VARCHAR(255)         NOT NULL,
    parent      VARCHAR(255),
    account_id  VARCHAR(255)         NOT NULL,

    PRIMARY KEY (id)
);
-- indexes
CREATE INDEX index_organization_parent ON organization (parent);
-- constraints
ALTER TABLE organization ADD CONSTRAINT fk_organization_parent FOREIGN KEY (parent) REFERENCES organization (id);
ALTER TABLE organization ADD CONSTRAINT fk_organization_account_id FOREIGN KEY (account_id) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Organization member ---------------------------------------------------------
CREATE TABLE member (
    id              VARCHAR(255)         NOT NULL,
    organizationid  VARCHAR(255),
    userid          VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_member_userid_organizationid ON member (userid, organizationid);
CREATE INDEX index_member_organizationid ON member (organizationid);
-- constraints
ALTER TABLE member ADD CONSTRAINT fk_member_organizationid FOREIGN KEY (organizationid) REFERENCES organization (id);
ALTER TABLE member ADD CONSTRAINT fk_member_userid FOREIGN KEY (userid) REFERENCES usr (id);
--------------------------------------------------------------------------------


--Member actions ---------------------------------------------------------------
CREATE TABLE member_actions (
    member_id       VARCHAR(255),
    actions         VARCHAR(255)
);
-- indexes
CREATE INDEX index_member_actions_actions ON member_actions (actions);
-- constraints
 ALTER TABLE member_actions ADD CONSTRAINT fk_member_actions_member_id FOREIGN KEY (member_id) REFERENCES member (id);
--------------------------------------------------------------------------------


-- Resource --------------------------------------------------------------------
CREATE TABLE resource (
    id          BIGINT          NOT NULL,
    amount      BIGINT,
    type        VARCHAR(255)    NOT NULL,
    unit        VARCHAR(255)    NOT NULL,

    PRIMARY KEY (id)
);
--------------------------------------------------------------------------------


-- Free resource limit ---------------------------------------------------------
CREATE TABLE freeresourceslimit (
    accountid       VARCHAR(255)         NOT NULL,

    PRIMARY KEY (accountid)
);
-- constraints
ALTER TABLE freeresourceslimit ADD CONSTRAINT fk_freeresourceslimit_accountid FOREIGN KEY (accountid) REFERENCES account (id);
--------------------------------------------------------------------------------


-- Free resource limit resource ------------------------------------------------
CREATE TABLE freeresourceslimit_resource (
    freeresourceslimit_accountid        VARCHAR(255)    NOT NULL,
    resources_id                        BIGINT          NOT NULL,

    PRIMARY KEY (freeresourceslimit_accountid, resources_id)
);
-- constraints
ALTER TABLE freeresourceslimit_resource ADD CONSTRAINT fk_freeresourceslimit_resource_resources_id FOREIGN KEY (resources_id) REFERENCES resource (id);
ALTER TABLE freeresourceslimit_resource ADD CONSTRAINT frresourceslimitresourcefreresourceslimitaccountid FOREIGN KEY (freeresourceslimit_accountid) REFERENCES freeresourceslimit (accountid);
--------------------------------------------------------------------------------
