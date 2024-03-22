INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415230701', '400', CURRENT_DATE + 0, 14, 14, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode,
                             responded,
                             police_check)
VALUES ('111111111', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        null),
       ('111111112', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'UNCHECKED_MAX_RETRIES_EXCEEDED'),
       ('111111113', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'IN_PROGRESS'),
       ('111111114', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ELIGIBLE'),
       ('111111115', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'INELIGIBLE'),
       ('111111116', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'NOT_CHECKED'),
       ('111111117', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ERROR_RETRY_CONNECTION_ERROR'),
       ('111111118', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ERROR_RETRY_NAME_HAS_NUMERICS'),
       ('111111119', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ERROR_RETRY_OTHER_ERROR_CODE'),
       ('111111120', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ERROR_RETRY_NO_ERROR_REASON'),
       ('111111121', NULL, 'LNAMEONE', 'FNAMEONE', '1990-07-25', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
        'ERROR_RETRY_UNEXPECTED_EXCEPTION'),
       ('111111122', NULL, 'LNAMEONE', 'FNAMEONE', null, '543 STREET NAME', 'ANYTOWN', null, true,
        'INSUFFICIENT_INFORMATION');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status, next_date)
VALUES ('400', '111111111', '415230701', true, 2, current_date),
       ('400', '111111112', '415230701', true, 2, current_date),
       ('400', '111111113', '415230701', true, 2, current_date),
       ('400', '111111114', '415230701', true, 2, current_date),
       ('400', '111111115', '415230701', true, 2, current_date),
       ('400', '111111116', '415230701', true, 2, current_date),
       ('400', '111111117', '415230701', true, 2, current_date),
       ('400', '111111118', '415230701', true, 2, current_date),
       ('400', '111111119', '415230701', true, 2, current_date),
       ('400', '111111120', '415230701', true, 2, current_date),
       ('400', '111111121', '415230701', true, 2, current_date),
       ('400', '111111122', '415230701', true, 2, current_date);
