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
       ('041500010', 'CName10', 'CSurname10', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500011', 'CName11', 'CSurname11', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500012', 'CName12', 'CSurname12', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500013', 'CName13', 'CSurname13', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500014', 'CName14', 'CSurname14', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500015', 'CName15', 'CSurname15', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500016', 'CName16', 'CSurname16', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500017', 'CName17', 'CSurname17', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500018', 'CName18', 'CSurname18', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500019', 'CName19', 'CSurname19', '1980-01-01', 'addressLine1', 'CH1 2AN', true),
       ('041500020', 'CName20', 'CSurname20', '1980-01-01', 'addressLine1', 'CH1 2AN', true);


INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, next_date)
VALUES ('415', '041500001', '415240101', 4, true, current_date),
       ('415', '041500002', '415240101', 4, true, current_date),
       ('415', '041500003', '415240101', 4, true, current_date),
       ('415', '041500004', '415240101', 4, true, current_date),
       ('415', '041500005', '415240101', 4, true, current_date),
       ('415', '041500006', '415240101', 4, true, current_date),
       ('415', '041500007', '415240101', 4, true, current_date),
       ('415', '041500008', '415240101', 4, true, current_date),
       ('415', '041500009', '415240101', 4, true, current_date),
       ('415', '041500010', '415240101', 4, true, current_date),
       ('415', '041500011', '415240101', 4, true, current_date),
       ('415', '041500012', '415240101', 4, true, current_date),
       ('415', '041500013', '415240101', 4, true, current_date),
       ('415', '041500014', '415240101', 4, true, current_date),
       ('415', '041500015', '415240101', 4, true, current_date),
       ('415', '041500016', '415240101', 4, true, current_date),
       ('415', '041500017', '415240101', 4, true, current_date),
       ('415', '041500018', '415240101', 4, true, current_date),
       ('415', '041500019', '415240101', 4, true, current_date),
       ('415', '041500020', '415240101', 4, true, current_date);


INSERT INTO juror_mod.judge (id, owner, code, description)
VALUES (1, '415', 'JUD1', 'The Judge');
INSERT INTO juror_mod.courtroom (id, loc_code, room_number, description)
VALUES (1, '415', '1', 'The Courtroom');
INSERT INTO juror_mod.trial (trial_number, loc_code, trial_start_date, trial_type, description, courtroom, judge)
VALUES ('111111', '415', current_date, 'CRI', 'CName1, et el', 1, 1);
INSERT INTO juror_mod.trial (trial_number, loc_code, trial_start_date, trial_type, description, courtroom, judge)
VALUES ('111112', '415', current_date, 'CRI', 'CName1, et el', 1, 1);


INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, time_in, time_out, trial_number,
                                  appearance_stage)
VALUES (current_date, '041500001', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500002', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500003', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500004', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500005', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500006', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500007', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500008', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500009', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500010', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500011', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500012', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500013', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500014', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500015', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500016', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500017', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500018', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500019', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN'),
       (current_date, '041500020', '415240101', '415', '09:30:00', NULL, '111111', 'CHECKED_IN');


INSERT INTO juror_mod.juror_trial (loc_code, juror_number, trial_number, date_selected, result, completed,
                                   empanelled_date, return_date)
VALUES ('415', '041500001', '111111', current_date, 'CD', true, current_date, NULL),
       ('415', '041500002', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500003', '111111', current_date, 'NU', true, current_date, NULL),
       ('415', '041500004', '111111', current_date, 'CD', true, current_date, NULL),
       ('415', '041500005', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500006', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500007', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500008', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500009', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500010', '111111', current_date, 'R', true, current_date, current_date),
       ('415', '041500011', '111111', current_date, 'R', true, current_date, current_date),
       ('415', '041500012', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500013', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500014', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500015', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500016', '111111', current_date, 'R', true, current_date, current_date),
       ('415', '041500017', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500018', '111111', current_date, 'J', true, current_date, NULL),
       ('415', '041500019', '111111', current_date, 'NU', true, current_date, NULL),
       ('415', '041500020', '111111', current_date, 'CD', true, current_date, NULL);
