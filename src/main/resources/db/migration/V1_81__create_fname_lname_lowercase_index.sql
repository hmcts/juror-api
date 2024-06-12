drop index last_name_1;
create index juror_fname_idx on juror_mod.juror (lower(first_name));
create index juror_lname_idx on juror_mod.juror (lower(last_name));