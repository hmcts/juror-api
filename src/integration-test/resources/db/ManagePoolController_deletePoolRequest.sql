INSERT INTO JUROR_MOD.POOL (OWNER,POOL_NO,RETURN_DATE,TOTAL_NO_REQUIRED,NO_REQUESTED,POOL_TYPE,LOC_CODE,NEW_REQUEST,LAST_UPDATE,ADDITIONAL_SUMMONS,ATTEND_TIME) VALUES
 ('400','415220110',TIMESTAMP'2022-01-20 00:00:00.0',10,10,'CRO','415','N', TIMESTAMP'2022-02-02 09:22:09.0',NULL,NULL);

INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('111111111','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','COURT_USER_1',1,0,NULL);

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('415','111111111','415220110','COURT_USER_1',7,'N','0109','415','N');

	 INSERT
     INTO
         juror_mod.juror_response
         (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, COMPLETED_AT, WELSH,    reply_type)
     VALUES
         ('111111111', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
         'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
         NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0,
         NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL,
          'N', NULL, 'N', 'Paper');

insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('111111111', 'POLICE', 'Some Police Work History');

insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('111111111', 'W', 'Wheel chair access');

