--
-- Copyright (c) 2012-2017 Red Hat, Inc.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--   Red Hat, Inc. - initial API and implementation
--

-- Organization migration ------------------------------------------------------
INSERT INTO che_organization(id, parent, account_id)
SELECT                       id, parent, account_id
FROM organization;


-- Members migration -----------------------------------------------------------
INSERT INTO che_member(id, organization_id, user_id)
SELECT                 id, organizationid,  userid
FROM member;

INSERT INTO che_member_actions(member_id, actions)
SELECT                         member_id, actions
FROM member_actions;


-- Organization resources migration --------------------------------------------
INSERT INTO che_organization_distributed_resources(organization_id)
SELECT                                             organization_id
FROM organization_distributed_resources;

INSERT INTO che_organization_distributed_resources_resource(organization_distributed_resources_id, resource_id)
SELECT                                                      organization_distributed_resources_id, resource_id
FROM organization_distributed_resources_resource;


-- Updates references for invite table -----------------------------------------
ALTER TABLE codenvy_invite DROP CONSTRAINT fk_codenvy_invite_org_id;
ALTER TABLE codenvy_invite ADD CONSTRAINT fk_codenvy_invite_org_id FOREIGN KEY (organization_id) REFERENCES che_organization (id);

DROP TABLE member_actions;
DROP TABLE member;

DROP TABLE organization_distributed_resources_resource;
DROP TABLE organization_distributed_resources;
DROP TABLE organization;
