delete from juror_mod.staff_juror_response_audit ;
delete from juror_mod.juror_response_aud;
delete from juror_mod.juror_response;
delete from juror_mod.juror_history;

delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;
delete from juror_mod.users;

INSERT INTO juror_mod.users (owner, username,email, name, active, team_id,version,user_type)
VALUES ('400','bureauOfficer','bureauOfficer@email.gov.uk','Bureau Officer',true,1,0,'BUREAU'),
       ('400','teamLeader','teamLeader@email.gov.uk','Team Leader',true,2,0,'BUREAU');
INSERT INTO juror_mod.user_roles (username, role)
VALUES ('teamLeader', 'MANAGER');


insert into juror_mod.pool(pool_no, owner, return_date, date_created, total_no_required, loc_code)
VALUES
    ('415220502', '415', TIMESTAMP '2024-03-08 00:00:00.000000', current_date, 100, '415'),
    ('415220503', '415', TIMESTAMP '2024-03-08 00:00:00.000000', current_date, 100, '415');

-- Create Pool Members
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
     ('111222333','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','PO19 1SX','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
     ('352004504','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','PO19 1SX','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
     ('333222111','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,''),
     ('222222222','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,''),
     ('555555555','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,next_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
     ('400','111222333','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
     ('415','352004504','415220502',NULL,'BUREAU_USER_1',1,'Y','0109','415','N'),
     ('415','333222111','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
     ('400','222222222','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
     ('400','555555555','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N');

-- Create Digital Reply entries
INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3,
  address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL,
  RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS,
  CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON,
  PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
  email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
  STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH, reply_type)
VALUES('111222333', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'TestOne', 'PersonOne', 'Address Line 1',
    'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO',
    TIMESTAMP '1998-03-08 00:00:00.000000',NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N',
    NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL, NULL, NULL, '01111111110', '01234098765',
    'new_email@address.com', NULL, NULL, NULL, NULL, 'bureauOfficer', NULL, 'N', 'N', NULL, 'N', 'Digital');

INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3,
  address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL,
  RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS,
  CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON,
  PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
  email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
  STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH, reply_type)
VALUES('333222111', TIMESTAMP '2023-03-08 10:00:00.000000', 'Mr', 'TestTwo', 'PersonTwo', 'Address Line 1',
    'AddressLine 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO',
    TIMESTAMP '1989-03-08 00:00:00.000000',
    NULL, NULL, NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', NULL, NULL, NULL, NULL, NULL, false, 0, NULL,
    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, 'bureauOfficer', NULL,
    'N', 'N', NULL, 'N', 'Digital');

INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3,
  address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL,
  RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS,
  CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON,
  PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
  email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
  STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH, reply_type)
VALUES('352004504', TIMESTAMP '2024-03-15 00:00:00.000000', 'Mr', 'Test3', 'Person3', 'Address Line 1',
    'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO',
     TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'N', NULL,
     null, null, NULL, NULL, 'N', 0, NULL,NULL, NULL, '01111111110', '01234098765', 'new_email@address.com',
     NULL, NULL, NULL, NULL, 'bureauOfficer', NULL, 'N', 'N', NULL,'N', 'Digital');

-- Create Paper Reply entries
INSERT INTO juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3,
    address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL,
    RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS,
    CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON,
    PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
    email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
    STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
    reply_type)
VALUES('222222222', TIMESTAMP '2023-03-09 00:00:00.000000', 'Mr', 'Test4Paper', 'Person4Paper', 'Address Line 1',
    'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED',
     TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C',
     '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL, NULL, NULL, '01111111110', '01234098765',
     'new_email@address.com', NULL, NULL, NULL, NULL, 'bureauOfficer', NULL, 'N', 'N', NULL, 'N', 'Paper');

INSERT INTO juror_mod.juror_response
     (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3,
     address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL,
     RESIDENCY, RESIDENCY_DETAIL, MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS,
     CONVICTIONS_DETAILS, DEFERRAL_REASON, DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON,
     PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME, THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE,
     email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON, JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN,
     STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
     reply_type)
VALUES('555555555', TIMESTAMP '2023-03-09 10:00:00.000000', 'Mr', 'Test5Paper', 'Person5Paper', 'Address Line 1',
     'Address Line 2', 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'AWAITING_COURT_REPLY',
      TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL, NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C',
      '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL, NULL, NULL, '01111111110', '01234098765',
      'new_email@address.com', NULL, NULL, NULL, NULL, 'JDoe', NULL, 'N', 'N', NULL, 'N', 'Paper');