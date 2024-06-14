insert into juror_mod.juror (juror_number, last_name, first_name, dob, no_def_pos, m_phone, w_phone, h_phone, h_email, police_check, address_line_1, responded, reasonable_adj_code, reasonable_adj_msg)
values ('141500073', 'Smith0', 'John0', '1980-01-01', 0, '000000001', '000000002', '000000003', '141500073@email.gov.uk', 'ELIGIBLE', 'addressLine1', true, null, null);


insert into juror_mod.pool
    (owner, pool_no, return_date, total_no_required, no_requested, pool_type, loc_code, new_request, attend_time)
values
    ('400', '415240801', '2024-06-06', 5, 5, 'CRO', '415', 'N', '2024-06-06 09:30:00.000');

insert into juror_mod.juror_pool
    (owner, juror_number, pool_number, status, is_active, on_call, next_date)
values
    ('414', '141500073', '415240801', 2, true, false, '2024-01-01');


insert into juror_mod.juror_history 
    (id, juror_number, date_created, history_code, user_id, other_information, pool_number, other_info_date, other_info_reference)
values 
    ('15', '141500073', '2024-06-06 15:41:20.162', 'RESP', 'Court_user', 'Responded',             '415240801', null,         ''),
    ('16', '141500073', '2024-06-06 15:41:20.281', 'PDET', 'Court_user', 'Date Of Birth Changed', '415240801', null,         ''),
    ('17', '141500073', '2024-06-06 15:41:57.117', 'APOL', 'Court_user', 'P10000000',             '415240801', null,         ''),
    ('18', '141500073', '2024-06-06 15:42:18.754', 'FADD', 'Court_user', '£20.00',                '415240801', '2024-06-06', 'F1'),
    ('19', '141500073', '2024-06-07 10:15:53.433', 'AEDF', 'MODCOURT',   '£20.00',                '',          '2024-06-06', 'F2'),
    ('20', '141500073', '2024-06-07 10:18:07.342', 'APOL', 'Court_user', 'P10000001',             '415240801', null,         ''),
    ('21', '141500073', '2024-06-07 10:18:36.476', 'FADD', 'Court_user', '£5.00',                 '415240801', '2024-06-07', 'F3'),
    ('22', '141500073', '2024-06-07 10:19:00.505', 'APOL', 'Court_user', 'P10000002',             '415240801', null,         '');