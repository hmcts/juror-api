-- add new auditing user properties to users table
alter table juror_mod.users
add column  created_by varchar(20) null,
add column  updated_by varchar(20) null;

create table juror_mod.users_audit (
	revision int8 not null,
	rev_type int4 null,
	"owner" varchar(3) null,
	username varchar(20) not null,
	"name" varchar(50) not null,
	active bool default true not null,
	approval_limit numeric(8, 2) default 0 not null,
	user_type varchar(30) null,
	email varchar(200) not null,
	created_by varchar(20) null,
	updated_by varchar(20) null,
	constraint users_audit_pkey primary key (revision, username),
	constraint fk_revision_number foreign key (revision) references juror_mod.rev_info(revision_number)
);

create table juror_mod.user_roles_audit (
	revision int8 not null,
	rev_type int4 null,
	username varchar(20) not null,
	role varchar(30) not null
);

create table juror_mod.user_courts_audit (
	revision int8 not null,
	rev_type int4 null,
	username varchar(20) not null,
	loc_code varchar(3) not null
);