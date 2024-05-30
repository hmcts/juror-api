-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230102', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230103', '2023-01-07', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('414', '415230104', '2023-01-07', 5, 5, 'CRO', '414', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, m_phone, w_phone, h_phone, h_email,
                             police_check, address_line_1, responded, reasonable_adj_code, reasonable_adj_msg)
values ('641500020', 'Smith0', 'John0', '1980-01-01', 0, '000000001', '000000002', '000000003',
        '641500020@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, null, null),
       ('641500021', 'Smith1', 'John1', '1980-01-01', 0, '100000001', '100000002', '100000003',
        '641500021@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, 'E', 'Test Message'),
       ('641500022', 'Smith2', 'John2', '1980-01-01', 0, '200000001', '200000002', '200000003',
        '641500022@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, null, null),
       ('641500023', 'Smith3', 'John3', '1980-01-01', 0, '300000001', '300000002', '300000003',
        '641500023@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, null, null),
       ('641500024', 'Smith4', 'John4', '1980-01-01', 0, '400000001', '400000002', '400000003',
        '641500024@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true, null, null),
       ('641500025', 'Smith5', 'John5', '1980-01-01', 0, '500000001', '500000002', '500000003',
        '641500025@email.gov.uk', 'INELIGIBLE', 'addressLine1', true, null, null),
       ('641500026', 'Smith6', 'John6', '1980-01-01', 0, '600000001', '600000002', '600000003',
        '641500026@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true, 'U', 'Test Message 2'),
       ('641500027', 'Smith7', 'John7', '1980-01-01', 0, '700000001', '700000002', '700000003',
        '641500027@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true, null, null),

       ('641500028', 'Smith8', 'John8', '1980-01-01', 0, '800000001', '800000002', '800000003',
        '641500028@email.gov.uk', 'INELIGIBLE', 'addressLine1', true, null, null),
       ('641500029', 'Smith9', 'John9', '1980-01-01', 0, '900000001', '900000002', '900000003',
        '641500029@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true, 'W', 'Test Message 3'),
       ('641500030', 'Smith10', 'John10', '1980-01-01', 0, '100000002', '100000003', '100000004',
        '641500030@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true, null, null)
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, on_call, next_date)
values ('414', '641500020', '415230104', 2, true, false, '2024-01-01'),
       ('414', '641500021', '415230104', 2, true, true, '2024-01-02'),
       ('414', '641500022', '415230104', 2, true, true, '2024-01-01'),

       ('415', '641500023', '415230103', 1, true, false, '2024-01-01'),
       ('415', '641500024', '415230103', 2, true, true, '2024-01-01'),
       ('415', '641500025', '415230103', 4, true, false, '2024-01-01'),
       ('415', '641500026', '415230103', 3, true, true, '2024-01-01'),
       ('415', '641500027', '415230103', 2, true, false, '2024-01-02'),

       ('415', '641500028', '415230102', 2, true, false, '2024-01-01'),
       ('415', '641500029', '415230102', 1, true, false, '2024-01-01'),
       ('415', '641500030', '415230102', 1, true, false, '2024-01-02');