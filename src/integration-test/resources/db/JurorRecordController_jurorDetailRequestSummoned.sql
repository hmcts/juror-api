DELETE FROM juror_mod.contact_log;
DELETE FROM juror_mod.juror_pool;
DELETE FROM juror_mod.juror;
DELETE FROM juror_mod.pool;


INSERT INTO juror_mod.pool
(pool_no, "owner", return_date, total_no_required, no_requested, pool_type, loc_code, new_request)
VALUES ('416220901', '400', current_date - interval '6 months', 5, 5, 'CRO', '416','N');

INSERT INTO juror_mod.juror (juror_number, title, last_name, first_name, dob, address_line_1, address_line_4, postcode, responded, h_phone, reasonable_adj_code, reasonable_adj_msg, welsh, m_phone)
VALUES ('641600092', NULL, 'LNAMEFIVEFOURTWO', 'FNAMEFIVEFOURTWO', '1979-04-01', '544 STREET NAME', 'Chichester', 'PO19 1SX', true, '0202 2342345', 'M', 'Reasonable adjustment test message', false, '07888888888');

INSERT INTO juror_mod.juror_pool (owner, juror_number, pool_number, is_active, status)
VALUES ('400', '641600092', '416220901', true, 1);

INSERT
INTO
    juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,   postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
VALUES
    ('641600092', current_date - interval '6 months', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N',
    NULL,    'N', 'Digital');

