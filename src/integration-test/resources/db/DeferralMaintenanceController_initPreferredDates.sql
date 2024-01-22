-- Create juror records associated with DEFER_DBF records
delete from juror_mod.juror_audit;
delete from juror_mod.juror_history;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502', TIMESTAMP'2023-06-12 00:00:00.000000', 1, 1, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- Pool 415220504 requested 1 jurors for 2023-06-12, 1 already supplied (0 needed) - active with the court
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('415', '415220504', TIMESTAMP'2023-06-12 00:00:00.000000', 1, 1, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- Pool 415220401 requested 2 jurors for 2023-05-30, 4 already supplied (2 surplus) - active with the bureau
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220401', TIMESTAMP'2023-05-30 00:00:00.000000', 2, 2, 'CRO', '415', 'N', TIMESTAMP'2022-02-02 09:22:09.0');

INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('555555551','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555552','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555553','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555554','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555555','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555556','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555557','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL);

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','555555551','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('400','555555552','415220401','BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('400','555555553','415220401','BUREAU_USER_1',11,'Y','0109','415','N'),
	 ('400','555555554','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('400','555555555','415220502','BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('400','555555556','415220502','BUREAU_USER_1',11,'Y','0109','415','N'),
	 ('415','555555557','415220504','BUREAU_USER_1',1,'Y','0109','415','N');


INSERT INTO juror_mod.juror_response
(JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
 reply_type)
VALUES('555555551', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
 NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '29/05/2023, 12/6/2023, 3/7/2023', NULL, NULL, 'N', 0, NULL,
 NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,
 'N', 'Digital');
 INSERT INTO juror_mod.juror_response
 (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
 postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
 MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
 DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
 THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
 JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
  reply_type)
 VALUES('555555552', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
 'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
  NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '', NULL, NULL, 'N', 0, NULL,
  NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,
  'N', 'Digital');
  INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
  MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
  DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
  THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
  JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,
   reply_type)
  VALUES('555555557', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
  2',
  'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
   NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
   NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,
   'N', 'Digital');
