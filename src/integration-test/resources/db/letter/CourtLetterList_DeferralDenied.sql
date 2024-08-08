INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,attend_time,nil_pool,total_no_required) VALUES
('415220401','415',current_date + 10,2,'CRO','415','N',(current_date + 10 || ' ' || '09:00')::timestamp,false,2),
('415220504','415',current_date + 14,4,'CRO','415','N',(current_date + 14 || ' ' || '09:00')::timestamp,false,4),
('415220402','415',current_date + 10,2,'CRO','415','N',(current_date + 10 || ' ' || '09:00')::timestamp,false,2),
('415220403','415',current_date + 20,2,'CRO','415','N',(current_date + 20 || ' ' || '09:00')::timestamp,false,2),
('457220405','457',current_date + 20,2,'CRO','457','N',(current_date + 20 || ' ' || '09:30')::timestamp,false,2),
('415220404','415',current_date + 25,2,'CRO','415','N',(current_date + 25 || ' ' || '09:00')::timestamp,false,2);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,responded,acc_exc,user_edtq,welsh,police_check,last_update,bureau_transfer_date,no_def_pos) VALUES
('555555561','540',NULL,'PERSON','TEST_ONE','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,1),
('555555562','540',NULL,'PERSON','TEST_TWO','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555563','540',NULL,'PERSON','TEST_THREE','1998-03-08 00:00:00.000','Address Line 1','Address   Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555564','540',NULL,'PERSON','TEST_FOUR','1998-03-08 00:00:00.000','Address Line 1','Address   Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555565','540',NULL,'PERSON','TEST_FIVE','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,NULL,'COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555566','540',NULL,'PERSON','TEST_SIX','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555567','540',NULL,'PERSON','TEST_SEVEN','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0),
('555555568','540',NULL,'PERSON','TEST_SEVEN','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',true,'NOT_CHECKED','2024-01-16 12:07:42.000','2023-11-27',0),
('555555569','540',NULL,'PERSON','TEST_SIX','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',true,'Z','COURT_USER',NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',current_date - 10,0);

INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,def_date,pool_seq,deferral_code,last_update) VALUES
-- 555555561 - deferred by the bureau - subsequent deferral denied by the court (currently responded, owned by court)
('555555561','415220401','400','COURT_USER',false,7,current_date + 14,'0109','A','2024-01-16 12:07:42.162505'),
('555555561','415220504','415','COURT_USER',true,2,NULL,'0110','G','2024-01-16 12:07:42.162969'),
-- 555555562 - deferral denied by the bureau (currently responded, owned by court)
('555555562','415220401','415','COURT_USER',true,2,NULL,'0109','I','2024-01-16 12:07:42.162505'),
-- 555555563 - deferral denied by the court (no letter printed)
('555555563','415220401','415','COURT_USER',true,2,NULL,'0109','C','2024-01-16 12:07:42.162505'),
-- 555555564 - deferral denied by the court (court letter previously printed)
('555555564','415220401','415','COURT_USER',true,2,NULL,'0109','S','2024-01-16 12:07:42.162505'),
-- 555555565 - never requested a deferral
('555555565','415220401','415','COURT_USER',true,2,NULL,'0109',null,'2024-01-16 12:07:42.162505'),
-- 555555566 - multiple deferrals denied by the court (one letter printed)
('555555566','415220402','415','COURT_USER',true,7,NULL,'0109','A','2024-01-16 12:07:42.162505'),
-- 555555567 - multiple deferrals denied by the court and bureau (multiple letters printed)
('555555567','415220401','415','COURT_USER',true,2,null,'0109','C','2024-01-16 12:07:42.162505'),
-- 555555568 - deferral denied by the court (no letter printed) - welsh
('555555568','457220405','457','COURT_USER',true,7,NULL,'0109','A','2024-01-16 12:07:42.162505'),
('555555569','415220402','415','COURT_USER',true,2,NULL,'0109','A','2024-01-16 12:07:42.162505');

insert into juror_mod.juror_history (juror_number, date_created, history_code, user_id, other_information, pool_number, other_info_date, other_info_reference) values
-- bureau defer status history event
('555555561', current_date - interval '10 weeks', 'PDEF', 'bureau_user_1', 'Defer to ' || current_date + 14, '415220401', current_date + 10, 'A'),
-- bureau defer letter history event
('555555561', current_date - interval '10 weeks', 'RDEF', 'bureau_user_1', '', '415220401', null, null),
-- bureau defer status history event (denied)
('555555561', current_date - 9, 'PDEF', 'court_user_1', 'Deferral Denied - G', '415220504', null, null),
-- bureau defer status history event (denied)
('555555562', current_date - interval '10 weeks', 'PDEF', 'bureau_user_1', 'Deferral Denied - I', '415220401', null, null),
-- bureau deferral denied letter history event
('555555562', current_date - interval '10 weeks', 'RDDL', 'bureau_user_1', '', '415220401', null, null),
-- court defer status history event (denied)
('555555563', current_date - 9, 'PDEF', 'court_user_1', 'Deferral Denied - C', '415220401', null, null),
-- court defer status history event (denied)
('555555564', current_date - 9, 'PDEF', 'court_user_1', 'Deferral Denied - S', '415220401', null, null),
-- court deferral denied letter history event
('555555564', current_date - 9, 'RDDL', 'court_user_1', '', '415220401', null, 'S'),
-- court defer status history event (denied)
('555555566', current_date - 9, 'PDEF', 'court_user_1', 'Deferral Denied - B', '415220402', null, null),
-- court deferral denied letter history event
('555555566', current_date - 9, 'RDDL', 'court_user_1', '', '415220402', null, 'B'),
-- court defer status history event (denied)
('555555566', current_date - 8, 'PDEF', 'court_user_1', 'Deferral Denied - A', '415220402', null, null),
-- bureau defer status history event (denied)
('555555567', current_date - 11, 'PDEF', 'bureau_user_1', 'Deferral Denied - B', '415220401', null, null),
-- bureau deferral denied letter history event
('555555567', current_date - 11, 'RDDL', 'bureau_user_1', '', '415220401', null, 'B'),
-- court defer status history event (denied)
('555555567', current_date - 8, 'PDEF', 'court_user_1', 'Deferral Denied - A', '415220401', null, null),
-- court deferral denied letter history event
('555555567', current_date - 8, 'RDDL', 'court_user_1', '', '415220401', null, 'A'),
-- court defer status history event (denied)
('555555568', current_date - 5, 'PDEF', 'court_user_1', 'Deferral Denied - R', '457220405', null, null),
-- court defer status history event (denied)
('555555569', current_date - 9, 'PDEF', 'court_user_1', 'Deferral Denied - B', '415220402', null, null),
-- court deferral denied letter history event
('555555569', current_date - 9, 'RDDL', 'court_user_1', '', '415220402', null, 'B');