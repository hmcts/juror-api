-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('415', '200000000', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
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
       ('100000009', 'FName 9', 'LName 9', true, 'addressLine1')
;


insert into juror_mod.trial (trial_number, loc_code, trial_type, courtroom, description, judge)
values ('T123', '415','CIV', 99992,'TEST DEFENDANT', 999991),
       ('T1234', '415','CIV', 99992,'TEST DEFENDANT', 999991);

INSERT INTO juror_mod.appearance(attendance_date, juror_number, loc_code, attendance_audit_number, time_out, time_in,
                                 trial_number)
values ('2024-01-01', '100000000', '415', 'J12345678', '17:30', '08:30', 'T123'),
       ('2024-01-01', '100000001', '415', 'J12345678', '17:30', '08:30', 'T123'),
       ('2024-01-01', '100000002', '415', 'J12345678', '17:30', '08:30', 'T123'),
       ('2024-01-01', '100000003', '415', 'J12345678', '17:30', '08:30', 'T123'),
       ('2024-01-01', '100000004', '415', 'J12345678', '17:30', '08:30', 'T123'),
       ('2024-01-01', '100000005', '415', 'J12345678', '17:30', '08:30', 'T123'),

       ('2024-01-01', '100000006', '415', 'J1234', '17:30', '08:30', 'T1234'),
       ('2024-01-01', '100000007', '415', 'J1234', '17:30', '08:30', 'T1234'),
       ('2024-01-01', '100000008', '415', 'P12345678', '17:30', '08:30', null),
       ('2024-01-01', '100000009', '415', 'P12345678', '17:30', '08:30', null)

