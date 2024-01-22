DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '416', current_date - 20, 5, 5, 'CRO', '416', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded,excusal_code, acc_exc, summons_file)
VALUES ('641600090', 'MR', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1988-01-01', '542 STREET NAME', 'Chichester', 'PO19 1SX', true, null, null, null),
('641600091', 'MR', 'LNAMEFIVEFOURONE', 'FNAMEFIVEFOURONE', '1989-04-01', '543 STREET NAME', 'Chichester', 'PO19 1SX', true, null, null, null),
('641600092', 'MR', 'LNAMEFIVEFOURTWO', 'FNAMEFIVEFOURTWO', '1979-04-01', '544 STREET NAME', 'Chichester', 'PO19 1SX', false, null, null, null),
('641600093', 'MR', 'LNAMEFIVEFOURTHREE', 'FNAMEFIVEFOURTHREE', '1977-04-01', '545 STREET NAME', 'Chichester', 'PO19 1SX', true, null, null, 'Disq. on selection'),
('641600094', 'MR', 'LNAMEFIVEFOURFOUR', 'FNAMEFIVEFOURFOUR', '1966-01-01', '546 STREET NAME', 'Chichester', 'PO19 1SX', true, null, null, null),
('641600095', 'MR', 'LNAMEFIVEFOURFOUR', 'FNAMEFIVEFOURFOUR', '1966-01-01', '546 STREET NAME', 'Chichester', 'PO19 1SX', true, null, null, null),
('641600096', 'MR', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1988-01-01', '542 STREET NAME', 'Chichester', 'PO19 1SX', true, 'C', 'Y', null),
('641600097', 'MR', 'LNAMEFIVEFOURZERO', 'FNAMEFIVEFOURZERO', '1988-01-01', '542 STREET NAME', 'Chichester', 'PO19 1SX', true, 'C', 'Y', null);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('416', '641600090', '416220901', 'Y', 3),
('416', '641600091', '416220901', 'Y', 2),
('400', '641600092', '416220901', 'Y', 1),
('400', '641600093', '416220901', 'Y', 6),
('416', '641600094', '416220901', 'Y', 2),
('416', '641600095', '416220901', 'Y', 6),
('416', '641600096', '416220901', 'Y', 2),
('416', '641600097', '416220901', 'Y', 2);

INSERT
INTO
    juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
VALUES
    ('641600096', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
    NULL,    'N', 'Paper'),
    ('641600091', TIMESTAMP '2022-10-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
        NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL,
            NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N',
            'N', NULL,    'N', 'Digital'),
    ('641600097', TIMESTAMP '2022-10-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
      NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
        NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
         NULL,    'N', 'Digital');

DELETE FROM juror_mod.juror_history;

INSERT INTO juror_mod.juror_history (id, juror_number, date_created, history_code, user_id, other_information,
pool_number) VALUES
(nextval('juror_mod.juror_history_id_seq'::regclass), '641600094', '2018-11-08', 'RSUM', 'TESTSQL', 'Summoned',
'416220901'),
(nextval('juror_mod.juror_history_id_seq'::regclass), '641600094', '2018-11-08', 'RESP', 'TESTSQL', 'Summoned',
'416220901'),
(nextval('juror_mod.juror_history_id_seq'::regclass), '641600090', current_date - 90, 'RSUM', 'TESTSQL', 'Summoned',
'416220901'),
(nextval('juror_mod.juror_history_id_seq'::regclass), '641600090', current_date - 60, 'RESP', 'TESTSQL', 'Responded',
'416220901'),
(nextval('juror_mod.juror_history_id_seq'::regclass), '641600090', current_date - 10, 'VADD', 'TESTSQL', 'Panel',
'416220901');
