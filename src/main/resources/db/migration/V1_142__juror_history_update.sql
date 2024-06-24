update juror_mod.t_history_code
set template = E'Service completed {other_info_date:d MMM yyyy}'
where history_code in ('SCOM');

update juror_mod.t_history_code
set template = E'{other_information}'
where history_code in ('RMES','RSUP');

update juror_mod.t_history_code
set template = E'Trial: {other_info_reference}'
where history_code in ('VADD');

update juror_mod.t_history_code
set template = E'{other_information}\nTransferred on {other_info_date:d MMM yyyy}'
where history_code in ('PTRA');

create index juror_fname_lname_idx on juror_mod.juror (lower(((first_name||' ')||last_name)));
create index juror_postcode_idx on juror_mod.juror (lower(postcode));
