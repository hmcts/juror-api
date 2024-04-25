-- users
INSERT INTO juror_mod.users (owner, user_type, username, email, name, active, version)
VALUES ('400', 'BUREAU', 'MODTESTBUREAU', 'MODTESTBUREAU@email.gov.uk', 'MODTESTBUREAU', true, 1),
       ('415', 'COURT', 'MODTESTCOURT', 'MODTESTCOURT@email.gov.uk', 'MODTESTCOURT', true, 1);

INSERT INTO juror_mod.users ("owner", username, "name", active, last_logged_in, "version", team_id, approval_limit, user_type, email) VALUES
('471', 'court-southwark', 'Court Southwark', true, NULL, 1, NULL, 0.00, 'COURT', 'court-southwark@email.gov.uk');

-- user_roles
INSERT INTO juror_mod.user_roles (username, role)
VALUES ('MODTESTBUREAU', 'TEAM_LEADER');

-- user_courts
INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('MODTESTBUREAU', '400'),
       ('MODTESTCOURT', '415');

INSERT INTO juror_mod.user_courts (username, loc_code) VALUES ('court-southwark', '471');

-- pool
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '415', CURRENT_DATE + interval '6 weeks', 14, 14, 'CRO', '415','N');

INSERT INTO juror_mod.pool(pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update, additional_summons, attend_time, nil_pool, total_no_required, date_created) VALUES
('415240601', '415', '2024-06-11', 10, 'CRO', '415', 'N', '2024-04-09 15:06:58.000', NULL, '2024-06-11 09:00:00.000', false, 10, '2024-04-09 15:06:53.136');

INSERT INTO juror_mod.pool(pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update, additional_summons, attend_time, nil_pool, total_no_required, date_created) VALUES
('471240401', '471', '2024-04-29', NULL, 'CRO', '471', 'N', '2024-04-09 15:12:49.000', NULL, NULL, false, 0, '2024-04-09 15:12:48.608');

-- juror
INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode,
responded, notes, reasonable_adj_code, reasonable_adj_msg)
VALUES ('111111111', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, '', null,
null),
('123456789', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'PO19 1SX', true, 'SOME EXAMPLE NOTES', null, null),
('121314151', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'PO19 1SX', true, 'SOME EXAMPLE NOTES', null, null),
('987654321', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null, null, null);


INSERT INTO juror_mod.juror(juror_number, poll_number, title, last_name, first_name, dob, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, h_phone, w_phone, w_ph_local, responded, last_update, summons_file, m_phone, h_email, contact_preference, notifications, date_created, claiming_subsistence_allowance, service_comp_comms_status, login_attempts, is_locked) VALUES
('641500001', '1', NULL, 'LNAMEONE', 'FNAMEONE', '1993-03-25 00:00:00.000', '1 STREET NAME', NULL, NULL, 'ANYTOWN', NULL, 'CH1 2AN', NULL, NULL, NULL, true, '2024-04-09 15:07:33.000', NULL, NULL, '', NULL, 0, '2024-04-09 15:06:58.063', false, NULL, 0, false);

-- juror_pool
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('415', '111111111', '415220502', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '123456789', '415220502', true, CURRENT_DATE + interval '6 weeks', 2),
('415', '987654321', '415220502', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '121314151', '415220502', true, CURRENT_DATE + interval '6 weeks', 2);

INSERT INTO juror_mod.juror_pool(juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date, "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created) VALUES
('641500001', '415240601', '415', 'court-southwark', true, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0002', NULL, '2024-06-11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-04-09 15:07:32.570', NULL, NULL, '2024-04-09 15:06:58.069');

INSERT INTO juror_mod.juror_pool(juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date, "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created) VALUES
('641500001', '471240401', '471', 'court-southwark', true, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0001', NULL, '2024-04-29', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-04-09 15:12:48.674', NULL, NULL, '2024-04-09 15:12:48.674');


-- juror_response
INSERT INTO juror_mod.juror_response (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,   postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
 VALUES
     ('123456789', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
     'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, 'MODTESTBUREAU', NULL, 'N', 'N', NULL, 'N', 'Paper'),
     ('121314151', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
     'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, 'MODTESTBUREAU', NULL, 'N', 'N', NULL, 'N', 'Paper'),
     ('987654321', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
     'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, 'MODTESTBUREAU', NULL, 'N', 'N', NULL, 'N', 'Paper');

INSERT INTO juror_mod.juror_response (juror_number, date_received, title, first_name, last_name, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, processing_status, date_of_birth, phone_number, alt_phone_number, email, residency, residency_detail, mental_health_act, mental_health_capacity, mental_health_act_details, bail, bail_details, convictions, convictions_details, deferral, excusal, excusal_reason, processing_complete, signed, "version", thirdparty_fname, thirdparty_lname, relationship, staff_login, super_urgent, completed_at, welsh, reply_type) VALUES
('641500001', '2024-04-09 15:07:15.880', NULL, 'FNAMEONE', 'LNAMEONE', '1 STREET NAME', NULL, NULL, 'ANYTOWN', NULL,  'CH1 2AN', 'CLOSED', '1993-03-25', NULL, NULL, '', true, NULL, false, false, NULL, false, NULL, false, NULL, false, false, NULL, true, true, 0, NULL, NULL, '', 'court-southwark', false, '2024-04-09 15:07:32.593', false, 'Paper');

-- Create entries related to paper replies
insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('123456789', 'POLICE', 'Some Police Work History');

insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('123456789', 'V', 'Visual impairment');
