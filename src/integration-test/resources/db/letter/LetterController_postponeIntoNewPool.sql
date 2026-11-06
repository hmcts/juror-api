-- Model postponement directly into a future pool using the base postponement-letter fixture.
UPDATE juror_mod.juror_pool
SET is_active = false, postpone = true, def_date = CURRENT_DATE + 90, last_update = CURRENT_TIMESTAMP
WHERE juror_number IN ('555555551', '555555552');

INSERT INTO juror_mod.pool
    (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('400', '415220402', CURRENT_DATE + 90, 4, 2, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror_pool
    (owner, juror_number, pool_number, status, is_active, def_date, deferral_code, postpone, was_deferred, next_date)
VALUES
    ('400', '555555551', '415220402', 2, true, NULL, NULL, NULL, true, CURRENT_DATE + 90),
    ('400', '555555552', '415220402', 2, true, NULL, NULL, NULL, true, CURRENT_DATE + 90);

UPDATE juror_mod.bulk_print_data
SET extracted_flag = false
WHERE juror_no IN ('555555551', '555555552');

-- An older postponement must not supply the displayed date.
INSERT INTO juror_mod.juror_pool
    (owner, juror_number, pool_number, status, is_active, def_date, deferral_code, postpone, last_update)
VALUES ('400', '555555551', '411220401', 7, false, CURRENT_DATE + 30, 'P', true,
        CURRENT_TIMESTAMP - INTERVAL '1 day');
