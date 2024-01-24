DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.pool_history;
DELETE FROM juror_mod.pool_comments;
DELETE FROM juror_mod.pool;
DELETE FROM juror_mod.juror;

-- POOL REQUESTS
-- CHESTER COURT (415)
-- Requested Pool - with the court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('415', '415230101', TIMESTAMP '2023-01-16 00:00:00.000000', 100, 100, 'CRO', '415', 'Y');


-- CHICHESTER COURT (416)
-- Nil Pool - with the bureau
INSERT INTO juror_mod.pool
(owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, nil_pool)
VALUES('400', '416230101', TIMESTAMP '2023-01-09 00:00:00.000000', 0, 0, 'CIV', '416', 'N', true);
-- Active Pool - with the court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('416', '416230102', TIMESTAMP '2023-01-16 00:00:00.000000', 100, 100, 'CRO', '416', 'N');
-- Active Pool - with the court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('416', '416230103', TIMESTAMP '2023-01-23 00:00:00.000000', 100, 100, 'CRO', '416', 'N');

-- COVENTRY COURT (417)
-- Active Pool - with the court - active jurors
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('417', '417230101', TIMESTAMP '2023-01-09 00:00:00.000000', 100, 100, 'CIV', '417', 'N');
-- Completed Pool - with the bureau - no active jurors
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('400', '417230102', TIMESTAMP '2023-01-16 00:00:00.000000', 100, 100, 'CRO', '417', 'N');
-- Nil Pool - with the bureau
INSERT INTO juror_mod.pool
(owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, nil_pool)
VALUES('400', '417230103', TIMESTAMP '2023-01-23 00:00:00.000000', 0, 0, 'CRO',
'417', 'N', true);
-- Requested Pool - with the court
INSERT INTO JUROR_MOD.POOL
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST)
VALUES('417', '417230104', TIMESTAMP '2023-01-30 00:00:00.000000', 100, 100,
'CIV', '417', 'Y');
-- Court Only Pool - Future Service Start Date (Active)
INSERT INTO juror_mod.pool
(owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, nil_pool)
VALUES('417', '417230105', current_date + interval '1 week', 0, 0,
'CRO', '417', 'N', false);
-- Court Only Pool - Past Service Start Date - no active jurors (Completed)
INSERT INTO juror_mod.pool
(owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, nil_pool)
VALUES('417', '417230106', current_date - interval '1 week', 0, 0,
'CRO', '417', 'N', false);
-- Court Only Pool - Past Service Start Date - active jurors (Active)
INSERT INTO juror_mod.pool
(owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, nil_pool)
VALUES('417', '417230107', current_date - interval '1 week', 0, 0,
'CRO', '417', 'N', false);

-- POOL MEMBERS

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,completion_date) VALUES
('641600001', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700001', 'PERSON', 'TEST', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '2023-01-23'),
('641700002', 'PERSON_TWO', 'TEST_TWO', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('641700003', 'PERSON_THREE', 'TEST_THREE', '1990-05-16', '542 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status) VALUES
('416', '641600001', '416230103', true, '2023-01-23', 2),
('417', '641700001', '417230101', true, '2023-01-09', 13),
('417', '641700002', '417230101', true, '2023-01-09', 7),
('417', '641700003', '417230107', true, current_date, 2);
