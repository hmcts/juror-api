-- add new sequence for generating new attendance_audit_numbers
create sequence juror_mod.attendance_audit_seq
	increment by 1
	minvalue 1
	maxvalue 99999999
	start 1
	cache 1
	cycle;

-- add new column in the appearance table to store the attendance_audit_number
alter table juror_mod.appearance add attendance_audit_number varchar(9) null;

-- add new column in the appearance_audit table to store the attendance_audit_number
alter table juror_mod.appearance_audit add attendance_audit_number varchar(9) null;

