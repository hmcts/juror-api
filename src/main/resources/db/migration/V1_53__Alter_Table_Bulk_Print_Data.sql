-- flyway script to alter the bulk_print_data table id column and create new sequence
DROP SEQUENCE IF EXISTS bulk_print_data_seq;
CREATE SEQUENCE bulk_print_data_seq;
ALTER TABLE juror_mod.bulk_print_data ALTER COLUMN id SET DEFAULT nextval('bulk_print_data_seq');
ALTER SEQUENCE bulk_print_data_seq OWNED BY juror_mod.bulk_print_data.id;