
-- Snapshot of key columns from juror_pool table on pool transfer from Bureau to Court where status is 1 (summoned) or 2
-- (responded) and is_active is true. Also contains some columns from the juror table at that point in time.

create table juror_mod.bureau_snapshot (
	juror_number varchar(9) not null,
	pool_number varchar(9) not null,
	"owner" varchar(3) not null,
	user_edtq varchar(20) null,
	is_active bool null,
	status int4 null,
	def_date date null,
	pool_seq varchar(4) null,
	edit_tag varchar(1) null,
	next_date date null,
	was_deferred bool null,
	deferral_code varchar(2) null,
	postpone bool null,
	scan_code varchar(9) null,
	last_update timestamp null,
	reminder_sent bool null,
	transfer_date date null,
	date_created timestamp null,
	excusal_code varchar(2) null,  -- from juror
	acc_exc varchar(1) null, -- from juror
	police_check varchar(50) null, -- from juror
	constraint bureau_snapshot_pkey primary key (juror_number, pool_number)
);
create index bureau_snapshot_pool_no on juror_mod.bureau_snapshot using btree (pool_number);