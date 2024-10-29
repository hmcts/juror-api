alter sequence attendance_audit_seq restart with 10000000;
ALTER SEQUENCE juror_mod.judge_id_seq
RESTART WITH 1;
ALTER SEQUENCE juror_mod.courtroom_id_seq
RESTART WITH 1;

insert into juror_mod.judge (owner, code, description) values
('415', '9999', 'judge jose');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '99995', 'big room');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, anonymous,courtroom)
values ('T10000000', '415', 'TEST DEFENDANT', 1, 'CIV', current_date - 2, false, 1);

--JUROR_MOD.POOL
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230101', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230102', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

--JUROR_MOD.JUROR
INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('111111111', 'LASTNAME', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('222222222',  'TWO', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('333333333',  'THREE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('444444444',  'FOUR', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('555555555',  'FIVE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('666666666',  'SIX', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('777777777',  'SEVEN', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('888888888',  'EIGHT', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED)
VALUES ('999999999',  'NINE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

--JUROR_MOD.JUROR_POOL
INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,
WAS_DEFERRED)
VALUES ('415', '111111111', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '222222222', '415230101', current_date - interval '2 days', NULL, 4, TRUE, TRUE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '333333333', '415230101', current_date - interval '2 days', NULL, 3, TRUE, TRUE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '444444444', '415230101', current_date - interval '2 days', NULL, 1, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '555555555', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '666666666', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '777777777', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '888888888', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);
-- Test absent juror (not in appearance table)

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,
WAS_DEFERRED)
VALUES ('415', '999999999', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);
-- Test absent juror (not in appearance table)

--JUROR_MOD.APPEARANCE
INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance)
VALUES (current_date - interval '1 day','111111111','415',null,null,false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage)
VALUES (current_date - interval '2 days','111111111','415','09:30:00','17:03',false,'CHECKED_OUT');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage)
VALUES (current_date - interval '2 days','222222222','415','09:30:00','17:30',false,'CHECKED_OUT');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage)
VALUES (current_date - interval '2 days','333333333','415','09:30:00',null,false,'CHECKED_IN');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance)
VALUES (current_date - interval '2 days','555555555','415','09:30:00',null,false);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage)
VALUES (current_date - interval '2 days','666666666','415','09:30:00',null,false,'CHECKED_IN');

-- intentional rogue entry (time_out without time_in)
INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage)
VALUES (current_date - interval '2 days','777777777','415',null,'12:30',false,'CHECKED_OUT');
-- Note: do not include juror_numbers 888888888, 999999999 in the appearance table - these are set up in juror_pool
-- table to test absent jurors

INSERT INTO juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result",completed, empanelled_date) values
('415', '222222222', 'T10000000', 10,current_date - interval '2 days' + time '09:00:00', 'J', null, current_date - interval '2 days'),
('415', '333333333', 'T10000000', 10,current_date - interval '2 days' + time '09:00:00', null, null, null);
