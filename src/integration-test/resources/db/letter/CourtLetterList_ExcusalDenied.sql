INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
	 ('415220502','415','2023-03-08',0,NULL,'415','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00'),
	 ('415220503','457','2023-03-08',0,NULL,'457','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00');

INSERT INTO juror_mod.juror (juror_number, last_name, first_name, dob, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, postcode, responded, welsh, bureau_transfer_date, date_excused, excusal_code, acc_exc) values
	('123456789','LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true, null,'2023-12-13','2024-02-14 00:00:00.000','B', 'Y'),
	('987654321','LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true, true,'2023-12-13','2024-02-14 00:00:00.000','C', 'Y');

insert into juror_mod.juror_pool (juror_number , pool_number, "owner", status, date_created, is_active, "location") values
	('123456789','415220502', '415', 2, '2024-01-17 00:00:00.000',true,'415'),
	('987654321','415220503', '457', 2, '2024-01-17 00:00:00.000',true,'457');


INSERT INTO juror_mod.juror_history (juror_number,date_created,history_code,user_id,other_information,pool_number,other_info_date,other_info_reference) VALUES
	 ('123456789','2024-02-14 15:44:50.277','PDET','MODCOURT','Address Changed','415220502',NULL,NULL),
	 ('123456789','2024-02-14 15:44:50.285','PDET','MODCOURT','Date Of Birth Changed','415220502',NULL,NULL),
	 ('123456789','2024-02-14 15:44:50.286','PDET','MODCOURT','Postcode Changed','415220502',NULL,NULL),
	 ('123456789','2024-02-14 15:44:50.289','PEXC','MODCOURT','Refuse Excuse','415220502',NULL,NULL),
	 ('123456789','2024-02-14 15:44:50.291','RESP','MODCOURT','Responded','415220502',NULL,NULL),
	 ('123456789','2024-02-14 15:44:50.292','REDL','MODCOURT','Refused Excusal','415220502',NULL,'B'),
	 ('987654321','2024-02-14 15:44:50.277','PDET','MODCOURT','Address Changed','415220503',NULL,NULL),
     ('987654321','2024-02-14 15:44:50.285','PDET','MODCOURT','Date Of Birth Changed','415220503',NULL,NULL),
     ('987654321','2024-02-14 15:44:50.286','PDET','MODCOURT','Postcode Changed','415220503',NULL,NULL),
     ('987654321','2024-02-14 15:44:50.289','PEXC','MODCOURT','Refuse Excuse','415220503',NULL,NULL),
     ('987654321','2024-02-14 15:44:50.291','RESP','MODCOURT','Responded','415220503',NULL,NULL),
     ('987654321','2024-02-14 15:44:50.292','REDL','MODCOURT','Refused Excusal','415220503',NULL,'C');


--INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
--	 ('415220502','415','2023-03-08',0,NULL,'415','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00'),
--	 ('415220503','457','2023-03-08',0,NULL,'457','Y',NULL,NULL,NULL,false,100,'2024-02-14 00:00:00');
--
--INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,amount_spent,financial_loss,travel_time,bureau_transfer_date) VALUES
--	 ('123456789','543',NULL,'LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,'2024-02-14 00:00:00.000','B','Y',NULL,NULL,'MODCOURT','SOME EXAMPLE NOTES',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-02-14 15:44:50.000',NULL,NULL,NULL,0,0,NULL,NULL,'Mr','Test','Person',NULL,NULL,NULL,NULL,'2023-12-13'),
--	 ('987654321','543',NULL,'LNAME','FNAME','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,'2024-02-14 00:00:00.000','C','Y',NULL,NULL,'MODCOURT','SOME EXAMPLE NOTES',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,'NOT_CHECKED','2024-02-14 15:44:50.000',NULL,NULL,NULL,0,0,NULL,NULL,'Mr','Test','Person',NULL,NULL,NULL,NULL,'2023-12-13');
--
--INSERT into juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
--	 ('123456789','415220502','415','MODCOURT',true,2,NULL,NULL,'415',NULL,NULL,NULL,NULL,'0109',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-02-14 15:44:50.294',NULL,NULL,'2024-01-17 00:00:00.000'),
--	 ('987654321','415220503','457','MODCOURT',true,2,NULL,NULL,'415',NULL,NULL,NULL,NULL,'0109',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-02-14 15:44:50.294',NULL,NULL,'2024-01-17 00:00:00.000');
--
--INSERT INTO juror_mod.juror_history (juror_number,date_created,history_code,user_id,other_information,pool_number,other_info_date,other_info_reference) VALUES
--	 ('123456789','2024-02-14 15:44:50.277','PDET','MODCOURT','Address Changed','415220502',NULL,NULL),
--	 ('123456789','2024-02-14 15:44:50.285','PDET','MODCOURT','Date Of Birth Changed','415220502',NULL,NULL),
--	 ('123456789','2024-02-14 15:44:50.286','PDET','MODCOURT','Postcode Changed','415220502',NULL,NULL),
--	 ('123456789','2024-02-14 15:44:50.289','PEXC','MODCOURT','Refuse Excuse','415220502',NULL,NULL),
--	 ('123456789','2024-02-14 15:44:50.291','RESP','MODCOURT','Responded','415220502',NULL,NULL),
--	 ('123456789','2024-02-14 15:44:50.292','REDL','MODCOURT','Refused Excusal','415220502',NULL,'B'),
--	 ('987654321','2024-02-14 15:44:50.277','PDET','MODCOURT','Address Changed','415220503',NULL,NULL),
--     ('987654321','2024-02-14 15:44:50.285','PDET','MODCOURT','Date Of Birth Changed','415220503',NULL,NULL),
--     ('987654321','2024-02-14 15:44:50.286','PDET','MODCOURT','Postcode Changed','415220503',NULL,NULL),
--     ('987654321','2024-02-14 15:44:50.289','PEXC','MODCOURT','Refuse Excuse','415220503',NULL,NULL),
--     ('987654321','2024-02-14 15:44:50.291','RESP','MODCOURT','Responded','415220503',NULL,NULL),
--     ('987654321','2024-02-14 15:44:50.292','REDL','MODCOURT','Refused Excusal','415220503',NULL,'C');