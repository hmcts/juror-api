
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230101', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE,LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230102', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED, POLICE_CHECK)
VALUES ('111111111', 'LASTNAME', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE, 'NOT_CHECKED');

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '111111111', '415230101', current_date - interval '2 weeks', NULL, 2, TRUE, FALSE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED, POLICE_CHECK)
VALUES ('222222222',  'TWO', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE, 'ELIGIBLE');

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '222222222', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED, POLICE_CHECK)
VALUES ('333333333',  'THREE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE, 'INELIGIBLE');

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE, WAS_DEFERRED)
VALUES ('415', '333333333', '415230101', current_date - interval '2 weeks', NULL, 3, TRUE, TRUE);

INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER,  LAST_NAME,  FIRST_NAME,  DOB,  address_line_1,  address_line_4,  postcode,  RESPONDED, POLICE_CHECK)
VALUES ('444444444',  'FOUR', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE, 'ERROR_RETRY_UNEXPECTED_EXCEPTION');

INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,WAS_DEFERRED)
VALUES ('415', '444444444', '415230101', current_date - interval '2 weeks', NULL, 1, TRUE, FALSE);

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,non_attendance,appearance_stage,pool_number) VALUES
    (current_date - interval '1 day','111111111','415','09:30:00',false,'CHECKED_IN','415230101');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,non_attendance,appearance_stage,pool_number) VALUES
    (current_date - interval '2 days','111111111','415','09:30:00',false,'CHECKED_IN','415230101');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,non_attendance,appearance_stage,pool_number) VALUES
    (current_date - interval '2 days','222222222','415','09:30:00',false,'CHECKED_IN','415230101');

INSERT INTO juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,non_attendance,appearance_stage,pool_number) VALUES
    (current_date - interval '2 days','333333333','415','09:30:00',false,'CHECKED_IN','415230101');
