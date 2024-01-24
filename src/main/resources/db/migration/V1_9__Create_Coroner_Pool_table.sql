CREATE TABLE juror_mod.coroner_pool (
	cor_pool_no varchar(9) not null,
	cor_name varchar(35) not null,
	email varchar(254) not null,
	phone varchar(15),
	cor_court_loc varchar(3) not null,
	cor_request_dt date not null,
	cor_service_dt date not null,
	cor_no_requested integer not null,
	CONSTRAINT cor_pool_pk PRIMARY KEY (cor_pool_no)
);