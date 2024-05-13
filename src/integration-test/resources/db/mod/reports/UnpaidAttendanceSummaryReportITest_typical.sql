-- create a pool for court location 415
insert into juror_mod.pool (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code,
                            new_request, attend_time)
values ('415', '415230103', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000'),
       ('415', '415230104', '2023-01-05', 5, 5, 'CRO', '415', 'N', '2023-01-05 09:30:00.000');

-- create juror records
insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, m_phone, w_phone, h_phone, h_email,
                             police_check, address_line_1, responded, postcode)
values ('641500020', 'Smith0', 'John0', '1980-01-01', 0, '000000001', '000000002', '000000003',
        '641500020@email.gov.uk', 'ELIGIBLE', 'addressLine1', true,'AB1 0CD'),
       ('641500021', 'Smith1', 'John1', '1980-01-01', 0, '100000001', '100000002', '100000003',
        '641500021@email.gov.uk', 'ELIGIBLE', 'addressLine1', true,'AB1 1CD'),
       ('641500022', 'Smith2', 'John2', '1980-01-01', 0, '200000001', '200000002', '200000003',
        '641500022@email.gov.uk', 'ELIGIBLE', 'addressLine1', true,'AB1 2CD'),
       ('641500023', 'Smith3', 'John3', '1980-01-01', 0, '300000001', '300000002', '300000003',
        '641500023@email.gov.uk', 'ELIGIBLE', 'addressLine1', true,'AB1 3CD'),
       ('641500024', 'Smith4', 'John4', '1980-01-01', 0, '400000001', '400000002', '400000003',
        '641500024@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true,'AB1 4CD'),
       ('641500025', 'Smith5', 'John5', '1980-01-01', 0, '500000001', '500000002', '500000003',
        '641500025@email.gov.uk', 'INELIGIBLE', 'addressLine1', true,'AB1 5CD'),
       ('641500026', 'Smith6', 'John6', '1980-01-01', 0, '600000001', '600000002', '600000003',
        '641500026@email.gov.uk', 'NOT_CHECKED', 'addressLine1', true,'AB1 6CD')
;

-- create juror_pool associative records
insert into juror_mod.juror_pool (owner, juror_number, pool_number, status, is_active, def_date,deferral_code)
values ('415', '641500023', '415230103', 7, true, '2023-01-01 09:30:00.000','P'),
       ('415', '641500024', '415230103', 7, true, '2023-01-02 09:30:00.000','P'),
       ('415', '641500025', '415230104', 7, true, '2023-01-01 09:30:00.000','P'),
       ('415', '641500026', '415230104', 7, true, '2023-01-02 09:30:00.000','P');

-- create appearance records
insert into juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code, time_in, misc_total_paid,
appearance_stage, non_attendance) values
('2024-10-10', '641500023', '415230103', '415','08:00',0,'EXPENSE_ENTERED',false),
('2024-10-11', '641500023', '415230103', '415', '08:00',0,'EXPENSE_ENTERED',false),
('2024-10-12', '641500023', '415230103', '415', '08:00',0,'EXPENSE_ENTERED',false),
('2024-10-13', '641500023', '415230103', '415', '08:00',0,'EXPENSE_ENTERED',false),
('2024-10-14', '641500023', '415230103', '415', '08:00',0,'EXPENSE_ENTERED',false),
('2024-10-15', '641500023', '415230103', '415', '08:00',0,'EXPENSE_ENTERED',false);