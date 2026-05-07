
-- JS-804: Update user table to have composite primary key of (username, la_code)
-- This is to allow users to have access to multiple local authorities without needing to
-- create multiple user accounts

-- Drop the foreign key
ALTER TABLE juror_er.file_uploads
DROP CONSTRAINT file_uploads_username_fk;

-- Drop the old primary key
ALTER TABLE juror_er."user"
DROP CONSTRAINT user_pkey;

-- Create new composite primary key
ALTER TABLE juror_er."user"
ADD CONSTRAINT user_pkey PRIMARY KEY (username, la_code);

-- Recreate foreign key to reference composite key
ALTER TABLE juror_er.file_uploads
ADD CONSTRAINT file_uploads_username_fk
FOREIGN KEY (la_username, la_code)
REFERENCES juror_er."user" (username, la_code);
