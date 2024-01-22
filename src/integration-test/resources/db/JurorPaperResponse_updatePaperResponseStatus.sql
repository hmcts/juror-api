delete from juror_mod.juror_history;
delete from juror_mod.juror_audit;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

INSERT INTO juror_mod.pool
(OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST,LAST_UPDATE, ADDITIONAL_SUMMONS, ATTEND_TIME)
VALUES('400', '411220502', '2022-05-03 00:00:00', 10, 10, 'CRO', '411', 'T', TIMESTAMP '2023-06-27 08:10:49.000000', NULL, '2022-05-03 09:00:00');

-- Pool 415220502 requested 4 jurors for 2023-06-01, 2 already supplied (2 needed) - active with the bureau
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502',  '2022-05-03 00:00:00', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST,LAST_UPDATE, ADDITIONAL_SUMMONS, ATTEND_TIME)
VALUES('435', '435220502', '2022-05-03 00:00:00', 10, 10, 'CRO', '411', 'T', TIMESTAMP '2023-06-27 08:10:49.000000', NULL, '2022-05-03 09:00:00');

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3, address_line_4, address_line_5,postcode,responded,date_excused,user_edtq,no_def_pos,notifications,notes) values
	 ('111111111','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('222222222','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('444444444','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('555555555','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666666','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666601','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666602','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666603','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666604','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666605','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666606','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666607','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666608','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666609','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666610','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666611','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('666666612','543',NULL,'LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','111111111','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('400','222222222','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('415','444444444','415220502','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('435','444444444','435220502','BUREAU_USER_1',1,'Y','0109','435','N'),
	 ('411','555555555','415220502','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('415','666666666','415220502','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('411','666666601','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666602','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666603','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666604','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666605','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666606','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666607','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666608','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666609','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666610','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666611','411220502','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('411','666666612','411220502','BUREAU_USER_1',1,'Y','0109','411','N');

INSERT
INTO
    juror_mod.juror_response
    (JUROR_NUMBER, DATE_RECEIVED, TITLE, FIRST_NAME, LAST_NAME, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5,   postcode, PROCESSING_STATUS, DATE_OF_BIRTH, PHONE_NUMBER, ALT_PHONE_NUMBER, EMAIL, RESIDENCY, RESIDENCY_DETAIL,   MENTAL_HEALTH_ACT, MENTAL_HEALTH_ACT_DETAILS, BAIL, BAIL_DETAILS, CONVICTIONS, CONVICTIONS_DETAILS, DEFERRAL_REASON,   DEFERRAL_DATE, reasonable_adjustments_arrangements , EXCUSAL_REASON, PROCESSING_COMPLETE, VERSION, THIRDPARTY_FNAME,   THIRDPARTY_LNAME, RELATIONSHIP, MAIN_PHONE, OTHER_PHONE, email_address, THIRDPARTY_REASON, THIRDPARTY_OTHER_REASON,   JUROR_PHONE_DETAILS, JUROR_EMAIL_DETAILS, STAFF_LOGIN, STAFF_ASSIGNMENT_DATE, URGENT, SUPER_URGENT, COMPLETED_AT, WELSH,    reply_type)
VALUES
    ('111111111', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('222222222', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'CLOSED', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,
        NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'Y', 0, NULL,
            NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('444444444', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('555555555', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666666', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666601', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666602', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666603', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666604', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666605', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666606', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666607', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666608', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666609', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666610', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666611', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL, NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper'),
    ('666666612', TIMESTAMP '2023-03-08 00:00:00.000000', 'Mr', 'Test', 'Person', 'Address Line 1', 'Address Line 2',
    'Address Line 3', 'CARDIFF', 'Some County', 'CH1 2AN', 'TODO', TIMESTAMP '1998-03-08 00:00:00.000000', NULL,
    NULL,    NULL, 'Y', NULL, 'N', NULL, 'N', NULL, 'N', NULL, 'C', '7/6/2023, 3/7/2023, 9/8/2023', NULL, NULL, 'N', 0, NULL,    NULL, NULL, '01111111110', '01234098765', 'new_email@address.com', NULL, NULL, NULL, NULL, NULL, NULL, 'N', 'N', NULL,    'N', 'Paper');

update juror_mod.juror_response
set bail = false, convictions = false, residency = false, mental_health_act = false, mental_health_capacity = false,
signed = true where juror_number = '111111111' or juror_number = '555555555';

insert into juror_mod.juror_response_cjs_employment (juror_number, cjs_employer, cjs_employer_details) VALUES
('111111111', 'POLICE', 'Some Police Work History');

insert into juror_mod.juror_reasonable_adjustment (juror_number, reasonable_adjustment, reasonable_adjustment_detail) VALUES
('111111111', 'W', 'Wheel chair access'),
('111111111', 'V', 'Visual impairment');

