INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
('415220401','415',current_date + 10,2,'CRO','415','N',current_date + 10,NULL,'2024-01-01 09:00:00.000',false,2,NULL),
('415220504','416',current_date + 14,4,'CRO','416','N','2022-03-02 09:22:09',NULL,'2024-01-01 09:00:00.000',false,4,NULL);

-- target juror record
INSERT INTO JUROR_MOD.JUROR (juror_number, first_name, last_name, address_line_1, address_line_2, address_line_3, address_line_4, address_line_5, bank_acct_no, sort_code, bank_acct_name, notes, responded)
VALUES ('123456789', 'FNAME', 'LNAME', 'Address Line 1','Address Line 2','Address Line 3', 'Address Line 4', 'Address Line 5', '12345678', '123456', 'Account Name', 'Notes', true);

-- add some more random juror records
INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,bureau_transfer_date) VALUES
('555555561','540',NULL,'PERSON','TEST_ONE','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - interval '9 weeks'),
('555555562','540',NULL,'PERSON','TEST_TWO','1998-03-08 00:00:00.000','Address Line 1','Address  Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10),
('555555563','540',NULL,'PERSON','TEST_THREE','1998-03-08 00:00:00.000','Address Line 1','Address   Line 2','Address Line 3','CARDIFF','Some County','CH1 2AN',NULL,NULL,NULL,true,NULL,NULL,NULL,NULL,NULL,'COURT_USER',NULL,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'NOT_CHECKED','2024-01-16 12:07:42.000',NULL,NULL,NULL,0,0,NULL,'12345678', current_date - 10);

INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
('123456789','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555561','415220401','415','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL),
('555555562','415220504','416','COURT_USER',true,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0109',NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'2024-01-16 12:07:42.162505',NULL,NULL,NULL);
