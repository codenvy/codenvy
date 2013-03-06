-----------------------------------------------------------------------------
-- Is used to calculate amount of active users per time-frame.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the timeframe
-- toDate     - ending of the timeframe
--
-- How to run:
-- pig -x local -param log="<DIRECTORY1>,<DIRECTORY2>..." 
--              -param date=<YYYYMMDD> -param toDate=<YYYYMMDD>
--              active-user-count.pig
---------------------------------------------------------------------------
IMPORT 'macros.pig';

log = LOAD '$log' using PigStorage() as (message : chararray);

--
-- Remove unrelated events
--
f1 = extractAndFilterByDate(log, $date, $toDate);
fR = FILTER f1 BY INDEXOF(message, 'EVENT#user-sso-logged-out#', 0) == -1;

--
-- extract user name out of USER identifier
--
a1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*USER\\#([^\\#]*)\\#.*')) AS user;
aR = FILTER a1 BY user != '';

--
-- extract user name out of message
--
b1 = FOREACH fR GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*\\[(.*)\\]\\[.*\\]\\[.*\\] - .*')) AS user;
bR = FILTER b1 BY user != '';

u1 = UNION aR, bR;
u2 = DISTINCT u1;
uR = FOREACH u2 GENERATE '$date' AS date, user; 

g1 = GROUP uR BY date;
result = FOREACH g1 GENERATE FLATTEN(group), '$toDate', COUNT(uR) AS value;

DUMP result;