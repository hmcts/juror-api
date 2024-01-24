-- public.rev_info_seq definition
DROP SEQUENCE IF EXISTS public.rev_info_seq;

CREATE SEQUENCE public.rev_info_seq
	INCREMENT BY 50
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

-- juror_mod.rev_info definition
CREATE TABLE juror_mod.rev_info (
	revision_number bigint NOT NULL,
	revision_timestamp bigint NULL,
	CONSTRAINT rev_info_pkey PRIMARY KEY (revision_number)
);

-- juror_mod.juror_audit definition
CREATE TABLE juror_mod.juror_audit (
	revision bigint NOT NULL,
	juror_number varchar(255) NOT NULL,
	rev_type integer NULL,
	title varchar(255) NULL,
	first_name varchar(255) NULL,
	last_name varchar(255) NULL,
	dob date NULL,
	address varchar(255) NULL,
	address2 varchar(255) NULL,
	address3 varchar(255) NULL,
	address4 varchar(255) NULL,
	address5 varchar(255) NULL,
	address6 varchar(255) NULL,
	zip varchar(255) NULL,
	h_email varchar(255) NULL,
	h_phone varchar(255) NULL,
	m_phone varchar(255) NULL,
	w_phone varchar(255) NULL,
	w_ph_local varchar(255) NULL,
	bank_acct_name varchar(255) NULL,
	bank_acct_no varchar(255) NULL,
	bldg_soc_roll_no varchar(255) NULL,
	sort_code varchar(255) NULL,
	pending_title varchar(255) NULL,
	pending_first_name varchar(255) NULL,
	pending_last_name varchar(255) NULL,
	CONSTRAINT juror_audit_pkey PRIMARY KEY (revision, juror_number)
);

-- juror_mod.juror_audit foreign keys
ALTER TABLE juror_mod.juror_audit ADD CONSTRAINT fk_revision_number FOREIGN KEY (revision) REFERENCES juror_mod.rev_info
(revision_number);