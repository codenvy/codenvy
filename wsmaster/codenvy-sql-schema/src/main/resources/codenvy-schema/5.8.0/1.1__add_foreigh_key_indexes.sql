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

CREATE INDEX index_member_actions_memberid ON member_actions (member_id);
CREATE INDEX index_worker_actions_workerid ON worker_actions (worker_id);
CREATE INDEX index_stackpermissions_actions_stackpermissionsid ON stackpermissions_actions (stackpermissions_id);
CREATE INDEX index_systempermissions_actions_systempermissionsid ON systempermissions_actions (systempermissions_id);
CREATE INDEX index_recipepermissions_actions_recipepermissionsid ON recipepermissions_actions (recipepermissions_id);
