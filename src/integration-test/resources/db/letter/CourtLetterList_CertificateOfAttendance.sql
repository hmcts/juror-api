INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,attend_time,nil_pool,total_no_required) VALUES
('415220401','415',current_date + 10,2,'CRO','415','N',(current_date + 10 || ' ' || '09:00')::timestamp,false,2),
('415220504','457',current_date + 14,4,'CRO','457','N',(current_date + 14 || ' ' || '09:00')::timestamp,false,4);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,bureau_transfer_date) VALUES
('555555561','540',NULL,'JurorSurname61','JurorForename61','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555562','540',NULL,'JurorSurname62','JurorForename62','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks');

INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,def_date,pool_seq,deferral_code,last_update) VALUES
('555555561','415220401','415','COURT_USER',true,2,NULL,'0109','A','2024-01-16 12:07:42.162505'),
('555555562','415220504','457','COURT_USER',true,2,NULL,'0109','A','2024-01-16 12:07:42.162969');

insert into juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number, other_info_date) values
-- bureau cert of attendance status history event
('555555561', current_date - interval '10 weeks', 'RCER', 'COURT_USER', 'Certificate of Attendance' || current_date +
 14, '415220401', current_date + 10),
-- welsh cert of attendance history event
('555555562', current_date - interval '10 weeks', 'RCER', 'COURT_USER', 'Certificate of Attendance' || current_date +
 14, '415220504', current_date + 10);

-- add appearance values for cert of attendance values
INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,time_in,time_out,
travel_time,appearance_stage, loss_of_earnings_due, childcare_total_due, misc_total_due, non_attendance) VALUES
	(current_date + 10,'555555561','415220401','415','09:30:00','16:30:00','01:12','CHECKED_OUT', 40,
	10, 10, false),
	(current_date + 11,'555555561','415220401','415','09:30:00','16:30:00','01:12','CHECKED_OUT', 40,
	10, 10, false),
	(current_date + 12,'555555561','415220401','415','09:30:00','16:30:00','01:12','CHECKED_OUT', 40,
	10, 10, false),
	(current_date + 13,'555555561','415220401','415','09:30:00','16:30:00','01:12','CHECKED_OUT', 40,
	10, 10, false),
	(current_date + 10,'555555562','415220504','457','09:30:00','16:30:00','01:12','CHECKED_OUT', 50,
	30, 10, false);
