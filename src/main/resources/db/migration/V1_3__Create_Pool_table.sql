-- create the new Pool table to store pool requests
CREATE TABLE juror_mod.pool (
	pool_no varchar(9) NOT NULL,
	"owner" varchar(3) NOT NULL,
	return_date timestamp(0) NOT NULL,
	no_requested integer NULL DEFAULT 0,
	pool_type varchar(3) NULL,
	loc_code varchar(3) NULL,
	new_request varchar(1) NULL DEFAULT 'Y'::character varying,
	last_update timestamp(0) NULL,
	additional_summons integer NULL,
	attend_time timestamp(0) NULL,
	nil_pool boolean null default false,
	CONSTRAINT pool_pk PRIMARY KEY (pool_no)
);

CREATE INDEX pool_rtndate_loccode ON juror_mod.pool USING btree (return_date, loc_code);


