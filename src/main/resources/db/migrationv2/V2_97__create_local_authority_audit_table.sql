
CREATE TABLE juror_er.local_authority_audit (
      revision int8 NOT NULL,
      rev_type int4 NULL,
      la_code varchar(3) NOT NULL,
      la_name varchar(100) NULL,
      is_active boolean,
      upload_status varchar(40) NULL,
      notes varchar(2000) NULL,
      inactive_reason varchar(2000) NULL,
      updated_by varchar(30) NULL,
      last_updated timestamp(3) NULL
);
