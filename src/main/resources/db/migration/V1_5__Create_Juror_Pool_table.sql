CREATE TABLE juror_mod.juror_pool (
	juror_number varchar(9) NOT NULL,
	pool_number varchar(9) NOT NULL,
	"owner" varchar(3) NOT NULL,
	ret_date date NOT NULL,
	user_edtq varchar(20) NULL,
	is_active bool NULL,
	status integer NULL,
	times_sel integer NULL,
	def_date date NULL,
	mileage integer NULL,
	"location" varchar(6) NULL,
	no_attendances integer NULL,
	no_attended integer NULL,
	no_fta integer NULL,
	no_awol integer NULL,
	pool_seq varchar(4) NULL,
	edit_tag varchar(1) NULL,
	next_date date NULL,
	on_call bool NULL,
	smart_card varchar(20) NULL,
	amt_spent float8 NULL,
	completion_flag bool NULL,
	completion_date date NULL,
	was_deferred bool NULL,
	deferral_code varchar(1) NULL,
	id_checked varchar(1) NULL,
	postpone bool NULL,
	paid_cash bool NULL,
	travel_time float8 NULL,
	scan_code varchar(9) NULL,
	financial_loss float8 NULL,
	last_update timestamp NULL,
	reminder_sent bool NULL,
	transfer_date date NULL,
	date_created timestamp NULL,
	CONSTRAINT juror_pool_pkey PRIMARY KEY (juror_number, pool_number),
	CONSTRAINT Juror_pool_fk_juror FOREIGN KEY (juror_number) REFERENCES juror_mod.juror
);

CREATE INDEX i_pool_no ON juror_mod.juror_pool USING btree (pool_number);
CREATE INDEX i_juror_no ON juror_mod.juror_pool USING btree (juror_number);
CREATE INDEX i_next_date ON juror_mod.juror_pool USING btree (next_date);
CREATE INDEX i_comp_date ON juror_mod.juror_pool USING btree (completion_date);