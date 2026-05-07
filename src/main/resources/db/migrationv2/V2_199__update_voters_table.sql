
-- JS-892 update voters table schema

-- back up the existing voters table before making changes
ALTER TABLE juror_mod.voters RENAME TO voters_old;
ALTER INDEX IF EXISTS juror_mod.voters_optimized_idx RENAME TO voters_old_optimized_idx;
ALTER INDEX IF EXISTS juror_mod.voters_postcode_start_idx RENAME TO voters_old_postcode_start_idx;

-- create the new voters table with the updated schema
CREATE TABLE juror_mod.voters (
  hash_id int8 NOT NULL,
  register_lett varchar(5) NULL,
  poll_number varchar(5) NULL,
  title varchar(10) NULL,
  lname varchar(25) NOT NULL,
  fname varchar(20) NOT NULL,
  dob date NULL,
  flags varchar(2) NULL,
  address varchar(35) NOT NULL,
  address2 varchar(35) NULL,
  address3 varchar(35) NULL,
  address4 varchar(35) NULL,
  address5 varchar(35) NULL,
  postcode varchar(10) NULL,
  date_selected1 date NULL,
  la_id int4 NULL,
  perm_disqual varchar(1) NULL,
  source_id varchar(1) NULL,
  postcode_start varchar(10) GENERATED ALWAYS AS (split_part(postcode::text, ' '::text, 1)) STORED,
  CONSTRAINT voters_pkey PRIMARY KEY (hash_id)
);

CREATE INDEX voters_optimized_idx ON juror_mod.voters USING btree (postcode_start, date_selected1, perm_disqual, flags);
CREATE INDEX voters_postcode_start_idx ON juror_mod.voters USING btree (postcode_start, perm_disqual, flags, dob);
