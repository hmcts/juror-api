-- create a pool for court location 415
INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
	 ('415240601','415',current_date,5,'CRO','415','N','2024-04-22 12:26:41.000',NULL,'2024-06-24 09:00:00.000',
	 false,5,'2024-04-22 12:26:31.969'),
	 ('415240602','415',current_date,5,'HGH','415','N','2024-04-22 12:26:41.000',NULL,'2024-06-24 09:00:00.000',
	 false,5,'2024-04-22 12:26:31.969'),
	 ('417240601','417',current_date,5,'CRO','417','N','2024-04-22 12:26:41.000',NULL,'2024-06-24 09:00:00.000',
	 false,5,'2024-04-22 12:26:31.969');

-- create juror records
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
	 ('641500011','11',NULL,'LNAMEONEONE','FNAMEONEONE',NULL,'11 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH2 2AB',NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-04-22 12:26:41',NULL,NULL,NULL,NULL,0,'2024-04-22 12:26:40.749869',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
	 ('641500003','3',NULL,'LNAMETHREE','FNAMETHREE','1975-01-01 00:00:00','3 STREET NAME','ANYTOWN','','London','','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-04-22 12:29:18',NULL,NULL,'',NULL,0,'2024-04-22 12:26:40.781637',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
	 ('641500004','4',NULL,'LNAMEFOUR','FNAMEFOUR','1970-01-01 00:00:00','4 STREET NAME','ANYTOWN','','London','','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'MODTESTCOURT',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-04-22 12:30:26',NULL,NULL,'',NULL,0,'2024-04-22 12:26:40.81258',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
	 ('641500007','7',NULL,'LNAMESEVEN','FNAMESEVEN','1975-01-01 00:00:00','7 STREET NAME','ANYTOWN','','TOWN','','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 00:00:00',NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-04-22 15:14:11',NULL,NULL,'',NULL,0,'2024-04-22 12:26:40.841916',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
	 ('641500009','9',NULL,'LNAMENINE','FNAMENINE','1975-01-01 00:00:00','9 STREET NAME','ANYTOWN','','TOWN','','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 00:00:00',NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-04-22 15:14:11',NULL,NULL,'',NULL,0,'2024-04-22 12:26:40.841916',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
	 ('641500021','21',NULL,'LNAMETWOONE','FNAMETWOONE',NULL,'21 STREET NAME','ANYTOWN',NULL,NULL,NULL,'CH1 2AN',NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-04-22 15:15:16',NULL,NULL,NULL,NULL,0,'2024-04-22 12:26:40.681355',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);

-- create juror_pool associative records
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
	 ('641500021','415240601','415','MODTESTBUREAU',true,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0001',NULL,
	 current_date+1,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 12:26:40.702989',NULL,NULL,'2024-04-22 12:26:40.702987'),
	 ('641500011','415240601','415','MODTESTBUREAU',true,3,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0002',NULL,
	 current_date+1,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 12:26:40.757378',NULL,NULL,'2024-04-22 12:26:40.757376'),
	 ('641500003','415240601','415','MODTESTCOURT',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0003',NULL,
	 current_date+1,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 12:29:18.125891',NULL,NULL,'2024-04-22 12:26:40.788798'),
	 ('641500009','415240602','415','MODTESTCOURT',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0003',NULL,
	 current_date+1,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 12:29:18.125891',NULL,NULL,'2024-04-22 12:26:40.788798'),
	 ('641500004','417240601','417','MODTESTCOURT',true,1,NULL,current_date+30,NULL,NULL,NULL,NULL,NULL,'0004',NULL,
	 current_date+2,false,NULL,NULL,'DC',NULL,NULL,NULL,NULL,'2024-04-22 12:30:25.628652',NULL,NULL,'2024-04-22 12:26:40.819485'),
	 ('641500007','415240601','415','MODTESTCOURT',true,4,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0005',NULL,
	 current_date+2,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-04-22 15:14:10.871528',NULL,NULL,'2024-04-22 12:26:40.848865');


INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance,no_show,attendance_type) VALUES
(current_date - 25,'641500021','415240601','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 5,'641500021','415240601','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 4,'641500021','415240601','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 4,'641500009','415240602','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 5,'641500004','415240601','417',123456789,null,null,null,null,true, true,'ABSENT');
