-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '415230102', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230103', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, address_line_1, address_line_2,
                             address_line_3, address_line_4, address_line_5, postcode, responded)
values
    ('641500020', 'Smith1', 'John1', '1980-01-01', 0, '1 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK1 1LA', false),
    ('641500021', 'Smith2', 'John2', '1980-01-01', 0, '2 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK2 2LA', false),
    ('641500022', 'Smith3', 'John3', '1980-01-01', 0, '3 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK3 3LA', true),
    ('641500023', 'Smith4', 'John4', '1980-01-01', 0, '4 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK4 4LA', true),
    ('641500024', 'Smith5', 'John5', '1980-01-01', 0, '5 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK5 5LA', false),
    ('641500025', 'Smith6', 'John6', '1980-01-01', 0, '6 AddressLine1', 'AddressLine2', 'AddressLine3', 'AddressLine4', 'AddressLine5', 'MK6 6LA', false),
    ('641500026', 'Smith7', 'John7', '1980-01-01', 0, '7 AddressLine1', 'AddressLine2', null, 'AddressLine4', 'AddressLine5', 'MK7 7LA', false);

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active)
values ('400', '641500020', '415230101', 1, true),
       ('400', '641500021', '415230101', 2, true),
       ('400', '641500022', '415230101', 3, true),
       ('415', '641500023', '415230103', 4, true),
       ('415', '641500024', '415230103', 5, true),
       ('415', '641500025', '415230103', 9, true),
       ('415', '641500026', '415230103', 9, true);

INSERT INTO juror_mod.appearance (attendance_date, juror_number, pool_number, loc_code, attendance_type)
values ('2023-01-01', '641500023', '415230103', '415', 'ABSENT'),
       ('2023-01-02', '641500023', '415230103', '415', 'FULL_DAY'),
       ('2023-01-03', '641500023', '415230103', '415', 'NON_ATTENDANCE'),
       ('2023-01-01', '641500024', '415230103', '415', 'HALF_DAY'),
       ('2023-01-02', '641500024', '415230103', '415', 'FULL_DAY'),
       ('2023-01-03', '641500024', '415230103', '415', 'NON_ATTENDANCE');