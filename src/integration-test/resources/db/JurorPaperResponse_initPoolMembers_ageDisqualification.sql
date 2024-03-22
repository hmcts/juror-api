
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '411220502','2022-05-03 00:00:00', 4, 4, 'CRO', '411', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220502','2022-05-03 00:00:00', 4, 4, 'CRO', '411', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3, address_line_4, address_line_5,postcode,responded,date_excused,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('111111111','543',NULL,'LNAME','FNAME','2006-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('222222222','543',NULL,'LNAME','FNAME','2006-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('444444444','543',NULL,'LNAME','FNAME','1945-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,''),
	 ('555555555','543',NULL,'LNAME','FNAME','1945-03-31 00:00:00','543 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN','N',NULL,'BUREAU_USER_1',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,next_date,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','111111111','411220502','2022-05-03 00:00:00','BUREAU_USER_1',1,'Y','0109','411','N'),
	 ('400','222222222','411220502','2022-05-03 00:00:00','BUREAU_USER_1',7,'Y','0109','411','N'),
	 ('411','444444444','415220502','2022-05-03 00:00:00','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('411','555555555','415220502','2022-05-03 00:00:00','BUREAU_USER_1',1,'Y','0109','415','N');
