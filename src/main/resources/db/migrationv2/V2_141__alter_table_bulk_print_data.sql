ALTER TABLE juror_mod.bulk_print_data
    ADD COLUMN extracted_date timestamp NULL;

CREATE INDEX bulk_print_data_extracted_date_idx
    ON juror_mod.bulk_print_data USING btree (extracted_date, form_type);
