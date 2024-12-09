insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, anonymous,
                             courtroom)
values ('T10000000', '415', 'TEST DEFENDANT', 999992, 'CIV', current_date, false, 99995),
       ('T10000001', '415', 'TEST DEFENDANT2', 999992, 'CIV', current_date, false, 99995);

INSERT INTO juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result",
                                   completed)
values ('415', '222222222', 'T10000001', 10, '2024-02-22 13:50:59.110', 'J', true),
       ('415', '333333333', 'T10000001', 5, '2024-02-22 13:50:58.821', 'J', true);

--JUROR_MOD.POOL
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE,
                            NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230101', current_date - interval '2 weeks', 10, 10, 'CRO', '415', 'N',
        TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE,
                            NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415230102', current_date - interval '2 weeks', 10, 10, 'CRO', '415', 'N',
        TIMESTAMP'2022-02-02 09:22:09.0');

--JUROR_MOD.JUROR
INSERT INTO JUROR_MOD.JUROR (JUROR_NUMBER, LAST_NAME, FIRST_NAME, DOB, address_line_1, address_line_4, postcode,
                             RESPONDED)
VALUES ('111111111', 'LASTNAME', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('222222222', 'TWO', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('333333333', 'THREE', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('444444444', 'FOUR', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('555555555', 'FIVE', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('666666666', 'SIX', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('777777777', 'SEVEN', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('888888888', 'EIGHT', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE),
('999999999', 'NINE', 'TEST', NULL, '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', TRUE);

--JUROR_MOD.JUROR_POOL
INSERT INTO JUROR_MOD.JUROR_POOL (OWNER, JUROR_NUMBER, POOL_NUMBER, NEXT_DATE, DEF_DATE, STATUS, IS_ACTIVE,
                                  WAS_DEFERRED)
VALUES ('415', '111111111', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, FALSE),
('415', '222222222', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, TRUE),
('415', '333333333', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, TRUE),
('415', '444444444', '415230101', current_date - interval '2 weeks', NULL, 1, TRUE, FALSE),
('415', '555555555', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, FALSE),
('415', '666666666', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, FALSE),
('415', '777777777', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, FALSE),
('415', '888888888', '415230101', current_date - interval '2 days', NULL, 4, TRUE, FALSE),
('415', '999999999', '415230101', current_date - interval '2 days', NULL, 4, TRUE, FALSE);

--JUROR_MOD.APPEARANCE
INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, non_attendance,
                                  attendance_type)
VALUES (current_date - interval '1 day', '111111111', '415230101', '415', '09:31:00', false, 'FULL_DAY'),
(current_date - interval '2 days', '111111111', '415230101', '415', '09:30:00', false, 'FULL_DAY');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, non_attendance,
                                  appearance_stage, attendance_type)
VALUES (current_date - interval '2 days', '222222222', '415230101', '415', '09:30:00', false, 'CHECKED_IN', 'FULL_DAY');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, non_attendance,
                                  appearance_stage, attendance_type)
VALUES (current_date - interval '2 days', '333333333', '415230101', '415', '09:30:00', false, 'CHECKED_IN', 'FULL_DAY');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, non_attendance,
                                  attendance_type)
VALUES (current_date - interval '2 days', '555555555', '415230101', '415', null, false, 'FULL_DAY');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, non_attendance,
                                  appearance_stage, attendance_type)
VALUES (current_date - interval '2 days', '666666666', '415230101', '415', '09:30:00', false, 'CHECKED_IN', 'FULL_DAY');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, time_out,
                                  non_attendance, appearance_stage, attendance_type)
VALUES (current_date - interval '2 days', '777777777', '415230101', '415', '15:53', '12:30', false, 'CHECKED_IN',
        'FULL_DAY');

alter sequence attendance_audit_seq restart with 10123456;
