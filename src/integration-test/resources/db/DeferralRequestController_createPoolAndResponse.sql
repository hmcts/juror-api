INSERT INTO juror_mod.pool (OWNER, POOL_NO, RETURN_DATE, TOTAL_NO_REQUIRED, NO_REQUESTED, POOL_TYPE, LOC_CODE, NEW_REQUEST, LAST_UPDATE)
VALUES
('400', '415220502', TIMESTAMP'2022-05-03 00:00:00.000000', 4, 4, 'CRO', '415', 'N', TIMESTAMP'2022-03-02 09:22:09.0');

-- Create Pool Members
INSERT INTO juror_mod.juror (juror_number,poll_number,last_name,first_name,dob,address_line_1,address_line_2,postcode,responded,user_edtq,no_def_pos,notifications,notes) VALUES
	 ('987654321','543','LNAME','FNAME','1989-03-31 00:00:00','543 STREET NAME','ANYTOWN','CH1 2AN','Y','BUREAU_USER',1,0,'');

INSERT INTO juror_mod.juror_pool ("owner",juror_number,pool_number,user_edtq,status,is_active,pool_seq,"location",on_call) VALUES
	 ('400','987654321','415220502','BUREAU_USER',2,'Y','0109','415','N');


INSERT INTO juror_mod.juror_response (juror_number,date_received,title,first_name,last_name,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,processing_status,date_of_birth,phone_number,alt_phone_number,email,residency,residency_detail,mental_health_act,mental_health_capacity,mental_health_act_details,bail,bail_details,convictions,convictions_details,deferral,deferral_reason,deferral_date,reasonable_adjustments_arrangements,excusal,excusal_reason,processing_complete,signed,"version",thirdparty_fname,thirdparty_lname,relationship,main_phone,other_phone,email_address,thirdparty_reason,thirdparty_other_reason,juror_phone_details,juror_email_details,staff_login,staff_assignment_date,urgent,completed_at,welsh,reply_type) VALUES
	 ('987654321','2022-05-17 13:34:17.202',NULL,'FNAMEONE','LNAMEONE','1 STREET NAME','ANYTOWN',NULL,'New Town',NULL,'CH1 2AN','TODO',NULL,NULL,NULL,NULL,true,NULL,false,false,NULL,false,NULL,false,NULL,false,NULL,NULL,NULL,false,NULL,false,true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,true,'MODTESTBUREAU',NULL,false,NULL,false,'Paper');
