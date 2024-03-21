delete from juror_mod.contact_log;
delete from juror_mod.pool_history;
delete from juror_mod.pool_comments;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.rev_info;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

-- Create Pool Request records

-- Pool 415220701 - primary court - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230701', CURRENT_DATE + interval '6 weeks', 2, 2, 'CRO', '415', 'N', TIMESTAMP'2022-06-08 09:22:09.0');

-- Pool 767220701 - secondary court - active with the court
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '767230701', CURRENT_DATE + interval '6 weeks', 2, 2, 'CRO', '767', 'N', TIMESTAMP'2022-06-08 09:22:09.0');

-- Pool 457220701 - primary court - active with the bureau
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '457230701', CURRENT_DATE + interval '6 weeks', 2, 2, 'CRO', '457', 'N', TIMESTAMP'2022-06-08 09:22:09.0');

-- Create juror records

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, optic_reference)
VALUES ('111111111', 'LNAMEONES', 'FNAMEONES', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN',true, '12345678'),
('111111112', 'LNAMEONETWO', 'FNAMEONETWO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('111111113', 'LNAMEONETHREE', 'FNAMEONETHREE', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('222222222', 'LNAMETWOS', 'FNAMETWOS', CURRENT_DATE - interval '77 years', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('333333333', 'LNAMETHREES', 'FNAMETHREES', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('555555555', 'LNAMEFIVES', 'FNAMEFIVES', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES
-- Pool Member 111111111 - active with the court - passes validation
('415', '111111111', '415230701', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '111111112', '767230701', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '111111113', '457230701', true, CURRENT_DATE + interval '6 weeks', 2),
-- Pool Member 222222222 - active with the court - fails validation (over maximum age limit)
('415', '222222222', '415230701', true, CURRENT_DATE + interval '6 weeks', 1),
-- Pool Member 333333333 - active with the court - fails validation (invalid status - transferred)
('415', '333333333', '415230701', true, CURRENT_DATE + interval '6 weeks', 10),
-- Pool Member 555555555 - active with the court - passes validation
('415', '555555555', '415230701', true, CURRENT_DATE + interval '6 weeks', 2);

-- Create Juror History records for jurors
INSERT INTO juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number)
VALUES ('111111111', '2023-06-09', 'RSUM', 'BUREAU_USER', 'SOME OTHER INFO', '415230701'),
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'DOB Changed', '415230701'),
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'First Name Changed', '415230701'),
('111111111', '2023-06-09', 'PDET', 'BUREAU_USER', 'Last Name Changed', '415230701'),
('111111111', '2023-06-09', 'RESP', 'BUREAU_USER', 'SOME OTHER INFO', '415230701');

-- Create Juror Audit version records
INSERT INTO juror_mod.rev_info (revision_number, revision_timestamp) VALUES
(1, EXTRACT (EPOCH FROM current_date)),
(2, EXTRACT (EPOCH FROM current_date));

INSERT INTO juror_mod.juror_audit
(revision, juror_number, rev_type, title, first_name, last_name, dob, address_line_1, address_line_4, postcode) VALUES
(1, '111111111', 1, NULL, 'Test', 'Person', null, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN'),
(2, '111111111', 2, NULL, 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN');

-- Create Contact Log records
INSERT INTO juror_mod.contact_log
(juror_number, user_id, start_call, enquiry_type, notes)
VALUES('111111111', '415_COURT_USER', '2023-06-09', 'RC', 'Some Contact Log related notes');

-- Clear existing certificate of attendance letters
DELETE FROM JUROR.CERT_LETT;
