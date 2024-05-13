create table juror_mod.f_audit_mapping (
	loc_code varchar(3) not null,
	heritage_f_audit varchar(9) not null,
	f_audit varchar(9) null,
	constraint f_audit_mapping_pk primary key (loc_code, heritage_f_audit),
	constraint f_audit_mapping_court_location_fk foreign key (loc_code) references juror_mod.court_location(loc_code)
);