DROP SCHEMA IF EXISTS juror_dashboard CASCADE;
CREATE SCHEMA juror_dashboard;

CREATE TABLE juror_dashboard.stats_auto_processed (
	processed_date timestamp(0) NOT NULL,
	processed_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_auto_processed_pkey PRIMARY KEY (processed_date)
);

CREATE TABLE juror_dashboard.stats_deferrals (
	bureau_or_court varchar(6) NOT NULL,
	exec_code varchar(1) NOT NULL,
	calendar_year varchar(4) NOT NULL,
	financial_year varchar(7) NOT NULL,
	week varchar(7) NOT NULL,
	excusal_count int4 NOT NULL,
	CONSTRAINT stats_deferrals_pkey PRIMARY KEY (bureau_or_court, exec_code, calendar_year, financial_year, week)
);

CREATE TABLE juror_dashboard.stats_excusals (
	bureau_or_court varchar(6) NOT NULL,
	exec_code varchar(1) NOT NULL,
	calendar_year varchar(4) NOT NULL,
	financial_year varchar(7) NOT NULL,
	week varchar(7) NOT NULL,
	excusal_count int4 NOT NULL,
	CONSTRAINT stats_excusals_pkey PRIMARY KEY (bureau_or_court, exec_code, calendar_year, financial_year, week)
);

CREATE TABLE juror_dashboard.stats_not_responded (
	summons_month timestamp(0) NOT NULL,
	loc_code varchar(3) NOT NULL,
	non_responsed_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_not_responded_pkey PRIMARY KEY (summons_month, loc_code)
);

CREATE TABLE juror_dashboard.stats_response_times (
	summons_month timestamp(0) NOT NULL,
	response_month timestamp(0) NOT NULL,
	response_period varchar(15) NOT NULL,
	loc_code varchar(3) NOT NULL,
	response_method varchar(13) NOT NULL,
	response_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_response_times_pkey PRIMARY KEY (summons_month, response_month, response_period, loc_code, response_method)
);

CREATE TABLE juror_dashboard.stats_thirdparty_online (
	summons_month timestamp(0) NOT NULL,
	thirdparty_response_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_thirdparty_online_pkey PRIMARY KEY (summons_month)
);

CREATE TABLE juror_dashboard.stats_unprocessed_responses (
	loc_code varchar(3) NOT NULL,
	unprocessed_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_unprocessed_responses_pkey PRIMARY KEY (loc_code)
);

CREATE TABLE juror_dashboard.stats_welsh_online_responses (
	summons_month timestamp(0) NOT NULL,
	welsh_response_count int4 NULL DEFAULT 0,
	CONSTRAINT stats_welsh_online_responses_pkey PRIMARY KEY (summons_month)
);

CREATE TABLE juror_dashboard.survey_response (
	id varchar(20) NOT NULL,
	survey_id varchar(20) NOT NULL,
	user_no numeric(38) NULL,
	survey_response_date timestamp(0) NULL,
	satisfaction_desc varchar(50) NULL,
	created timestamp NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'::text),
	CONSTRAINT survey_response_pkey PRIMARY KEY (id, survey_id)
);