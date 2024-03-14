DELETE FROM juror_mod.app_setting WHERE SETTING = 'SLA_OVERDUE_DAYS';

-- Set up application settings
INSERT INTO juror_mod.app_setting
(SETTING, VALUE)
VALUES('SLA_OVERDUE_DAYS', 5);

DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', '2022-05-03', 5, 5, 'CRO', '415', 'N'),
('435220502', '400', '2022-05-03', 5, 5, 'CRO', '435', 'N'),
('457230801', '400', '2022-05-03', 5, 5, 'CRO', '457', 'N');


INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, welsh)
VALUES ('111111111', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, true),
('222222222', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, true),
('444444444', null,'LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, false),
('555555555', 'Mr','LNAMEFIVEFOURTHREE','FNAMEFIVEFOURTHREE', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN',
true, false);

-- Create Pool Member(s)
INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
-- (111111111 - current active record owned by court location 415)
VALUES ('415', '111111111', '415220502', true, '2022-05-03', 2),
-- (222222222 - current active record owned by bureau - originally summoned to 415 but reassigned to 435)
('400', '222222222', '415220502', false, '2022-05-03', 10),
('400', '222222222', '435220502', true, '2022-05-03', 1),
-- (444444444 - current active record owned by court location 435, previously reassigned from court location 415)
('415', '444444444', '415220502', false, '2022-05-03', 10),
('435', '444444444', '435220502', true, '2022-05-03', 2),
-- (555555555 - current active record owned by bureau, summoned for welsh court location 457)
('400', '555555555', '457230801', true, '2022-05-03', 2);


INSERT
INTO
    juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
VALUES
    ('111111111', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
    NULL,    'Y', 'Digital'),
    ('222222222', TIMESTAMP '2022-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
        NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL,
            NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N',
            'N', NULL,    'Y', 'Digital'),
    ('444444444', TIMESTAMP '2022-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
        'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL,
        NULL,
            NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL,
                NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N',
                'N', NULL,    'N', 'Digital'),
    ('555555555', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
      NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
        NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
         NULL,    'N', 'Digital');

insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('111111111', 'POLICE', 'Some Police Work History');

insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('111111111', 'W', 'Wheel chair access'),
('111111111', 'V', 'Visual impairment');


-- Create a phone log record associated with this juror
INSERT INTO juror_mod.contact_log
(juror_number, user_id, start_call, enquiry_type, notes, repeat_enquiry)
VALUES('111111111', 'BUREAU_USER', current_date - interval '2 weeks', 'GE', 'Some  general communication occurred',
false);

INSERT INTO juror_mod.juror_history
(juror_number, date_created, history_code, user_id, other_information, pool_number) VALUES
-- JUROR 111111111 summoned by the Bureau (history transferred to court 415)
('111111111', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
-- JUROR 222222222 originally summoned by the Bureau for court location 415
('222222222', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
-- JUROR 444444444 unlikely scenario of 2 summons history events (shouldn't happen) - both transferred to court location 435
('444444444', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '415220502'),
('444444444', '2023-01-09 16:13:45.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '435220502'),
-- JUROR 555555555 summoned by the Bureau for welsh court location 457 (Swansea)
('555555555', '2023-01-09 16:13:44.000000', 'RSUM', 'EXISTING1', 'File -JURY141601.001', '457230801');
