-- This SQL script alters the 'user_id' column in the 'pool_comments' table within the 'juror_mod' schema.
-- It changes the data type of the 'user_id' column to VARCHAR(30) and sets it to NOT NULL.


ALTER TABLE juror_mod.pool_comments
ALTER COLUMN user_id TYPE VARCHAR(30),
ALTER COLUMN user_id SET NOT NULL;
