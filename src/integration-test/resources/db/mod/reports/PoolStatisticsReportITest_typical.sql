-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '200000000', '2023-01-01', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000001', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000002', '2023-01-30', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000003', '2023-02-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000004', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number,
                             first_name, last_name, responded, address_line_1)
values ('100000000', 'FName', 'LName', true, 'addressLine1'),
       ('100000001', 'FName', 'LName', true, 'addressLine1'),
       ('100000002', 'FName', 'LName', true, 'addressLine1'),
       ('100000003', 'FName', 'LName', true, 'addressLine1'),
       ('100000004', 'FName', 'LName', true, 'addressLine1'),
       ('100000005', 'FName', 'LName', true, 'addressLine1'),
       ('100000006', 'FName', 'LName', true, 'addressLine1'),
       ('100000007', 'FName', 'LName', true, 'addressLine1'),
       ('100000008', 'FName', 'LName', true, 'addressLine1'),
       ('100000009', 'FName', 'LName', true, 'addressLine1'),
       ('100000010', 'FName', 'LName', true, 'addressLine1'),
       ('100000011', 'FName', 'LName', true, 'addressLine1'),
       ('100000012', 'FName', 'LName', true, 'addressLine1'),
       ('100000013', 'FName', 'LName', true, 'addressLine1'),
       ('100000014', 'FName', 'LName', true, 'addressLine1'),

       ('100000015', 'FName', 'LName', true, 'addressLine1'),
       ('100000016', 'FName', 'LName', true, 'addressLine1'),
       ('100000017', 'FName', 'LName', true, 'addressLine1'),
       ('100000018', 'FName', 'LName', true, 'addressLine1'),
       ('100000019', 'FName', 'LName', true, 'addressLine1'),
       ('100000020', 'FName', 'LName', true, 'addressLine1'),
       ('100000021', 'FName', 'LName', true, 'addressLine1'),
       ('100000022', 'FName', 'LName', true, 'addressLine1'),
       ('100000023', 'FName', 'LName', true, 'addressLine1'),
       ('100000024', 'FName', 'LName', true, 'addressLine1'),
       ('100000025', 'FName', 'LName', true, 'addressLine1'),
       ('100000026', 'FName', 'LName', true, 'addressLine1'),
       ('100000027', 'FName', 'LName', true, 'addressLine1'),
       ('100000028', 'FName', 'LName', true, 'addressLine1'),
       ('100000029', 'FName', 'LName', true, 'addressLine1'),
       ('100000030', 'FName', 'LName', true, 'addressLine1'),
       ('100000031', 'FName', 'LName', true, 'addressLine1'),
       ('100000032', 'FName', 'LName', true, 'addressLine1'),
       ('100000033', 'FName', 'LName', true, 'addressLine1'),

       ('100000034', 'FName', 'LName', true, 'addressLine1'),
       ('100000035', 'FName', 'LName', true, 'addressLine1')

;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active)
values ('400', '100000000', '200000000', 1, true),
       ('400', '100000001', '200000000', 1, true),
       ('400', '100000002', '200000000', 2, true),
       ('400', '100000003', '200000000', 2, true),
       ('400', '100000004', '200000000', 3, true),
       ('400', '100000005', '200000001', 4, true),
       ('400', '100000006', '200000001', 5, true),
       ('400', '100000007', '200000001', 6, true),
       ('400', '100000008', '200000001', 7, true),
       ('400', '100000009', '200000001', 8, true),
       ('400', '100000010', '200000000', 9, true),
       ('400', '100000011', '200000000', 10, true),
       ('400', '100000012', '200000000', 11, true),
       ('400', '100000013', '200000000', 12, true),
       ('400', '100000014', '200000000', 13, true),

       ('400', '100000015', '200000000', 1, false),
       ('400', '100000016', '200000000', 2, false),
       ('400', '100000017', '200000000', 2, false),
       ('400', '100000018', '200000000', 3, false),
       ('400', '100000019', '200000000', 3, false),
       ('400', '100000020', '200000000', 4, false),
       ('400', '100000021', '200000002', 5, false),
       ('400', '100000022', '200000002', 6, false),
       ('400', '100000023', '200000002', 7, false),
       ('400', '100000024', '200000002', 8, false),
       ('400', '100000025', '200000002', 9, false),
       ('400', '100000026', '200000002', 10, false),
       ('400', '100000027', '200000000', 11, false),
       ('400', '100000028', '200000000', 12, false),
       ('400', '100000029', '200000000', 13, false),

       ('400', '100000030', '200000003', 11, false),
       ('400', '100000031', '200000003', 12, false),
       ('400', '100000032', '200000003', 13, false),
       ('400', '100000033', '200000003', 13, false)
       ,
       ('400', '100000034', '200000004', 2, false),
       ('400', '100000035', '200000004', 2, false)
;