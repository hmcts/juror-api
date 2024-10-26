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
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type,loc_code, new_request, last_update) values
('415', '415230101', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0'),
('415', '415230102', current_date - interval '2 weeks', 10, 10, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

--JUROR_MOD.JUROR
insert into juror_mod.juror (juror_number,  last_name,  first_name,  dob,  address_line_1,  address_line_4,  postcode,  responded) values
('111111111', 'LASTNAME', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('222222222',  'TWO', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('333333333',  'THREE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('444444444',  'FOUR', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('555555555',  'FIVE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('666666666',  'SIX', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('777777777',  'SEVEN', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('888888888',  'EIGHT', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE),
('999999999',  'NINE', 'TEST',  NULL,  '540 STREET NAME',  'ANYTOWN',  'CH1 2AN',  TRUE);

--JUROR_MOD.JUROR_POOL
insert into juror_mod.juror_pool (owner, juror_number, pool_number, next_date, def_date, status, is_active, was_deferred) values
('415', '111111111', '415230101', current_date - interval '2 weeks', NULL, 2, TRUE, FALSE),
('415', '222222222', '415230101', current_date - interval '2 weeks', NULL, 4, TRUE, TRUE),
('415', '333333333', '415230101', current_date - interval '2 weeks', NULL, 3, TRUE, TRUE),
('415', '444444444', '415230101', current_date - interval '2 weeks', NULL, 1, TRUE, FALSE),
('415', '555555555', '415230101', current_date - interval '2 weeks', NULL, 2, TRUE, FALSE),
('415', '666666666', '415230101', current_date - interval '2 weeks', NULL, 2, TRUE, FALSE),
('415', '777777777', '415230101', current_date - interval '2 weeks', NULL, 2, TRUE, FALSE),
('415', '888888888', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE),
('415', '999999999', '415230101', current_date - interval '2 days', NULL, 2, TRUE, FALSE);

--JUROR_MOD.APPEARANCE
insert into juror_mod.appearance (attendance_date,juror_number,loc_code,time_in,time_out,non_attendance,appearance_stage,attendance_type, trial_number) values
(current_date - interval '1 day','111111111','415','09:31:00',null,false,'CHECKED_IN','FULL_DAY',null),
(current_date - interval '2 days','111111111','415','09:30:00',null,false,'CHECKED_IN','FULL_DAY',null),
(current_date - interval '2 days','222222222','415','09:30:00',null,false,'CHECKED_IN','FULL_DAY','T10000000'),
(current_date - interval '2 days','333333333','415','09:30:00',null,false,'CHECKED_IN','FULL_DAY','T10000000'),
(current_date - interval '2 days','555555555','415','06:30:00',null,false,'CHECKED_IN','FULL_DAY',null),
(current_date - interval '2 days','666666666','415','09:30:00',null,false,'CHECKED_IN','FULL_DAY',null),
(current_date - interval '2 days','777777777','415','12:30','15:50',false,'CHECKED_IN','FULL_DAY',null);

INSERT INTO juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result",completed, empanelled_date) values
('415', '222222222', 'T10000000', 10,current_date - interval '2 days' + time '09:00:00', 'J', null, current_date - interval '2 days'),
('415', '333333333', 'T10000000', 10,current_date - interval '2 days' + time '09:00:00', null, null, null);
