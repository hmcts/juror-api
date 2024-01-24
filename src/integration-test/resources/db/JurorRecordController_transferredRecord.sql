DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '415', '2022-05-03', 5, 5, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, notes)
VALUES ('123456789', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, 'SOME EXAMPLE NOTES');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('415', '123456789', '415220502', true, '2022-05-03', 2);


INSERT INTO JUROR_MOD.CONTACT_LOG
(JUROR_NUMBER, USER_ID, START_CALL, ENQUIRY_TYPE, NOTES, REPEAT_ENQUIRY)
VALUES('123456789', 'BUREAU_USER', TIMESTAMP '1990-07-25 10:12:14.000000', 'GE', 'Some general communication occurred', false);
INSERT INTO JUROR_MOD.CONTACT_LOG
(JUROR_NUMBER, USER_ID, START_CALL, ENQUIRY_TYPE, NOTES, REPEAT_ENQUIRY)
VALUES('123456789', 'BUREAU_USER', TIMESTAMP '2022-12-01 16:36:01.000000', 'CA', 'Juror has moved from A to B', false);
