DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;
DELETE FROM juror_mod.users;

INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('415220502', '400', CURRENT_DATE + interval '6 weeks', 14, 14, 'CRO', '415', 'N'),
('415220503', '400', CURRENT_DATE + interval '6 weeks', 14, 14, 'CRO', '415', 'N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, welsh)
VALUES ('222222222', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, null),
('222222223', NULL, 'LNAME', 'FNAME', '1989-03-31', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, true),
('111111000', NULL, 'Rao', 'Frank', '1983-03-23', '543 STREET NAME', 'ANYTOWN', 'CH1 2AN', true, false),
('111111001', NULL, 'LNAME', 'FNAME', '1990-07-25', '1 TEST STREET', 'Wrexham', 'LL130BH', true, true);

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, next_date, status)
VALUES ('400', '222222222', '415220502', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '222222223', '415220502', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '111111000', '415220503', true, CURRENT_DATE + interval '6 weeks', 2),
('400', '111111001', '415220503', true, CURRENT_DATE + interval '6 weeks', 2);

INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
  MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
  DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
  THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
  JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, COMPLETED_AT, WELSH,
   reply_type)
  VALUES('222222222', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
  2',
  'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
   NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
   NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,
   'N', 'Paper'),
   ('222222223', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
   2',
   'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,
    'N', 'Paper'),
    ('111111000', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
    2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,
    'N', 'Digital'),
    ('111111001', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
    2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,
    'N', 'Digital');

INSERT INTO juror_mod.users (created_by, updated_by,username, email, name, active)
VALUES ('BUREAU_USER','BUREAU_USER','BUREAU_USER','BUREAU_USER@email.gov.uk','Test User',true);

insert into juror_mod.user_courts (username, loc_code)
values ('BUREAU_USER', '400');