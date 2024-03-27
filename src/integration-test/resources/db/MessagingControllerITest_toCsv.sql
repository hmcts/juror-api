-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('400', '415230101', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('400', '415230102', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230103', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230104', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');;

insert into juror_mod.juror (juror_number,
                             title,
                             first_name,
                             last_name,
                             h_email,
                             h_phone,
                             m_phone,
                             w_phone,
                             address_line_1,
                             address_line_2,
                             address_line_3,
                             address_line_4,
                             address_line_5,
                             postcode,
                             welsh,
                             completion_date,
                             responded)
values ('641500021', 'T1', 'FName1', 'LName1', 'email1@email.com', '1234567891', '1234567881','1234567871',
        'address1 1', 'address2 1', 'address3 1', 'address4 1', 'address5 1', 'CF10 1AA', false, '2023-01-01', 'Y'),
       ('641500022', 'T2', 'FName2', 'LName2', 'email2@email.com', '1234567891', '1234567882','1234567872',
        'address1 2', 'address2 2', 'address3 2', 'address4 2', 'address5 2', 'CF10 2AA', false, null, 'Y'),
       ('641500023', 'T3', 'FName3', 'LName3', 'email3@email.com', '1234567893', '1234567883','1234567873',
        'address1 3', 'address2 3', 'address3 3', 'address4 3', 'address5 3', 'CF10 3AA', true, '2023-01-03', 'Y'),
       ('641500024', 'T4', 'FName4', 'LName4', 'email4@email.com', '1234567894', '1234567884','1234567874',
        'address1 4', 'address2 4', 'address3 4', 'address4 4', 'address5 4', 'CF10 4AA', false, '2023-01-04', 'Y'),
       ('641500025', 'T5', 'FName5', 'LName5', 'email5@email.com', '1234567896', '1234567885','1234567875',
        'address1 5', 'address2 5', 'address3 5', 'address4 5', 'address5 5', 'CF10 5AA', false, '2023-01-05', 'Y')
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, next_date, def_date, is_active)
values ('400', '641500021', '415230101', 1, null, null, true),
       ('400', '641500022', '415230101', 2, '2023-01-02', null, true),
       ('400', '641500023', '415230103', 3, null, '2023-02-03', true),
       ('400', '641500024', '415230103', 4, '2023-01-04', null, true),
       ('400', '641500025', '415230104', 5, '2023-01-05', '2023-02-05', true);