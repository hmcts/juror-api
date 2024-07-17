update juror_mod.juror_response
set postcode = upper(postcode)
where postcode != upper(postcode);

update juror_mod.juror
set postcode = upper(postcode)
where postcode != upper(postcode);

update juror_mod.juror_audit
set postcode = upper(postcode)
where postcode != upper(postcode);