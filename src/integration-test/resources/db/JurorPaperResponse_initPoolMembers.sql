INSERT INTO juror_mod.users (created_by, updated_by,user_type, username, email, name, active)
VALUES ('BUREAU','BUREAU','BUREAU', 'MODTESTBUREAU', 'MODTESTBUREAU@email.gov.uk', 'MODTESTBUREAU', true),
       ('COURT', 'COURT', 'COURT', 'MODTESTCOURT', 'MODTESTCOURT@email.gov.uk', 'MODTESTCOURT', true);

INSERT INTO juror_mod.user_roles (username, role)
VALUES ('MODTESTBUREAU', 'MANAGER');

INSERT INTO juror_mod.user_courts (username, loc_code)
VALUES ('MODTESTBUREAU', '400'),
       ('MODTESTCOURT', '415');

-- Pool 415220502 requested 4 jurors for 2023-06-01, 2 already supplied (2 needed) - active with the bureau
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502',  current_date + interval '1 weeks', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
('400', '415220533',  current_date + interval '2 weeks', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3, address_line_4, address_line_5,postcode,responded,date_excused,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('123456789','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
	 ('987654321','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,''),
	 ('222222222','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,''),
	 ('123456791','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',1,0,''),
	 ('123456711','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',1,0,''),
	 ('111111111','543','Mr','Person','Test',NULL,'Address Line 1','Address Line 2','Address Line 3','Some Town','Some County','CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,'SOME EXAMPLE NOTES'),
	 ('333333333','543','Mr','Person','Test',NULL,'Address Line 1','Address Line 2','Address Line 3','Some Town','Some County','CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,'SOME EXAMPLE NOTES');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,next_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','123456789','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('415','987654321','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('415','222222222','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('415','123456791','415220502',NULL,'BUREAU_USER_1',2,'Y','0109','415','Y'),
	 ('415','123456711','415220533',NULL,'BUREAU_USER_1',2,'Y','0109','415','Y'),
	 ('400','111111111','415220502',NULL,'BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('415','333333333','415220502',NULL,'BUREAU_USER_1',1,'Y','0109','415','N');

 INSERT INTO juror_mod.juror_response
  (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,
  postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,
  MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,
  DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,
  THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,
  JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT,  COMPLETED_AT, WELSH,
   reply_type)
  VALUES('222222222', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line
  2',
  'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
   NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,
   NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', NULL,
   'N', 'Paper');

