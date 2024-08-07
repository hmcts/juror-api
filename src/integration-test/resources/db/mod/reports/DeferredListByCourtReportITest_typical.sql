-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '415230102', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230103', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230104', '2023-01-05', 5, 5, 'CRO', '462', 'N', '2023-01-05 09:30:00.000');


-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, m_phone, w_phone, h_phone, h_email,
                             police_check, address_line_1, responded)
values ('641500020', 'Smith0', 'John0', '1980-01-01', 0, '000000001', '000000002', '000000003',
        '641500020@email.gov.uk', 'ELIGIBLE', 'addressLine1', true),
       ('641500021', 'Smith1', 'John1', '1980-01-01', 1, '100000001', '100000002', '100000003',
        '641500021@email.gov.uk', 'ELIGIBLE', 'addressLine1', true),
       ('641500022', 'Smith2', 'John2', '1980-01-01', 2, '200000001', '200000002', '200000003',
        '641500022@email.gov.uk', 'ELIGIBLE', 'addressLine1', true),
       ('641500023', 'Smith3', 'John3', '1980-01-01', 3, '300000001', '300000002', '300000003',
        '641500023@email.gov.uk', 'ELIGIBLE', 'addressLine1', true),
       ('641500024', 'Smith4', 'John4', '1980-01-01', 4, '400000001', '400000002', '400000003',
        '641500024@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true),
       ('641500025', 'Smith5', 'John5', '1980-01-01', 5, '500000001', '500000002', '500000003',
        '641500025@email.gov.uk', 'INELIGIBLE', 'addressLine1', true),
       ('641500026', 'Smith6', 'John6', '1980-01-01', 6, '600000001', '600000002', '600000003',
        '641500026@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true),
       ('641500027', 'Smith7', 'John7', '1980-01-01', 6, '600000001', '600000002', '600000003',
         '641500026@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true),
       ('641500028', 'Smith8', 'John8', '1980-01-01', 6, '600000001', '600000002', '600000003',
        '641500027@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true)
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, def_date)
values ('400', '641500020', '415230101', 1, true, '3023-01-05 09:30:00.000'),
       ('400', '641500021', '415230101', 2, true, '3023-01-05 09:30:00.000'),
       ('400', '641500022', '415230101', 3, true, '3023-01-06 09:30:00.000'),
       ('415', '641500023', '415230103', 4, true, '3023-01-06 09:30:00.000'),
       ('415', '641500024', '415230103', 5, true, '3023-01-07 09:30:00.000'),
       ('415', '641500025', '415230103', 6, true, '3023-01-07 09:30:00.000'),
       ('415', '641500026', '415230103', 7, true, '3023-01-08 09:30:00.000'),
       ('415', '641500027', '415230104', 7, true, '3023-01-09 09:30:00.000'),
       ('415', '641500028', '415230104', 7, true, '2023-01-09 09:30:00.000');

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type)
values ('2023-01-01', '641500023', '415230103', '415', 'ABSENT'),
       ('2023-01-02', '641500023', '415230103', '415', 'FULL_DAY'),
       ('2023-01-03', '641500023', '415230103', '415', 'NON_ATTENDANCE'),
       ('2023-01-01', '641500024', '415230103', '415', 'HALF_DAY'),
       ('2023-01-02', '641500024', '415230103', '415', 'FULL_DAY'),
       ('2023-01-03', '641500024', '415230103', '415', 'NON_ATTENDANCE'),
       ('2023-01-04', '641500024', '415230103', '415', 'ABSENT');