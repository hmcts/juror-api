INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, last_update)
VALUES ('400', '415220901', CURRENT_DATE + 10, 1, 1, 'CRO', '415', 'N', CURRENT_DATE),
       ('400', '416220902', CURRENT_DATE + 10, 1, 1, 'CRO', '416', 'N', CURRENT_DATE);

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded, notes, optic_reference, police_check)
VALUES ('555555563', 'LNAMEOVERAGE', 'FNAMEOVERAGE', CURRENT_DATE - INTERVAL '76 years' + INTERVAL '10 days',
        '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null, 'ELIGIBLE');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('400', '555555563', '415220901', true, CURRENT_DATE, 2);
