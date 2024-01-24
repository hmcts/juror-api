-- juror_mod.app_settings definition
DROP TABLE IF EXISTS juror_mod.app_settings;
SELECT setting, value INTO juror_mod.app_settings from juror_digital.app_settings;
ALTER TABLE juror_mod.app_settings ADD CONSTRAINT app_settings_pkey PRIMARY KEY (setting);

-- juror_mod.accused definition
DROP TABLE IF EXISTS juror_mod.accused;
CREATE TABLE juror_mod.accused (
	"owner" varchar(3) NOT NULL,
	trial_no varchar(16) NOT NULL,
	lname varchar(20) NOT NULL,
	fname varchar(20) NOT NULL,
	CONSTRAINT accused_pkey PRIMARY KEY (owner, trial_no, lname, fname)
);
CREATE INDEX accused_trial_idx ON juror_mod.accused USING btree (owner, trial_no);

-- juror_mod.content_store definition
DROP TABLE IF EXISTS juror_mod.content_store;
CREATE TABLE juror_mod.content_store (
	request_id int8 NULL,
	document_id varchar(50) NOT NULL,
	date_on_q_for_send timestamp(0) NULL DEFAULT statement_timestamp(),
	file_type varchar(10) NOT NULL,
	date_sent timestamp(0) NULL,
	"data" text NULL
);

-- juror_mod.export_placeholders definition
DROP TABLE IF EXISTS juror_mod.export_placeholders;
CREATE TABLE juror_mod.export_placeholders (
	placeholder_name varchar(48) NOT NULL,
	source_table_name varchar(48) NOT NULL,
	source_column_name varchar(48) NOT NULL,
	"type" varchar(12) NOT NULL,
	description varchar(100) NULL,
	default_value varchar(200) NULL,
	editable varchar(1) NULL,
	validation_rule varchar(600) NULL,
	validation_message varchar(200) NULL,
	validation_format varchar(60) NULL
);

-- juror_mod.message definition
DROP TABLE IF EXISTS juror_mod.message;
CREATE TABLE juror_mod.message (
	part_no varchar(9) NOT NULL,
	file_datetime varchar(15) NOT NULL,
	username varchar(20) NOT NULL,
	loc_code varchar(3) NOT NULL,
	phone varchar(15) NULL,
	email varchar(254) NULL,
	loc_name varchar(100) NULL,
	pool_no varchar(9) NULL,
	subject varchar(50) NULL,
	message_text varchar(2000) NULL,
	message_id int8 NOT NULL,
	message_read varchar(2) NULL DEFAULT 'NR'::character varying,
	CONSTRAINT message_pkey PRIMARY KEY (part_no, file_datetime, username, loc_code)
);

-- juror_mod.password_export_placeholders definition
DROP TABLE IF EXISTS juror_mod.password_export_placeholders;
CREATE TABLE juror_mod.password_export_placeholders (
	"owner" varchar(3) NOT NULL,
	login varchar(20) NOT NULL,
	placeholder_name varchar(48) NOT NULL,
	use varchar(1) NULL
);

-- juror_mod.pool_transfer_weekday definition
DROP TABLE IF EXISTS juror_mod.pool_transfer_weekday;
CREATE TABLE juror_mod.pool_transfer_weekday (
	transfer_day varchar(3) NULL,
	run_day varchar(3) NULL,
	adjustment int2 NULL
);

-- juror_mod.utilisation_stats
DROP TABLE IF EXISTS juror_mod.utilisation_stats;
CREATE TABLE juror_mod.utilisation_stats (
	"owner" varchar(3) NOT NULL,
	month_start timestamp(0) NOT NULL,
	loc_code varchar(3) NOT NULL,
	available_days int4 NULL,
	attendance_days int4 NULL,
	sitting_days int4 NULL,
	no_trials int4 NULL,
	last_update timestamp(0) NULL,
	CONSTRAINT attendance_pkey PRIMARY KEY (month_start, loc_code, owner)
);
CREATE INDEX attendance_locde_mths_own ON juror_mod.utilisation_stats USING btree (loc_code, month_start, owner);

-- juror_mod.attendance foreign keys
ALTER TABLE juror_mod.utilisation_stats ADD CONSTRAINT utilisation_stats_fk FOREIGN KEY (loc_code)
REFERENCES juror_mod.court_location(loc_code);
