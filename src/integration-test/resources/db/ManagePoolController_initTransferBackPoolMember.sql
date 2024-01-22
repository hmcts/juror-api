delete from juror_mod.pool_history;
DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.rev_info;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

-- Pool 415220701 - primary court - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230701', TIMESTAMP'2023-07-03 00:00:00.000000', 2, 2, 'CRO','415', 'N', TIMESTAMP'2022-06-08 09:22:09.0');

-- Pool 457220701 - primary court - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('457', '457230702', TIMESTAMP'2023-07-17 00:00:00.000000', 1, null, 'CRO', '457', 'N', TIMESTAMP'2022-06-08 09:22:09.0');

-- Create Pool Member records

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('111111111', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN',  true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('415', '111111111', '415230701', false, null, 10),
('457', '111111111', '457230702', true, CURRENT_DATE - interval '3 months', 2);


-- Create Part History records for pool members
INSERT INTO juror_mod.juror_history
(juror_number, date_created, history_code, user_id, other_information, pool_number) VALUES
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'DOB Changed', '415230701'),
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'First Name Changed', '415230701'),
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'Last Name Changed', '415230701'),
('111111111', '2023-06-09', 'RESP', 'BUREAU_USER', 'SOME OTHER INFO', '415230701'),
('111111111', '2023-06-09', 'RSUM', 'BUREAU_USER', 'SOME OTHER INFO', '415230701'),
( '111111111', '2023-06-09', 'APOL', 'COURT_USER', 'Pool Attendance', '457230702');

-- Create Participant Amendment records
INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp) VALUES
(1, EXTRACT (EPOCH FROM current_date)),
(2, EXTRACT (EPOCH FROM current_date));

INSERT INTO juror_mod.juror_audit
(revision, juror_number, rev_type, title, first_name, last_name, dob, address_line_1, address_line_4, postcode) VALUES
(1, '111111111', 1, NULL, 'Test', 'Person', null, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN'),
(2, '111111111', 2, NULL, 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN');

-- Create Contact Log records
INSERT INTO juror_mod.contact_log (juror_number, user_id, start_call, enquiry_type, notes) VALUES
('111111111', '415_COURT_USER', '2023-06-09', 'RC', 'Some Contact Log related notes'),
('111111111', '457_COURT_USER', '2023-06-12', 'GE', 'Some additional Contact Log related notes');

-- Create certificate of attendance letter
DELETE FROM JUROR.CERT_LETT;

INSERT INTO JUROR.CERT_LETT
(OWNER, PART_NO, PRINTED, DATE_PRINTED)
VALUES('415', '111111111', NULL, NULL);

