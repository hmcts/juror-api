--JUROR_MOD.POOL
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230101', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230102', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

--JUROR_MOD.JUROR
INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('111111111', 'LASTNAME', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('222222222',  'TWO', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('333333333',  'THREE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('444444444',  'FOUR', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('555555555',  'FIVE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('666666666',  'SIX', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('777777777',  'SEVEN', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('888888888',  'EIGHT', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
 ('999999999',  'NINE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

--JUROR_MOD.JUROR_POOL
INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,WAS_DEFERRED)
VALUES ('415', '111111111', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE),
 ('415', '222222222', '415230101', current_date - interval '2 days', NULL, 4, TRUE, TRUE),
 ('415', '333333333', '415230101', current_date - interval '2 days', NULL, 3, TRUE, TRUE),
 ('415', '444444444', '415230101', current_date - interval '2 days', NULL, 1, TRUE, FALSE),
 ('415', '555555555', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE),
 ('415', '666666666', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE),
 ('415', '777777777', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE),
 ('415', '888888888', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);
-- Test absent juror (not in appearance table)

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,
WAS_DEFERRED)
VALUES ('415', '999999999', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);
-- Test absent juror (not in appearance table)

--JUROR_MOD.APPEARANCE
INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage, appearance_confirmed)
VALUES
    (current_date - interval '9 days','111111111','415','09:30:00','17:03',false,'EXPENSE_ENTERED', true),
    (current_date - interval '9 days','222222222','415','09:30:00','17:30',false,'EXPENSE_ENTERED', true),
    (current_date - interval '2 days','111111111','415','09:30:00','17:03',false,'EXPENSE_ENTERED', true),
    (current_date - interval '2 days','222222222','415','09:30:00','17:30',false,'EXPENSE_ENTERED', true),
    (current_date - interval '2 days','333333333','415','09:30:00',null,false,'CHECKED_IN', false),
    (current_date - interval '2 days','555555555','415',null,null,true, null, true),
    (current_date - interval '2 days','666666666','415','09:30:00',null,false,'CHECKED_IN', false),
    (current_date - interval '8 days','777777777','415',null,'12:30',false,'CHECKED_OUT', false),
    (current_date - interval '8 days','333333333','415','09:30:00',null,false,'CHECKED_IN', false),
    (current_date - interval '8 days','666666666','415','09:30:00',null,false,'CHECKED_IN', false);
