INSERT INTO juror_mod.users (username,email, name, active)
VALUES ('STAFF1','STAFF1@email.gov.uk','Staff One',true);

insert into juror_mod.user_courts (username, loc_code)
values ('STAFF1', '448');
INSERT INTO juror_mod.user_roles (username, role)
VALUES ('STAFF1', 'TEAM_LEADER');

INSERT INTO juror_mod.pool (pool_no,"owner",return_date,no_requested,pool_type,loc_code,new_request,last_update,additional_summons,attend_time,nil_pool,total_no_required,date_created) VALUES
    ('555','400','2022-05-03',5,'CRO','448','N',NULL,NULL,NULL,false,5,NULL);

INSERT INTO juror_mod.juror (juror_number,poll_number,title,last_name,first_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,h_phone,w_phone,w_ph_local,responded,date_excused,excusal_code,acc_exc,date_disq,disq_code,user_edtq,notes,no_def_pos,perm_disqual,reasonable_adj_code,reasonable_adj_msg,smart_card_number,completion_date,sort_code,bank_acct_name,bank_acct_no,bldg_soc_roll_no,welsh,police_check,last_update,summons_file,m_phone,h_email,contact_preference,notifications,date_created,optic_reference,pending_title,pending_first_name,pending_last_name,mileage,financial_loss,travel_time,bureau_transfer_date,claiming_subsistence_allowance,service_comp_comms_status,login_attempts,is_locked) VALUES
    ('644892530','21112','DR','CASTILLO','JANE','1984-07-24 00:00:00','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB3 9RY','07032096993','01095495625',NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,'NOT_CHECKED','2024-03-12 22:27:17',NULL,'01455252390','jcastillo0@ed.gov',0,0,'2024-03-12 22:27:17',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,NULL,0,false);
INSERT INTO juror_mod.juror_history (juror_number,date_created,history_code,user_id,other_information,pool_number,other_info_date,other_info_reference) VALUES
    ('644892530','2024-03-12 00:00:00','RSUM','TESTSQL','Summoned','',NULL,NULL);
INSERT INTO juror_mod.juror_pool (juror_number,pool_number,"owner",user_edtq,is_active,status,times_sel,def_date,"location",no_attendances,no_attended,no_fta,no_awol,pool_seq,edit_tag,next_date,on_call,smart_card,was_deferred,deferral_code,id_checked,postpone,paid_cash,scan_code,last_update,reminder_sent,transfer_date,date_created) VALUES
    ('644892530','555','400',NULL,true,1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,CURRENT_DATE + 35,false,NULL,false,NULL,NULL,false,false,NULL,'2024-03-12 22:27:17',false,NULL,NULL);
INSERT INTO juror_mod.juror_reasonable_adjustment (juror_number,reasonable_adjustment,reasonable_adjustment_detail) VALUES
    ('644892530','V','Photosensitive epilepsy');
INSERT INTO juror_mod.juror_response (juror_number,date_received,title,first_name,last_name,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,postcode,processing_status,date_of_birth,phone_number,alt_phone_number,email,residency,residency_detail,mental_health_act,mental_health_capacity,mental_health_act_details,bail,bail_details,convictions,convictions_details,deferral,deferral_reason,deferral_date,reasonable_adjustments_arrangements,excusal,excusal_reason,processing_complete,signed,"version",thirdparty_fname,thirdparty_lname,relationship,main_phone,other_phone,email_address,thirdparty_reason,thirdparty_other_reason,juror_phone_details,juror_email_details,staff_login,staff_assignment_date,urgent,super_urgent,completed_at,welsh,reply_type) VALUES
    ('644892530','2024-03-12 00:00:00','DR','JANE','DOE','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,'AB3 9RY','TODO','1984-07-24','07032096993','01095495625','jcastillo0@ed.gov',true,NULL,false,NULL,NULL,false,NULL,false,NULL,NULL,'I am working in another country','Will return to the UK in 4 weeks time',NULL,NULL,NULL,false,NULL,2,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,false,false,NULL,NULL,false,false,NULL,false,'Digital');

INSERT INTO juror_mod.rev_info(revision_number, revision_timestamp)
VALUES (2100, 3),
       (2150, 3);
INSERT INTO juror_mod.juror_audit (revision,juror_number,rev_type,title,first_name,last_name,dob,address_line_1,address_line_2,address_line_3,address_line_4,address_line_5,address6,postcode,h_email,h_phone,m_phone,w_phone,w_ph_local,bank_acct_name,bank_acct_no,bldg_soc_roll_no,sort_code,pending_title,pending_first_name,pending_last_name,claiming_subsistence_allowance,smart_card_number) VALUES
(2100,'644892530',0,' ','JANE','CASTILLO','1984-07-24','4 Knutson Trail','Scotland','Aberdeen','United Kingdom',NULL,NULL,'AB3 9RY',NULL,'07032096993','01455252390','01095495625',NULL,NULL,NULL,NULL,'jcastillo0@ed.gov',NULL,NULL,NULL,false,NULL);
