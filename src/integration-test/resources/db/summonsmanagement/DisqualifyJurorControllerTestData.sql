delete from juror_mod.juror_reasonable_adjustment;
delete from juror_mod.juror_response_cjs_employment;
delete from juror_mod.juror_response_aud;
delete from juror_mod.juror_response;

delete from juror_digital_user.disq_lett;


delete from juror_mod.juror_history;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;
delete from juror_mod.users;

INSERT INTO juror_mod.pool
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502',  '2022-05-03 00:00:00', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- juror
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3, address_line_4, address_line_5,postcode,responded,date_excused,user_edtq,no_def_pos,notifications,notes) values
	 ('123456789','543','MRS','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'PO19 1SX','N',NULL,'BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
	 ('987654321','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'COURT_USER_1',0,0,''),
	 ('111111111','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'COURT_USER_1',0,0,''),
	 ('222222222','543','MRS','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'PO19 1SX','N',NULL,'BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,next_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','123456789','415220502','2023-05-30 00:00:00','BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('415','987654321','415220502','2023-05-30 00:00:00','COURT_USER_1',2,'Y','0109','415','Y'),
	 ('416','111111111','415220502',NULL,'COURT_USER_1',2,'Y','0109','416','N'),
	 ('400','222222222','415220502','2023-05-30 00:00:00','BUREAU_USER_1',1,'Y','0109','415','N');



  INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
  MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
  DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
  THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
  JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
   reply_type)
  VALUES('987654321', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
  2',
  'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
   NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
   NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,
   'N', 'Paper');

  INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
  MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
  DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
  THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
  JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
   reply_type)
  VALUES('123456789', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
  2',
  'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
   NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
   NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,
   'N', 'Digital');


-- staff
INSERT INTO juror_mod.users (username,email, name, active,version)
VALUES ('BUREAU_USER','BUREAU_USER@email.gov.uk','Test User',true,0),
       ('COURT_USER','COURT_USER@email.gov.uk','Test User',true,0);

insert into juror_mod.user_courts (username, loc_code)
values ('COURT_USER', '415'),
       ('BUREAU_USER', '400');
