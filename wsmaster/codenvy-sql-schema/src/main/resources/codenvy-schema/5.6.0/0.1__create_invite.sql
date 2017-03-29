--
--  [2012] - [2017] Codenvy, S.A.
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

-- Invite ---------------------------------------------------------
CREATE TABLE codenvy_invite (
    id                  BIGINT               NOT NULL,
    email               VARCHAR(255)         NOT NULL,
    domain_id           VARCHAR(255)         NOT NULL,
    organization_id     VARCHAR(255),
    workspace_id        VARCHAR(255),

    PRIMARY KEY (id)
);
-- indexes
CREATE UNIQUE INDEX index_codenvy_invite_email_domain_org ON codenvy_invite (email, domain_id, organization_id);
CREATE UNIQUE INDEX index_codenvy_invite_email_domain_ws ON codenvy_invite (email, domain_id, workspace_id);
CREATE INDEX index_codenvy_invite_domain_org ON codenvy_invite (domain_id, organization_id);
CREATE INDEX index_codenvy_invite_domain_ws ON codenvy_invite (domain_id, workspace_id);
-- checks
ALTER TABLE codenvy_invite ADD CONSTRAINT check_codenvy_invite_instance_not_null
                                          CHECK (domain_id = 'organization' AND organization_id IS NOT NULL    AND workspace_id    IS NULL
                                              OR domain_id = 'workspace'    AND workspace_id    IS NOT NULL    AND organization_id IS NULL);
-- constraints
ALTER TABLE codenvy_invite ADD CONSTRAINT fk_codenvy_invite_org_id FOREIGN KEY (organization_id) REFERENCES organization (id);
ALTER TABLE codenvy_invite ADD CONSTRAINT fk_codenvy_invite_ws_id FOREIGN KEY (workspace_id) REFERENCES workspace (id);
--------------------------------------------------------------------------------

--Invite actions ---------------------------------------------------------------
CREATE TABLE codenvy_invite_action (
    invite_id       BIGINT,
    action          VARCHAR(255)
);
-- indexes
CREATE UNIQUE INDEX index_codenvy_invite_action_action ON codenvy_invite_action (invite_id, action);
-- constraints
ALTER TABLE codenvy_invite_action ADD CONSTRAINT fk_codenvy_invite_action_invite_id FOREIGN KEY (invite_id) REFERENCES codenvy_invite (id);
--------------------------------------------------------------------------------
