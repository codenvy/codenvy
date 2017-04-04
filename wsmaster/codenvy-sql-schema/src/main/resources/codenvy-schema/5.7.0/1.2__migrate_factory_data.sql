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


-- Factory button migration ----------------------------------------------------
INSERT INTO che_factory_button
SELECT * FROM button;
--------------------------------------------------------------------------------


-- Factory action migration ----------------------------------------------------
INSERT INTO che_factory_action(entity_id, id)
SELECT                         entityid,  id
FROM action;
--------------------------------------------------------------------------------


-- Factory action properties migration -----------------------------------------
INSERT INTO che_factory_action_properties(action_entity_id, property_value, property_key)
SELECT                                    action_entityid,  properties,     properties_key
FROM action_properties;
--------------------------------------------------------------------------------


-- Factory on app closed action migration --------------------------------------
INSERT INTO che_factory_on_app_closed_action
SELECT * FROM onappclosed;
--------------------------------------------------------------------------------


-- Factory on propjects loaded action migration --------------------------------
INSERT INTO che_factory_on_projects_loaded_action
SELECT * FROM onprojectsloaded;
--------------------------------------------------------------------------------


-- Factory on app loaded action migration --------------------------------------
INSERT INTO che_factory_on_app_loaded_action
SELECT * FROM onapploaded;
--------------------------------------------------------------------------------


-- Factory on app closed action values migration -------------------------------
INSERT INTO che_factory_on_app_closed_action_value(on_app_closed_id, action_entity_id)
SELECT                                             onappclosed_id,   actions_entityid
FROM onappclosed_action;
--------------------------------------------------------------------------------


-- Factory on projects loaded action values migration --------------------------
INSERT INTO che_factory_on_projects_loaded_action_value(on_projects_loaded_id, action_entity_id)
SELECT                                                  onprojectsloaded_id,   actions_entityid
FROM onprojectsloaded_action;
--------------------------------------------------------------------------------


-- Factory on app loaded action values migration -------------------------------
INSERT INTO che_factory_on_app_loaded_action_value(on_app_loaded_id, action_entity_id)
SELECT                                             onapploaded_id,   actions_entityid FROM onapploaded_action;
--------------------------------------------------------------------------------

-- Factory ide migration -------------------------------------------------------
INSERT INTO che_factory_ide(id, on_app_closed_id, on_app_loaded_id, on_projects_loaded_id)
SELECT                      id, onappclosed_id,   onapploaded_id,   onprojectsloaded_id   FROM ide;
--------------------------------------------------------------------------------


-- Factory migration -----------------------------------------------------------
INSERT INTO che_factory(id, name, version, created, user_id, creation_strategy, match_reopen, referrer, since, until, button_id, ide_id, workspace_id)
SELECT                  id, name, version, created, userid,  creation_strategy, match_reopen, referer,  since, until, button_id, ide_id, workspace_id
FROM factory;
--------------------------------------------------------------------------------


-- Factory images migration ----------------------------------------------------
INSERT INTO che_factory_image(image_data, media_type, name, factory_id)
SELECT                        imagedata,  mediatype,  name, factory_id
FROM factory_images;
--------------------------------------------------------------------------------


-- Drop old factory tables -----------------------------------------------------
DROP TABLE factory_images;
DROP TABLE factory;
DROP TABLE button;
DROP TABLE ide;
DROP TABLE onappclosed_action;
DROP TABLE onprojectsloaded_action;
DROP TABLE onapploaded_action;
DROP TABLE onappclosed;
DROP TABLE onprojectsloaded;
DROP TABLE onapploaded;
DROP TABLE action_properties;
DROP TABLE action;
--------------------------------------------------------------------------------
