DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;

DELETE FROM JUROR_MOD.POOL_COMMENTS;
DELETE FROM JUROR_MOD.JUROR_POOL;
DELETE FROM JUROR_MOD.JUROR;
DELETE FROM JUROR_MOD.POOL;

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE) VALUES
 ('400', '415220502', CURRENT_DATE + interval '6 weeks', 100, 100, 'CRO', '415',   'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded)
VALUES ('123456789', 'LNAME', 'FNAME', CURRENT_DATE - interval '30 years', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', false),
('222222224', 'LNAME', 'FNAME', CURRENT_DATE - interval '30 years', '540 STREET NAME', 'ANYTOWN','CH1 2AN', false);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status, def_date)
VALUES
('400', '123456789', '415220502', true, CURRENT_DATE + interval '6 weeks', 9, '2022-10-03'),
('415', '222222224', '415220502', true, CURRENT_DATE + interval '6 weeks', 9, '2022-10-03');