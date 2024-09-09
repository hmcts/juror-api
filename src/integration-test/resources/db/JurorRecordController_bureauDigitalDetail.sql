DELETE FROM juror_mod.app_setting WHERE SETTING = 'SLA_OVERDUE_DAYS';

-- Set up application settings
INSERT INTO juror_mod.app_setting (SETTING, VALUE) VALUES('SLA_OVERDUE_DAYS', 5);

DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;
DELETE FROM juror_mod.users where username = 'name';
DELETE FROM juror_mod.user_courts where loc_code = '471';

-- users
INSERT INTO juror_mod.users (created_by, updated_by,username, "name", active, last_logged_in,  team_id, approval_limit, user_type, email) VALUES
('court-southwark','court-southwark','court-southwark', 'Court Southwark', true, NULL, NULL, 0.00, 'COURT', 'court-southwark@email.gov.uk');

-- user_courts
INSERT INTO juror_mod.user_courts (username, loc_code) VALUES ('court-southwark', '471');

-- pool
INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request) VALUES
('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N'),
('435220502', '400', '2022-05-03', 5, 5, 'CRO', '435', 'N'),
('435220503', '415', '2022-05-03', 5, 5, 'CRO', '435', 'N'),
('457230801', '400', '2022-05-03', 5, 5, 'CRO', '457', 'N');

INSERT INTO juror_mod.pool(pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update, additional_summons, attend_time, nil_pool, total_no_required, date_created) VALUES
('415240601', '415', '2024-06-11', 10, 'CRO', '415', 'N', '2024-04-09 15:06:58.000', NULL, '2024-06-11 09:00:00.000', false, 10, '2024-04-09 15:06:53.136');

INSERT INTO juror_mod.pool(pool_no, "owner", return_date, no_requested, pool_type, loc_code, new_request, last_update, additional_summons, attend_time, nil_pool, total_no_required, date_created) VALUES
('471240401', '471', '2024-04-29', NULL, 'CRO', '471', 'N', '2024-04-09 15:12:49.000', NULL, NULL, false, 0, '2024-04-09 15:12:48.608');

-- juror
INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, welsh) VALUES
('111111111', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, true),
('222222222', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, true),
('444444444', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, false),
('444444445', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, false),
('555555555', 'Mr','LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, false);

INSERT INTO juror_mod.juror(juror_number, poll_number, title, last_name, first_name, dob, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, h_phone, w_phone, w_ph_local, responded, last_update, summons_file, m_phone, h_email, contact_preference, notifications, date_created, claiming_subsistence_allowance, service_comp_comms_status, login_attempts, is_locked) VALUES
('641500001', '1', NULL, 'LNAMEONE', 'FNAMEONE', '1993-03-25 00:00:00.000', '1 STREET NAME', NULL, NULL, 'ANYTOWN', NULL, 'CH1 2AN', NULL, NULL, NULL, true, '2024-04-09 15:07:33.000', NULL, NULL, '', NULL, 0, '2024-04-09 15:06:58.063', false, NULL, 0, false);

-- Create Pool Member(s)
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status) VALUES
('415', '111111111', '415220502', true, '2022-05-03', 2),
('400', '222222222', '415220502', false, '2022-05-03', 10),
('400', '222222222', '435220502', true, '2022-05-03', 1),
('415', '444444444', '415220502', false, '2022-05-03', 10),
('435', '444444444', '435220502', true, '2022-05-03', 2),
('435', '444444445', '435220502', true, '2022-05-03', 2),
('415', '444444445', '435220503', true, '2022-05-03', 10),
('400', '555555555', '457230801', true, '2022-05-03', 2);

INSERT INTO juror_mod.juror_pool(juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date, "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created) VALUES
('641500001', '415240601', '415', 'court-southwark', true, 10, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0002', NULL, '2024-06-11', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-04-09 15:07:32.570', NULL, NULL, '2024-04-09 15:06:58.069');

INSERT INTO juror_mod.juror_pool(juror_number, pool_number, "owner", user_edtq, is_active, status, times_sel, def_date, "location", no_attendances, no_attended, no_fta, no_awol, pool_seq, edit_tag, next_date, on_call, smart_card, was_deferred, deferral_code, id_checked, postpone, paid_cash, scan_code, last_update, reminder_sent, transfer_date, date_created) VALUES
('641500001', '471240401', '471', 'court-southwark', true, 2, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '0001', NULL, '2024-04-29', false, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2024-04-09 15:12:48.674', NULL, NULL, '2024-04-09 15:12:48.674');

-- juror_response
INSERT INTO juror_mod.juror_response (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1,
address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH,
PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL,
BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements,
EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
STAFF_ASSIGNMENT_DATE, URGENT, COMPLETED_AT, WELSH, reply_type) VALUES
('111111111', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL, NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,    'Y', 'Digital'),
('222222222', TIMESTAMP '2022-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL, NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,    'Y', 'Digital'),
('444444444', TIMESTAMP '2022-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL, NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,    'N', 'Digital'),
('555555555', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL, NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,    'N', 'Digital');

INSERT INTO juror_mod.juror_response (juror_number, date_received, title, first_name, last_name, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, processing_status, date_of_birth, phone_number, alt_phone_number, email, residency, residency_detail, mental_health_act, mental_health_capacity, mental_health_act_details, bail, bail_details, convictions, convictions_details, deferral, excusal, excusal_reason, processing_complete, signed, "version", thirdparty_fname, thirdparty_lname, relationship, staff_login, completed_at, welsh, reply_type) VALUES
('641500001', '2024-04-09 15:07:15.880', NULL, 'FNAMEONE', 'LNAMEONE', '1 STREET NAME', NULL, NULL, 'ANYTOWN', NULL,  'CH1 2AN', 'CLOSED', '1993-03-25', NULL, NULL, '', true, NULL, false, false, NULL, false, NULL, false, NULL, false, false, NULL, true, true, 0, NULL, NULL, '', 'court-southwark', '2024-04-09 15:07:32.593', false, 'Digital');

-- juror_response_cjs_employment
insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('111111111', 'POLICE', 'Some Police Work History');

-- juror_reasonable_adjustment
insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('111111111', 'W', 'Wheel chair access'),
('111111111', 'V', 'Visual impairment');

-- Create a phone log record associated with this juror
INSERT INTO juror_mod.contact_log(juror_number, user_id, start_call, enquiry_type, notes, repeat_enquiry) VALUES
('111111111', 'BUREAU_USER', current_date - interval '2 weeks', 'GE', 'Some  general communication occurred',  false);

-- juror_history
INSERT INTO juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number) VALUES
('111111111', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
('222222222', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
('444444444', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
('444444444', '2023-01-09 16:13:45.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '435220502'),
('555555555', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '457230801');
