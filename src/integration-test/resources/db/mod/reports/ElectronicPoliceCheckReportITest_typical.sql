-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '200000000', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000001', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000002', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '200000003', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '200000004', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, police_check, police_check_last_update,
                             first_name, last_name, responded, address_line_1)
values ('100000000', 'NOT_CHECKED', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000001', 'ELIGIBLE', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000002', 'INELIGIBLE', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000003', 'INELIGIBLE', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000004', 'ERROR_RETRY_NAME_HAS_NUMERICS', '2024-01-01 09:30:00.000', 'FName', 'LName', true,
        'addressLine1'),
       ('100000005', 'ERROR_RETRY_CONNECTION_ERROR', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000006', 'ERROR_RETRY_OTHER_ERROR_CODE', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000007', 'ERROR_RETRY_NO_ERROR_REASON', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000008', 'IN_PROGRESS', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000009', 'NOT_CHECKED', '2024-01-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000010', 'UNCHECKED_MAX_RETRIES_EXCEEDED', '2024-01-01 09:30:00.000', 'FName', 'LName', true,
        'addressLine1'),
       ('100000011', 'UNCHECKED_MAX_RETRIES_EXCEEDED', '2024-02-01 09:30:00.000', 'FName', 'LName', true,
        'addressLine1'),
       ('100000012', 'ELIGIBLE', '2024-03-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1'),
       ('100000013', 'INSUFFICIENT_INFORMATION', '2024-03-01 09:30:00.000', 'FName', 'LName', true, 'addressLine1')
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status)
values ('400', '100000000', '200000000', 1),
       ('400', '100000001', '200000000', 2),
       ('400', '100000002', '200000000', 2),
       ('400', '100000003', '200000001', 2),
       ('400', '100000004', '200000001', 2),
       ('400', '100000005', '200000001', 2),
       ('400', '100000006', '200000002', 2),
       ('400', '100000007', '200000002', 2),
       ('400', '100000008', '200000002', 2),
       ('400', '100000009', '200000003', 2),
       ('400', '100000010', '200000003', 2),
       ('400', '100000011', '200000003', 2),
       ('400', '100000012', '200000001', 2),
       ('415', '100000013', '200000004', 2)
;