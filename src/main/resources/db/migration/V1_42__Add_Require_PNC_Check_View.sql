CREATE VIEW juror_mod.require_pnc_check_view AS
SELECT j.police_check                                                  as police_check,
       j.juror_number                                                  as juror_number,
       regexp_replace(j.first_name, '\s.*', '')                        as first_name,
       nullif(regexp_replace(j.first_name, '.*?\s', ''), j.first_name) as middle_name,
       j.last_name                                                     as last_name,
       j.dob                                                           as date_of_birth,
       regexp_replace(j.zip, '\s', '')                                 as post_code
FROM juror_mod.juror j
         JOIN juror_mod.juror_pool jp on jp.juror_number = j.juror_number
WHERE jp.status = 2
  and (
        j.police_check is null
        OR j.police_check not in ('UNCHECKED_MAX_RETRIES_EXCEEDED', 'ELIGIBLE', 'INELIGIBLE')
    )
  and jp.owner = '400'
  and jp.is_active = true;