-- Clear previous Pool History
delete from juror_mod.pool_history;

-- Clear previous Participant History
DELETE FROM juror_mod.juror_history;
DELETE FROM juror_mod.juror_audit;

-- Create Pool records associated with Pool Member records
delete from juror_mod.pool_comments;
delete from juror_mod.juror_pool;
delete from juror_mod.juror;
delete from juror_mod.pool;

-- Pool 415220401 requested 2 jurors for TIMESTAMP'2023-05-30 00:00:00.000000', 4 already supplied (2 surplus) - active with the buruea
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '415220401', TIMESTAMP'2023-05-30 00:00:00.000000', 2, 2, 'CRO','415', 'N', TIMESTAMP'2022-02-02 09:22:09.0'),
       ('415', '415220402', current_date, 2, 2, 'CRO','415', 'N', current_date);


-- Pool 767220401 requested 5 jurors for TIMESTAMP'2023-05-30 00:00:00.000000', 0 already supplied (5 still needed) - active with the buruea
INSERT INTO JUROR_MOD.POOL (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES ('400', '767220401', TIMESTAMP'2023-05-30 00:00:00.000000', 5, 5, 'CRO','767', 'N', TIMESTAMP'2022-02-02 09:22:09.0'),
-- Pool 415220502 requested 4 jurors for TIMESTAMP'2023-06-01 00:00:00.000000', 2 already supplied (2 needed) - active with the buruea
 ('400', '415220502',  TIMESTAMP'2023-06-01 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
 ('400', '416220502', TIMESTAMP'2023-06-01 00:00:00.000000', 3, 3, 'CRO', '416', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
-- Pool 415220503 requested 4 jurors for TIMESTAMP'2023-06-12 00:00:00.000000', none currently supplied (4 needed) - active with the buruea
 ('400', '415220503',  TIMESTAMP'2023-06-12 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
-- Pool 415220504 requested 4 jurors for TIMESTAMP'2023-06-12 00:00:00.000000', none currently supplied (4 needed) - active with the court
 ('415', '415220504', TIMESTAMP'2023-06-12 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- Create juror records associated with DEFER_DBF records
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('555555551','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555552','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555553','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555554','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555555','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555556','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555557','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555558','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555559','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL),
	 ('555555560','540','LNAMEFIVEFOURZERO','FNAMEFIVEFOURZERO','1990-07-25 00:00:00','540 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER_1',0,0,NULL);


INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','555555551','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('400','555555552','415220401','BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('400','555555553','415220401','BUREAU_USER_1',11,'Y','0109','415','N'),
	 ('400','555555554','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('400','555555555','415220502','BUREAU_USER_1',2,'Y','0109','415','N'),
	 ('400','555555556','415220502','BUREAU_USER_1',11,'Y','0109','415','N'),
	 ('400','555555557','415220504','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('415','555555558','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('415','555555559','415220401','BUREAU_USER_1',1,'Y','0109','415','N'),
	 ('415','555555560','415220402','COURT_USER_1',1,'Y','0109','415','N');


-- add appearance values for cert of attendance values
INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,time_in,time_out,
travel_time,appearance_stage, loss_of_earnings_paid, childcare_total_paid, misc_total_paid, non_attendance, no_show,
attendance_type)
VALUES
	(current_date,'555555560','415220402','415','09:30:00',NULL,'01:12','CHECKED_IN', null, null, null, false, false, NULL);
