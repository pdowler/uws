ALTER TABLE <schema>.Job
ADD COLUMN jobInfo_content_arr text[];

UPDATE <schema>.Job
SET jobInfo_content_arr = ARRAY[jobInfo_content]
WHERE jobInfo_content IS NOT NULL;

ALTER TABLE <schema>.Job
DROP COLUMN jobInfo_content;

ALTER TABLE <schema>.Job
RENAME COLUMN jobInfo_content_arr TO jobInfo_content;