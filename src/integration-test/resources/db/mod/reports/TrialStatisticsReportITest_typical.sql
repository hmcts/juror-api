insert into juror_mod.juror (juror_number, first_name, last_name, responded, address_line_1)
values ('100000000', 'FName 0', 'LName 0', true, 'addressLine1'),
       ('100000001', 'FName 1', 'LName 1', true, 'addressLine1'),
       ('100000002', 'FName 2', 'LName 2', true, 'addressLine1'),
       ('100000003', 'FName 3', 'LName 3', true, 'addressLine1'),
       ('100000004', 'FName 4', 'LName 4', true, 'addressLine1'),
       ('100000005', 'FName 5', 'LName 5', true, 'addressLine1'),
       ('100000006', 'FName 6', 'LName 6', true, 'addressLine1'),
       ('100000007', 'FName 7', 'LName 7', true, 'addressLine1'),
       ('100000008', 'FName 8', 'LName 8', true, 'addressLine1'),
       ('100000009', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000010', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000011', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000012', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000013', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000014', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000015', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000016', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000017', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000018', 'FName 9', 'LName 9', true, 'addressLine1'),
       ('100000019', 'FName 9', 'LName 9', true, 'addressLine1');

insert into juror_mod.trial (trial_number, loc_code, trial_type, courtroom, description, judge, trial_start_date, trial_end_date)
values ('T123', '415', 'CIV', 99991, 'TEST DEFENDANT', 999991, '2024-01-01', '2024-02-01'),
       ('T1234', '415', 'CIV', 99992, 'TEST DEFENDANT', 999992, '2023-12-25', '2024-01-05'),
       ('T1235', '415', 'CIV', 99993, 'TEST DEFENDANT', 999991, '2024-01-01', '2024-01-30'),
       ('T1236', '415', 'CRI', 99994, 'TEST DEFENDANT', 999993, '2024-01-20', '2024-02-11'),
       ('T1237', '415', 'CIV', 99996, 'TEST DEFENDANT', 999992, '2024-02-01', '2024-02-14');

insert into juror_mod.juror_trial(trial_number, loc_code, juror_number, date_selected, result)
values ('T123', '415', '100000000', current_date, 'J'),
       ('T123', '415', '100000001', current_date, 'J'),
       ('T123', '415', '100000002', current_date, 'J'),
       ('T123', '415', '100000003', current_date, 'J'),
       ('T123', '415', '100000004', current_date, 'J'),
       ('T123', '415', '100000005', current_date, 'J'),
       ('T123', '415', '100000006', current_date, 'NU'),
       ('T123', '415', '100000007', current_date, 'CD'),
       ('T123', '415', '100000008', current_date, 'R'),
       ('T123', '415', '100000009', current_date, 'NU'),
       ('T123', '415', '100000010', current_date, 'NU'),

       ('T1234', '415', '100000007', current_date, 'R'),
       ('T1234', '415', '100000008', current_date, 'J'),
       ('T1234', '415', '100000009', current_date, 'J'),
       ('T1234', '415', '100000010', current_date, 'J'),

       ('T1234', '415', '100000011', current_date, 'R'),
       ('T1235', '415', '100000012', current_date, 'J'),
       ('T1235', '415', '100000013', current_date, 'J'),
       ('T1235', '415', '100000014', current_date, 'J'),
       ('T1235', '415', '100000015', current_date, 'NU'),
       ('T1235', '415', '100000016', current_date, 'J'),
       ('T1236', '415', '100000017', current_date, 'J'),
       ('T1236', '415', '100000018', current_date, 'CD'),
       ('T1236', '415', '100000019', current_date, 'J'),
       ('T1237', '415', '100000018', current_date, 'J'),
       ('T1237', '415', '100000019', current_date, 'J')
;
