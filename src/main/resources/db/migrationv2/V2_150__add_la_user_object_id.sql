-- Existing users will be populated separately from an Entra extract.
ALTER TABLE juror_er."user"
    ADD COLUMN IF NOT EXISTS object_id varchar(36);

-- A user can belong to several LAs, but an object ID must identify at most one user per LA.
CREATE UNIQUE INDEX IF NOT EXISTS user_object_id_la_code_idx
    ON juror_er."user" (upper(object_id), la_code);
