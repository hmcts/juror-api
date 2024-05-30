update juror_mod.bulk_print_data
set digital_comms = false
where digital_comms is null;


alter table juror_mod.bulk_print_data
    alter column digital_comms set default false,
    alter column digital_comms set NOT NULL;