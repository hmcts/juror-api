
-- increase the size of the file_format column in the file_uploads table and
-- updated_by column in the local_authority table

ALTER TABLE juror_er.file_uploads
ALTER COLUMN file_format type varchar(100);

ALTER TABLE juror_er.local_authority
ALTER COLUMN updated_by type varchar(200);
