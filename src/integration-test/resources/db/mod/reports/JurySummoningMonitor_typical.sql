
-- TODO expand on this to include more data for testing and update the test to match

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415230701', '2024-03-04', 2, 2, 'CRO','415', 'N', '2024-03-04');

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '457230702', current_date - 20, 1, null, 'CRO', '457', 'N', current_date - 30);

-- Create Pool Member records
INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('111111111', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN',  true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES
('400', '111111111', '415230701', true, null, 2),
('400', '111111111', '457230702', true, null, 2);
