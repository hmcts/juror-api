-- data scenarios:
-- 555555561, 555555562: happy (juror failed to attend)
-- 555555563: Not printed (no juror_history record)
-- 555555564: No_show is null (juror attended court)
-- 555555565: different pool (415220402)
-- 555555566: Not court owner
-- 555555567: History_code is PDEF (not RFTA)
-- 555555568: Different pool (415220403) and welsh
-- 555555569: Different absent code - RSHC
-- 555555570: Different pool (415220404) and multiple absences

-- POOL
INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
('415220401','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,TIMESTAMP'2022-09-04 09:00:00.0',false,2,NULL),
('415220402','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,TIMESTAMP'2022-09-04 09:00:00.0',false,2,NULL),
('415220403','457',current_date + 10,2,'CRO','457','N',current_date + 10,NULL,TIMESTAMP'2022-09-04 09:00:00.0',false,2,NULL),
('415220404','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,TIMESTAMP'2022-09-04 09:00:00.0',false,2,NULL);

-- JUROR
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,bureau_transfer_date) VALUES
('555555561','540',NULL,'JurorSurname61','JurorForename61','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555562','540',NULL,'JurorSurname62','JurorForename62','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555563','540',NULL,'JurorSurname63','JurorForename63','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555564','540',NULL,'JurorSurname64','JurorForename64','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555565','540',NULL,'JurorSurname65','JurorForename65','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555566','540',NULL,'JurorSurname66','JurorForename66','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555567','540',NULL,'JurorSurname67','JurorForename67','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555568','540',NULL,'JurorSurname68','JurorForename68','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,true,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555569','540',NULL,'JurorSurname69','JurorForename69','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555570','540',NULL,'JurorSurname70','JurorForename70','1998-03-08 00:00:00.000','Address Line 1','Address Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks');

-- JUROR_POOL
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
('555555561','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555562','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555563','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555564','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555565','415220402','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555566','415220401','400','BUREAU_USER',true,7,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555567','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555568','415220403','457','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555569','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL),
('555555570','415220404','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,'0110',NULL,'2023-06-12',false,NULL,true,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162969',NULL,NULL,NULL);

-- APPEARANCE
INSERT INTO juror_mod.appearance (attendance_date,juror_number,pool_number,loc_code,f_audit,time_in,time_out,travel_time,appearance_stage,non_attendance,no_show,attendance_type) VALUES
(current_date - 10,'555555561','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date,'555555562','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 10,'555555563','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date,'555555564','415220401','415',123456789,'09:30:00',null,'01:12','CHECKED_IN',true,null,null),
(current_date - 3,'555555565','415220402','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date,'555555566','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 5,'555555567','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date,'555555568','415220403','457',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 10,'555555569','415220401','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 9,'555555570','415220404','415',123456789,null,null,null,null,true, true,'ABSENT'),
(current_date - 10,'555555570','415220404','415',123456789,null,null,null,null,true, true,'ABSENT');

-- JUROR_HISTORY
insert into juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number, other_info_date, other_info_reference) values
('555555561', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220401', null, null),
('555555562', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220401', null, null),
('555555564', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220401', null, null),
('555555565', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220402', null, null),
('555555567', current_date - 1, 'PDEF', 'court_user_1', 'Deferred Pool Member', '415220401', null, null),
('555555568', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220403', null, null),
('555555569', current_date - 1, 'RSHC', 'court_user_1', 'Show Cause Letter', '415220403', null, null),
('555555570', current_date - 1, 'RFTA', 'court_user_1', 'Failed To Attend Letter', '415220404', null, null);