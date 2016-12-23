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

-- Organization distributed resources  -----------------------------------------------------------
CREATE TABLE organization_distributed_resources (
    organization_id       VARCHAR(255)         NOT NULL,

    PRIMARY KEY (organization_id)
);
-- constraints
ALTER TABLE organization_distributed_resources ADD CONSTRAINT fk_organization_distributed_resources_organization_id FOREIGN KEY (organization_id) REFERENCES organization (id);
--------------------------------------------------------------------------------


-- Organization distributed resources to resource ------------------------------------------------
CREATE TABLE organization_distributed_resources_resource (
    organization_distributed_resources_id               VARCHAR(255)    NOT NULL,
    resource_id                                         BIGINT          NOT NULL,

    PRIMARY KEY (organization_distributed_resources_id, resource_id)
);
-- constraints
ALTER TABLE organization_distributed_resources_resource ADD CONSTRAINT fk_organization_distributed_resources_resource_resource_id FOREIGN KEY (resource_id) REFERENCES resource (id);
ALTER TABLE organization_distributed_resources_resource ADD CONSTRAINT fk_organization_distributed_resources_resource_organization_distributed_resources_id FOREIGN KEY (organization_distributed_resources_id) REFERENCES organization_distributed_resources (organization_id);
--------------------------------------------------------------------------------
