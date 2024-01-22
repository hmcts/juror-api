delete from juror_digital.paper_response;
delete from juror_digital.paper_response_cjs_employment;
delete from juror_digital.paper_response_special_needs;

delete from juror_mod.juror_audit;
delete from juror_mod.juror_history;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
DELETE FROM juror_mod.pool;

INSERT INTO juror_mod.pool
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE,
ADDITIONAL_SUMMONS, ATTEND_TIME)
VALUES('400', '415220502', TIMESTAMP '2023-08-28 00:00:00.000000', 10, 10, 'CRO', '415', 'T', TIMESTAMP '2023-06-27 08:10:49.000000', NULL, TIMESTAMP '2023-08-28 09:15:00.000000');

INSERT INTO juror_mod.pool
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE, ADDITIONAL_SUMMONS, ATTEND_TIME)
VALUES('400', '435220502', TIMESTAMP '2023-08-21 00:00:00.000000', 10, 10, 'CRO', '435', 'N', TIMESTAMP '2023-06-27 08:10:49.000000', NULL, TIMESTAMP '2023-08-21 09:15:00.000000');

INSERT INTO juror_mod.pool
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE, ATTEND_TIME)
VALUES('400', '457220502', TIMESTAMP '2023-08-21 00:00:00.000000', 10, 10, 'CRO', '457', 'N', TIMESTAMP '2023-06-27 08:10:49.000000', TIMESTAMP '2023-08-21 09:15:00.000000');

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3, address_line_4, address_line_5,postcode,responded,date_excused,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('222222222','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,''),
	 ('333333333','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,''),
	 ('444444444','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','Y',NULL,'BUREAU_USER_1',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,next_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','222222222','457220502',NULL,'BUREAU_USER_1',2,'Y','0109','457','N'),
	 ('435','333333333','435220502',NULL,'BUREAU_USER_1',2,'Y','0109','435','N'),
	 ('415','333333333','415220502',NULL,'BUREAU_USER_1',10,'Y','0109','415','N'),
	 ('400','444444444','457220502',NULL,'BUREAU_USER_1',2,'Y','0109','457','N');

 INSERT
 INTO
     juror_mod.juror_response
     (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
 VALUES
     ('222222222', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
     'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
     ('333333333', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
     'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
     ('444444444', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
      'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper');

-- Create Part Hist entries
INSERT INTO juror_mod.juror_history
(juror_number, date_created, history_code, user_id, other_information, pool_number) VALUES
('222222222', current_date - interval '6 months', 'RSUM', 'EXISTING1', 'File -JURY152301.001', '415220502'),
('333333333', current_date - interval '6 months', 'RSUM', 'EXISTING1', 'File -JURY152301.001', '415220502'),
('444444444', current_date - interval '6 months', 'RSUM', 'EXISTING1', 'File -JURY152301.001', '457220502');
