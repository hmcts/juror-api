alter table juror_mod.bulk_print_data alter column extracted_flag set default false;
alter table juror_mod.bulk_print_data alter column extracted_flag set not null;

create index bulk_print_data_extracted_flag_idx on juror_mod.bulk_print_data (extracted_flag);
