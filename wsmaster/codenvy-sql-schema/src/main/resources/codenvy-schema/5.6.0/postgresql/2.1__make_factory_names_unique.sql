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


-- Generate names where they are not set.
UPDATE factory
SET name = concat('f', right(id, 9)) WHERE name IS NULL OR name = '';

-- Make names unique for the same user, e.g if there is more than one factory with same name and user,
-- leave first one and rename others.
WITH dupes AS
  ( SELECT id, userid, name
    FROM factory
    WHERE (name,userid)
    IN (SELECT name, userid
      FROM factory
      GROUP BY name, userid
      HAVING count(*) > 1)
  ),
  uniques AS
  ( WITH q as
    ( SELECT *, row_number()
      OVER (PARTITION BY name,userid ORDER BY name,userid)
      AS rn FROM factory
    )
    SELECT id FROM q WHERE rn = 1
  )
UPDATE factory
SET name = concat(factory.name, '-', right(dupes.id, 9))
FROM dupes, uniques
WHERE dupes.id = factory.id AND NOT EXISTS (SELECT id FROM uniques WHERE factory.id = uniques.id);
