-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('415', '200000000', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '200000004', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number,
                             first_name, last_name, responded, address_line_1)
values ('100000000', 'FName', 'LName', true, 'addressLine1'),
       ('100000001', 'FName', 'LName', true, 'addressLine1'),
       ('100000002', 'FName', 'LName', true, 'addressLine1'),
       ('100000003', 'FName', 'LName', true, 'addressLine1'),
       ('100000004', 'FName', 'LName', true, 'addressLine1'),
       ('100000005', 'FName', 'LName', true, 'addressLine1');
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number)
values ('415', '100000000', '200000000'),
       ('415', '100000001', '200000000'),
       ('415', '100000002', '200000000'),
       ('415', '100000003', '200000000'),
       ('415', '100000004', '200000000'),
       ('415', '100000005', '200000000');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type)
values ('2024-01-01', '100000000', '200000000', '415', 'ABSENT'),
       ('2024-01-01', '100000001', '200000000', '415', 'NON_ATTENDANCE'),
       ('2024-01-01', '100000002', '200000000', '415', 'FULL_DAY'),
       ('2024-01-01', '100000003', '200000000', '415', 'HALF_DAY'),
       ('2024-01-01', '100000004', '200000000', '415', 'FULL_DAY_LONG_TRIAL'),
       ('2024-01-01', '100000005', '200000000', '415', 'HALF_DAY_LONG_TRIAL'),

       ('2024-01-02', '100000001', '200000000', '415', 'NON_ATTENDANCE_LONG_TRIAL'),
       ('2024-01-02', '100000002', '200000000', '415', 'FULL_DAY'),
       ('2024-01-02', '100000003', '200000000', '415', 'FULL_DAY'),
       ('2024-01-02', '100000004', '200000000', '415', 'FULL_DAY'),
       ('2024-01-02', '100000005', '200000000', '415', 'FULL_DAY'),

       ('2024-01-03', '100000001', '200000000', '415', 'FULL_DAY_LONG_TRIAL'),
       ('2024-01-03', '100000002', '200000000', '415', 'FULL_DAY'),
       ('2024-01-03', '100000003', '200000000', '415', 'HALF_DAY'),
       ('2024-01-03', '100000004', '200000000', '415', 'HALF_DAY'),
       ('2024-01-03', '100000005', '200000000', '415', 'FULL_DAY'),

       ('2024-02-02', '100000001', '200000000', '415', 'NON_ATTENDANCE_LONG_TRIAL'),
       ('2024-02-02', '100000002', '200000000', '415', 'FULL_DAY'),
       ('2024-02-02', '100000003', '200000000', '415', 'FULL_DAY'),
       ('2024-02-02', '100000004', '200000000', '415', 'FULL_DAY'),
       ('2024-02-02', '100000005', '200000000', '415', 'FULL_DAY')
;