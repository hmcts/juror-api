create table juror_mod.t_reply_type (
	"type" varchar(32) primary key,
	description varchar(1000)	
);

insert into juror_mod.t_reply_type ("type", "description")
values ('Paper', 'Paper response');

insert into juror_mod.t_reply_type ("type", "description")
values ('Digital', 'Online response');


CREATE TABLE juror_mod.juror_response (
	juror_number varchar(9) NOT NULL references juror_mod.juror ("juror_number"),
	date_received timestamp NULL,
	title varchar(10) NULL,
	first_name varchar(20) NULL,
	last_name varchar(20) NULL,
	address varchar(35) NULL,
	address2 varchar(35) NULL,
	address3 varchar(35) NULL,
	address4 varchar(35) NULL,
	address5 varchar(35) NULL,
	zip varchar(10) NULL,
	processing_status varchar(50) NULL,
	date_of_birth date NULL,
	phone_number varchar(15) NULL,
	alt_phone_number varchar(15) NULL,
	email varchar(254) NULL,
	residency boolean null,
	residency_detail varchar(1250) NULL,
	mental_health_act boolean null,
	mental_health_capacity boolean null,
	mental_health_act_details varchar(2020) NULL,
	bail boolean null ,
	bail_details varchar(1250) NULL,
	convictions boolean NULL ,
	convictions_details varchar(1250) NULL,	
	deferral boolean null ,
	deferral_reason varchar(1250) NULL,
	deferral_date varchar(1000) NULL,
	reasonable_adjustments_arrangements varchar(1000) NULL,
	excusal boolean null ,
	excusal_reason varchar(1250) NULL,
	processing_complete boolean null,
	signed boolean null,
	"version" integer NULL DEFAULT 0,
	thirdparty_fname varchar(50) NULL,
	thirdparty_lname varchar(50) NULL,
	relationship varchar(50) NULL,
	main_phone varchar(50) NULL,
	other_phone varchar(50) NULL,
	email_address varchar(254) NULL,
	thirdparty_reason varchar(1250) NULL,
	thirdparty_other_reason varchar(1250) NULL,
	juror_phone_details boolean NULL,
	juror_email_details boolean NULL,
	staff_login varchar(20) NULL,
	staff_assignment_date timestamp NULL,
	urgent boolean NULL,
	super_urgent boolean NULL,
	completed_at timestamp NULL,
	welsh boolean NULL,
	reply_type varchar(32) references juror_mod.t_reply_type ("type"),
	CONSTRAINT juror_response_pkey PRIMARY KEY (juror_number)
);


create or replace view juror_mod.juror_paper_response as
select
    juror_number,
    date_received,
    title,
    first_name,
    last_name,
    address,
    address2,
    address3,
    address4,
    address5,
    zip,
    processing_status,
    date_of_birth,
    phone_number,
    alt_phone_number,
    email,
    residency,
    mental_health_act,
    mental_health_capacity,
    bail,
    convictions,
    reasonable_adjustments_arrangements,
    relationship,
    thirdparty_reason,
    deferral
    excusal,
    signed,
    staff_login,
    urgent,
    super_urgent,
    processing_complete,
    completed_at,
    welsh
from juror_mod.juror_response jr
where LOWER(reply_type) = 'paper';

create or replace view juror_mod.juror_digital_response as 
select
    juror_number,
    date_received,
    title,
    first_name,
    last_name,
    address,
    address2,
    address3,
    address4,
    address5,
    zip,
    processing_status,
    date_of_birth,
    phone_number,
    alt_phone_number,
    email,
    residency,
    residency_detail,
    mental_health_act,
    mental_health_act_details,
    bail,
    bail_details,
    convictions,
    convictions_details,
    deferral_reason,
    deferral_date,
    reasonable_adjustments_arrangements,
    excusal_reason,
    processing_complete,
    version,
    thirdparty_fname,
    thirdparty_lname,
    relationship,
    main_phone,
    other_phone,
    email_address,
    thirdparty_reason,
    thirdparty_other_reason,
    juror_phone_details,
    juror_email_details,
    staff_login,
    staff_assignment_date,
    urgent,
    super_urgent,
    completed_at,
    welsh
from juror_mod.juror_response jr
where LOWER(reply_type) = 'digital';

CREATE TABLE juror_mod.juror_response_aud (
	juror_number varchar(9) NULL references juror_mod.juror_response ("juror_number"),
	changed timestamp NULL,
	login varchar(20) NULL,
	old_processing_status varchar(50) NULL,
	new_processing_status varchar(50) NULL
);

CREATE TABLE juror_mod.juror_response_cjs_employment (
	juror_number varchar(9) NOT NULL references juror_mod.juror_response ("juror_number"),
	cjs_employer varchar(100) NOT NULL,
	cjs_employer_details varchar(1000) NOT NULL,
	id bigserial NOT NULL,
	CONSTRAINT juror_response_cjs_employment_pkey PRIMARY KEY (id)
);


CREATE TABLE juror_mod.juror_reasonable_adjustment (
	juror_number varchar(9) NOT NULL,
	reasonable_adjustment varchar(1) references juror_mod.t_reasonable_adjustments (code),
	reasonable_adjustment_detail varchar(1000) NOT NULL,
	id bigserial NOT NULL,
	CONSTRAINT juror_response_reasonable_adjustments_pkey PRIMARY KEY (id)
);

CREATE TABLE juror_mod.staff_juror_response_audit (
	team_leader_login varchar(20) NOT NULL,
	staff_login varchar(20) NULL,
	juror_number varchar(9) NOT NULL references juror_mod.juror_response ("juror_number"),
	date_received timestamp NOT NULL,
	staff_assignment_date timestamp NOT NULL,
	created timestamp NOT NULL,
	"version" integer NULL,
	CONSTRAINT staff_juror_response_audit_pkey PRIMARY KEY (team_leader_login, juror_number, date_received, created)
);

ALTER TABLE juror_mod.juror
    ADD CONSTRAINT reasonable_adjustment_code_fk
    FOREIGN KEY (reasonable_adj_code) REFERENCES juror_mod.t_reasonable_adjustments (code)
