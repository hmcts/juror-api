CREATE TABLE juror_mod.juror_third_party (
	juror_number varchar(9) NOT NULL,
	first_name varchar(50) NULL,
	last_name varchar(50) NULL,
	relationship varchar(50) NULL,
	main_phone varchar(50) NULL,
	other_phone varchar(50) NULL,
	email_address varchar(254) NULL,
	reason varchar(1250) NULL,
	other_reason varchar(1250) NULL,
	contact_juror_by_phone bool not NULL,
	contact_juror_by_email bool not null,
	CONSTRAINT juror_third_party_pk PRIMARY KEY (juror_number)
);

CREATE TABLE juror_mod.juror_third_party_audit (
    revision int8 NOT NULL,
    rev_type int4 NULL,
	juror_number varchar(9) NOT NULL,
	first_name varchar(50) NULL,
	last_name varchar(50) NULL,
	relationship varchar(50) NULL,
	main_phone varchar(50) NULL,
	other_phone varchar(50) NULL,
	email_address varchar(254) NULL,
	reason varchar(1250) NULL,
	other_reason varchar(1250) NULL,
	contact_juror_by_phone bool NULL,
	contact_juror_by_email bool null,
	CONSTRAINT juror_third_party_audit_pkey PRIMARY KEY (revision, juror_number),
    CONSTRAINT juror_third_party_fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info(revision_number)
);