-- create the juror_mod.bulk_print_data table based on the juror.print_files table

CREATE TABLE juror_mod.bulk_print_data (
    id bigint NOT NULL,
	juror_no varchar(9) NOT NULL,
	creation_date date NOT NULL,
	form_type varchar(6) NOT NULL,
	detail_rec varchar(1260) NOT NULL,
	extracted_flag boolean,
	digital_comms boolean NULL DEFAULT false,
	CONSTRAINT bulk_print_data_pkey PRIMARY KEY (id)
);

CREATE SEQUENCE juror_mod.bulk_print_data_seq
	INCREMENT BY 1
	MINVALUE 0
	MAXVALUE 9223372036854775807
	START 1000
	CACHE 1
	NO CYCLE;