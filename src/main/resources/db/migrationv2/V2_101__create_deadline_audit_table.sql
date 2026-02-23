
CREATE TABLE juror_er.deadline_audit (
      revision int8 NOT NULL,
      rev_type int4 NULL,
      id smallint NOT NULL,
      deadline_date date NULL,
      updated_by varchar(30) NULL,
      last_updated timestamp(3) NULL
);
