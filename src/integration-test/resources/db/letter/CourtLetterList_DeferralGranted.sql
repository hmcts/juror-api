INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
('415220401','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,'2024-01-01 09:00:00.000',false,2,NULL),
('415220504','415',current_date + 14,4,'CRO','415','N','2022-03-02 09:22:09',NULL,'2024-01-01 09:00:00.000',false,4,NULL),
('415220402','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,'2024-01-01 09:00:00.000',false,2,NULL),
('415220403','415',current_date + 20,2,'CRO','415','N',current_date + 10,NULL,'2024-01-01 09:00:00.000',false,2,NULL),
('457220405','457',current_date + 20,2,'CRO','457','N',current_date + 10,NULL,'2024-01-01 09:30:00.000',false,2,NULL),
('415220404','415',current_date + 25,2,'CRO','415','N',current_date + 10,NULL,'2024-01-01 09:00:00.000',false,2,NULL);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,bureau_transfer_date) VALUES
('555555561','540',NULL,'PERSON','TEST_ONE','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555562','540',NULL,'PERSON','TEST_TWO','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555563','540',NULL,'PERSON','TEST_THREE','1998-03-08 00:00:00.000','Address Line 1','Address   Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555564','540',NULL,'PERSON','TEST_FOUR','1998-03-08 00:00:00.000','Address Line 1','Address   Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555565','540',NULL,'PERSON','TEST_FIVE','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555566','540',NULL,'PERSON','TEST_SIX','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555567','540',NULL,'PERSON','TEST_SEVEN','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '10 weeks'),
('555555568','540',NULL,'PERSON','TEST_SEVEN','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', '2023-11-27');

INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
('555555561','415220401','400','COURT_USER',false,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555561','415220504','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555562','415220401','415','COURT_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555563','415220401','415','COURT_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555564','415220401','415','COURT_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555565','415220401','415','COURT_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555566','415220402','415','COURT_USER',true,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220401','415','COURT_USER',false,7,NULL,current_date + 10,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'C',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220403','415','COURT_USER',false,7,NULL,current_date + 20,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,'B',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555567','415220404','415','COURT_USER',true,7,NULL,current_date + 25,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
false,NULL,NULL,'T',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555568','457220405','457','COURT_USER',true,7,NULL,'2024-02-25',NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,
false,NULL,NULL,'A',NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL);

insert into juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number, other_info_date, other_info_reference) values
('555555561', current_date - interval '10 weeks', 'RDEF', 'court_user_1', 'Defer to ' || current_date + 14, '415220401', current_date + 10, 'A'),
('555555562', current_date - 9, 'RDEF', 'court_user_1', 'Defer to ' || current_date + 10, '415220401', current_date + 10, 'A'),
('555555567', current_date - interval '8 weeks', 'RDEF', 'court_user_2', 'Defer to ' || current_date + 20, '415220401', current_date + 10, 'C'),
('555555567', current_date - interval '4 weeks', 'RDEF', 'court_user_3', 'Defer to ' || current_date + 25, '415220403', current_date + 10, 'B');
