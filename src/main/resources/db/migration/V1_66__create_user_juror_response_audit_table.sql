create sequence juror_mod.user_juror_response_audit_seq
	increment by 1
	minvalue 1
	maxvalue 9223372036854775807
	start 1
	cache 1;

create table juror_mod.user_juror_response_audit (
	id int8 not null primary key default nextval('juror_mod.user_juror_response_audit_seq'::regclass),
	juror_number varchar(9) not null,
	assigned_by varchar(20) null,
	assigned_to varchar(20) null,
	assigned_on timestamp(0) not null,
	constraint user_juror_response_fk foreign key (juror_number) references juror_mod.juror_response(juror_number)
);