INSERT INTO juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
VALUES ('415', '415240101', current_date, 20, 20, 'CRO', '415', 'N', current_date);


INSERT INTO juror_mod.juror (juror_number, first_name, last_name, dob, address_line_1, postcode, responded)
VALUES ('041500001', 'CName1', 'CSurname1', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500002', 'CName2', 'CSurname2', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500003', 'CName3', 'CSurname3', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500004', 'CName4', 'CSurname4', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500005', 'CName5', 'CSurname5', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500006', 'CName6', 'CSurname6', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500007', 'CName7', 'CSurname7', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500008', 'CName8', 'CSurname8', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500009', 'CName9', 'CSurname9', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500010', 'CName10', 'CSurname10', '1980-01-01', 'addressLine1', 'CH1 2AN', true);


INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('415', '041500001', '415240101', 2, true, current_date),
       ('415', '041500002', '415240101', 2, true, current_date),
       ('415', '041500003', '415240101', 2, true, current_date),
       ('415', '041500004', '415240101', 2, true, current_date),
       ('415', '041500005', '415240101', 2, true, current_date),
       ('415', '041500006', '415240101', 2, true, current_date),
       ('415', '041500007', '415240101', 2, true, current_date),
       ('415', '041500008', '415240101', 2, true, current_date),
       ('415', '041500009', '415240101', 2, true, current_date),
       ('415', '041500010', '415240101', 2, true, current_date);


INSERT INTO juror_mod.judge (id, owner, code, description)
VALUES (1, '415', 'JUD1', 'The Judge');
INSERT INTO juror_mod.courtroom (id, loc_code, room_number, description)
VALUES (1, '415', '1', 'The Courtroom');
INSERT INTO juror_mod.trial (trial_number, loc_code, trial_start_date, trial_type, description, courtroom, judge)
VALUES ('111111', '415', current_date, 'CRI', 'The Trial', 1, 1);


INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, time_out, trial_number,
                                  appearance_stage)
VALUES (current_date - 1, '041500001', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date - 1, '041500002', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date - 1, '041500003', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date - 1, '041500004', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date - 1, '041500005', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date - 1, '041500010', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500001', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500002', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500003', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500004', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500005', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500006', '415240101', '415', '09:00:00', NULL, NULL, 'CHECKED_IN'),
       (current_date, '041500007', '415240101', '415', '09:00:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500008', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500009', '415240101', '415', '09:00:00', '17:30:00', '111111', 'CHECKED_OUT'),
       (current_date, '041500010', '415240101', '415', NULL, NULL, NULL, NULL);
