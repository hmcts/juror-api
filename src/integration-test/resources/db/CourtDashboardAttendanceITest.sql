-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('415', '200000000', current_date - 7, 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '200000004', current_date - 7, 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number,
                             first_name, last_name, responded, address_line_1, reasonable_adj_code)
values ('100000000', 'FName', 'LName', true, 'addressLine1', 'D'),
       ('100000001', 'FName', 'LName', true, 'addressLine1', 'R'),
       ('100000002', 'FName', 'LName', true, 'addressLine1', NULL),
       ('100000003', 'FName', 'LName', true, 'addressLine1', NULL),
       ('100000004', 'FName', 'LName', true, 'addressLine1', NULL),
       ('100000005', 'FName', 'LName', true, 'addressLine1', NULL);
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status, next_date)
values ('415', '100000000', '200000000', true, 2, current_date + 1),
       ('415', '100000001', '200000000', true, 2, current_date + 2),
       ('415', '100000002', '200000000', true, 2, current_date + 3),
       ('415', '100000003', '200000000', true, 2, current_date + 4),
       ('415', '100000004', '200000000', true, 2, current_date + 5),
       ('415', '100000005', '200000000', true, 4, current_date + 6);

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type, appearance_stage, no_show)
values (current_date - 3, '100000000', '200000000', '415', 'ABSENT', NULL, true);

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type, appearance_stage)
values (current_date - 3, '100000001', '200000000', '415', 'NON_ATTENDANCE', NULL),
       (current_date - 3, '100000002', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),
       (current_date - 3, '100000003', '200000000', '415', 'HALF_DAY', 'EXPENSE_ENTERED'),
       (current_date - 3, '100000004', '200000000', '415', 'FULL_DAY_LONG_TRIAL', 'EXPENSE_ENTERED'),
       (current_date - 3, '100000005', '200000000', '415', 'HALF_DAY_LONG_TRIAL', 'EXPENSE_ENTERED'),

       (current_date - 2, '100000001', '200000000', '415', 'NON_ATTENDANCE_LONG_TRIAL', NULL),
       (current_date - 2, '100000002', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),
       (current_date - 2, '100000003', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),
       (current_date - 2, '100000004', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),
       (current_date - 2, '100000005', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),

       (current_date - 1, '100000001', '200000000', '415', 'FULL_DAY_LONG_TRIAL', 'CHECKED_IN'),
       (current_date - 1, '100000002', '200000000', '415', 'FULL_DAY', 'CHECKED_OUT'),
       (current_date - 1, '100000003', '200000000', '415', 'HALF_DAY', 'CHECKED_OUT'),
       (current_date - 1, '100000004', '200000000', '415', 'HALF_DAY', 'CHECKED_IN'),
       (current_date - 1, '100000005', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED'),

       (current_date, '100000001', '200000000', '415', 'NON_ATTENDANCE_LONG_TRIAL', NULL),
       (current_date, '100000002', '200000000', '415', 'FULL_DAY', 'CHECKED_OUT'),
       (current_date, '100000003', '200000000', '415', 'FULL_DAY', 'CHECKED_IN'),
       (current_date, '100000004', '200000000', '415', 'FULL_DAY', 'CHECKED_IN'),
       (current_date, '100000005', '200000000', '415', 'FULL_DAY', 'EXPENSE_ENTERED');


alter sequence juror_mod.judge_id_seq restart with 1;
alter sequence juror_mod.courtroom_id_seq restart with 1;

insert into juror_mod.courtroom (id, loc_code, room_number, description)
values (1, '415', '1', 'large room fits 100 people');

insert into juror_mod.judge (id, owner, code, description)
values (1, '415', '1234', 'Test judge');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, trial_end_date, anonymous, courtroom)
values ('T100000002', '415', 'TEST DEFENDANT', 1, 'CIV', current_date, null, false, 1);

insert into juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed, empanelled_date) values
('415', '100000005','T100000002', 5, current_date -2, 'J', false, current_date -2 );
