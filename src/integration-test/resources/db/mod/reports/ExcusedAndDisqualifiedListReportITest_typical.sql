-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('415', '200000000', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000001', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '200000002', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number,
                             first_name, last_name, responded, address_line_1, excusal_code, date_excused, disq_code,
                             date_disq)
values ('100000000', 'FName 0', 'LName 0', true, 'addressLine1', 'I', '2024-01-01', null, null),
       ('100000001', 'FName 1', 'LName 1', true, 'addressLine1', 'G', '2024-01-01', null, null),
       ('100000002', 'FName 2', 'LName 2', true, 'addressLine1', 'B', '2024-01-02', null, null),
       ('100000003', 'FName 3', 'LName 3', true, 'addressLine1', 'W', '2024-01-02', null, null),
       ('100000004', 'FName 4', 'LName 4', true, 'addressLine1', 'CE', '2024-01-03', null, null),
       ('100000005', 'FName 5', 'LName 5', true, 'addressLine1', null, null, null, null),
       ('100000006', 'FName 6', 'LName 6', true, 'addressLine1', null, null, 'A', '2024-01-01'),
       ('100000007', 'FName 7', 'LName 7', true, 'addressLine1', null, null, 'B', '2024-01-02'),
       ('100000008', 'FName 8', 'LName 8', true, 'addressLine1', null, null, 'A', '2024-01-01'),
       ('100000009', 'FName 9', 'LName 9', true, 'addressLine1', null, null, 'E', '2024-01-03'),

       ('100000010', 'FName 10', 'LName 10', true, 'addressLine1', null, null, 'A', '2024-01-01'),
       ('100000011', 'FName 11', 'LName 11', true, 'addressLine1', null, null, 'B', '2024-01-01'),
       ('100000012', 'FName 12', 'LName 12', true, 'addressLine1', null, null, 'D', '2024-01-01'),

       ('100000013', 'FName 13', 'LName 13', true, 'addressLine1', null, null, null, null);
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number)
values ('415', '100000000', '200000000'),
       ('415', '100000001', '200000000'),
       ('415', '100000002', '200000000'),
       ('415', '100000003', '200000000'),
       ('415', '100000004', '200000000'),
       ('415', '100000005', '200000000'),
       ('415', '100000006', '200000000'),
       ('415', '100000007', '200000000'),
       ('415', '100000008', '200000000'),
       ('415', '100000009', '200000000'),
       ('400', '100000010', '200000001'),
       ('400', '100000011', '200000001'),
       ('400', '100000012', '200000001'),
       ('415', '100000013', '200000002');