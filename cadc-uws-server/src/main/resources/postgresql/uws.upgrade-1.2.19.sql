ALTER TABLE <schema>.Job
ALTER COLUMN jobInfo_content TYPE text[]
USING CASE WHEN jobInfo_content IS NULL THEN NULL ELSE ARRAY[jobInfo_content] END;