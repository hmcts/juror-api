DELETE FROM juror_mod.juror_audit;
DELETE FROM juror_mod.juror_history;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('123456789','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
	 ('123456790','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
	 ('123456791','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES'),
	 ('123456792','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',1,0,'SOME EXAMPLE NOTES');

INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE) VALUES
('400', '415220502', '2022-05-03 00:00:00', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
('400', '416220502', '2022-05-03 00:00:00', 4, 4, 'CRO', '416', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
('400','123456789','415220502','BUREAU_USER_1',7,true,'0109','415','N'),
('400','123456790','415220502','BUREAU_USER_1',7,true,'0109','415','N'),
('400','123456791','415220502','BUREAU_USER_1',7,true,'0109','415','N'),
('400','123456792','415220502','BUREAU_USER_1',7,true,'0109','415','N');
