-- juror_mod.payment_file_count definition
-- needs updating as part of migration to set the next value based on the maximum migrated value
CREATE SEQUENCE juror_mod.payment_file_count
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 999999999
	START 1
	CACHE 1
	NO CYCLE;

-- juror_mod.payment_data_invoice_number definition
-- needs updating as part of migration to set the next value based on the maximum migrated value in part_invoice (last 7
--   digits)
CREATE SEQUENCE juror_mod.payment_data_invoice_number_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9999999
	START 1
	CACHE 1
	CYCLE;

-- juror_mod.payment_data_unique_id_seq definition
-- needs updating as part of migration to set the next value based on the maximum migrated value in unique_id
CREATE SEQUENCE juror_mod.payment_data_unique_id_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9999999
	START 1
	CACHE 1
	CYCLE;

-- juror_mod.content_store_seq definition
-- needs updating as part of migration to set the next value based on the latest request_id in the latest file in
-- content_store
CREATE SEQUENCE juror_mod.content_store_seq
	INCREMENT BY 1
	MINVALUE 1
	MAXVALUE 9223372036854775807
	START 1
	CACHE 1
	NO CYCLE;

CREATE TABLE juror_mod.payment_data (
	loc_code varchar(3) NOT NULL,
	unique_id varchar(7) NOT NULL DEFAULT nextval('juror_mod.payment_data_unique_id_seq'),
	creation_date timestamp(0) NOT NULL DEFAULT statement_timestamp(),
	expense_total numeric(8, 2) NOT NULL,
	juror_number varchar(9) NOT NULL, -- first 9 digits of part_invoice
	invoice_id varchar(7) NOT NULL DEFAULT nextval('juror_mod.payment_data_invoice_number_seq'),-- last 7 digits of part_invoice
	bank_sort_code varchar(6) NULL,
	bank_ac_name varchar(18) NULL,
	bank_ac_number varchar(8) NULL,
	build_soc_number varchar(18) NULL,
	address_line_1 varchar(35) NULL,  -- renamed from address_line1
	address_line_2 varchar(35) NULL,  -- renamed from address_line2
	address_line_3 varchar(35) NULL,  -- renamed from address_line3
	address_line_4 varchar(35) NULL,  -- renamed from address_line4
	address_line_5 varchar(35) NULL,  -- renamed from address_line5
	postcode varchar(10) NULL,
	auth_code varchar(9) NOT NULL,  -- renamed from aramis_auth_code
	juror_name varchar(50) NOT NULL, -- Juror name (concatenation of title, fname and lname separated by a space)
	loc_cost_centre varchar(5) NOT NULL,
	travel_total numeric(8, 2) NULL,
	subsistence_total numeric(8, 2) NULL, -- renamed from sub_total
	financial_loss_total numeric(8, 2) NULL,  -- renamed from floss_total
	expense_file_name varchar(30) NULL,  -- renamed from con_file_ref
	extracted boolean not null default false, -- new property derived from if expense_file_name exists (for improved indexing)
	CONSTRAINT payment_data_pkey PRIMARY KEY (loc_code, unique_id)
);

CREATE INDEX payment_data_extracted_idx ON juror_mod.payment_data USING btree (extracted);