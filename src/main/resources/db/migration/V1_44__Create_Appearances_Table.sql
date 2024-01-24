-- create the appearance table for checking in jurors
create table juror_mod.appearance (
	att_date date not null,
	juror_number varchar(9) not null,
	loc_code varchar(3) not null,
	faudit varchar(11) null,
	court_emp varchar(1) null,
	timein time null,
	timeout time null,
	pool_trial_no varchar(16) null,
	audits varchar(11) null,
	amount float null,
	app_stage int2 null,
	date_paid date null,
	non_attendance boolean null
);

alter table juror_mod.appearance
add constraint appearance_pkey primary key (juror_number, att_date, loc_code);

alter table juror_mod.appearance
add constraint app_loc_code_fk foreign key (loc_code) references juror_mod.court_location(loc_code);