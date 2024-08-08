-- performance indexes creation moved to V2_12__create_performance_indexes.sql from
-- stabilisation3 fixes to avoid deployment issues

drop index juror_history_history_code_idx;
CREATE INDEX juror_history_history_code_idx ON juror_mod.juror_history USING btree (history_code, juror_number);
CREATE INDEX bulk_print_data_form_type_idx ON juror_mod.bulk_print_data (form_type, juror_no);
CREATE INDEX bulk_print_data_juror_no_idx ON juror_mod.bulk_print_data (juror_no);
CREATE INDEX bulk_print_data_creation_date_idx ON juror_mod.bulk_print_data (creation_date,form_type,juror_no);

CREATE INDEX juror_summons_file_idx ON juror_mod.juror (summons_file);