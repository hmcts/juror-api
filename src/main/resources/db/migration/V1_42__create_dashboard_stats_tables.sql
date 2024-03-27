create schema if not exists juror_dashboard;

create table juror_dashboard.stats_auto_processed (
	processed_date timestamp(0) not null,
	processed_count int not null default 0,
	constraint stats_auto_processed_pkey primary key (processed_date)
);

create table juror_dashboard.stats_deferrals (
	bureau_or_court varchar(6) not null,
	exec_code varchar(2) not null,
	calendar_year varchar(4) not null,
	financial_year varchar(7) not null,
	week varchar(7) not null,
	excusal_count int not null,
	constraint stats_deferrals_pkey primary key (bureau_or_court, exec_code, calendar_year, financial_year, week),
	constraint stats_deferrals_exc_code_fk foreign key (exec_code) references juror_mod.t_exc_code(exc_code)

);

create table juror_dashboard.stats_excusals (
	bureau_or_court varchar(6) not null,
	exec_code varchar(2) not null,
	calendar_year varchar(4) not null,
	financial_year varchar(7) not null,
	week varchar(7) not null,
	excusal_count int not null,
	constraint stats_excusals_pkey primary key (bureau_or_court, exec_code, calendar_year, financial_year, week),
	constraint stats_excusals_exc_code_fk foreign key (exec_code) references juror_mod.t_exc_code(exc_code)
);

create table juror_dashboard.stats_not_responded (
	summons_month timestamp(0) not null,
	loc_code varchar(3) not null,
	non_responsed_count int not null default 0,
	constraint stats_not_responded_pkey primary key (summons_month, loc_code),
	constraint stats_not_responded_loc_code_fk foreign key (loc_code) references juror_mod.court_location(loc_code)
);

create table juror_dashboard.stats_response_times (
	summons_month timestamp(0) not null,
	response_month timestamp(0) not null,
	response_period varchar(15) not null,
	loc_code varchar(3) not null,
	response_method varchar(13) not null,
	response_count int not null default 0,
	constraint stats_response_times_pkey primary key (summons_month, response_month, response_period, loc_code, response_method),
	constraint stats_response_times_loc_code_fk foreign key (loc_code) references juror_mod.court_location(loc_code)
);

create table juror_dashboard.stats_thirdparty_online (
	summons_month timestamp(0) not null,
	thirdparty_response_count int not null default 0,
	constraint stats_thirdparty_online_pkey primary key (summons_month)
);

create table juror_dashboard.stats_unprocessed_responses (
	loc_code varchar(3) not null,
	unprocessed_count int not null default 0,
	constraint stats_unprocessed_responses_pkey primary key (loc_code),
	constraint stats_unprocessed_responses_loc_code_fk foreign key (loc_code) references juror_mod.court_location(loc_code)
);

create table juror_dashboard.stats_welsh_online_responses (
	summons_month timestamp(0) not null,
	welsh_response_count int not null default 0,
	constraint stats_welsh_online_responses_pkey primary key (summons_month)
);

create table juror_dashboard.survey_response (
	id varchar(20) not null,
	survey_id varchar(20) not null,
	user_no int null,
	survey_response_date timestamp(0) null,
	satisfaction_desc varchar(50) null,
	created timestamp not null default (current_timestamp at time zone 'utc'::text),
	constraint survey_response_pkey primary key (id, survey_id)
);

create or replace view juror_dashboard.stats_not_responded_totals as
select  sum(coalesce(non_responsed_count, 0)) as not_responsed_total
from    juror_dashboard.stats_not_responded s;


create or replace view juror_dashboard.stats_response_times_totals as
select  sum(coalesce(response_count, 0)) as all_responses_total,
        sum(case
                when response_method::text = 'Online'::text then coalesce(response_count, 0)
                else 0
            end) as online_responses_total
from    juror_dashboard.stats_response_times s;