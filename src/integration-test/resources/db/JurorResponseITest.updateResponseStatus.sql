-- juror 644892530
INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, notes,
optic_reference, title)
VALUES
('644892530', 'CASTILLO', 'FNAMEFIVEFOURZERO', '1990-07-25', '540 STREET NAME', 'ANYTOWN', 'CH1 2AN', true,
'These are some notes for 415220502', null, 'Mr');



insert into juror_mod.pool(pool_no, owner, return_date, date_created, total_no_required, loc_code)
VALUES
    ('415220502', '415', TIMESTAMP '2023-03-08 00:00:00.000000', current_date, 100, '415');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('400', '644892530', '415220502', true, '2022-05-03', 2);


-- response 644892530 (LAST_NAME changed)

INSERT
INTO
    juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
VALUES
    ('644892530', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'CASTILLO', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
    NULL,    'N', 'Digital');

insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('644892530', 'POLICE', 'Some Police Work History');

insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('644892530', 'W', 'Wheel chair access');

-- team leader with login enabled
INSERT INTO juror_mod.users (username,email, name, active,last_logged_in,team_id,version, user_type)
VALUES ('testlogin','testlogin@email.gov.uk','Test Login',true,CURRENT_DATE - 3,1,0, 'BUREAU');
INSERT INTO juror_mod.user_roles (username, role)
VALUES ('testlogin', 'TEAM_LEADER');

insert into juror_mod.user_courts (username, loc_code)
values ('testlogin', '400');