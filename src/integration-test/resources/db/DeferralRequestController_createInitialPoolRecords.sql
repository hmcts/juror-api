-- Pool 415220502 requested 4 jurors for 2023-06-01, 2 already supplied (2 needed) - active with the bureau
INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES
('400', '415220502', TIMESTAMP'2022-05-03 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0'),
('415', '415220503', TIMESTAMP'2022-05-03 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');


-- Create Pool Members
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('123456789','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','PO19 1SX','Y','BUREAU_USER',1,0,''),
	 ('987654321','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER',1,0,''),
	 ('111111111','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER',0,0,''),
	 ('222222222','543','LNAME2','FNAME2','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER',0,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('415','123456789','415220502','BUREAU_USER',2,'Y','0109','415','N'),
	 ('400','987654321','415220502','BUREAU_USER',2,'Y','0109','415','N'),
	 ('400','111111111','415220502','BUREAU_USER',2,'Y','0109','415','N'),
	 ('415','222222222','415220503','BUREAU_USER',2,'Y','0109','415','N');

-- add appearance values for cert of attendance values
INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,time_in,time_out,
travel_time,appearance_stage, loss_of_earnings_paid, childcare_total_paid, misc_total_paid, non_attendance, no_show,
attendance_type)
VALUES
	(current_date,'222222222','415220503','415','09:30:00',NULL,'01:12','CHECKED_IN', null, null, null, false, false, NULL);

