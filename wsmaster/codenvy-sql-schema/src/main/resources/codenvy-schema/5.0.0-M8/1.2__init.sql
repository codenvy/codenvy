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

-- License actions -------------------------------------------------------------
CREATE TABLE license_action (
    action_timestamp             BIGINT          NOT NULL,
    action_type                  VARCHAR(255)    NOT NULL,
    license_qualifier            VARCHAR(255),
    license_type                 VARCHAR(255)    NOT NULL,

    PRIMARY KEY (license_type, action_type)
);
--------------------------------------------------------------------------------

CREATE TABLE license_action_attributes (
    license_type                 VARCHAR(255)    NOT NULL,
    action_type                  VARCHAR(255)    NOT NULL,
    value                        VARCHAR(255)    NOT NULL,
    name                         VARCHAR(255)    NOT NULL
);

-- constraints
ALTER TABLE license_action_attributes ADD CONSTRAINT unq_license_action_attributes_0 UNIQUE (license_type, action_type, name);
ALTER TABLE license_action_attributes ADD CONSTRAINT fk_license_action_attributes_0 FOREIGN KEY (license_type, action_type) REFERENCES license_action (license_type, action_type);
--------------------------------------------------------------------------------
