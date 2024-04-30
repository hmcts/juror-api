
INSERT INTO juror_mod.juror (juror_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
('415000001','Ms','Magura','Jenna','1982-08-19 00:00:00','1 Test Street','Scotland','Giffnock','United Kingdom',NULL,'G46 6JF','44141101-1110','44141201-1110',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-11 23:47:40',NULL,'44776-301-1110','Magura0@email.com',0,0,'2024-03-11 23:47:40',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
('415000002','Ms','Lovejoy','Rhonda','1964-11-19 00:00:00','2 Test Street','Scotland','Giffnock','United Kingdom',NULL,'G46 6JF','44141101-1111','44141201-1111',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-11 23:47:40',NULL,'44776-301-1111','Lovejoy1@email.com',0,0,'2024-03-11 23:47:40',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false),
('415000003','Mrs','Rowsey','Clarine','1962-01-03 00:00:00','3 Test Street','Scotland','Giffnock','United Kingdom',NULL,'G466JF','44141101-1112','44141201-1112',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-11 23:47:40',NULL,'44776-301-1112','Rowsey2@email.com',0,0,'2024-03-11 23:47:40',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);

insert into juror_mod.appearance (attendance_date, juror_number,loc_code, time_in, time_out, non_attendance) values
(current_date, '415000001', '415', current_time,null,false);

-- Reset sequence numbers
SELECT setval('juror_mod.courtroom_id_seq', 65);
SELECT setval('juror_mod.judge_id_seq', 20);

-- Dummy test data
insert into juror_mod.judge (owner, code, description) values
('415', '1234','Test judge'),
('462', '4321','Judge Test'),
('416', '4322','Judge Test2'),
('416', '4323','Judge Test3');

insert into juror_mod.courtroom (loc_code, room_number, description) values
('415', '1', 'large room fits 100 people'),
('462', '2', 'large room fits 100 people'),
('416', '3', 'large room fits 101 people'),
('416', '4', 'large room fits 102 people');

insert into juror_mod.trial (trial_number, loc_code, description, judge, trial_type, trial_start_date, trial_end_date, anonymous, courtroom) values
('T100000000','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000001','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000002','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000003','415', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 66),
('T100000004','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000005','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000006','462', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 67),
('T100000007','415', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 66),
('T100000008','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000009','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000010','462', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 67),
('T100000011','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000012','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000013','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000014','462', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 67),
('T100000015','415', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 66),
('T100000016','462', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 67),
('T100000017','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000018','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000019','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000020','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000021','415', 'TEST DEFENDANT', 21,'CIV', current_date, null, false, 66),
('T100000022','462', 'TEST DEFENDANT', 21,'CIV', current_date, current_date, false, 67),
('T100000023','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000024','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date, false, 67),
('T100000025','415', 'TEST DEFENDANT', 22,'CIV', current_date, null, false, 66),
('T100000026','462', 'TEST DEFENDANT', 22,'CIV', current_date, current_date,false, 67),
('TEST000012','416', 'JOE BLOGGS', 23,'CIV', current_date, null,false, 68);

INSERT INTO juror_mod.trial
(trial_number, loc_code, description, courtroom, judge, trial_type, trial_start_date, trial_end_date, anonymous, juror_requested, jurors_sent) VALUES
('T100000027', '462', 'TEST DEFENDANT62', 67, 22, 'CRI', current_date, NULL, false, NULL, NULL),
('T100000027', '415', 'TEST DEFENDANT15', 66, 21, 'CRI', current_date - 1, NULL, false, NULL, NULL);

insert into juror_mod.juror_trial (loc_code, juror_number, trial_number, rand_number, date_selected, "result", completed) values
('415', '415000001', 'T100000001', 1, current_date - 1, 'J', false),
('415', '415000002', 'T100000001', 1, current_date - 1, 'J', false),
('415', '415000003', 'T100000001', 1, current_date - 1, 'J', false);


